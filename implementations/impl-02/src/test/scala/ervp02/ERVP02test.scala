// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.iotesters._

class ERVP02test extends ChiselFlatSpec {

  it should "check the decoder" in {
    assertTesterPasses(new DecoderTest())
  }

  it should "check the register file" in {
    assertTesterPasses(new RegFileTest())
  }

  it should "check the ALU" in {
    assertTesterPasses(new ALUTest())
  }

  it should "check the branch controller" in {
    assertTesterPasses(new BranchTest())
  }

  it should "check the CPU ALU" in {
    Driver.execute(
      Array(
        "--backend-name",
        "verilator",
        "--fint-write-vcd",
        "--test-seed",
        "1234",
        "--target-dir",
        "test_run_dir/CpuTestAlu",
        "--top-name",
        "CpuTestAlu"
      ),
      () => new Cpu
    ) { c =>
      new CpuTestAlu(c)
    } should be(true)
  }

  it should "check the CPU branching" in {
    Driver.execute(
      Array(
        "--backend-name",
        "verilator",
        "--fint-write-vcd",
        "--test-seed",
        "1234",
        "--target-dir",
        "test_run_dir/CpuTestBranch",
        "--top-name",
        "CpuTestBranch"
      ),
      () => new Cpu
    ) { c =>
      new CpuTestBranch(c)
    } should be(true)
  }

  it should "check the entire SoC" in {
    Driver.execute(
      Array(
        "--backend-name",
        "verilator",
        "--fint-write-vcd",
        "--test-seed",
        "1234",
        "--target-dir",
        "test_run_dir/SocTest",
        "--top-name",
        "SocTest"
      ),
      () => new ERVP02
    ) { c =>
      new SocTest(c)
    } should be(true)
  }
}
