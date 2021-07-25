package ervp02

import chisel3._
import chisel3.iotesters.SteppedHWIOTester

class StoreLoadTest extends SteppedHWIOTester{
  override val device_under_test = Module(new StoreLoad(10))

  poke(device_under_test.io.decoder_itype.funct3, 5)
  poke(device_under_test.io.enable_op_load, 1)
  poke(device_under_test.io.enable_op_store, 0)
  poke(device_under_test.io.act, 1)
  poke(device_under_test.io.mem_data.din, 0xFFFF0203L)
  poke(device_under_test.io.addr, 0)
  poke(device_under_test.io.din, 0x01020304)
  step(1)
  expect(device_under_test.io.dout, 0x0203)

}
