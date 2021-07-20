
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#include <assert.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

#include "cpu.h"
#include "elf_loader.h"
#include "mem.h"

int main() {

  uint32_t entry_point;
  struct mem_section *first_section =
      load_elf("../../software/hello_world", &entry_point);

  // add a section for the stack
  append_section(first_section, 0x40800000 - 1024, 1024, 0x6);

  t_mem mem_impl;
  mem_init(&mem_impl, first_section);

  t_cpu cpu;
  t_mem_ops mem_ops = {
      .read32 = mem_read32,
      .read16 = mem_read16,
      .read8 = mem_read8,
      .write32 = mem_write32,
      .write16 = mem_write16,
      .write8 = mem_write8,
      .print_diag = mem_print_diag,
  };

  create_cpu(&cpu, (void *)&mem_impl, &mem_ops);
  cpu.regs.pc = entry_point;
  cpu.regs.x[2] = 0x407ffff0; // TODO: figure out where to get this?

  const int NR_INSTR_TO_EXEC = 1000;
  for (int i = 0; i < NR_INSTR_TO_EXEC; i++) {
    cpu_exec_instr(&cpu);
  }

  return 0;
}
