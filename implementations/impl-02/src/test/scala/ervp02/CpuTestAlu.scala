// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

class CpuTestAlu(c: Cpu) extends CpuTestGeneric(c) {

  override val INSTRS: String =
    """ 200:	00100513          	li	a0,1
      | 204:	00200593          	li	a1,2
      | 208:	00b50633          	add	a2,a0,a1
      | 20c:	00a60613          	addi	a2,a2,10
      | 210:	000022b7          	lui	t0,0x2
      | 214:	10028293          	addi	t0,t0,256 # 2100 <_end+0x1ecc>
      | 218:	00c2a823          	sw	a2,16(t0)
      | 21c:	fea28fa3          	sb	a0,-1(t0)
      | 220:	feb28f23          	sb	a1,-2(t0)
      |""".stripMargin

  override def final_check(): Unit = {
    val mem_txs = mem_dummy.mem_txs.toList
    expect(mem_txs(0).addr == 0x110, "mem addr 0")
    expect(mem_txs(0).data == 0xd, "mem data 0")

    expect(mem_txs(1).addr == 0xfc, "mem addr 1")
    expect(mem_txs(1).data == 0x1, "mem data 1")
    // TODO: add byte enable, byte addressing

    expect(mem_txs(2).addr == 0xfc, "mem addr 2")
    expect(mem_txs(2).data == 0x2, "mem data 2")
    // TODO: add byte enable, byte addressing
  }

  run_test()
}
