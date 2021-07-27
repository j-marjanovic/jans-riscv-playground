// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._

class MemoryInterface(data_w: Int, addr_w: Int) extends Bundle {
  val addr = Output(UInt(addr_w.W))
  val din = Input(UInt(data_w.W))
  val dout = Output(UInt(data_w.W))
  val we = Output(Bool())
  val byte_en = Output(UInt((data_w/8).W))
}
