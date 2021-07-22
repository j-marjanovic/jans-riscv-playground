// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.iotesters.PeekPokeTester
import chisel3._

import scala.collection.mutable.ListBuffer

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
  /*
   200:	00100513          	li	a0,1
   204:	00200593          	li	a1,2
   208:	00b50633          	add	a2,a0,a1
   20c:	00a60613          	addi	a2,a2,10
   210:	000022b7          	lui	t0,0x2
   214:	00c2a823          	sw	a2,16(t0) # 2010 <_end+0x1dec>
   */

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

  poke(c.enable, 1)

  poke(c.mem_instr.din, 0x00100513)
  wait_for_next_pc()

  poke(c.mem_instr.din, 0x00200593)
  wait_for_next_pc()

  poke(c.mem_instr.din, 0x00b50633)
  wait_for_next_pc()

  poke(c.mem_instr.din, 0x00a60613)
  wait_for_next_pc()

  poke(c.mem_instr.din, 0x000022b7)
  wait_for_next_pc()

  poke(c.mem_instr.din, 0x00c2a823)
  wait_for_next_pc(() => mem_dummy.check_data(c))
  expect(mem_dummy.mem_txs.head.addr == 0x10, "mem addr")
  expect(mem_dummy.mem_txs.head.data == 0xd, "mem data")

}
