// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle{
    val decoder_rtype = Input(new InstrRtype())
    val decoder_itype = Input(new InstrItype())

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val enable_op_alu = Input(Bool())
    val enable_op_alu_imm = Input(Bool())

    val dout = Output(UInt(32.W))
  })

  val op1 = WireInit(io.reg_din1)
  val op2 = Mux(io.enable_op_alu_imm, io.decoder_itype.imm, io.reg_din2)

  val dout = Reg(UInt(32.W))
  io.dout := dout

  // TODO: check those operations
  switch (io.decoder_rtype.funct3) {
    is (0.U) {
      when (io.enable_op_alu && (io.decoder_rtype.funct7 === BigInt("0100000", 2).U)) {
        dout := op1 - op2
      } .otherwise {
        dout := op1 + op2
      }
    }
    is (1.U) {
      dout := op1 << op2(4, 0)
    }
    is (2.U) {
      dout := op1.asSInt() < op1.asSInt()
    }
    is (3.U) {
      dout := op1 < op2
    }
    is (4.U) {
      dout := op1 ^ op2
    }
    is (5.U) {
      dout := op1 >> op2(4, 0) // TODO: SRA
    }
    is (6.U) {
      dout := op1 | op2
    }
    is (7.U) {
      dout := op1 & op2
    }
  }
}
