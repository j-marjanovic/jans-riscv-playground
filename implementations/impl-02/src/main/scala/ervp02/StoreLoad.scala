// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._

class StoreLoad(val mem_addr_w: Int) extends Module {
  val io = IO(new Bundle {
    val decoder_itype = Input(new InstrItype())
    //val decoder_stype = Input(new InstrStype())

    val act = Input(Bool())

    val enable_op_store = Input(Bool())
    val enable_op_load = Input(Bool())

    val addr = Input(UInt(32.W))
    val din = Input(UInt(32.W))
    val dout = Output(UInt(32.W))
    val valid = Output(Bool())

    val mem_data = new MemoryInterface(32, mem_addr_w)
  })

  when(io.act) {
    printf("[StoreLoad] addr = %x, data = %x\n", io.addr, io.din);
  }

  io.mem_data.addr := io.addr
  io.mem_data.dout := io.din
  io.mem_data.we := io.enable_op_store && io.act

  when(io.enable_op_load) {
    io.dout := DontCare

    switch(io.decoder_itype.funct3) {
      is(0.U) {
        io.dout := io.mem_data.din(7, 0).asSInt().asUInt()
      }
      is(1.U) {
        io.dout := io.mem_data.din(15, 0).asSInt().asUInt()
      }
      is(2.U) {
        io.dout := io.mem_data.din
      }
      is(4.U) {
        io.dout := io.mem_data.din(7, 0)
      }
      is(5.U) {
        io.dout := io.mem_data.din(15, 0)
      }
    }
  }.otherwise {
    io.dout := io.mem_data.din
  }

  io.valid := RegNext((io.enable_op_load || io.enable_op_store) && io.act)

}
