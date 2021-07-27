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
      | 220:	02b51263          	bne	a0,a1,244 <exit>
      | 224:	00300513          	li	a0,3
      | 228:	00400513          	li	a0,4
      | 22c:	00500513          	li	a0,5
      | 230:	00600513          	li	a0,6
      | 234:	00700513          	li	a0,7
      | 238:	00800513          	li	a0,8
      | 23c:	00900513          	li	a0,9
      | 240:	00a00513          	li	a0,10
      |
      |00000244 <exit>:
      | 244:	0FF00513          	li	a0,0xff
      | 248:	00000013          	nop
      | 24c:	00000013          	nop
      | 250:	00000013          	nop
      | 254:	00000013          	nop
      | 258:	00000013          	nop
      | 25c:	05d00893          	li	a7,93
      | 260:	00000073          	ecall""".stripMargin

  val instrs: mutable.Map[Int, (BigInt, String)] =
    DisassemblyParser.parse_instrs(instrs_txt)

  def step_single(): Unit = {
    val addr = peek(c.io.mem.addr)
    println(f"addr = ${addr}%x")
    val instr = instrs(addr.toInt)
    println(s"instr = ${instr._2}")
    poke(c.io.mem.din, instr._1)
    super.step(1)
  }

  override def step(n: Int): Unit ={
    for (_ <- 0 until n) {
      step_single()
    }
  }

  step(20)

}
