// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3.iotesters.PeekPokeTester

import scala.collection.mutable

class ExperimentPipelineTest(c: ExperimentPipeline) extends PeekPokeTester(c) {

  val instrs_txt2 =
    """00000200 <_start>:
       | 200:	00100513          	li	a0,1
       | 204:	00200513          	li	a0,2
       | 208:	00b51e63          	bne	a0,a1,224 <exit>
       | 20c:	00300513          	li	a0,3
       | 210:	00400513          	li	a0,4
       | 214:	00500513          	li	a0,5
       | 218:	00600513          	li	a0,6
       | 21c:	00700513          	li	a0,7
       | 220:	00800513          	li	a0,8
       |
       |00000224 <exit>:
       | 224:	00000013          	nop
       | 228:	00000013          	nop
       | 22c:	00000013          	nop
       | 230:	00000013          	nop
       | 234:	00000013          	nop
       | 238:	00000013          	nop
       | 23c:	05d00893          	li	a7,93
       | 240:	00000073          	ecall""".stripMargin


  val instrs_txt =
    """00000200 <_start>:
      | 200:	00100513          	li	a0,1
      | 204:	00200513          	li	a0,2
      | 208:	00000013          	nop
      | 20c:	00000013          	nop
      | 210:	00000013          	nop
      | 214:	00000013          	nop
      | 218:	00000013          	nop
      | 21c:	00000013          	nop
      | 220:	00c586b3          	add	a3,a1,a2
      | 224:	00000013          	nop
      | 228:	00000013          	nop
      | 22c:	00000013          	nop
      | 230:	00000013          	nop
      | 234:	00000013          	nop
      | 238:	00000013          	nop
      | 23c:	00000013          	nop
      | 240:	00000013          	nop
      | 244:	00000013          	nop
      | 248:	00000013          	nop
      | 24c:	00b51e63          	bne	a0,a1,268 <exit>
      | 250:	00300513          	li	a0,3
      | 254:	00400513          	li	a0,4
      | 258:	00500513          	li	a0,5
      | 25c:	00600513          	li	a0,6
      | 260:	00700513          	li	a0,7
      | 264:	00800513          	li	a0,8
      |
      |00000268 <exit>:
      | 268:	00000013          	nop
      | 26c:	00000013          	nop
      | 270:	00000013          	nop
      | 274:	00000013          	nop
      | 278:	00000013          	nop
      | 27c:	00000013          	nop
      | 280:	05d00893          	li	a7,93
      | 284:	00000073          	ecall""".stripMargin

  val instrs: mutable.Map[Int, (BigInt, String)] =
    DisassemblyParser.parse_instrs(instrs_txt)

  for (instr <- instrs.toSeq.sortBy(_._1).map(_._2)) {
    println(s"instr = ${instr._2}")
    poke(c.io.mem.din, instr._1)
    step(1)
  }

}
