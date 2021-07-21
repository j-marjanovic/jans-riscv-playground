package ervp02

import chisel3._

class MemoryInterface(data_w:Int, addr_w:Int) extends Bundle {
  val addr = Output(UInt(addr_w.W))
  val din = Input(UInt(data_w.W))
  val dout = Output(UInt(data_w.W))
  val we = Output(Bool())
}

class Cpu extends MultiIOModule {
  val mem_instr = IO(new MemoryInterface(32, 10))

  val addr_reg = RegInit(UInt(10.W), 0.U)
  addr_reg := addr_reg + 1.U

  val data_reg = RegInit(UInt(32.W), 0.U)
  data_reg := data_reg - 1.U

  mem_instr.addr := addr_reg
  mem_instr.dout := data_reg
  mem_instr.we := true.B
}
