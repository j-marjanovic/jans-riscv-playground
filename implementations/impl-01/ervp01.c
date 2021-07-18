
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#include <assert.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

#include "array_mem.h"
#include "cpu.h"

int main() {
  int fd = open("../../software/hello_world", O_RDWR);
  if (fd < 0) {
    perror("open()");
    return EXIT_FAILURE;
  }

  off_t end = lseek(fd, 0, SEEK_END);
  lseek(fd, 0x8c, SEEK_SET);
  size_t instr_size = end - 0x8c;

  uint8_t *instr = malloc(instr_size);
  assert(instr);

  int bytes_read = read(fd, instr, instr_size);
  printf("bytes read = %d\n", bytes_read);

  t_array_mem mem_impl;
  mem_impl.size = instr_size;
  mem_impl.mem = instr;

  t_cpu cpu;
  t_mem_ops mem_ops = {.read = array_mem_read, .write = array_mem_write};

  create_cpu(&cpu, (void *)&mem_impl, &mem_ops);

  for (int i = 0; i < 12; i++) {
    cpu_exec_instr(&cpu);
  }

  return 0;
}
