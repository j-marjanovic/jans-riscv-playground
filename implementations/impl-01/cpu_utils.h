
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#define KNRM "\x1B[0m"
#define KGRY "\x1B[38;5;240m"
#define KORA "\x1B[38;5;208m"
#define KCYN "\x1B[36m"

const char REG_ABI_NAME[32][4] = {
    "0  ", "ra ", "sp ", "gp ", "tp ", "t0 ", "t1 ", "t2 ", "s0 ", "s1 ", "a0 ",
    "a1 ", "a2 ", "a3 ", "a4 ", "a5 ", "a6 ", "a7 ", "s2 ", "s3 ", "s4 ", "s5 ",
    "s6 ", "s7 ", "s8 ", "s9 ", "s10", "s11", "t3 ", "t4 ", "t5 ", "t6 ",
};

void cpu_dump_regs(t_cpu *cpu, char *func_name, uint32_t func_name_offs) {
  printf("----------------------------------------------\n");
  for (int i = 0; i < 32; i++) {
    if ((i != 0) && (i % 8 == 0)) {
      printf("\n");
    }

    if (i % 8 == 0) {
      printf(" %2d | ", i);
    }

    printf(" %s ", REG_ABI_NAME[i]);

    if (cpu->regs.x[i]) {
      printf(KNRM);
    } else {
      printf(KGRY);
    }

    if (cpu->_regs_prev.x[i] != cpu->regs.x[i]) {
      printf(KORA);
    }

    printf("%08x ", cpu->regs.x[i]);
    printf(KNRM);
  }

  printf("\n");
  printf(" pc = " KCYN "%08x (%s+0x%x)\n" KNRM, cpu->regs.pc, func_name,
         func_name_offs);
  printf("----------------------------------------------\n");
}
