
CC=riscv32-unknown-elf-gcc
OBJDUMP=riscv32-unknown-elf-objdump

all: hello_world cpu_test1 cpu_test1.txt

hello_world: hello_world.c
	$(CC) -o $@ -Wall -Wextra -pedantic  $<
 
cpu_test1: cpu_test1.S
	$(CC) -nostdlib -nostartfiles -Tlink.ld -o cpu_test1 cpu_test1.S

cpu_test1.txt: cpu_test1
	$(OBJDUMP) --disassemble-all $< > $@

.PHONY: clean

clean:
	rm -rf hello_world
	rm -rf cpu_test1
	rm -rf cpu_test1.txt
