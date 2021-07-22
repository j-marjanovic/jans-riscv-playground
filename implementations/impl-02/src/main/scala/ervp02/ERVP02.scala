// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import bfmtester.util._
import bfmtester.AxiLiteIf
import chisel3.util.log2Up

class Uart extends Bundle {
  val tx = Output(Bool())
  val rx = Input(Bool())
}

class ERVP02 extends MultiIOModule {
  import AxiLiteSubordinateGenerator._

  val MEM_INSTR_SIZE: Int = 0x1000

  // format: off
  val area_map = new AreaMap(
    new Reg("ID_REG", 0x0,
      new Field("ID",     hw_access = Access.NA, sw_access = Access.R,  hi = 31, Some(0), reset = Some(0xe2117002L.U)) // ERVP002
    ),
    new Reg("VERSION", 0x4,
      new Field("PATCH",  hw_access = Access.W,  sw_access = Access.R,  hi =  7, lo = Some(0)),
      new Field("MINOR",  hw_access = Access.W,  sw_access = Access.R,  hi = 15, lo = Some(8)),
      new Field("MAJOR",  hw_access = Access.W,  sw_access = Access.R,  hi = 23, lo = Some(16))
    ),
    new Reg("SCRATCH", 0xc,
      new Field("FIELD",  hw_access = Access.NA, sw_access = Access.RW, hi = 31, lo = Some(0), reset = Some(0.U))
    ),
    new Reg("STATUS", 0x10,
      new Field("RUNNING",   hw_access = Access.W,  sw_access = Access.R,  hi =  0, lo = None),
    ),
    new Reg("CONTROL", 0x14,
      new Field("ENABLE", hw_access = Access.R,  sw_access = Access.RW, hi = 0, lo = None)
    ),
    new Mem("INSTR", addr = MEM_INSTR_SIZE, nr_els = MEM_INSTR_SIZE, data_w = 32),
    new Mem("DATA", addr = MEM_INSTR_SIZE*2, nr_els = MEM_INSTR_SIZE, data_w = 32),
  )
  // format: on

  private val mem_start_addr =
    area_map.els.find(_.name == "DATA").get.asInstanceOf[Mem].addr
  private val addr_w = log2Up(mem_start_addr + MEM_INSTR_SIZE * 4)

  // IO
  val uart = IO(new Uart())
  val led = IO(Output(Bool()))
  val ctrl = IO(new AxiLiteIf(addr_w = addr_w.W))

  val mod_ctrl = Module(new AxiLiteSubordinateGenerator(area_map, addr_w))
  ctrl <> mod_ctrl.io.ctrl

  mod_ctrl.io.inp("VERSION_MAJOR") := 0x00.U
  mod_ctrl.io.inp("VERSION_MINOR") := 0x01.U
  mod_ctrl.io.inp("VERSION_PATCH") := 0x00.U

  mod_ctrl.io.inp("STATUS_RUNNING") := false.B // TODO:

  // instr memory
  val mod_instr_mem = Module(new DualPortRam(32, MEM_INSTR_SIZE))
  mod_instr_mem.io.clk := this.clock
  mod_instr_mem.io.addrb := mod_ctrl.io.out("MEM_INSTR_ADDR").asUInt()
  mod_instr_mem.io.dinb := mod_ctrl.io.out("MEM_INSTR_DIN").asUInt()
  mod_instr_mem.io.web := mod_ctrl.io.out("MEM_INSTR_WE").asUInt().asBool()
  mod_ctrl.io.inp("MEM_INSTR_DOUT") := mod_instr_mem.io.doutb

  // data memory
  val mod_data_mem = Module(new DualPortRam(32, MEM_INSTR_SIZE))
  mod_data_mem.io.clk := this.clock
  mod_data_mem.io.addrb := mod_ctrl.io.out("MEM_DATA_ADDR").asUInt()
  mod_data_mem.io.dinb := mod_ctrl.io.out("MEM_DATA_DIN").asUInt()
  mod_data_mem.io.web := mod_ctrl.io.out("MEM_DATA_WE").asUInt().asBool()
  mod_ctrl.io.inp("MEM_DATA_DOUT") := mod_data_mem.io.doutb

  // CPU
  val mod_cpu = Module(new Cpu())
  mod_instr_mem.io.addra := mod_cpu.mem_instr.addr
  mod_instr_mem.io.dina := mod_cpu.mem_instr.dout
  mod_instr_mem.io.wea := mod_cpu.mem_instr.we
  mod_cpu.mem_instr.din := mod_instr_mem.io.doutb

  mod_data_mem.io.addra := mod_cpu.mem_data.addr
  mod_data_mem.io.dina := mod_cpu.mem_data.dout
  mod_data_mem.io.wea := mod_cpu.mem_data.we
  mod_cpu.mem_data.din := mod_data_mem.io.doutb

  mod_cpu.enable := mod_ctrl.io.out("CONTROL_ENABLE")

  // UART
  uart.tx := uart.rx

  // LED
  val led_cntr = Reg(UInt(27.W))
  led_cntr := led_cntr + 1.U
  led := led_cntr(4)
}
