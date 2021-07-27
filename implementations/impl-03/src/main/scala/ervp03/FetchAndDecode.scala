// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._

class FetchAndDecode extends Module {
  val io = IO(new Bundle {
    val mem = new MemoryInterface(32, 32)

    // pipeline
    val instr_raw = Output(UInt(32.W))
    val cs_out = Output(new ControlSet)
  })

  object Rv32Instr extends Enumeration {
    type Rv32Instr = Value

    val AUIPC = BigInt("0010111", 2)
    val JAL = BigInt("1101111", 2)
    val JALR = BigInt("1100111", 2)
    val LUI = BigInt("0110111", 2)
    val ALU = BigInt("0110011", 2)
    val ALU_IMM = BigInt("0010011", 2)
    val STORE = BigInt("0100011", 2)
    val LOAD = BigInt("0000011", 2)
    val BRANCH = BigInt("1100011", 2)
    val SYSTEM = BigInt("1110011", 2)
  }

  val pc = RegInit(UInt(32.W), 0x200.U)

  // decode
  val cs = Wire(new ControlSet)

  // TODO cs.enable_op_auipc := RegNext(io.instr_raw(6, 0) === Rv32Instr.AUIPC.U)
  cs.enable_op_alu := RegNext(io.instr_raw(6, 0) === Rv32Instr.ALU.U)
  cs.enable_op_alu_imm := RegNext(io.instr_raw(6, 0) === Rv32Instr.ALU_IMM.U)
  cs.enable_op_store := RegNext(io.instr_raw(6, 0) === Rv32Instr.STORE.U)
  cs.enable_op_load := RegNext(io.instr_raw(6, 0) === Rv32Instr.LOAD.U)
  // TODO cs.enable_op_branch := RegNext(io.instr_raw(6, 0) === Rv32Instr.BRANCH.U)
  cs.enable_op_lui := RegNext(io.instr_raw(6, 0) === Rv32Instr.LUI.U)
  // TODO cs.enable_op_jal := RegNext(io.instr_raw(6, 0) === Rv32Instr.JAL.U)
  cs.enable_op_jalr := RegNext(io.instr_raw(6, 0) === Rv32Instr.JALR.U)
  // TODO cs.enable_op_system := RegNext(io.instr_raw(6, 0) === Rv32Instr.SYSTEM.U)

  // pipeline output
  io.instr_raw := RegNext(io.mem.din)
  io.cs_out := RegNext(cs)

  // memory interface
  io.mem.addr := pc // TODO

  // tie offs
  io.mem.dout := 0.U
  io.mem.we := false.B
  io.mem.byte_en := 0.U
}
