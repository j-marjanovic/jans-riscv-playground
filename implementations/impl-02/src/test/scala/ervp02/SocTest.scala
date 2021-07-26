// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import bfmtester._

import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.{break, breakable}

trait AxiLiteHelper {
  this: {
    val mod_axi_mngr: AxiLiteMaster
    def step(n: Int): Unit
  } =>

  def read_blocking(addr: BigInt, CYC_TIMEOUT: Int = 100): BigInt = {
    mod_axi_mngr.readPush(addr)
    for (_ <- 0 to CYC_TIMEOUT) {
      val resp = mod_axi_mngr.getResponse()
      if (resp.isDefined) {
        return resp.get.rd_data
      }
      step(1)
    }

    throw new RuntimeException("AXI read timeout")
  }

  def write_blocking(addr: BigInt, data: BigInt, CYC_TIMEOUT: Int = 100): Unit = {
    mod_axi_mngr.writePush(addr, data)
    for (_ <- 0 to CYC_TIMEOUT) {
      val resp = mod_axi_mngr.getResponse()
      if (resp.isDefined) {
        return
      }
      step(1)
    }

    throw new RuntimeException("AXI write timeout")
  }
}

class SocTest(c: ERVP02) extends BfmTester(c) with AxiLiteHelper {
  val mod_axi_mngr: AxiLiteMaster = BfmFactory.create_axilite_master(c.ctrl)

  // parse ELF
  val mem_secs = ElfParser.parse("../../software/hello_world")
  for (mem_sec <- mem_secs) {
    println(mem_sec.toString)
  }

  // read control registers
  expect(read_blocking(0) == 0xe2117002L, "ID reg")

  val ver_reg = read_blocking(addr = 4)
  expect(ver_reg > 0 && ver_reg < 0x00ffffff, "version reg")

  // load ELF into mem
  val mem_instr = mem_secs.head
  assert(mem_instr.ph_entry.p_offset == 0)
  assert((mem_instr.ph_entry.p_flags.toInt & 0x5) == 0x5) // Read, Execute

  for ((instr, i) <- mem_instr.mem.grouped(4).zipWithIndex) {
    val mem_word = instr.foldRight(0) { (a: Byte, b: Int) => (b << 8) | (a & 0xff) }
    println(f"mem addr = ${i * 4}%08x, data = ${mem_word}%08x")
    write_blocking(0x1000 + 4 * i, mem_word)
  }

  // initialize stack
  write_blocking(0x1000 + 0x10000 + (0x407ffff0 & 0xfff), 0x00000001)
  write_blocking(0x1000 + 0x10000 + (0x407ffff4 & 0xfff), 0x408001a9)
  write_blocking(0x1000 + 0x10000 + (0x407ffff8 & 0xfff), 0x00000000)
  write_blocking(0x1000 + 0x10000 + (0x407ffffc & 0xfff), 0x408001c0)

  // start CPU
  write_blocking(0x14, 1)

  breakable {
    for (_ <- 0 until 1000) {
      step(1000)
      val status = read_blocking(0x10).toLong
      if ((status & 1) == 0) {
        break
      }
    }
  }

  val out_buffer = ListBuffer[Char]()
  breakable {
    for (addr <- 0 until 64 by 4) {
      val data = read_blocking(0x1000 + addr).toLong
      for (bs <- 0 until 32 by 8) {
        val c : Char = ((data >> bs) & 0xFF).toChar
        println(s"c = ${c}")
        if (c == 0) {
          break
        }
        out_buffer += c
      }
    }
  }

  val out_str = out_buffer.toList.mkString

  println("Output buffer:")
  println("--- start output buffer ---")
  println(out_str)
  println("---  end output buffer  ---");

  expect(out_str == "1 + 2 = 3, 6 * 7 = 42\n", "output string")
}
