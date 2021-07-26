// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

class Cpu(val mem_addr_w : Int) extends MultiIOModule {
  val XLEN: Int = 32

  // IO
  val mem_if = IO(new MemoryInterface(32, mem_addr_w))
  val enable_pulse = IO(Input(Bool()))
  val running_out = IO(Output(Bool()))
  val dbg_instr_done = IO(Output(Bool()))

  // TODO
  val SP_INIT = 0x407ffff0

  // modules
  val mod_fetch = Module(new Fetch(mem_addr_w))
  val mod_decoder = Module(new Decoder())
  val mod_reg_file = Module(new RegFile(SP_INIT))
  val mod_alu = Module(new ALU())
  val mod_store_load = Module(new StoreLoad(mem_addr_w))
  val mod_branch = Module(new Branch())
  val mod_jump = Module(new Jump())
  val mod_pc = Module(new PC())

  // enable
  val running = RegInit(false.B)
  when (enable_pulse) {
    running := true.B
  } .elsewhen (!RegNext(mod_decoder.io.enable_op_system) && mod_decoder.io.enable_op_system) {
    running := false.B
  }
  running_out := running

  // controller
  object State extends ChiselEnum {
    val sFetch, sDecode, sRegRead, sExec, sExec2, sStore = Value
  }

  val state = RegInit(State.sFetch)
  dbg_instr_done := state === State.sStore

  switch(state) {
    is(State.sFetch) {
      when(running) {
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
  mod_fetch.io.act := state === State.sFetch
  mod_fetch.io.pc := mod_pc.io.pc
  mod_fetch.io.mem_instr.din := mem_if.din

  // decode
  mod_decoder.io.act := state === State.sDecode
  mod_decoder.io.instr_raw := mod_fetch.io.instr_raw

  // reg file
  mod_reg_file.io.act := state === State.sRegRead
  mod_reg_file.io.dbg_print := state === State.sStore
  mod_reg_file.io.rs1 := mod_decoder.io.decoder_rtype.rs1
  mod_reg_file.io.rs2 := mod_decoder.io.decoder_rtype.rs2
  mod_reg_file.io.rd := mod_decoder.io.decoder_rtype.rd
  mod_reg_file.io.we := state === State.sStore &&
    (mod_decoder.io.enable_op_alu ||
      mod_decoder.io.enable_op_alu_imm ||
      mod_decoder.io.enable_op_lui ||
      mod_decoder.io.enable_op_load ||
      mod_decoder.io.enable_op_auipc ||
      mod_decoder.io.enable_op_jal) // TODO: check if more instr needed

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
  mod_alu.io.enable_op_load := mod_decoder.io.enable_op_load
  mod_alu.io.enable_op_jalr := mod_decoder.io.enable_op_jalr

  // store/load
  mod_store_load.io.decoder_itype := mod_decoder.io.decoder_itype
  mod_store_load.io.act := state === State.sExec2
  mod_store_load.io.enable_op_store := mod_decoder.io.enable_op_store
  mod_store_load.io.enable_op_load := mod_decoder.io.enable_op_load
  mod_store_load.io.addr := mod_alu.io.dout
  mod_store_load.io.din := mod_reg_file.io.dout2
  mod_store_load.io.mem_data.din := mem_if.din

  mod_reg_file.io.din := Mux(
    mod_decoder.io.enable_op_alu || mod_decoder.io.enable_op_alu_imm || mod_decoder.io.enable_op_lui,
    mod_alu.io.dout,
    Mux(
      mod_decoder.io.enable_op_load,
      mod_store_load.io.dout,
      Mux(
        mod_decoder.io.enable_op_jal || mod_decoder.io.enable_op_jalr,
        mod_pc.io.pc + 4.U,
        Mux(
          mod_decoder.io.enable_op_auipc,
          mod_pc.io.pc + (mod_decoder.io.decoder_utype.imm20 << 12.U),
          0.U
        )
      )
    )
  )

  // branch
  mod_branch.io.act := state === State.sExec && mod_decoder.io.enable_op_branch
  mod_branch.io.decoder_btype := mod_decoder.io.decoder_btype
  mod_branch.io.reg_din1 := mod_reg_file.io.dout1
  mod_branch.io.reg_din2 := mod_reg_file.io.dout2

  // jump
  mod_jump.io.decoder_jtype := mod_decoder.io.decoder_jtype

  // program counter
  mod_pc.io.inc_by_4 := state === State.sStore &&
    ((mod_decoder.io.enable_op_alu ||
      mod_decoder.io.enable_op_alu_imm ||
      mod_decoder.io.enable_op_store ||
      mod_decoder.io.enable_op_load ||
      mod_decoder.io.enable_op_lui ||
      mod_decoder.io.enable_op_auipc) ||
      (mod_decoder.io.enable_op_branch && mod_branch.io.pc_inc))

  mod_pc.io.add_offs := state === State.sStore && mod_decoder.io.enable_op_branch && mod_branch.io.pc_load
  mod_pc.io.offs := mod_branch.io.pc_offs

  mod_pc.io.jump := state === State.sStore && mod_decoder.io.enable_op_jal
  mod_pc.io.jump_offs := mod_jump.io.jump_offs

  mod_pc.io.load := state === State.sStore && (mod_decoder.io.enable_op_jal || mod_decoder.io.enable_op_jalr)
  mod_pc.io.load_pc := mod_alu.io.dout

  // mem connection
  when (state === State.sFetch) {
    mem_if.addr := mod_fetch.io.mem_instr.addr
    mem_if.dout := 0.U
    mem_if.we := false.B
    mem_if.byte_en := 0.U
  } .otherwise {
    mem_if.addr := mod_store_load.io.mem_data.addr
    mem_if.dout := mod_store_load.io.mem_data.dout
    mem_if.we := mod_store_load.io.mem_data.we
    mem_if.byte_en := mod_store_load.io.mem_data.byte_en
  }
}
