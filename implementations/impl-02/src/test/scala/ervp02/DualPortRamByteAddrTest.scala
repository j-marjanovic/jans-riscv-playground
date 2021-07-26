package ervp02

import chisel3.iotesters.PeekPokeTester

class DualPortRamByteAddrTest(c: DualPortRamByteAddr) extends PeekPokeTester(c) {

  // write on A port, read back later
  poke(c.io.dina, 0x0a0b0c0d)
  poke(c.io.byte_ena, 0xf)
  poke(c.io.addra, 0x100)
  poke(c.io.wea, 1)
  step(1)
  poke(c.io.dina, 0x01020304)
  poke(c.io.byte_ena, 0xf)
  poke(c.io.addra, 0x104)
  poke(c.io.wea, 1)
  step(1)
  poke(c.io.dina, 0)
  poke(c.io.byte_ena, 0)
  poke(c.io.addra, 0x100)
  poke(c.io.wea, 0)
  step(1)
  expect(c.io.douta, 0x0a0b0c0d)
  poke(c.io.addra, 0x104)
  step(1)
  expect(c.io.douta, 0x01020304)

  // use byte enable, port B
  poke(c.io.dinb, 0x42)
  poke(c.io.byte_enb, 0x1)
  poke(c.io.addrb, 0x100)
  poke(c.io.web, 1)
  step(1)
  poke(c.io.dinb, 0x69)
  poke(c.io.byte_enb, 0x1)
  poke(c.io.addrb, 0x104)
  poke(c.io.web, 1)
  step(1)
  poke(c.io.web, 0)
  poke(c.io.addrb, 0x100)
  step(1)
  expect(c.io.doutb, 0x0a0b0c42)
  poke(c.io.addrb, 0x104)
  step(1)
  expect(c.io.doutb, 0x01020369)

  // using byte addressing, read
  poke(c.io.addra, 0x101)
  step(1)
  expect(c.io.douta, 0x0a0b0c)
  poke(c.io.addra, 0x102)
  step(1)
  expect(c.io.douta, 0x0a0b)
  poke(c.io.addra, 0x103)
  step(1)
  expect(c.io.douta, 0x0a)

  // using byte addressing, write
  poke(c.io.addrb, 0x105)
  poke(c.io.dinb, 0x7700)
  poke(c.io.byte_enb, 0x2)
  poke(c.io.web, 1)
  step(1)
  poke(c.io.web, 0)

  poke(c.io.addrb, 0x104)
  step(1)
  expect(c.io.doutb, 0x01770369)

  step(1)
}
