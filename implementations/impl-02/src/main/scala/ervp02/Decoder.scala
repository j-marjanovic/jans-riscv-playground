// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._

class Decoder extends Module {
  val io = IO(new Bundle {
    val act = Input(Bool())
    val instr_raw = Input(UInt(32.W))
    val decoder_rtype = Output(new InstrRtype())
    val decoder_itype = Output(new InstrItype())
    val decoder_utype = Output(new InstrUtype())
    val decoder_stype = Output(new InstrStype())
    val decoder_btype = Output(new InstrBtype())
    val decoder_jtype = Output(new InstrJtype())

    val enable_op_auipc = Output(Bool())
    val enable_op_alu = Output(Bool())
    val enable_op_alu_imm = Output(Bool())
    val enable_op_store = Output(Bool())
    val enable_op_load = Output(Bool())
    val enable_op_branch = Output(Bool())
    val enable_op_lui = Output(Bool())
    val enable_op_jal = Output(Bool())
    val enable_op_jalr = Output(Bool())
    // TODO: more instr
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
  }

  when(RegNext(io.act)) {
    when(io.enable_op_auipc) {
      printf("[Decoder] AUIPC, rd = %d, imm = 0x%x\n", io.decoder_utype.rd, io.decoder_utype.imm20)
    }
    when(io.enable_op_alu) {
      printf(
        "[Decoder] ALU, rd = %d, funct3 = %d, rs1 = %d, rs2 = %d, funct7 = %d",
        io.decoder_rtype.rd,
        io.decoder_rtype.funct3,
        io.decoder_rtype.rs1,
        io.decoder_rtype.rs2,
        io.decoder_rtype.funct7
      )
    }
    when(io.enable_op_alu_imm) {
      printf(
        "[Decoder] ALU IMM, rd = %d, funct3 = %d, rs1 = %d, imm = 0x%x (%d)\n",
        io.decoder_itype.rd,
        io.decoder_itype.funct3,
        io.decoder_itype.rs1,
        io.decoder_itype.imm.asSInt(),
        io.decoder_itype.imm.asSInt()
      )
      when(io.enable_op_store) {
        printf(
          "[Decoder] STORE, funct3 = %d, rs1 = %d, rs2 = %d, imm = 0x%x\n",
          io.decoder_stype.funct3,
          io.decoder_stype.rs1,
          io.decoder_stype.rs2,
          Cat(io.decoder_stype.imm11_5, io.decoder_stype.imm4_0)
        )
      }
    }
    when(io.enable_op_load) {
      printf("[Decoder] LOAD\n") // TODO
    }
    when(io.enable_op_branch) {
      printf("[Decoder] BRANCH\n") // TODO
    }
    when(io.enable_op_lui) {
      printf("[Decoder] LUI\n") // TODO
    }
    when(io.enable_op_jal) {
      printf(
        "[Decoder] JAL, rd = %d, imm = 0x%x\n",
        io.decoder_jtype.rd,
        instr_jtype_imm(io.decoder_jtype)
      )
    }
    when(io.enable_op_jalr) {
      printf(
        "[Decoder] JALR, rd = %d, funct3 = %d, rs1 = %d, imm = 0x%x\n",
        io.decoder_itype.rd,
        io.decoder_itype.funct3,
        io.decoder_itype.rs1,
        io.decoder_itype.imm
      )
    }
  }

  io.decoder_rtype := RegNext(io.instr_raw.asTypeOf(new InstrRtype))
  io.decoder_itype := RegNext(io.instr_raw.asTypeOf(new InstrItype))
  io.decoder_utype := RegNext(io.instr_raw.asTypeOf(new InstrUtype))
  io.decoder_stype := RegNext(io.instr_raw.asTypeOf(new InstrStype))
  io.decoder_btype := RegNext(io.instr_raw.asTypeOf(new InstrBtype))
  io.decoder_jtype := RegNext(io.instr_raw.asTypeOf(new InstrJtype))

  io.enable_op_auipc := RegNext(io.instr_raw(6, 0) === Rv32Instr.AUIPC.U)
  io.enable_op_alu := RegNext(io.instr_raw(6, 0) === Rv32Instr.ALU.U)
  io.enable_op_alu_imm := RegNext(io.instr_raw(6, 0) === Rv32Instr.ALU_IMM.U)
  io.enable_op_store := RegNext(io.instr_raw(6, 0) === Rv32Instr.STORE.U)
  io.enable_op_load := RegNext(io.instr_raw(6, 0) === Rv32Instr.LOAD.U)
  io.enable_op_branch := RegNext(io.instr_raw(6, 0) === Rv32Instr.BRANCH.U)
  io.enable_op_lui := RegNext(io.instr_raw(6, 0) === Rv32Instr.LUI.U)
  io.enable_op_jal := RegNext(io.instr_raw(6, 0) === Rv32Instr.JAL.U)
  io.enable_op_jalr := RegNext(io.instr_raw(6, 0) === Rv32Instr.JALR.U)

}
