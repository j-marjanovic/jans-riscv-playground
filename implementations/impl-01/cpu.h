
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include <stdint.h>
#include <stdio.h>

#include "cpu_instr.h"
#include "cpu_types.h"
#include "cpu_utils.h"
#include "instr_types.h"

void create_cpu(t_cpu *cpu, void *mem_impl, t_mem_ops *mem_ops) {
  for (unsigned int i = 0; i < 32; i++) {
    cpu->regs.x[i] = 0;
    cpu->_regs_prev.x[i] = 0;
  }
  cpu->regs.pc = 0;

  cpu->mem_ops = mem_ops;
  cpu->mem_impl = mem_impl;
  cpu->_cyc = 0;
}

void cpu_exec_instr(t_cpu *cpu) {
  printf("====   CPU cycle %4d   ====\n", cpu->_cyc);
  uint32_t instr = cpu->mem_ops->read32(cpu->mem_impl, cpu->regs.pc, 1);
  printf("[decoder]   loaded instr = 0x%08x\n", instr);

  uint8_t opcode = instr & 0x7F;
  cpu_ops[opcode](cpu, instr);
  cpu->regs.x[0] = 0;

  cpu_dump_regs(cpu);
  cpu->mem_ops->print_diag(cpu->mem_impl);

  // for diagnostics, store previous register state
  for (unsigned int i = 0; i < 32; i++) {
    cpu->_regs_prev.x[i] = cpu->regs.x[i];
  }
  cpu->_cyc++;
}
