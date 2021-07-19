
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

typedef struct {
  uint32_t x[32];
  uint32_t pc;
} t_regs;

typedef struct {
  void (*write)(void *, uint32_t, uint32_t);
  uint32_t (*read)(void *, uint32_t);
} t_mem_ops;

typedef struct {
  t_regs regs;
  t_mem_ops *mem_ops;
  void *mem_impl;
} t_cpu;
