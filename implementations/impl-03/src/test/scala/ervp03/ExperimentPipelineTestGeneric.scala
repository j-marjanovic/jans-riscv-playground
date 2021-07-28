// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3.iotesters.PeekPokeTester

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class ExperimentPipelineTestGeneric(c: ExperimentPipeline)
    extends PeekPokeTester(c) {

  val instrs_txt: String

  lazy val instrs: mutable.Map[Int, (BigInt, String)] =
    DisassemblyParser.parse_instrs(instrs_txt)

  var mem_en: Boolean = true

  def step_single(): Unit = {
    if (mem_en) {
      val addr = peek(c.io.mem.addr)
      println(f"addr = ${addr}%x")
      val instr = instrs(addr.toInt)
      println(s"instr = ${instr._2}")
      poke(c.io.mem.din, instr._1)
    }
    super.step(1)
  }

  override def step(n: Int): Unit = {
    for (_ <- 0 until n) {
      step_single()
    }
  }

  def dump_regs(): Seq[Long] = {
    val l = ListBuffer[Long]()
    for (i <- 0 until 32) {
      poke(c.io.dbg_access.addr, i)
      step(1)
      val reg_val = peek(c.io.dbg_access.dout)
      l += reg_val.toLong
    }
    l.toList
  }

}
