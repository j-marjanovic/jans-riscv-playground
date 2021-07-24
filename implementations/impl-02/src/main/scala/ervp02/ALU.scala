// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val decoder_rtype = Input(new InstrRtype())
    val decoder_itype = Input(new InstrItype())
    val decoder_utype = Input(new InstrUtype())
    val decoder_stype = Input(new InstrStype())

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val enable_op_alu = Input(Bool())
    val enable_op_alu_imm = Input(Bool())
    val enable_op_lui = Input(Bool())
    val enable_op_store = Input(Bool())
    val enable_op_jalr = Input(Bool())

    val dout = Output(UInt(32.W))
  })

  val op1: UInt = WireInit(io.reg_din1)
  val itype_imm_ext: SInt = WireInit(SInt(32.W), io.decoder_itype.imm.asSInt())
  val op2: UInt =
    Mux(io.enable_op_alu_imm || io.enable_op_jalr, itype_imm_ext.asUInt(), io.reg_din2)

  val alu_out = Reg(UInt(32.W))

  val store_imm =
    WireInit(SInt(32.W), Cat(io.decoder_stype.imm11_5, io.decoder_stype.imm4_0).asSInt())
  val store_out = RegNext(op1 + store_imm.asUInt())

  io.dout := Mux(
    io.enable_op_lui,
    (io.decoder_utype.imm20 << 12).asUInt(),
    Mux(io.enable_op_store, store_out, alu_out)
  )

  // TODO: check those operations
  switch(io.decoder_rtype.funct3) {
    is(0.U) {
      when(io.enable_op_jalr) {
        alu_out := op1 + op2
      }.elsewhen(io.enable_op_alu && (io.decoder_rtype.funct7 === BigInt("0100000", 2).U)) {
          alu_out := op1 - op2
        }
        .otherwise {
          alu_out := op1 + op2
        }
    }
    is(1.U) {
      alu_out := op1 << op2(4, 0)
    }
    is(2.U) {
      alu_out := op1.asSInt() < op1.asSInt()
    }
    is(3.U) {
      alu_out := op1 < op2
    }
    is(4.U) {
      alu_out := op1 ^ op2
    }
    is(5.U) {
      alu_out := op1 >> op2(4, 0) // TODO: SRA
    }
    is(6.U) {
      alu_out := op1 | op2
    }
    is(7.U) {
      alu_out := op1 & op2
    }
  }
}
