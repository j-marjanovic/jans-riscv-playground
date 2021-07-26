// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

class CpuTestBranch(c: Cpu) extends CpuTestGeneric(c) {
  override val INSTRS: String =
    """ 00000200 <_start>:
      | 200:	00100293          	li	t0,1
      | 204:	00200313          	li	t1,2
      | 208:	00300393          	li	t2,3
      | 20c:	00628c63          	beq	t0,t1,224 <set_to_3>
      | 210:	00731663          	bne	t1,t2,21c <set_to_2>
      |
      |00000214 <set_to_1>:
      | 214:	00100513          	li	a0,1
      | 218:	0100006f          	j	228 <write_data>
      |
      |0000021c <set_to_2>:
      | 21c:	00200513          	li	a0,2
      | 220:	0080006f          	j	228 <write_data>
      |
      |00000224 <set_to_3>:
      | 224:	00300513          	li	a0,3
      |
      |00000228 <write_data>:
      | 228:	000022b7          	lui	t0,0x2
      | 22c:	00a2a023          	sw	a0,0(t0) # 2000 <_end+0x1dbc>
      |""".stripMargin

  override def final_check(): Unit = {
    val mem_txs = mem_dummy.mem_txs.toList
    expect(mem_txs.head.addr == 0x2000, "mem addr 0")
    expect(mem_txs.head.data == 0x2, "mem data 0")
  }

  run_test()
}
