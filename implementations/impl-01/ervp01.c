
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
#include "elf_symtab.h"
#include "mem.h"

int main() {

  uint32_t entry_point;
  struct mem_section *first_section =
      load_elf("../../software/hello_world", &entry_point);
  assert(first_section);

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

  struct symtab_entry *first_symtab =
      load_elf_symtab("../../software/hello_world");
  assert(first_symtab);

  cpu_create(&cpu, (void *)&mem_impl, &mem_ops);
  cpu.regs.pc = entry_point;
  cpu_register_symtab(&cpu, (void *)first_symtab, symtab_get_name);

  // TODO: figure out where to get this?
  cpu.regs.x[2] = 0x407ffff0;
  mem_write32(&mem_impl, 0x407ffff0, 0x00000001);
  mem_write32(&mem_impl, 0x407ffff4, 0x408001a9);
  mem_write32(&mem_impl, 0x407ffff8, 0x00000000);
  mem_write32(&mem_impl, 0x407ffffc, 0x408001c0);

  /*
  (gdb) x 0x407ffff0
  0x407ffff0:     0x00000001
  (gdb) x 0x407ffff4
  0x407ffff4:     0x408001a9
  (gdb) x 0x407ffff8
  0x407ffff8:     0x00000000
  (gdb) x 0x407ffffc
  0x407ffffc:     0x408001c0
  */

  const int NR_INSTR_TO_EXEC = 587;
  for (int i = 0; i < NR_INSTR_TO_EXEC; i++) {
    cpu_exec_instr(&cpu);
  }

  return 0;
}
