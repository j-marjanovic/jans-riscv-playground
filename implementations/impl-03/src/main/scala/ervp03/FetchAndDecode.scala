// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._
import chisel3.util._
import org.scalacheck.Prop.False

class FetchAndDecode extends Module {
  val io = IO(new Bundle {
    val mem = new MemoryInterface(32, 32)

    // pipeline
    val instr_raw = Output(UInt(32.W))
    val cs_out = Output(new ControlSet)

    // branches and jump
    val branch_cmd = Input(new BranchCmd)
    val cs_back = Input(new ControlSet)
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
  val cs = Wire(new ControlSet)

  when(io.branch_cmd.valid) {
    pc := io.branch_cmd.new_pc
  } .elsewhen(cs.valid) {
    pc := pc + 4.U
  }

  // control set
  // TODO cs.enable_op_auipc := (io.mem.din(6, 0) === Rv32Instr.AUIPC.U)
  cs.enable_op_alu := io.mem.din(6, 0) === Rv32Instr.ALU.U
  cs.enable_op_alu_imm := io.mem.din(6, 0) === Rv32Instr.ALU_IMM.U
  cs.enable_op_store := io.mem.din(6, 0) === Rv32Instr.STORE.U
  cs.enable_op_load := io.mem.din(6, 0) === Rv32Instr.LOAD.U
  cs.enable_op_branch := io.mem.din(6, 0) === Rv32Instr.BRANCH.U
  cs.enable_op_lui := io.mem.din(6, 0) === Rv32Instr.LUI.U
  // TODO cs.enable_op_jal := (io.mem.din(6, 0) === Rv32Instr.JAL.U)
  cs.enable_op_jalr := io.mem.din(6, 0) === Rv32Instr.JALR.U
  // TODO cs.enable_op_system := (io.mem.din(6, 0) === Rv32Instr.SYSTEM.U)
  cs.pc := pc

  // branch shadow
  val br_shadow = RegInit(UInt(2.W), 0.U)
  cs.br_shadow := br_shadow
  when(cs.enable_op_branch && cs.valid) {
    br_shadow := br_shadow + 1.U
    cs.br_shadow := br_shadow + 1.U
  }.elsewhen(io.branch_cmd.valid && io.cs_back.valid) {
    br_shadow := br_shadow - 1.U
    // TODO: check what happens if both come at the same time
  }

  cs.br_shadow_en.valid := RegNext(io.branch_cmd.valid)
  cs.br_shadow_en.bits := RegNext(io.branch_cmd.br_shadow)

  // pipeline output
  io.instr_raw := RegNext(io.mem.din)
  io.cs_out := RegNext(cs)

  // reg allocation
  val instr_rtype = WireInit(io.mem.din.asTypeOf(new InstrRtype))
  val reg_locks: Vec[Bool] = RegInit(VecInit(Seq.fill(32)(false.B)))

  val rs1_free = WireInit(instr_rtype.rs1 === 0.U || !reg_locks(instr_rtype.rs1))
  val rs2_free = WireInit(instr_rtype.rs2 === 0.U || !reg_locks(instr_rtype.rs2))
  val rd_free = WireInit(instr_rtype.rd === 0.U || !reg_locks(instr_rtype.rd))

  cs.valid := false.B
  cs.reg_dep_lock_rs1.valid := false.B
  cs.reg_dep_lock_rs2.valid := false.B
  cs.reg_dep_lock_rd.valid := false.B
  cs.reg_dep_lock_rs1.bits := DontCare
  cs.reg_dep_lock_rs2.bits := DontCare
  cs.reg_dep_lock_rd.bits := DontCare

  when (cs.enable_op_alu) {
    cs.reg_dep_lock_rs1.valid := true.B
    cs.reg_dep_lock_rs2.valid := true.B
    cs.reg_dep_lock_rd.valid := true.B
    cs.reg_dep_lock_rs1.bits := instr_rtype.rs1
    cs.reg_dep_lock_rs2.bits := instr_rtype.rs2
    cs.reg_dep_lock_rd.bits := instr_rtype.rd
    when (rs1_free && rs2_free && rd_free) {
      reg_locks(instr_rtype.rs1) := true.B
      reg_locks(instr_rtype.rs2) := true.B
      reg_locks(instr_rtype.rd) := true.B
      cs.valid := true.B
    }
  } .elsewhen (cs.enable_op_alu_imm) {
    cs.reg_dep_lock_rs1.valid := true.B
    cs.reg_dep_lock_rd.valid := true.B
    cs.reg_dep_lock_rs1.bits := instr_rtype.rs1
    cs.reg_dep_lock_rd.bits := instr_rtype.rd
    when (rs1_free && rd_free) {
      reg_locks(instr_rtype.rs1) := true.B
      reg_locks(instr_rtype.rd) := true.B
      cs.valid := true.B
    }
  } .elsewhen (cs.enable_op_branch) {
    cs.reg_dep_lock_rs1.valid := true.B
    cs.reg_dep_lock_rs2.valid := true.B
    cs.reg_dep_lock_rs1.bits := instr_rtype.rs1
    cs.reg_dep_lock_rs2.bits := instr_rtype.rs2
    when (rs1_free && rd_free) {
      reg_locks(instr_rtype.rs1) := true.B
      reg_locks(instr_rtype.rs2) := true.B
      cs.valid := true.B
    }
  }

  // TODO: check the behavior when both arrive at the same time
  when (io.cs_back.valid && io.cs_back.reg_dep_lock_rs1.valid) {
    reg_locks(io.cs_back.reg_dep_lock_rs1.bits) := false.B
  }
  when (io.cs_back.valid && io.cs_back.reg_dep_lock_rs2.valid) {
    reg_locks(io.cs_back.reg_dep_lock_rs2.bits) := false.B
  }
  when (io.cs_back.valid && io.cs_back.reg_dep_lock_rd.valid) {
    reg_locks(io.cs_back.reg_dep_lock_rd.bits) := false.B
  }

  // memory interface
  io.mem.addr := pc

  // tie offs
  io.mem.dout := 0.U
  io.mem.we := false.B
  io.mem.byte_en := 0.U
}
