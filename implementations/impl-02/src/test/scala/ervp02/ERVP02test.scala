// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02

import chisel3.iotesters.ChiselFlatSpec

class ERVP02test extends ChiselFlatSpec {
  behavior of "ERVP02"

  it should "should check the decoder" in {
    assertTesterPasses(new DecodeTest())
  }

  it should "should check the register file" in {
    assertTesterPasses(new RegFileTest())
  }
}
