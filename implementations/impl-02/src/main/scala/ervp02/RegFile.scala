// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._

class RegFile extends Module {
  val io = IO(new Bundle {
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val dout1 = Output(UInt(32.W))
    val dout2 = Output(UInt(32.W))

    val rd = Input(UInt(5.W))
    val din = Input(UInt(32.W))
    val we = Input(Bool())
  })

  val mod_mem1 = Module(new DualPortRam(32, 32))
  mod_mem1.io.clk := this.clock
  mod_mem1.io.addra := io.rd
  mod_mem1.io.dina := io.din
  mod_mem1.io.wea := io.we

  val rs1_prev = RegNext(io.rs1)
  mod_mem1.io.addrb := io.rs1
  io.dout1 := Mux(rs1_prev === 0.U, 0.U, mod_mem1.io.doutb)


  val mod_mem2 = Module(new DualPortRam(32, 32))
  mod_mem2.io.clk := this.clock
  mod_mem2.io.addra := io.rd
  mod_mem2.io.dina := io.din
  mod_mem2.io.wea := io.we

  val rs2_prev = RegNext(io.rs2)
  mod_mem2.io.addrb := io.rs2
  io.dout2 := Mux(rs2_prev === 0.U, 0.U, mod_mem2.io.doutb)

}
