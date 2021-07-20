
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "elf_types.h"
#include "mem_types.h"

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

void *append_section(struct mem_section *first_section, uint32_t addr,
                     uint32_t size, uint32_t flags) {
  // find last section
  struct mem_section *last_section = first_section;
  while (last_section->next) {
    last_section = last_section->next;
  }

  struct mem_section *new_section = malloc(sizeof(struct mem_section));
  assert(new_section);
  last_section->next = new_section;

  new_section->ph.p_type = -1;   // not needed by the emulator
  new_section->ph.p_offset = -1; // not needed by the emulator
  new_section->ph.p_vaddr = addr;
  new_section->ph.p_paddr = addr;
  new_section->ph.p_filesz = size;
  new_section->ph.p_memsz = size;
  new_section->ph.p_flags = flags;
  new_section->ph.p_align = -1; // not needed by the emulator

  new_section->next = NULL;

  new_section->mem = malloc(size);
  assert(new_section->mem);

  return new_section->mem;
}
