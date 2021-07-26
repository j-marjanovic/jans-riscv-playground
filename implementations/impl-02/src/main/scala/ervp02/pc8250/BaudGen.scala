// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02.pc8250

import chisel3._
import chisel3.util._

class BaudGen extends MultiIOModule {
  val dl = IO(Input(UInt(16.W)))
  val en_x16 = IO(Output(Valid(UInt(4.W))))

  val cntr_reset = Wire(Bool())
  val cntr: (UInt, Bool) = Counter(0 to 65535, enable = true.B, reset = cntr_reset)
  cntr_reset := cntr._1 >= (dl - 1.U)

  val en_x16_reg: (UInt, Bool) = Counter(0 to 15, enable = cntr_reset, reset = this.reset.asBool())

  en_x16.bits := en_x16_reg._1
  en_x16.valid := RegNext(cntr_reset)

}
