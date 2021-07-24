// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._

class Fetch(val instr_addr_w : Int) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))

    val mem_instr = new MemoryInterface(32, instr_addr_w)

    val instr_raw = Output(UInt(32.W))
  })

  io.mem_instr.addr := io.pc
  io.mem_instr.we := false.B
  io.mem_instr.dout := DontCare
  io.instr_raw := io.mem_instr.din

}
