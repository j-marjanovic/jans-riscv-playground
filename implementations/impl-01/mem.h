
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "mem_types.h"

#include <stdarg.h>
#include <stdint.h>
#include <stdio.h>

#define NR_DIAG_MSGS (8)

typedef struct {
  struct mem_section *first_section;

  // diagnostics
  int diag_wr_ptr;
  int diag_nr_els;
  char diag_msgs[NR_DIAG_MSGS][64];
} t_mem;

void _mem_add_diag_msg(t_mem *mem, char *format, ...) {
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

void mem_print_diag(void *mem_impl) {
  t_mem *mem = (t_mem *)mem_impl;

  printf(KGRN);
  if (mem->diag_nr_els == 0) {
    printf("No memory operations to report yet\n");
    printf(KNRM);
    return;
  }

  printf("Last %d memory operations (last first):\n", NR_DIAG_MSGS);

  int cur_pos = mem->diag_wr_ptr - 1;
  if (cur_pos < 0) {
    cur_pos = NR_DIAG_MSGS - 1;
  }

  for (int i = 0; i < mem->diag_nr_els; i++) {
    printf("  %2d: %s\n", i, mem->diag_msgs[cur_pos]);
    if (--cur_pos < 0) {
      cur_pos = NR_DIAG_MSGS - 1;
    }
  }
  printf(KNRM);
}

struct mem_section *_find_section(t_mem *mem, uint32_t addr, uint32_t *offs) {
  struct mem_section *section = mem->first_section;
  while (section) {
    if ((addr >= section->ph.p_paddr) &&
        (addr < section->ph.p_paddr + section->ph.p_memsz)) {
      *offs = addr - section->ph.p_paddr;
      return section;
    }
    section = section->next;
  }

  return NULL;
}

#define PT_FLAGS_WRITE (1 << 1)

void mem_write32(void *mem_impl, uint32_t addr, uint32_t data) {
  t_mem *mem = (t_mem *)mem_impl;

  printf("[mem]       write32 to 0x%x = 0x%x\n", addr, data);
  _mem_add_diag_msg(mem, "write word to 0x%x = 0x%x", addr, data);

  uint32_t offs;
  struct mem_section *section = _find_section(mem, addr, &offs);
  assert(section);
  assert(section->ph.p_flags & PT_FLAGS_WRITE);

  memcpy((uint8_t *)section->mem + offs, &data, 4);
}

void mem_write16(void *mem_impl, uint32_t addr, uint16_t data) {
  t_mem *mem = (t_mem *)mem_impl;

  printf("[mem]       write16 to 0x%x = 0x%x\n", addr, data);
  _mem_add_diag_msg(mem, "write halfword to 0x%x = 0x%x", addr, data);

  uint32_t offs;
  struct mem_section *section = _find_section(mem, addr, &offs);
  assert(section);
  assert(section->ph.p_flags & PT_FLAGS_WRITE);

  memcpy((uint8_t *)section->mem + offs, &data, 2);
}

void mem_write8(void *mem_impl, uint32_t addr, uint8_t data) {
  t_mem *mem = (t_mem *)mem_impl;

  printf("[mem]       write8 to 0x%x = 0x%x\n", addr, data);
  _mem_add_diag_msg(mem, "write byte to 0x%x = 0x%x", addr, data);

  uint32_t offs;
  struct mem_section *section = _find_section(mem, addr, &offs);
  assert(section);
  assert(section->ph.p_flags & PT_FLAGS_WRITE);

  memcpy((uint8_t *)section->mem + offs, &data, 1);
}

uint32_t mem_read32(void *mem_impl, uint32_t addr, int instr) {
  t_mem *mem = (t_mem *)mem_impl;

  printf("[mem]       read32 from 0x%x\n", addr);

  uint32_t offs;
  struct mem_section *section = _find_section(mem, addr, &offs);
  assert(section);

  uint32_t data;
  memcpy(&data, (uint8_t *)section->mem + offs, 4);
  printf("[mem]       ... read32 from 0x%x = 0x%x\n", addr, data);
  // ignore instr fetch from the diag messages
  if (!instr) {
    _mem_add_diag_msg(mem, "read word from 0x%x = 0x%x", addr, data);
  }
  return data;
}

uint16_t mem_read16(void *mem_impl, uint32_t addr) {
  t_mem *mem = (t_mem *)mem_impl;

  printf("[mem]       read16 from 0x%x\n", addr);

  uint32_t offs;
  struct mem_section *section = _find_section(mem, addr, &offs);
  assert(section);

  uint16_t data;
  memcpy(&data, (uint8_t *)section->mem + offs, 2);

  printf("[mem]       ... read16 from 0x%x = 0x%x\n", addr, data);
  _mem_add_diag_msg(mem, "read halfword from 0x%x = 0x%x", addr, data);

  return data;
}

uint8_t mem_read8(void *mem_impl, uint32_t addr) {
  t_mem *mem = (t_mem *)mem_impl;

  printf("[mem]       read8 from 0x%x\n", addr);

  uint32_t offs;
  struct mem_section *section = _find_section(mem, addr, &offs);
  assert(section);

  uint8_t data;
  memcpy(&data, (uint8_t *)section->mem + offs, 1);

  printf("[mem]       ... read8 from 0x%x = 0x%x\n", addr, data);
  _mem_add_diag_msg(mem, "read byte from 0x%x = 0x%x", addr, data);

  return data;
}

void mem_init(t_mem *mem, struct mem_section *first_section) {
  mem->first_section = first_section;

  mem->diag_wr_ptr = 0;
  mem->diag_nr_els = 0;

  for (unsigned int i = 0; i < NR_DIAG_MSGS; i++) {
    memset(mem->diag_msgs[i], 0, sizeof(*mem->diag_msgs));
  }
}
