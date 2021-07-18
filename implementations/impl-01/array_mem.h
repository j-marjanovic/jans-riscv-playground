
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include <stdint.h>
#include <stdio.h>

typedef struct {
  uint32_t size;
  void *mem;
} t_array_mem;

void array_mem_write(void *mem_impl, uint32_t addr, uint32_t data) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  printf("[array_mem] write to 0x%x = 0x%x\n", addr, data);

  memcpy((uint8_t *)arr_mem->mem + addr, &data, 4);
}

uint32_t array_mem_read(void *mem_impl, uint32_t addr) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  uint32_t data;
  memcpy(&data, (uint8_t *)arr_mem->mem + addr, 4);

  printf("[array_mem] read from 0x%x = 0x%x\n", addr, data);

  return data;
}

void array_mem_alloc(t_array_mem *mem, uint32_t size) {
  mem->mem = malloc(size);
  assert(mem->mem);
  mem->size = size;
}

void array_mem_free(t_array_mem *mem) { free(mem->mem); }
