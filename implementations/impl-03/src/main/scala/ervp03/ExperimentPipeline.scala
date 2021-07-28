// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._

class ExperimentPipeline extends Module {
  val io = IO(new Bundle {
    val mem = new MemoryInterface(32, 32)

    val dbg_out = Output(UInt(32.W))

    val dbg_access = new DebugRegFile
  })

  val mod_fetch_dec = Module(new FetchAndDecode())
  val mod_reg_file = Module(new RegFile(0x2000 - 0x10))
  val mod_alu = Module(new ExperimentalALU)
  val mod_branch = Module(new Branch)

  mod_fetch_dec.io.mem <> io.mem

  mod_reg_file.io.dbg_print := false.B
  mod_reg_file.io.instr_raw := mod_fetch_dec.io.instr_raw
  mod_reg_file.io.cs_in := mod_fetch_dec.io.cs_out

  mod_alu.io.instr_raw := mod_reg_file.io.instr_raw_out
  mod_alu.io.cs_in := mod_reg_file.io.cs_out
  mod_alu.io.reg_din1 := mod_reg_file.io.dout1
  mod_alu.io.reg_din2 := mod_reg_file.io.dout2

  mod_branch.io.instr_raw := mod_reg_file.io.instr_raw_out
  mod_branch.io.cs_in := mod_reg_file.io.cs_out
  mod_branch.io.reg_din1 := mod_reg_file.io.dout1
  mod_branch.io.reg_din2 := mod_reg_file.io.dout2
  mod_fetch_dec.io.branch_cmd := mod_branch.io.branch_cmd

  mod_reg_file.io.rd := mod_alu.io.instr_raw_out.asTypeOf(new InstrRtype).rd
  mod_reg_file.io.din := mod_alu.io.dout
  mod_reg_file.io.we := mod_alu.io.cs_out.enable_op_alu || mod_alu.io.cs_out.enable_op_alu_imm
  mod_reg_file.io.wr_br_shadow := mod_alu.io.cs_out.br_shadow
  mod_reg_file.io.br_shadow_dis.valid := mod_branch.io.branch_cmd.valid
  mod_reg_file.io.br_shadow_dis.bits := mod_branch.io.branch_cmd.br_shadow
  mod_reg_file.io.br_shadow_en.valid := mod_alu.io.cs_out.br_shadow_en_valid
  mod_reg_file.io.br_shadow_en.bits := mod_alu.io.cs_out.br_shadow_en_bits
  mod_reg_file.io.dbg_access <> io.dbg_access

  io.dbg_out := mod_alu.io.dout
}
