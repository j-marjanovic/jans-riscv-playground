package ervp03

import scala.collection.mutable
import scala.util.matching.Regex

object DisassemblyParser {

  def parse_instrs(instrs: String): mutable.Map[Int, (BigInt, String)] = {
    val instr_mem = mutable.Map[Int, (BigInt, String)]()

    val LINE_FMT: Regex = raw"\s*([0-9a-fA-F]+):\s+([0-9a-fA-F]+)\s+(.*)".r
    for (line <- instrs.split("\n")) {
      val m: Option[Regex.Match] = LINE_FMT.findFirstMatchIn(line)
      if (m.isDefined) {
        val m_ = m.get
        val instr_addr: Int = BigInt(m_.group(1), 16).toInt
        val instr_raw: BigInt = BigInt(m_.group(2), 16)
        val instr_comment: String = m_.group(3)

        instr_mem += (instr_addr -> (instr_raw, instr_comment))
      }
    }

    instr_mem
  }
}
