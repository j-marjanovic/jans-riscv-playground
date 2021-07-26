// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02.pc8250

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

class Receiver extends MultiIOModule {
  // I/O
  val SIN = IO(Input(Bool()))
  val en_x16 = IO(Input(Valid(UInt(4.W))))
  val data = IO(Output(Valid(UInt(8.W))))

  // regs
  val sin_reg = RegNext(RegNext(SIN, true.B), true.B)
  val data_shift_reg = Reg(UInt(8.W))
  val en_x16_adv = Reg(UInt(4.W))
  val en_x16_samp = Reg(UInt(4.W))
  val bit_cntr = Reg(UInt(3.W))
  val data_reg = Reg(Valid(UInt(8.W)))
  data := data_reg
  data_reg.valid := false.B

  // state
  object State extends ChiselEnum {
    val sIdle, sStart, sData, sStop = Value
  }

  val state = RegInit(State.sIdle)

  switch(state) {
    is(State.sIdle) {
      when(!sin_reg) {
        en_x16_adv := en_x16.bits
        en_x16_samp := en_x16.bits + 8.U
        bit_cntr := 7.U // 8 bits
        state := State.sStart
      }
    }
    is(State.sStart) {
      when(en_x16.valid && (en_x16_adv === en_x16.bits)) {
        state := State.sData
      }
    }
    is(State.sData) {
      when(en_x16.valid && (en_x16_adv === en_x16.bits)) {
        bit_cntr := bit_cntr - 1.U
        when(bit_cntr === 0.U) {
          state := State.sStop
        }
      }
      when(en_x16.valid && (en_x16_samp === en_x16.bits)) {
        data_shift_reg := Cat(sin_reg, data_shift_reg(7, 1))
      }
    }
    is(State.sStop) {
      when(en_x16.valid && (en_x16_adv === en_x16.bits)) {
        state := State.sIdle
        data_reg.bits := data_shift_reg
        data_reg.valid := true.B
      }
    }
  }
}
