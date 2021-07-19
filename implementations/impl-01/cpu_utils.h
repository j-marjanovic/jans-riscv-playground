
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#define KNRM "\x1B[0m"
#define KGRY "\x1B[38;5;240m"

void cpu_dump_regs(t_cpu *cpu) {
  printf("----------------------------------------------\n");
  for (int i = 0; i < 32; i++) {
    if ((i != 0) && (i % 8 == 0)) {
      printf("\n");
    }

    if (i % 8 == 0) {
      printf(" %2d | ", i);
    }

    if (cpu->regs.x[i]) {
      printf(KNRM);
    } else {
      printf(KGRY);
    }
    printf("%08x ", cpu->regs.x[i]);
    printf(KNRM);
  }

  printf("\n");
  printf(" pc = %08x\n", cpu->regs.pc);
  printf("----------------------------------------------\n");
}
