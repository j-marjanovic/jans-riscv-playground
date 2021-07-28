package ervp03

import chisel3._

/// similar to `Valid` from chisel utils, but with no direction
class ValidNoDir[+T <: Data](gen: T) extends Bundle {
  val valid = Bool()
  val bits  = gen

  override def cloneType: this.type = ValidNoDir(gen).asInstanceOf[this.type]
}

object ValidNoDir {
  def apply[T <: Data](gen: T): ValidNoDir[T] = new ValidNoDir(gen)
}