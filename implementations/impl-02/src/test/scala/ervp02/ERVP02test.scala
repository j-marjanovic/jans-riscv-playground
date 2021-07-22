// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.iotesters.ChiselFlatSpec
import chisel3.iotesters

class ERVP02test extends ChiselFlatSpec {
  behavior of "ERVP02"

  it should "should check the decoder" in {
    assertTesterPasses(new DecoderTest())
  }

  it should "should check the register file" in {
    assertTesterPasses(new RegFileTest())
  }

  it should "should check the ALU" in {
    assertTesterPasses(new ALUTest())
  }

  it should "check the CPU" in {
    iotesters.Driver.execute(
      Array(
        "--backend-name",
        "verilator",
        "--fint-write-vcd",
        "--test-seed",
        "1234",
        "--target-dir",
        "test_run_dir/CpuTest",
        "--top-name",
        "CpuTest"
      ),
      () => new Cpu
    ) { c =>
      new CpuTest(c)
    }
  }
}
