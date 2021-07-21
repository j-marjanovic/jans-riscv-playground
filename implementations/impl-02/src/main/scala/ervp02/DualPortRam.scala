// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._

class DualPortRam(val RAM_WIDTH: Int, val RAM_DEPTH: Int)
    extends BlackBox(
      Map("RAM_WIDTH" -> RAM_WIDTH, "RAM_DEPTH" -> RAM_DEPTH)
    )
    with HasBlackBoxResource {

  val io = IO(new Bundle {
    val clk = Input(Clock())
    val addra = Input(UInt(log2Ceil(RAM_DEPTH).W))
    val dina = Input(UInt(RAM_WIDTH.W))
    val douta = Output(UInt(RAM_WIDTH.W))
    val wea = Input(Bool())
    val addrb = Input(UInt(log2Ceil(RAM_DEPTH).W))
    val dinb = Input(UInt(RAM_WIDTH.W))
    val doutb = Output(UInt(RAM_WIDTH.W))
    val web = Input(Bool())
  })

  addResource("/DualPortRam.v")

}
