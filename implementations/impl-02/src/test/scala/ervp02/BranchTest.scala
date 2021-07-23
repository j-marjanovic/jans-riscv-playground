// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.iotesters.SteppedHWIOTester

class BranchWrapper extends Module {
  val io = IO(new Bundle {
    val act = Input(Bool())

    val decoder_btype = Input(UInt(32.W))

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val pc_inc = Output(Bool())
    val pc_load = Output(Bool())
    val pc_offs = Output(UInt(13.W))
  })

  val dut = Module(new Branch)

  io.act <> dut.io.act

  dut.io.decoder_btype := io.decoder_btype.asTypeOf(new InstrBtype())

  io.reg_din1 <> dut.io.reg_din1
  io.reg_din2 <> dut.io.reg_din2

  io.pc_inc := dut.io.pc_inc
  io.pc_load := dut.io.pc_load
  io.pc_offs := dut.io.pc_offs.asUInt() // SteppedHWIOTester is having issues with signed ports

}

class BranchTest extends SteppedHWIOTester {
  override val device_under_test = Module(new BranchWrapper())

  // 'beq	t0,t1,224 <set_to_3>' = 0x00628c63
  poke(device_under_test.io.decoder_btype, 0x00628c63)
  poke(device_under_test.io.reg_din1, 10)
  poke(device_under_test.io.reg_din2, 10)
  step(1)
  expect(device_under_test.io.pc_inc, 0)
  expect(device_under_test.io.pc_load, 1)
  expect(device_under_test.io.pc_offs, 24)

  // 'beq	t0,t1,224 <set_to_3>' = 0x00628c63
  poke(device_under_test.io.decoder_btype, 0x00628c63)
  poke(device_under_test.io.reg_din1, 11)
  poke(device_under_test.io.reg_din2, 10)
  step(1)
  expect(device_under_test.io.pc_inc, 1)
  expect(device_under_test.io.pc_load, 0)

  // bne
  poke(device_under_test.io.decoder_btype, 0x00731663)
  poke(device_under_test.io.reg_din1, 11)
  poke(device_under_test.io.reg_din2, 10)
  step(1)
  expect(device_under_test.io.pc_inc, 0)
  expect(device_under_test.io.pc_load, 1)
  expect(device_under_test.io.pc_offs, 12)

}
