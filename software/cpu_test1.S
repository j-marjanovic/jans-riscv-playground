.align 4
.globl _start
_start:
	li a0, 1
	li a1, 2
	add a2, a0, a1
	addi a2, a2, 10
	li t0, 0x2000
	sw a2, 0x10(t0)
