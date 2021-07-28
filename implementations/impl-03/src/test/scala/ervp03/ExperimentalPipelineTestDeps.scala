package ervp03

class ExperimentalPipelineTestDeps(c: ExperimentPipeline) extends ExperimentPipelineTestGeneric(c) {
  override val instrs_txt: String =
    """00000200 <_start>:
      | 200:	00100513          	li	a0,1
      | 204:	00200593          	li	a1,2
      | 208:	00b50633          	add	a2,a0,a1
      | 20c:	00000013          	nop
      | 210:	00000013          	nop
      | 214:	00000013          	nop
      | 218:	00000013          	nop
      | 21c:	00000013          	nop
      | 220:	00000013          	nop
      | 224:	00000013          	nop
      | 228:	00000013          	nop
      | 22c:	05d00893          	li	a7,93
      | 230:	00000073          	ecall""".stripMargin

  //
  step(12)
  mem_en = false
  val regs = dump_regs()
  expect(regs(12) == 3, "reg a0")

}
