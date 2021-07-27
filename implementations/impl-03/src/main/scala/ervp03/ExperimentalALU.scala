// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._
import chisel3.util._

class ExperimentalALU extends Module {
  val io = IO(new Bundle {
    val instr_raw = Input(UInt(32.W))

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val cs_in = Input(new ControlSet)

    val dout = Output(UInt(32.W))
    val cs_out = Output(new ControlSet)
    val instr_raw_out = Output(UInt(32.W))
  })

  // used ALU_IMM and LOAD
  val itype_imm: SInt =
    WireInit(SInt(32.W), io.instr_raw.asTypeOf(new InstrItype).imm.asSInt())

  val store_imm: SInt =
    WireInit(
      SInt(32.W),
      Cat(
        io.instr_raw.asTypeOf(new InstrStype).imm11_5,
        io.instr_raw.asTypeOf(new InstrStype).imm4_0
      ).asSInt()
    )

  val utype_imm: SInt = WireInit(
    SInt(32.W),
    (io.instr_raw.asTypeOf(new InstrUtype).imm20 << 12).asSInt()
  )

  val din1: SInt = WireInit(io.reg_din1).asSInt()

  val op1 = MuxCase(
    din1,
    Array(
      io.cs_in.enable_op_alu -> din1,
      io.cs_in.enable_op_alu_imm -> itype_imm,
      io.cs_in.enable_op_load -> itype_imm,
      io.cs_in.enable_op_store -> store_imm,
      io.cs_in.enable_op_lui -> utype_imm,
      io.cs_in.enable_op_jalr -> itype_imm
    )
  )

  val op1_reg1: SInt = RegNext(op1)
  val op2_reg1: SInt = RegNext(io.reg_din2.asSInt())
  val cs_reg1: ControlSet = RegNext(io.cs_in)
  val ir_reg1: UInt = RegNext(io.instr_raw)

  //=============================================================
  val alu_out: SInt = Reg(SInt(32.W))

  switch(cs_reg1.asTypeOf(new InstrRtype).funct3) {
    is(0.U) {
      when(cs_reg1.enable_op_jalr) {
        alu_out := op1_reg1 + op2_reg1
      }.elsewhen(
          cs_reg1.enable_op_alu && (cs_reg1
            .asTypeOf(new InstrRtype)
            .funct7 === BigInt("0100000", 2).U)
        ) {
          alu_out := op1_reg1 - op2_reg1
        }
        .otherwise {
          alu_out := op1_reg1 + op2_reg1
        }
    }
    is(1.U) {
      alu_out := op1_reg1 << op2_reg1(4, 0)
    }
    is(2.U) { // SLT
      alu_out := (op1_reg1 < op2_reg1).asSInt()
    }
    is(3.U) { // SLTU
      alu_out := (op1_reg1.asUInt() < op2_reg1.asUInt()).asSInt()
    }
    is(4.U) {
      alu_out := op1_reg1 ^ op2_reg1
    }
    is(5.U) {
      alu_out := op1_reg1 >> op2_reg1(4, 0) // TODO: SRA
    }
    is(6.U) {
      alu_out := op1_reg1 | op2_reg1
    }
    is(7.U) {
      alu_out := op1_reg1 & op2_reg1
    }
  }

  val alu_out_reg2: SInt = RegNext(alu_out)
  val cs_reg2: ControlSet = RegNext(cs_reg1)
  val ir_reg2: UInt = RegNext(ir_reg1)

  // just for experiments, add additional two reg stages

  val alu_out_reg3: SInt = RegNext(alu_out_reg2)
  val cs_reg3: ControlSet = RegNext(cs_reg2)
  val ir_reg3: UInt = RegNext(ir_reg2)

  val alu_out_reg4: SInt = RegNext(alu_out_reg3)
  val cs_reg4: ControlSet = RegNext(cs_reg3)
  val ir_reg4: UInt = RegNext(ir_reg3)

  io.dout := alu_out_reg4.asUInt()
  io.cs_out := cs_reg4
  io.instr_raw_out := ir_reg4

}
