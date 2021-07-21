// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.stage.ChiselStage

object ERVP02main extends App {
  (new ChiselStage)
    .emitVerilog(new ERVP02, Array[String]("--target-dir", "output/ervp02/hdl") ++ args)
}
