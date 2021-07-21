// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._

class Uart extends Bundle {
  val tx = Output(Bool())
  val rx = Input(Bool())
}

class ERVP02 extends MultiIOModule {
  val uart = IO(new Uart())
  val led = IO(Output(Bool()))

  uart.tx := uart.rx

  val led_cntr = Reg(UInt(27.W))
  led_cntr := led_cntr + 1.U
  led := led_cntr(4)
}
