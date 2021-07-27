// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._

class ExperimentPipeline extends Module {
  val io = IO(new Bundle {
    val mem = new MemoryInterface(32, 32)

    val dbg_out = Output(UInt(32.W))
  })

  val mod_fetch_dec = Module(new FetchAndDecode())
  val mod_reg_file = Module(new RegFile(0x2000 - 0x10))
  val mod_alu = Module(new ExperimentalALU)

  mod_fetch_dec.io.mem <> io.mem

  mod_reg_file.io.dbg_print := false.B
  mod_reg_file.io.instr_raw := mod_fetch_dec.io.instr_raw
  mod_reg_file.io.cs_in := mod_fetch_dec.io.cs_out

  mod_alu.io.instr_raw := mod_reg_file.io.instr_raw_out
  mod_alu.io.cs_in := mod_reg_file.io.cs_out
  mod_alu.io.reg_din1 := mod_reg_file.io.dout1
  mod_alu.io.reg_din2 := mod_reg_file.io.dout2

  mod_reg_file.io.rd := mod_alu.io.instr_raw_out.asTypeOf(new InstrRtype).rd
  mod_reg_file.io.din := mod_alu.io.dout
  mod_reg_file.io.we := mod_alu.io.cs_out.enable_op_alu || mod_alu.io.cs_out.enable_op_alu_imm

  io.dbg_out := mod_alu.io.dout
}
