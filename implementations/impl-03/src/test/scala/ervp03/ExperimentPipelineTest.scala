// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp03

import chisel3.iotesters.PeekPokeTester

class ExperimentPipelineTest(c: ExperimentPipeline) extends PeekPokeTester(c) {}
