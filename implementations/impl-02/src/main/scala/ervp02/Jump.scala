package ervp02

import chisel3._
import chisel3.util._

class Jump extends Module {
  val io = IO(new Bundle {
    val decoder_jtype = Input(new InstrJtype())

    val jump_offs = Output(SInt(21.W))
  })

  io.jump_offs := Cat(
    io.decoder_jtype.imm20,
    io.decoder_jtype.imm19_12,
    io.decoder_jtype.imm11,
    io.decoder_jtype.imm10_1,
    0.U(1.W)
  ).asSInt()

}
