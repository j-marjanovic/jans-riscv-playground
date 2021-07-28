// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

class ExperimentPipelineTestShadow1(c: ExperimentPipeline) extends ExperimentPipelineTestGeneric(c) {

  val instrs_txt =
    """00000200 <_start>:
      | 200:	00100513          	li	a0,1
      | 204:	00200513          	li	a0,2
      | 208:	00000013          	nop
      | 20c:	00000013          	nop
      | 210:	00000013          	nop
      | 214:	00000013          	nop
      | 218:	00000013          	nop
      | 21c:	0ab00593          	li	a1,171
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


  //
  step(30)
  mem_en = false
  val regs = dump_regs()
  println(s"regs = ${regs}")
  expect(regs(10) == 0xFF, "reg a0")
  expect(regs(11) == 0xab, "reg a1")

}
