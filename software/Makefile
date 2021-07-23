
CC=riscv32-unknown-elf-gcc
OBJDUMP=riscv32-unknown-elf-objdump

all: hello_world cpu_test1 cpu_test2

hello_world: hello_world.c
	$(CC) -o $@ -Wall -Wextra -pedantic  $<
 
cpu_test1: cpu_test1.S
	$(CC) -nostdlib -nostartfiles -Tlink.ld -o cpu_test1 cpu_test1.S
	$(OBJDUMP) --disassemble-all $@ > cpu_test1.txt

cpu_test2: cpu_test2.S
	$(CC) -nostdlib -nostartfiles -Tlink.ld -o cpu_test2 cpu_test2.S
	$(OBJDUMP) --disassemble-all $@ > cpu_test2.txt

.PHONY: clean

clean:
	rm -rf hello_world
	rm -rf cpu_test1 cpu_test1.txt
	rm -rf cpu_test2 cpu_test2.txt
