package ervp02

import chisel3._
import chisel3.iotesters.SteppedHWIOTester

class RegFileTest extends SteppedHWIOTester {
  override val device_under_test = Module(new RegFile())

  for (i <- 0 until 31) {
    poke(device_under_test.io.rd, i)
    poke(device_under_test.io.din, i)
    poke(device_under_test.io.we, 1)
    step(1)
  }

  poke(device_under_test.io.we, 0)

  poke(device_under_test.io.rs1, 1)
  poke(device_under_test.io.rs2, 2)
  step(1)
  expect(device_under_test.io.dout1, 1)
  expect(device_under_test.io.dout2, 2)

  poke(device_under_test.io.rs1, 0)
  poke(device_under_test.io.rs2, 0)
  step(1)
  expect(device_under_test.io.dout1, 0)
  expect(device_under_test.io.dout2, 0)

  poke(device_under_test.io.we, 1)
  poke(device_under_test.io.rd, 10)
  poke(device_under_test.io.din, 0x1234)
  step(1)

  poke(device_under_test.io.we, 0)

  poke(device_under_test.io.rs1, 10)
  poke(device_under_test.io.rs2, 10)
  step(1)
  expect(device_under_test.io.dout1, 0x1234)
  expect(device_under_test.io.dout2, 0x1234)

}
