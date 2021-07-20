
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "mem_types.h"

const uint32_t ELF_HEADER_MAG = 0x464c457F;
const uint8_t ELF_HEADER_MACHINE_RISCV = 0xf3;

#define PT_LOAD (1)

struct mem_section *load_elf(const char *filename, uint32_t *entry) {

  // open file
  int fd = open(filename, O_RDWR);
  if (fd < 0) {
    perror("[elf loader] open()");
    return NULL;
  }

  // read header
  struct elf_header hdr;
  read(fd, &hdr, sizeof(hdr));
  *entry = hdr.e_entry;

  // go to the program header files
  lseek(fd, hdr.e_phoff, SEEK_SET);

  // prepare linked-list
  struct mem_section *first_section = NULL;
  struct mem_section *prev_section = NULL;

  // read headers
  for (int i = 0; i < hdr.e_phnum; i++) {
    struct mem_section *cur_section = malloc(sizeof(struct mem_section));

    read(fd, &cur_section->ph, sizeof(struct ph_entry));

    printf("[elf loader] header %d\n", i);
    printf("[elf loader]   type   = %d\n", cur_section->ph.p_type);

    printf("[elf loader]   flags  = %x (", cur_section->ph.p_flags);
    printf("%c", (cur_section->ph.p_flags & (1 << 2)) ? 'R' : '-');
    printf("%c", (cur_section->ph.p_flags & (1 << 1)) ? 'W' : '-');
    printf("%c", (cur_section->ph.p_flags & (1 << 0)) ? 'X' : '-');
    printf(")\n");

    printf("[elf loader]   offset = 0x%x\n", cur_section->ph.p_offset);
    printf("[elf loader]   vaddr  = 0x%x\n", cur_section->ph.p_vaddr);
    printf("[elf loader]   paddr  = 0x%x\n", cur_section->ph.p_paddr);
    printf("[elf loader]   filesz = 0x%x\n", cur_section->ph.p_filesz);
    printf("[elf loader]   memsz  = 0x%x\n", cur_section->ph.p_memsz);

    if (cur_section->ph.p_type != PT_LOAD) {
      printf("[elf loader] not a LOAD section, continue...\n");
      continue;
    }

    // linked-list management
    if (!first_section) {
      first_section = cur_section;
    }
    if (prev_section) {
      prev_section->next = cur_section;
    }
    prev_section = cur_section;
  }

  // allocate mem and copy data from file
  struct mem_section *temp_section = first_section;
  while (temp_section) {
    temp_section->mem = malloc(temp_section->ph.p_memsz);
    assert(temp_section->mem);

    ssize_t ret = pread(fd, temp_section->mem, temp_section->ph.p_filesz,
                        temp_section->ph.p_offset);
    assert(ret == temp_section->ph.p_filesz);

    temp_section = temp_section->next;
  }

  return first_section;
}