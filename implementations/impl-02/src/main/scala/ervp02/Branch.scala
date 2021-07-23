// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3._
import chisel3.util._

class Branch extends Module {
  val io = IO(new Bundle {
    val act = Input(Bool())

    val decoder_btype = Input(new InstrBtype())

    val reg_din1 = Input(UInt(32.W))
    val reg_din2 = Input(UInt(32.W))

    val pc_inc = Output(Bool())
    val pc_load = Output(Bool())
    val pc_offs = Output(SInt(13.W))
  })

  val take = WireInit(Bool(), false.B)

  switch(io.decoder_btype.funct3) {
    is(0.U) { // BEQ
      take := io.reg_din1 === io.reg_din2
    }
    is(1.U) { // BNE
      take := io.reg_din1 =/= io.reg_din2
    }
    is(4.U) { // BLT
      take := io.reg_din1.asSInt() < io.reg_din2.asSInt()
    }
    is(5.U) { // BGE
      take := io.reg_din1.asSInt() >= io.reg_din2.asSInt()
    }
    is(6.U) { // BLTU
      take := io.reg_din1 < io.reg_din2
    }
    is(7.U) { // BGEU
      take := io.reg_din1 >= io.reg_din2
    }
  }

  io.pc_inc := RegNext(!take)
  io.pc_load := RegNext(take)
  io.pc_offs := RegNext(
    Cat(
      io.decoder_btype.imm12,
      io.decoder_btype.imm11,
      io.decoder_btype.imm10_5,
      io.decoder_btype.imm4_1,
      0.U(1.W)
    ).asSInt()
  )

  when(RegNext(io.act)) {
    printf(
      "[Branch] funct3 = %d, inc = %d, load = %d\n",
      io.decoder_btype.funct3,
      io.pc_inc,
      io.pc_load
    )
  }

}
