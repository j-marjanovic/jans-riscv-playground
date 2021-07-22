// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

class Cpu extends MultiIOModule {
  val XLEN: Int = 32

  // IO
  val mem_instr = IO(new MemoryInterface(32, 10))
  mem_instr.dout := 0.U
  mem_instr.we := false.B
  val mem_data = IO(new MemoryInterface(32, 10))
  val enable = IO(Input(Bool()))

  // modules
  val mod_fetch = Module(new Fetch())
  val mod_decoder = Module(new Decoder())
  val mod_reg_file = Module(new RegFile())
  val mod_alu = Module(new ALU())
  val mod_store_load = Module(new StoreLoad())
  val mod_pc = Module(new PC())

  // controller
  object State extends ChiselEnum {
    val sFetch, sDecode, sRegRead, sExec, sExec2, sStore = Value
  }

  val state = RegInit(State.sFetch)

  switch(state) {
    is(State.sFetch) {
      when(enable) {
        state := State.sDecode
      }
    }
    is(State.sDecode) {
      state := State.sRegRead
    }
    is(State.sRegRead) {
      state := State.sExec
    }
    is(State.sExec) {
      when(mod_decoder.io.enable_op_load || mod_decoder.io.enable_op_store) {
        state := State.sExec2
      }.otherwise {
        state := State.sStore
      }
    }
    is(State.sExec2) {
      state := State.sStore
    }
    is(State.sStore) {
      state := State.sFetch
    }
  }

  // fetch
  mod_fetch.io.pc := mod_pc.io.pc
  mem_instr.addr := mod_fetch.io.mem_instr.addr
  mod_fetch.io.mem_instr.din := mem_instr.din

  // decode
  mod_decoder.io.instr_raw := mod_fetch.io.instr_raw

  // reg file
  mod_reg_file.io.rs1 := mod_decoder.io.decoder_rtype.rs1
  mod_reg_file.io.rs2 := mod_decoder.io.decoder_rtype.rs2
  mod_reg_file.io.rd := mod_decoder.io.decoder_rtype.rd
  mod_reg_file.io.we := state === State.sStore // TODO: only certain instrs
  // din, we

  // alu
  mod_alu.io.decoder_rtype := mod_decoder.io.decoder_rtype
  mod_alu.io.decoder_itype := mod_decoder.io.decoder_itype
  mod_alu.io.decoder_utype := mod_decoder.io.decoder_utype
  mod_alu.io.decoder_stype := mod_decoder.io.decoder_stype
  mod_alu.io.reg_din1 := mod_reg_file.io.dout1
  mod_alu.io.reg_din2 := mod_reg_file.io.dout2
  mod_alu.io.enable_op_alu := mod_decoder.io.enable_op_alu
  mod_alu.io.enable_op_alu_imm := mod_decoder.io.enable_op_alu_imm
  mod_alu.io.enable_op_lui := mod_decoder.io.enable_op_lui
  mod_alu.io.enable_op_store := mod_decoder.io.enable_op_store

  // store/load
  // mod_store_load.io.decoder_itype := mod_decoder.io.decoder_itype
  // mod_store_load.io.decoder_stype <> DontCare // TODO
  mod_store_load.io.act := state === State.sExec2
  mod_store_load.io.enable_op_store := mod_decoder.io.enable_op_store
  mod_store_load.io.enable_op_load := mod_decoder.io.enable_op_load
  mod_store_load.io.addr := mod_alu.io.dout // TODO: check
  mod_store_load.io.din := mod_reg_file.io.dout2 // TODO: check
  mod_store_load.io.mem_data <> mem_data

  mod_reg_file.io.din := Mux(
    mod_decoder.io.enable_op_alu || mod_decoder.io.enable_op_alu_imm || mod_decoder.io.enable_op_lui,
    mod_alu.io.dout,
    Mux(mod_decoder.io.enable_op_store, mod_store_load.io.dout, 0.U)
  )

  // program counter
  mod_pc.io.inc_by_4 := state === State.sStore &&
    (mod_decoder.io.enable_op_alu ||
      mod_decoder.io.enable_op_alu_imm ||
      mod_decoder.io.enable_op_store ||
      mod_decoder.io.enable_op_load ||
      mod_decoder.io.enable_op_lui)

  mod_pc.io.new_pc := 0.U // TODO - connect to branch
  mod_pc.io.load := false.B // TODO - connect to branch
}
