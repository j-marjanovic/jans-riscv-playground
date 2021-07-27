// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3.iotesters._

class ERVP03test extends ChiselFlatSpec {

  it should "check the pipeline behavior" in {
    Driver.execute(
      /*
      Array(
        "--backend-name",
        "treadle",
        // "--tr-verbose",
        "--tr-random-seed",
        "1234",
        "--tr-write-vcd",
        // "--target-dir",
        // "test_run_dir/ExperimentPipelineTest",
        "--top-name",
        "ExperimentPipelineTest",
      ),
      */
      Array(
        "--is-verbose",
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
      new ExperimentPipelineTest(c)
    } should be(true)
  }
}
