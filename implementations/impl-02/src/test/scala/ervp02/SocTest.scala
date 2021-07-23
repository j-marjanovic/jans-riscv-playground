// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.iotesters.PeekPokeTester

class SocTest(c: ERVP02) extends PeekPokeTester(c) {

  val mem_secs = ElfParser.parse("../../software/cpu_test2")
  for (mem_sec <- mem_secs) {
    println(mem_sec.toString)
  }
}
