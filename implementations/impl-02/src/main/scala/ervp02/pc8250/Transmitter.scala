// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02.pc8250

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

class Transmitter extends MultiIOModule {
  // I/O
  val en_x16 = IO(Input(Valid(UInt(4.W))))
  val data = IO(Input(Valid(UInt(8.W))))
  val SOUT = IO(Output(Bool()))

  // regs
  val data_shift_reg = Reg(UInt(8.W))
  val en_x16_match = Reg(UInt(4.W))
  val bit_cntr = Reg(UInt(3.W))

  // state
  object State extends ChiselEnum {
    val sIdle, sStart, sData, sStop = Value
  }

  val state = RegInit(State.sIdle)

  switch(state) {
    is(State.sIdle) {
      when(data.valid) {
        data_shift_reg := data.bits
        en_x16_match := en_x16.bits
        bit_cntr := 7.U // 8 bits
        state := State.sStart
      }
    }
    is(State.sStart) {
      when(en_x16.valid && (en_x16_match === en_x16.bits)) {
        state := State.sData
      }
    }
    is(State.sData) {
      when(en_x16.valid && (en_x16_match === en_x16.bits)) {
        bit_cntr := bit_cntr - 1.U
        data_shift_reg := data_shift_reg(7, 1)
        when(bit_cntr === 0.U) {
          state := State.sStop
        }
      }
    }
    is (State.sStop) {
      when(en_x16.valid && (en_x16_match === en_x16.bits)) {
        state := State.sIdle
      }
    }
  }

  SOUT := true.B
  switch(state) {
    is(State.sIdle) {
      SOUT := true.B
    }
    is(State.sStart) {
      SOUT := false.B
    }
    is(State.sData) {
      SOUT := data_shift_reg(0)
    }
    is (State.sStop) {
      SOUT := true.B
    }
  }
}
