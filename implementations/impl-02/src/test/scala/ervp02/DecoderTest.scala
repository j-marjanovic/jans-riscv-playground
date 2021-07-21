// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.iotesters.SteppedHWIOTester

class DecoderTest extends SteppedHWIOTester {
  override val device_under_test = Module(new Decoder())

  poke(device_under_test.io.instr_raw, 0x00c50533)
  step(1)
  expect(device_under_test.io.decoder_rtype.opcode, 0x33)
  expect(device_under_test.io.enable_op_alu, 1)
  expect(device_under_test.io.enable_op_alu_imm, 0)
  expect(device_under_test.io.enable_op_branch, 0)
  expect(device_under_test.io.enable_op_load, 0)
  expect(device_under_test.io.enable_op_store, 0)

  poke(device_under_test.io.instr_raw, 0x0015f593)
  step(1)
  expect(device_under_test.io.enable_op_alu, 0)
  expect(device_under_test.io.enable_op_alu_imm, 1)
  expect(device_under_test.io.enable_op_branch, 0)
  expect(device_under_test.io.enable_op_load, 0)
  expect(device_under_test.io.enable_op_store, 0)

  poke(device_under_test.io.instr_raw, 0xfd661ee3L)
  step(1)
  expect(device_under_test.io.enable_op_alu, 0)
  expect(device_under_test.io.enable_op_alu_imm, 0)
  expect(device_under_test.io.enable_op_branch, 1)
  expect(device_under_test.io.enable_op_load, 0)
  expect(device_under_test.io.enable_op_store, 0)
  poke(device_under_test.io.instr_raw, 0x00245703)
  step(1)
  expect(device_under_test.io.enable_op_alu, 0)
  expect(device_under_test.io.enable_op_alu_imm, 0)
  expect(device_under_test.io.enable_op_branch, 0)
  expect(device_under_test.io.enable_op_load, 1)
  expect(device_under_test.io.enable_op_store, 0)

  poke(device_under_test.io.instr_raw, 0x00f41123)
  step(1)
  expect(device_under_test.io.enable_op_alu, 0)
  expect(device_under_test.io.enable_op_alu_imm, 0)
  expect(device_under_test.io.enable_op_branch, 0)
  expect(device_under_test.io.enable_op_load, 0)
  expect(device_under_test.io.enable_op_store, 1)

}
