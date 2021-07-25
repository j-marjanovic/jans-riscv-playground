// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.iotesters.SteppedHWIOTester

class ALUWrapper extends Module {

  val io = IO(new Bundle {
    val decoder_rtype = Input(UInt(32.W))
    val decoder_itype = Input(UInt(32.W))
    val decoder_stype = Input(UInt(32.W))
    val decoder_utype = Input(UInt(32.W))

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val enable_op_alu = Input(Bool())
    val enable_op_alu_imm = Input(Bool())
    val enable_op_store = Input(Bool())
    val enable_op_load = Input(Bool())
    val enable_op_lui = Input(Bool())
    val enable_op_jalr = Input(Bool())

    val dout = Output(UInt(32.W))
  })

  val mod = Module(new ALU())
  mod.io.decoder_rtype := io.decoder_rtype.asTypeOf(new InstrRtype)
  mod.io.decoder_itype := io.decoder_itype.asTypeOf(new InstrItype)
  mod.io.decoder_stype := io.decoder_stype.asTypeOf(new InstrStype)
  mod.io.decoder_utype := io.decoder_utype.asTypeOf(new InstrUtype)

  mod.io.reg_din1 <> io.reg_din1
  mod.io.reg_din2 <> io.reg_din2

  mod.io.enable_op_alu <> io.enable_op_alu
  mod.io.enable_op_alu_imm <> io.enable_op_alu_imm
  mod.io.enable_op_store <> io.enable_op_store
  mod.io.enable_op_load <> io.enable_op_load
  mod.io.enable_op_lui <> io.enable_op_lui
  mod.io.enable_op_jalr <> io.enable_op_jalr

  mod.io.dout <> io.dout
}

class ALUTest extends SteppedHWIOTester {
  override val device_under_test = Module(new ALUWrapper())

  // 208:	00b50633          	add	a2,a0,a1
  poke(device_under_test.io.decoder_rtype, 0x00b50633)
  poke(device_under_test.io.reg_din1, 1)
  poke(device_under_test.io.reg_din2, 2)
  poke(device_under_test.io.enable_op_alu, 1)
  poke(device_under_test.io.enable_op_alu_imm, 0)
  step(1)
  expect(device_under_test.io.dout, 3)

  // 20c:	40e687b3          	sub	a5,a3,a4
  poke(device_under_test.io.decoder_rtype, 0x40e687b3)
  poke(device_under_test.io.reg_din1, 5)
  poke(device_under_test.io.reg_din2, 3)
  poke(device_under_test.io.enable_op_alu, 1)
  poke(device_under_test.io.enable_op_alu_imm, 0)
  step(1)
  expect(device_under_test.io.dout, 2)

  // 210:	00a60613          	addi	a2,a2,10
  poke(device_under_test.io.decoder_itype, 0x00a60613)
  poke(device_under_test.io.reg_din1, 11)
  poke(device_under_test.io.reg_din2, 12)
  poke(device_under_test.io.enable_op_alu, 0)
  poke(device_under_test.io.enable_op_alu_imm, 1)
  step(1)
  expect(device_under_test.io.dout, 21)

  // combinatorial influence of the enables to the input
  step(1)

  // 21c:	feb50fa3          	sb	a1,-1(a0)
  poke(device_under_test.io.decoder_stype, 0xfeb50fa3L)
  poke(device_under_test.io.reg_din1, 0x1000)
  poke(device_under_test.io.reg_din2, 123)
  poke(device_under_test.io.enable_op_alu, 0)
  poke(device_under_test.io.enable_op_alu_imm, 0)
  poke(device_under_test.io.enable_op_store, 1)
  step(1)
  expect(device_under_test.io.dout, 0xFFF)

  // 220:	fec58f23          	sb	a2,-2(a1)
  poke(device_under_test.io.decoder_stype, 0xfec58f23L)
  poke(device_under_test.io.reg_din1, 0x1000)
  poke(device_under_test.io.reg_din2, 123)
  poke(device_under_test.io.enable_op_alu, 0)
  poke(device_under_test.io.enable_op_alu_imm, 0)
  poke(device_under_test.io.enable_op_store, 1)
  step(1)
  expect(device_under_test.io.dout, 0xFFE)

  step(1)

  // 0xfff00313 - ADDI, rd = 6, funct3 = 0, rs1 = 0, imm = 0xfff (-1)
  poke(device_under_test.io.decoder_itype, 0xfff00313L)
  poke(device_under_test.io.reg_din1, 0)
  poke(device_under_test.io.reg_din2, 0)
  poke(device_under_test.io.enable_op_alu, 0)
  poke(device_under_test.io.enable_op_alu_imm, 1)
  poke(device_under_test.io.enable_op_store, 0)
  step(1)
  expect(device_under_test.io.dout, 0xffffffffL)
}
