
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

typedef struct {
  uint32_t x[32];
  uint32_t pc;
} t_regs;

typedef struct {
  void (*write32)(void *, uint32_t, uint32_t);
  void (*write16)(void *, uint32_t, uint16_t);
  void (*write8)(void *, uint32_t, uint8_t);
  uint32_t (*read32)(void *, uint32_t, int);
  uint16_t (*read16)(void *, uint32_t);
  uint8_t (*read8)(void *, uint32_t);
  void (*print_diag)(void *);
} t_mem_ops;

typedef struct {
  t_regs regs;
  t_mem_ops *mem_ops;
  void *mem_impl;
  // diagnostics only
  t_regs _regs_prev;
} t_cpu;

typedef void (*t_cpu_op)(t_cpu *cpu, uint32_t instr);
