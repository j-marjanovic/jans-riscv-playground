
CC=riscv32-unknown-elf-gcc
OBJDUMP=riscv32-unknown-elf-objdump

all: hello_world cpu_test1 cpu_test2 pl_test1

hello_world: hello_world.c
	$(CC) -o $@ -Wall -Wextra -pedantic -Tlink2.ld -Os $<
 
cpu_test1: cpu_test1.S
	$(CC) -nostdlib -nostartfiles -Tlink.ld -o $@ $<
	$(OBJDUMP) --disassemble-all $@ > cpu_test1.txt

cpu_test2: cpu_test2.S
	$(CC) -nostdlib -nostartfiles -Tlink.ld -o $@ $<
	$(OBJDUMP) --disassemble-all $@ > cpu_test2.txt

pl_test1: pl_test1.S
	$(CC) -nostdlib -nostartfiles -Tlink.ld -o $@ $<
	$(OBJDUMP) --disassemble-all $@ > pl_test1.txt

.PHONY: clean

clean:
	rm -rf hello_world
	rm -rf cpu_test1 cpu_test1.txt
	rm -rf cpu_test2 cpu_test2.txt
