// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3._

class ControlSet extends Bundle {
  val enable_op_alu = Bool()
  val enable_op_alu_imm = Bool()
  val enable_op_branch = Bool()
  val enable_op_jalr = Bool()
  val enable_op_load = Bool()
  val enable_op_lui = Bool()
  val enable_op_store = Bool()

  val br_shadow = UInt(2.W)
  val br_shadow_en = ValidNoDir(UInt(2.W))

  val pc = UInt(32.W)
}
