
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>

#define NR_DIAG_MSGS (8)

typedef struct {
  uint32_t size;
  void *mem;
  // diagnostics
  int diag_wr_ptr;
  int diag_nr_els;
  char diag_msgs[NR_DIAG_MSGS][32];
} t_array_mem;

void _array_mem_add_diag_msg(t_array_mem *mem, char *format, ...) {
  va_list args;
  va_start(args, format);
  vsnprintf(mem->diag_msgs[mem->diag_wr_ptr], sizeof(*mem->diag_msgs), format,
            args);
  va_end(args);

  mem->diag_wr_ptr = (mem->diag_wr_ptr + 1) % NR_DIAG_MSGS;
  if (mem->diag_nr_els < NR_DIAG_MSGS) {
    mem->diag_nr_els++;
  }
}

#define KNRM "\x1B[0m"
#define KGRN "\x1B[38;5;34m"

void array_mem_print_diag(void *mem_impl) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  printf(KGRN);
  if (arr_mem->diag_nr_els == 0) {
    printf("No memory operations to report yet\n");
    printf(KNRM);
    return;
  }

  printf("Last %d memory operations (last first):\n", NR_DIAG_MSGS);

  int cur_pos = arr_mem->diag_wr_ptr - 1;
  if (cur_pos < 0) {
    cur_pos = NR_DIAG_MSGS - 1;
  }

  for (int i = 0; i < arr_mem->diag_nr_els; i++) {
    printf("  %2d: %s\n", i, arr_mem->diag_msgs[cur_pos]);
    if (--cur_pos < 0) {
      cur_pos = NR_DIAG_MSGS - 1;
    }
  }
  printf(KNRM);
}

void array_mem_write32(void *mem_impl, uint32_t addr, uint32_t data) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  printf("[array_mem] write32 to 0x%x = 0x%x\n", addr, data);
  _array_mem_add_diag_msg(arr_mem, "write word to 0x%x = 0x%x", addr, data);

  memcpy((uint8_t *)arr_mem->mem + addr, &data, 4);
}

void array_mem_write16(void *mem_impl, uint32_t addr, uint16_t data) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  printf("[array_mem] write16 to 0x%x = 0x%x\n", addr, data);
  _array_mem_add_diag_msg(arr_mem, "write halfword to 0x%x = 0x%x", addr, data);

  memcpy((uint8_t *)arr_mem->mem + addr, &data, 2);
}

void array_mem_write8(void *mem_impl, uint32_t addr, uint8_t data) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  printf("[array_mem] write8 to 0x%x = 0x%x\n", addr, data);
  _array_mem_add_diag_msg(arr_mem, "write byte to 0x%x = 0x%x", addr, data);

  memcpy((uint8_t *)arr_mem->mem + addr, &data, 1);
}

uint32_t array_mem_read32(void *mem_impl, uint32_t addr, int instr) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  uint32_t data;
  memcpy(&data, (uint8_t *)arr_mem->mem + addr, 4);

  printf("[array_mem] read32 from 0x%x = 0x%x\n", addr, data);
  // ignore instr fetch from the diag messages
  if (!instr) {
    _array_mem_add_diag_msg(arr_mem, "read word from 0x%x = 0x%x", addr, data);
  }

  return data;
}

uint16_t array_mem_read16(void *mem_impl, uint32_t addr) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  uint16_t data;
  memcpy(&data, (uint8_t *)arr_mem->mem + addr, 2);

  printf("[array_mem] read16 from 0x%x = 0x%x\n", addr, data);
  _array_mem_add_diag_msg(arr_mem, "read halfword from 0x%x = 0x%x", addr,
                          data);

  return data;
}

uint8_t array_mem_read8(void *mem_impl, uint32_t addr) {
  t_array_mem *arr_mem = (t_array_mem *)mem_impl;

  assert(addr < arr_mem->size);

  uint8_t data;
  memcpy(&data, (uint8_t *)arr_mem->mem + addr, 1);

  printf("[array_mem] read8 from 0x%x = 0x%x\n", addr, data);
  _array_mem_add_diag_msg(arr_mem, "read byte from 0x%x = 0x%x", addr, data);

  return data;
}

void array_mem_alloc(t_array_mem *mem, uint32_t size) {
  mem->mem = malloc(size);
  assert(mem->mem);
  mem->size = size;

  mem->diag_wr_ptr = 0;
  mem->diag_nr_els = 0;

  for (unsigned int i = 0; i < NR_DIAG_MSGS; i++) {
    memset(mem->diag_msgs[i], 0, sizeof(*mem->diag_msgs));
  }
}

// use this if the memory was already allocated somewhere else
void array_mem_init(t_array_mem *mem, void *buffer, uint32_t size) {
  mem->mem = buffer;
  mem->size = size;

  mem->diag_wr_ptr = 0;
  mem->diag_nr_els = 0;

  for (unsigned int i = 0; i < NR_DIAG_MSGS; i++) {
    memset(mem->diag_msgs[i], 0, sizeof(*mem->diag_msgs));
  }
}

void array_mem_free(t_array_mem *mem) { free(mem->mem); }
