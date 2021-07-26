// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.iotesters.PeekPokeTester
import chisel3._

import scala.collection.mutable
import scala.util.matching.Regex

class MemDummy(val peek: Bits => BigInt) {
  class MemTx(val addr: BigInt, val data: BigInt, val byte_en : BigInt)

  val mem_txs = mutable.ListBuffer[MemTx]()

  def check_data(c: Cpu): Unit = {
    val we = peek(c.mem_if.we)
    if (we == 1) {
      val addr = peek(c.mem_if.addr)
      val data = peek(c.mem_if.dout)
      val byte_en = peek(c.mem_if.byte_en)
      println(f"mem write, addr = 0x${addr}%x, data = 0x${data}%x, byte_en = 0x${byte_en}")
      mem_txs += new MemTx(addr, data, byte_en)
    }
  }
}

abstract class CpuTestGeneric(c: Cpu) extends PeekPokeTester(c) {
  val INSTRS: String
  def final_check(): Unit

  private def bfm_peek(bs: Bits): BigInt = this.peek(bs)
  protected val mem_dummy = new MemDummy(bfm_peek)

  private def wait_for_next_pc(callback: () => Unit = () => {}): Unit = {
    var timeout = 100
    while (peek(c.dbg_instr_done) == 0) {
      callback()
      step(1)
      if (timeout == 0) {
        throw new RuntimeException("timeout while waiting for the next PC")
      }
      timeout -= 1
    }
    callback()
    step(1)
  }

  val instr_mem = mutable.Map[Int, (BigInt, String)]()

  private def parse_cmds(): Unit = {
    val LINE_FMT: Regex = raw"\s*([0-9a-fA-F]+):\s+([0-9a-fA-F]+)\s+(.*)".r
    for (line <- INSTRS.split("\n")) {
      val m: Option[Regex.Match] = LINE_FMT.findFirstMatchIn(line)
      if (m.isDefined) {
        val m_ = m.get
        val instr_addr: Int = BigInt(m_.group(1), 16).toInt
        val instr_raw: BigInt = BigInt(m_.group(2), 16)
        val instr_comment: String = m_.group(3)

        instr_mem += (instr_addr -> (instr_raw, instr_comment))
      }
    }
  }

  private def exec_cmds(): Unit = {
    val last_addr = instr_mem.keys.toList.max
    println(f"Last instr addr = 0x${last_addr}%x")

    while (true) {
      val addr = peek(c.mem_if.addr).toInt
      if (addr > last_addr) {
        println("Last instr, exiting")
        return
      }
      println(f"addr = ${addr}%x")
      val instr: (BigInt, String) = instr_mem(addr)
      println(s"Executing ${instr._2}")
      poke(c.mem_if.din, instr._1)
      wait_for_next_pc(() => mem_dummy.check_data(c))
    }
  }

  def run_test() {
    parse_cmds()
    poke(c.enable, 1)
    exec_cmds()
    final_check()
  }
}
