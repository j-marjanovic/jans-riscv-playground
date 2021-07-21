// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

import chisel3._

package object ervp02 {

  class InstrRtype extends Bundle {
    val funct7 = UInt(7.W)
    val rs2 = UInt(5.W)
    val rs1 = UInt(5.W)
    val funct3 = UInt(3.W)
    val rd = UInt(5.W)
    val opcode = UInt(7.W)
  }

  class InstrItype extends Bundle {
    val imm = UInt(12.W)
    val rs1 = UInt(5.W)
    val funct3 = UInt(3.W)
    val rd = UInt(5.W)
    val opcode = UInt(7.W)
  }

}
