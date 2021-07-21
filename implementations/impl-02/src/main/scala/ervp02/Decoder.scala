// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._

class Decoder extends Module {
  val XLEN: Int = 32

  val io = IO(new Bundle {
    val instr_raw = Input(UInt(32.W))
    val decoder_rtype = Output(new InstrRtype())
    val decoder_itype = Output(new InstrItype())

    val enable_op_alu = Output(Bool())
    val enable_op_alu_imm = Output(Bool())
    val enable_op_store = Output(Bool())
    val enable_op_load = Output(Bool())
    val enable_op_branch = Output(Bool())
    // TODO: more instr
  })

  object Rv32Instr extends Enumeration {
    type Rv32Instr = Value

    val ALU = BigInt("0110011", 2)
    val ALU_IMM = BigInt("0010011", 2)
    val STORE = BigInt("0100011", 2)
    val LOAD = BigInt("0000011", 2)
    val BRANCH = BigInt("1100011", 2)
  }

  io.decoder_rtype := RegNext(io.instr_raw.asTypeOf(new InstrRtype))
  io.decoder_itype := RegNext(io.instr_raw.asTypeOf(new InstrItype))

  io.enable_op_alu := RegNext(io.instr_raw(6, 0) === Rv32Instr.ALU.U)
  io.enable_op_alu_imm := RegNext(io.instr_raw(6, 0) === Rv32Instr.ALU_IMM.U)
  io.enable_op_store := RegNext(io.instr_raw(6, 0) === Rv32Instr.STORE.U)
  io.enable_op_load := RegNext(io.instr_raw(6, 0) === Rv32Instr.LOAD.U)
  io.enable_op_branch := RegNext(io.instr_raw(6, 0) === Rv32Instr.BRANCH.U)

}
