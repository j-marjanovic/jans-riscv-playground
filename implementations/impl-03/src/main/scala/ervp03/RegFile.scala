// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._
import chisel3.util._

class RegFile(val sp_init: Int) extends Module {
  val io = IO(new Bundle {
    val dbg_print = Input(Bool())

    val cs_in = Input(new ControlSet)
    val instr_raw = Input(UInt(32.W))

    val dout1 = Output(UInt(32.W))
    val dout2 = Output(UInt(32.W))

    // writeback
    val rd = Input(UInt(5.W))
    val din = Input(UInt(32.W))
    val we = Input(Bool())
    val wr_br_shadow = Input(UInt(2.W))

    // pipeline
    val cs_out = Output(new ControlSet)
    val instr_raw_out = Output(UInt(32.W))

    // branch shadow
    val br_shadow_dis = Input(Valid(UInt(2.W)))
    val br_shadow_en = Input(Valid(UInt(2.W)))
  })

  // @formatter:off
  val REG_NAMES = List[String](
    "0  ", "ra ", "sp ", "gp ", "tp ", "t0 ", "t1 ", "t2 ", "s0 ", "s1 ", "a0 ",
    "a1 ", "a2 ", "a3 ", "a4 ", "a5 ", "a6 ", "a7 ", "s2 ", "s3 ", "s4 ", "s5 ",
    "s6 ", "s7 ", "s8 ", "s9 ", "s10", "s11", "t3 ", "t4 ", "t5 ", "t6 ",
  )
  // @formatter:on

  // branch shadow
  val wr_shadow_disable: Vec[Bool] = RegInit(VecInit.tabulate(4)(_ => false.B))
  when(io.br_shadow_dis.valid) {
    wr_shadow_disable(io.br_shadow_dis.bits) := true.B
  }.elsewhen(io.br_shadow_en.valid) {
    wr_shadow_disable(io.br_shadow_en.bits) := false.B
  }
  // TODO: both at the same time

  when(io.we) {
    printf(
      "[Reg file] write: rd = %d, data = 0x%x, br shadow = %d\n",
      io.rd,
      io.din,
      io.wr_br_shadow
    )

    printf(
      "[Reg file] wr shadow disable = %d %d %d %d\n",
      wr_shadow_disable(3),
      wr_shadow_disable(2),
      wr_shadow_disable(1),
      wr_shadow_disable(0),
    )
  }

  // reg impl
  val rs1 = io.instr_raw.asTypeOf(new InstrRtype).rs1
  val rs2 = io.instr_raw.asTypeOf(new InstrRtype).rs2

  val mod_mem1 = Module(new DualPortRam(32, 32, 1, 2, sp_init))
  mod_mem1.io.clk := this.clock
  mod_mem1.io.addra := io.rd
  mod_mem1.io.dina := io.din
  mod_mem1.io.wea := io.we && !wr_shadow_disable(io.wr_br_shadow)
  mod_mem1.io.byte_ena := 0xf.U
  mod_mem1.io.byte_enb := 0xf.U

  val rs1_prev_is_0: Bool = RegNext(rs1 === 0.U)
  mod_mem1.io.addrb := rs1
  io.dout1 := Mux(rs1_prev_is_0, 0.U, mod_mem1.io.doutb)

  val mod_mem2 = Module(new DualPortRam(32, 32, 1, 2, sp_init))
  mod_mem2.io.clk := this.clock
  mod_mem2.io.addra := io.rd
  mod_mem2.io.dina := io.din
  mod_mem2.io.wea := io.we && !wr_shadow_disable(io.wr_br_shadow)
  mod_mem2.io.byte_ena := 0xf.U
  mod_mem2.io.byte_enb := 0xf.U

  val rs2_prev_is_0: Bool = RegNext(rs2 === 0.U)
  mod_mem2.io.addrb := rs2
  io.dout2 := Mux(rs2_prev_is_0, 0.U, mod_mem2.io.doutb)

  // pipeline
  io.cs_out := RegNext(io.cs_in)
  io.instr_raw_out := RegNext(io.instr_raw)

}
