// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._

class RegFile(val sp_init: Int) extends Module {
  val io = IO(new Bundle {
    val dbg_print = Input(Bool())

    val cs_in = Input(new ControlSet)
    val instr_raw = Input(UInt(32.W))

    val dout1 = Output(UInt(32.W))
    val dout2 = Output(UInt(32.W))

    // writeback
    val rd = Input(UInt(5.W))
    val din = Input(UInt(32.W))
    val we = Input(Bool())

    // pipeline
    val cs_out = Output(new ControlSet)
    val instr_raw_out = Output(UInt(32.W))
  })

  // @formatter:off
  val REG_NAMES = List[String](
    "0  ", "ra ", "sp ", "gp ", "tp ", "t0 ", "t1 ", "t2 ", "s0 ", "s1 ", "a0 ",
    "a1 ", "a2 ", "a3 ", "a4 ", "a5 ", "a6 ", "a7 ", "s2 ", "s3 ", "s4 ", "s5 ",
    "s6 ", "s7 ", "s8 ", "s9 ", "s10", "s11", "t3 ", "t4 ", "t5 ", "t6 ",
  )
  // @formatter:on

  val rs1 = io.instr_raw.asTypeOf(new InstrRtype).rs1
  val rs2 = io.instr_raw.asTypeOf(new InstrRtype).rs2

  val mod_mem1 = Module(new DualPortRam(32, 32, 1, 2, sp_init))
  mod_mem1.io.clk := this.clock
  mod_mem1.io.addra := io.rd
  mod_mem1.io.dina := io.din
  mod_mem1.io.wea := io.we
  mod_mem1.io.byte_ena := 0xf.U
  mod_mem1.io.byte_enb := 0xf.U

  val rs1_prev_is_0: Bool = RegNext(rs1 === 0.U)
  mod_mem1.io.addrb := rs1
  io.dout1 := Mux(rs1_prev_is_0, 0.U, mod_mem1.io.doutb)

  val mod_mem2 = Module(new DualPortRam(32, 32, 1, 2, sp_init))
  mod_mem2.io.clk := this.clock
  mod_mem2.io.addra := io.rd
  mod_mem2.io.dina := io.din
  mod_mem2.io.wea := io.we
  mod_mem2.io.byte_ena := 0xf.U
  mod_mem2.io.byte_enb := 0xf.U

  val rs2_prev_is_0: Bool = RegNext(rs2 === 0.U)
  mod_mem2.io.addrb := rs2
  io.dout2 := Mux(rs2_prev_is_0, 0.U, mod_mem2.io.doutb)

  // pipeline
  io.cs_out := RegNext(io.cs_in)
  io.instr_raw_out := RegNext(io.instr_raw)

  // debug
  val debug_reg_file = RegInit(
    VecInit.tabulate(32)(i => if (i == 2) sp_init.U(32.W) else 0.U(32.W))
  )

  when(io.we && io.rd =/= 0.U) {
    debug_reg_file(io.rd) := io.din
  }

  when(io.dbg_print) {
    // with `printf` one cannot be "smart" with '\n' - if the format string
    // does not contain an '\n', it might be overwritten by some other printf
    printf("-------------- register dump --------------\n")
    for (i <- 0 until 32 by 8) {
      val line_start_str = f"${i}%2d | "
      printf(
        f"${i}%2d | " +
          s"${REG_NAMES(i)} %x  ${REG_NAMES(i + 1)} %x  ${REG_NAMES(i + 2)} %x  ${REG_NAMES(i + 3)} %x  " +
          s"${REG_NAMES(i + 4)} %x  ${REG_NAMES(i + 5)} %x  ${REG_NAMES(i + 6)} %x  ${REG_NAMES(i + 7)} %x\n",
        debug_reg_file(i),
        debug_reg_file(i + 1),
        debug_reg_file(i + 2),
        debug_reg_file(i + 3),
        debug_reg_file(i + 4),
        debug_reg_file(i + 5),
        debug_reg_file(i + 6),
        debug_reg_file(i + 7)
      )
    }
    printf("---------- end of register dump -----------\n")
  }
}
