// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3.stage.ChiselStage

object ERVP03main extends App {
  (new ChiselStage)
    .emitVerilog(
      new ExperimentPipeline,
      Array[String]("--target-dir", "output/ervp03/experiment") ++ args
    )
}
