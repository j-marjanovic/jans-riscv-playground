// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.experimental.ChiselEnum

class MemoryInterface(data_w: Int, addr_w: Int) extends Bundle {
  val addr = Output(UInt(addr_w.W))
  val din = Input(UInt(data_w.W))
  val dout = Output(UInt(data_w.W))
  val we = Output(Bool())
}

class Cpu extends MultiIOModule {
  val XLEN: Int = 32

  val mem_instr = IO(new MemoryInterface(32, 10))

  object State extends ChiselEnum {
    val sFetch, sDecode, sRegRead, sExec, sStore = Value
  }

  val state = RegInit(State.sFetch)

  val pc = RegInit(UInt(XLEN.W), 0x200.U)
  mem_instr.addr := pc

  val mod_fetch = Module(new Fetch())

}
