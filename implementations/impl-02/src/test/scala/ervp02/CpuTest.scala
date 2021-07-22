// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.iotesters.PeekPokeTester
import chisel3._

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class MemDummy(val peek: Bits => BigInt) {
  class MemTx(val addr: BigInt, val data: BigInt)

  val mem_txs = ListBuffer[MemTx]()

  def check_data(c: Cpu): Unit = {
    val we = peek(c.mem_data.we)
    if (we == 1) {
      val addr = peek(c.mem_data.addr)
      val data = peek(c.mem_data.dout)
      println(f"mem write, addr = 0x${addr}%x, data = 0x${data}%x")
      mem_txs += new MemTx(addr, data)
    }
  }
}

class CpuTest(c: Cpu) extends PeekPokeTester(c) {

  def bfm_peek(bs: Bits): BigInt = this.peek(bs)
  val mem_dummy = new MemDummy(bfm_peek)

  def wait_for_next_pc(callback: () => Unit = () => {}): Unit = {
    var timeout = 100
    var pc = peek(c.mem_instr.addr)
    val pc_prev = pc
    while (pc == pc_prev) {
      pc = peek(c.mem_instr.addr)
      callback()
      step(1)
      if (timeout == 0) {
        throw new RuntimeException("timeout while waiting for the next PC")
      }
      timeout -= 1
    }
  }

  val INSTRS =
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

  val LINE_FMT: Regex = raw"\s*([0-9a-fA-F]+):\s+([0-9a-fA-F]+)\s+(.*)".r

  for (line <- INSTRS.split("\n")) {
    val m: Regex.Match = LINE_FMT.findFirstMatchIn(line).get
    val instr_addr = m.group(1)
    val instr_raw: BigInt = BigInt(m.group(2), 16)
    val instr_comment = m.group(3)
    println(f"execution instr '${instr_comment}' = 0x${instr_raw}%08x")

    poke(c.enable, 1)
    poke(c.mem_instr.din, instr_raw)
    wait_for_next_pc(() => mem_dummy.check_data(c))
  }

  val mem_txs = mem_dummy.mem_txs.toList
  expect(mem_txs(0).addr == 0x110, "mem addr 0")
  expect(mem_txs(0).data == 0xd, "mem data 0")

  expect(mem_txs(1).addr == 0xff, "mem addr 1")
  expect(mem_txs(1).data == 0x1, "mem data 1")
  // TODO: add byte enable

  expect(mem_txs(2).addr == 0xfe, "mem addr 2")
  expect(mem_txs(2).data == 0x2, "mem data 2")
  // TODO: add byte enable
}
