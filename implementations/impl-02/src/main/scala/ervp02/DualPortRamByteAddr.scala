// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._

class DualPortRamByteAddr(
    val RAM_WIDTH: Int,
    val RAM_DEPTH: Int,
    val MEM_INIT_EN0: Int = 0,
    val MEM_INIT_ADDR0: Int = 0,
    val MEM_INIT_DATA0: Int = 0
) extends Module {

  val addr_w: Int = log2Ceil(RAM_DEPTH)
  val subw_w: Int = log2Ceil(RAM_WIDTH / 8)

  val io = IO(new Bundle {
    val addra = Input(UInt((addr_w + subw_w).W))
    val dina = Input(UInt(RAM_WIDTH.W))
    val douta = Output(UInt(RAM_WIDTH.W))
    val wea = Input(Bool())
    val byte_ena = Input(UInt((RAM_WIDTH / 8).W))
    val addrb = Input(UInt((addr_w + subw_w).W))
    val dinb = Input(UInt(RAM_WIDTH.W))
    val doutb = Output(UInt(RAM_WIDTH.W))
    val web = Input(Bool())
    val byte_enb = Input(UInt((RAM_WIDTH / 8).W))
  })

  val mem = Module(
    new DualPortRam(RAM_WIDTH, RAM_DEPTH, MEM_INIT_EN0, MEM_INIT_ADDR0, MEM_INIT_DATA0)
  )

  mem.io.clk := this.clock
  mem.io.addra := io.addra / (RAM_WIDTH / 8).U
  mem.io.addrb := io.addrb / (RAM_WIDTH / 8).U

  mem.io.dina := io.dina << (io.addra(subw_w - 1, 0) * 8.U)
  mem.io.dinb := io.dinb << (io.addrb(subw_w - 1, 0) * 8.U)

  mem.io.wea := io.wea
  mem.io.web := io.web

  mem.io.byte_ena := io.byte_ena << io.addra(subw_w - 1, 0)
  mem.io.byte_enb := io.byte_enb << io.addrb(subw_w - 1, 0)

  io.douta := mem.io.douta >> RegNext(io.addra(subw_w - 1, 0) * 8.U)
  io.doutb := mem.io.doutb >> RegNext(io.addrb(subw_w - 1, 0) * 8.U)

}
