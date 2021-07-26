// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import bfmtester._
import chisel3.Bits

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class IntBusBfm(
    val iface: pc8250.MemoryInterface,
    val peek: Bits => BigInt,
    val poke: (Bits, BigInt) => Unit,
    val println: String => Unit
) extends Bfm {

  private class Cmd(val wr: Boolean, val addr: BigInt, val data: BigInt)

  private val cmds: mutable.Queue[Cmd] = mutable.Queue[Cmd]()
  private val resp: ListBuffer[Long] = ListBuffer[Long]()
  private var prev_read: Boolean = false

  private def printWithBg(s: String): Unit = {
    // black on cyan
    println("\u001b[30;46m" + s + "\u001b[39;49m")
  }

  override def update(t: Long, poke: (Bits, BigInt) => Unit): Unit = {
    prev_read = false
    poke(iface.din, 0)
    poke(iface.addr, 0)
    poke(iface.we, 0)

    if (cmds.nonEmpty) {
      val cmd = cmds.dequeue()
      poke(iface.din, cmd.data)
      poke(iface.addr, cmd.addr)
      poke(iface.we, if (cmd.wr) 1 else 0)
      prev_read = !cmd.wr
      if (cmd.wr) {
        printWithBg(f"${t}%5d IntBusBfm: write: addr = 0x${cmd.addr}%x, data = 0x${cmd.data}%x")
      } else {
        printWithBg(f"${t}%5d IntBusBfm: read: addr = 0x${cmd.addr}%x")
      }
    }

    if (prev_read) {
      prev_read = false
      val data = peek(iface.dout).toLong
      printWithBg(f"${t}%5d IntBusBfm: read: data = 0x${data}%x")
      resp += data
    }
  }

  def write(addr: BigInt, data: BigInt): Unit = {
    cmds += new Cmd(true, addr, data)
  }

  def read(addr: BigInt): Unit = {
    cmds += new Cmd(false, addr, 0)
  }

  def is_cmd_queue_empty(): Boolean = cmds.isEmpty

  def get_resps(): List[Long] = {
    val l = resp.toList
    resp.clear()
    l
  }
}

class PC8250Test(c: pc8250.PC8250) extends BfmTester(c) {
  val bfm_int_bus = new IntBusBfm(c.io.mem, bfm_peek, bfm_poke, println)

  //
  poke(c.io.ext.SIN, 1)

  // set the loopback mode
  bfm_int_bus.write(4, 0x10)
  step(5)

  // transmit 0xAB
  bfm_int_bus.write(0, 0xab)
  step(1000)

  bfm_int_bus.read(0)
  step(5)

  val resp = bfm_int_bus.get_resps()
  expect(resp.head == 0xab, "loopback data")

}
