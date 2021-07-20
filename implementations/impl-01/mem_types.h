
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

struct __attribute__((packed)) elf_header {
  uint32_t e_ident_mag;
  uint8_t e_ident_class;
  uint8_t e_ident_data;
  uint8_t e_ident_version;
  uint8_t e_ident_abi;
  uint8_t e_ident_abiversion;
  uint8_t e_ident_pad[7];
  uint16_t e_type;
  uint16_t e_machine;
  uint32_t e_version;
  uint32_t e_entry;
  uint32_t e_phoff;
  uint32_t e_shoff;
  uint32_t e_flags;
  uint16_t e_ehsize;
  uint16_t e_phentsize;
  uint16_t e_phnum;
  uint16_t e_shentsize;
  uint16_t e_shnum;
  uint16_t e_shstrndx;
};

struct __attribute__((packed)) ph_entry {
  uint32_t p_type;
  uint32_t p_offset;
  uint32_t p_vaddr;
  uint32_t p_paddr;
  uint32_t p_filesz;
  uint32_t p_memsz;
  uint32_t p_flags;
  uint32_t p_align;
};

struct mem_section {
  struct ph_entry ph;
  void *mem;
  struct mem_section *next;
};
