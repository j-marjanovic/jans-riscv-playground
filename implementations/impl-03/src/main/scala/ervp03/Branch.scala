// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._
import chisel3.util._

class BranchCmd extends Bundle {
  val valid = Bool()
  val new_pc = UInt(32.W)
  val br_shadow = UInt(2.W)
}

class Branch extends Module {
  val io = IO(new Bundle {
    val instr_raw = Input(UInt(32.W))
    val cs_in = Input(new ControlSet)

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val branch_cmd = Output(new BranchCmd)
    val cs_out = Output(new ControlSet)
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

  val offs_wire: SInt = WireInit(
    Cat(instr.imm12, instr.imm11, instr.imm10_5, instr.imm4_1, 0.U(1.W))
      .asSInt()
  )

  val take_reg: Bool = RegNext(take_wire && io.cs_in.enable_op_branch && io.cs_in.valid, false.B)
  val new_pc: UInt = RegNext(io.cs_in.pc.asSInt() + offs_wire).asUInt()
  val cs_reg = RegNext(io.cs_in)
  io.cs_out := cs_reg

  io.branch_cmd.new_pc := new_pc
  io.branch_cmd.valid := take_reg
  io.branch_cmd.br_shadow := cs_reg.br_shadow

  when (io.branch_cmd.valid) {
    printf("[Branch] take = %d, shadow = %d\n", io.branch_cmd.valid, cs_reg.br_shadow)
  }
}
