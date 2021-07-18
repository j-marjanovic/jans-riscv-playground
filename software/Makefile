
CC=riscv32-unknown-elf-gcc

hello_world: hello_world.c
	$(CC) -o $@ -Wall -Wextra -pedantic  $<
 
.PHONY: clean

clean:
	rm -rf hello_world
