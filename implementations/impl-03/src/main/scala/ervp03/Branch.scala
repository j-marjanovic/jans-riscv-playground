// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._
import chisel3.util._

class Branch extends Module {
  val io = IO(new Bundle {
    val instr_raw = Input(UInt(32.W))
    val cs_in = Input(new ControlSet)

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val branch_pc = Output(Valid(UInt(32.W)))
  })

  val instr: InstrBtype = WireInit(io.instr_raw.asTypeOf(new InstrBtype))
  val take_wire: Bool = WireInit(Bool(), false.B)

  switch(instr.funct3) {
    is(0.U) { // BEQ
      take_wire := io.reg_din1 === io.reg_din2
    }
    is(1.U) { // BNE
      take_wire := io.reg_din1 =/= io.reg_din2
    }
    is(4.U) { // BLT
      take_wire := io.reg_din1.asSInt() < io.reg_din2.asSInt()
    }
    is(5.U) { // BGE
      take_wire := io.reg_din1.asSInt() >= io.reg_din2.asSInt()
    }
    is(6.U) { // BLTU
      take_wire := io.reg_din1 < io.reg_din2
    }
    is(7.U) { // BGEU
      take_wire := io.reg_din1 >= io.reg_din2
    }
  }

  val take_reg: Bool = RegNext(take_wire && io.cs_in.enable_op_branch, false.B)
  val offs_wire: SInt = WireInit(
    Cat(instr.imm12, instr.imm11, instr.imm10_5, instr.imm4_1, 0.U(1.W))
      .asSInt()
  )
  val new_pc: UInt = RegNext(io.cs_in.pc.asSInt() + offs_wire).asUInt()

  io.branch_pc.bits := new_pc
  io.branch_pc.valid := take_reg

}
