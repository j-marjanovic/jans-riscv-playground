
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "elf_types.h"

struct symtab_entry {
  struct sym_entry entry;
  char name[32];
  struct symtab_entry *next;
};

// caller is responsible for free-ing the returned buffer
struct sh_entry *_get_sh_entry_by_type(int fd, struct elf_header *hdr,
                                       uint32_t sh_type) {
  lseek(fd, hdr->e_shoff, SEEK_SET);

  struct sh_entry *entry = malloc(sizeof(struct sh_entry));
  for (int i = 0; i < hdr->e_shnum; i++) {
    read(fd, entry, sizeof(struct sh_entry));
    if (entry->sh_type == sh_type) {
      return entry;
    }
  }

  return NULL;
}

// caller is responsible for free-ing the returned buffer
char *_get_sh_name_buffer(int fd, struct sh_entry *strtab) {
  off_t str_pos = strtab->sh_offset;
  size_t str_len = strtab->sh_size;
  char *str = malloc(str_len);
  assert(str != NULL);

  pread(fd, str, str_len, str_pos);

  return str;
}

struct symtab_entry *_create_symtab_list(int fd, struct sh_entry *hdr_entry,
                                         char *strtab_buffer) {

  struct symtab_entry *symtab_first = NULL;
  struct symtab_entry *symtab_prev = NULL;

  struct sym_entry *sym_entries = malloc(hdr_entry->sh_size);
  assert(sym_entries);

  pread(fd, sym_entries, hdr_entry->sh_size, hdr_entry->sh_offset);

  for (unsigned int i = 0; i < hdr_entry->sh_size / hdr_entry->sh_entsize;
       i++) {
    if ((sym_entries[i].st_info & 0xF) == STT_FUNC) {
      struct symtab_entry *symtab_cur = malloc(sizeof(struct symtab_entry));
      memcpy(&symtab_cur->entry, &sym_entries[i], sizeof(struct sym_entry));
      strncpy(symtab_cur->name, strtab_buffer + sym_entries[i].st_name, 31);
      symtab_cur->next = NULL;

      // linked-list management
      if (!symtab_first) {
        symtab_first = symtab_cur;
      }
      if (symtab_prev) {
        symtab_prev->next = symtab_cur;
      }
      symtab_prev = symtab_cur;
    }
  }

  return symtab_first;
}

struct symtab_entry *load_elf_symtab(const char *filename) {

  int fd = open(filename, O_RDWR);
  if (fd < 0) {
    perror("open()");
    abort();
  }

  struct elf_header hdr;
  read(fd, &hdr, sizeof(hdr));

  struct sh_entry *symtab = _get_sh_entry_by_type(fd, &hdr, SH_TYPE_SYMTAB);
  struct sh_entry *strtab = _get_sh_entry_by_type(fd, &hdr, SH_TYPE_STRTAB);
  char *name_buffer = _get_sh_name_buffer(fd, strtab);

  return _create_symtab_list(fd, symtab, name_buffer);
}

char *symtab_get_name(void *symtab_first, uint32_t addr, uint32_t *offs) {
  struct symtab_entry *symtab_cur = (struct symtab_entry *)symtab_first;
  while (symtab_cur) {
    if ((addr >= symtab_cur->entry.st_value) &&
        (addr < symtab_cur->entry.st_value + symtab_cur->entry.st_size)) {
      if (offs) {
        *offs = addr - symtab_cur->entry.st_value;
      }
      return symtab_cur->name;
    }
    symtab_cur = symtab_cur->next;
  }
  return "";
}
