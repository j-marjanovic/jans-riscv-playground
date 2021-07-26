// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02.pc8250

import chisel3._

class MemoryInterface extends Bundle {
  val addr = Input(UInt(3.W))
  val we = Input(Bool())
  val din = Input(UInt(8.W))
  val dout = Output(UInt(8.W))
}

class ExtInterface extends Bundle {
  // MODEM signals (CTS, RTS, DCD, DSR, ...) are not implemented
  val SIN = Input(Bool())
  val SOUT = Output(Bool())
}

class PC8250(val DL_INIT: Int = 13888) extends Module {
  val io = IO(new Bundle {
    val mem = new MemoryInterface()
    val ext = new ExtInterface()
  })

  val mod_regs = Module(new Registers(DL_INIT))
  val mod_baud_gen = Module(new BaudGen())
  val mod_tx = Module(new Transmitter())
  val mod_rx = Module(new Receiver())

  mod_regs.mem <> io.mem

  mod_baud_gen.dl := mod_regs.DL

  mod_tx.data.bits := mod_regs.THR.bits.data
  mod_tx.data.valid := mod_regs.THR.valid
  mod_tx.en_x16 := mod_baud_gen.en_x16

  mod_rx.en_x16 := mod_baud_gen.en_x16
  mod_regs.RBR.bits.data := mod_rx.data.bits
  mod_regs.RBR.valid := mod_rx.data.valid

  when (mod_regs.MCR.loop) {
    mod_rx.SIN := mod_tx.SOUT
    io.ext.SOUT := io.ext.SIN
  } .otherwise {
    io.ext.SOUT := mod_tx.SOUT
    mod_rx.SIN := io.ext.SIN
  }

  // TODO
  mod_regs.LSR := DontCare
  mod_regs.MSR := DontCare

}
