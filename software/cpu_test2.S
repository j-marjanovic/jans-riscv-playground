.align 4
.globl _start
_start:
	li t0, 1
	li t1, 2
  li t2, 3
	beq t0, t1, set_to_3
  bne t1, t2, set_to_2
set_to_1:
  li a0, 1
  j exit
set_to_2:
  li a0, 2
  j exit
set_to_3:
  li a0, 3
	li t0, 0x2000
	sw a0, 0x0(t0)

// this will stop the ERVP CPUs
exit:
	li      a7,93
	ecall
