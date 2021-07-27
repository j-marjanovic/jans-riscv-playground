.align 4
.global _start
_start:
  li a0, 1
  li a0, 2
  nop
  nop
  nop
  nop
  nop
  nop
  bne a0, a1, exit
  li a0, 3
  li a0, 4
  li a0, 5
  li a0, 6
  li a0, 7
  li a0, 8
  li a0, 9
  li a0, 0xa

// this will stop the ERVP CPUs
exit:
  nop
  nop
  nop
  nop
  nop
  nop
  li a7,93
  ecall
