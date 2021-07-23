// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._

class PC extends Module {
  val io = IO(new Bundle {
    val inc_by_4 = Input(Bool())

    val add_offs = Input(Bool())
    val offs = Input(SInt(13.W))

    val jump_offs = Input(SInt(21.W))
    val jump = Input(Bool())

    val pc = Output(UInt(32.W))
  })

  val pc = RegInit(UInt(32.W), 0x200.U)
  io.pc := pc

  when (io.inc_by_4) {
    pc := pc + 4.U
  } .elsewhen (io.jump) {
    pc := (pc.asSInt() + io.jump_offs).asUInt()
  } .elsewhen (io.add_offs) {
    pc := (pc.asSInt() + io.offs).asUInt()
  }
}
