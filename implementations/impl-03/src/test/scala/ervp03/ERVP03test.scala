// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3.iotesters._

class ERVP03test extends ChiselFlatSpec {

  it should "check the branch shadow - shadow completeness" in {
    Driver.execute(
      Array(
        // "--is-verbose",
        "--backend-name",
        "verilator",
        "--fint-write-vcd",
        "--test-seed",
        "1234",
        "--target-dir",
        "test_run_dir/ExperimentPipelineTest",
        "--top-name",
        "ExperimentPipelineTest"
      ),
      () => new ExperimentPipeline
    ) { c =>
      new ExperimentPipelineTestShadow0(c)
    } should be(true)
  }

  it should "check the branch shadow - shadow edge" in {
    Driver.execute(
      Array(
        // "--is-verbose",
        "--backend-name",
        "verilator",
        "--fint-write-vcd",
        "--test-seed",
        "1234",
        "--target-dir",
        "test_run_dir/ExperimentPipelineTest",
        "--top-name",
        "ExperimentPipelineTest"
      ),
      () => new ExperimentPipeline
    ) { c =>
      new ExperimentPipelineTestShadow1(c)
    } should be(true)
  }

  it should "check the branch shadow - reg deps" in {
    Driver.execute(
      Array(
        // "--is-verbose",
        "--backend-name",
        "verilator",
        "--fint-write-vcd",
        "--test-seed",
        "1234",
        "--target-dir",
        "test_run_dir/ExperimentPipelineTest",
        "--top-name",
        "ExperimentPipelineTest"
      ),
      () => new ExperimentPipeline
    ) { c =>
      new ExperimentalPipelineTestDeps(c)
    } should be(true)
  }

}
