# Jan's RISC-V Playground

Welcome to my playground for the RISC-V!

## Notes

### 2021-07-20

#### GDB + QEMU

https://qemu-project.gitlab.io/qemu/system/gdb.html

##### QEMU

```console
$ qemu-riscv32 -g 1234   ./software/hello_world
```

##### GDB

```console
$ riscv32-unknown-elf-gdb ./software/hello_world
GNU gdb (GDB) 10.1
Copyright (C) 2020 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
Type "show copying" and "show warranty" for details.
This GDB was configured as "--host=x86_64-pc-linux-gnu --target=riscv32-unknown-elf".
Type "show configuration" for configuration details.
For bug reporting instructions, please see:
<https://www.gnu.org/software/gdb/bugs/>.
Find the GDB manual and other documentation resources online at:
    <http://www.gnu.org/software/gdb/documentation/>.

For help, type "help".
Type "apropos word" to search for commands related to "word"...
Reading symbols from ./software/hello_world...
(gdb) target remote localhost:1234
Remote debugging using localhost:1234
0x0001008c in _start ()
(gdb) info registers
ra             0x0	0x0
sp             0x407ffff0	0x407ffff0
gp             0x0	0x0
tp             0x0	0x0
t0             0x0	0
t1             0x0	0
t2             0x0	0
fp             0x0	0x0
s1             0x0	0
<...>
```

Important commands:

* `target remote localhost:1234`
* `layout asm`
* `layout regs`
* `stepi`

### 2021-07-19

[CppCon 2018: Matt Godbolt “The Bits Between the Bits: How We Get to main()”](https://www.youtube.com/watch?v=dOfucXtyEsU)


### 2021-07-18

#### ELF file

https://blog.k3170makan.com/2018/09/introduction-to-elf-format-elf-header.html

```console
$ riscv64-unknown-elf-readelf -h ../software/hello_world
ELF Header:
  Magic:   7f 45 4c 46 02 01 01 00 00 00 00 00 00 00 00 00 
  Class:                             ELF64
  Data:                              2's complement, little endian
  Version:                           1 (current)
  OS/ABI:                            UNIX - System V
  ABI Version:                       0
  Type:                              EXEC (Executable file)
  Machine:                           RISC-V
  Version:                           0x1
  Entry point address:               0x100c4
  Start of program headers:          64 (bytes into file)
  Start of section headers:          131736 (bytes into file)
  Flags:                             0x5, RVC, double-float ABI
  Size of this header:               64 (bytes)
  Size of program headers:           56 (bytes)
  Number of program headers:         2
  Size of section headers:           64 (bytes)
  Number of section headers:         23
  Section header string table index: 22
```

https://people.redhat.com/mpolacek/src/devconf2012.pdf

https://sourceware.org/git/?p=glibc.git;a=blob;f=elf/elf.h;h=4738dfa28f6549fc11654996a15659dc8007e686;hb=HEAD

https://blog.k3170makan.com/2018/09/introduction-to-elf-file-format-part.html

##### sections

- `.text` - instructions
- `.rodata` - read only data
- `.eh_frame` - exception handling (https://stackoverflow.com/questions/26300819/why-gcc-compiled-c-program-needs-eh-frame-section)
- `.init_array` - functions called at the init (`DT_INIT_ARRAY`)
- `.fini_array` - functions called in final stage
- `.data` - initialized data
- `.sdata` - small data
- `.sbss` - small uninitialized data
- `.bss` - uninitialized data
- `.comment`
- `.riscv.attributes` (https://github.com/riscv/riscv-elf-psabi-doc/blob/master/riscv-elf.md#special-sections)
- `.debug_aranges`
- `.debug_info`
- `.debug_abbrev`
- `.debug_line`
- `.debug_frame`
- `.debug_str`
- `.debug_loc`
- `.debug_ranges`
- `.symtab`
- `.strtab`
- `.shstrtab`

```console
$ riscv64-unknown-elf-objdump --disassemble-all ../software/hello_world | head -n 30

../software/hello_world:     file format elf64-littleriscv


Disassembly of section .text:

00000000000100b0 <register_fini>:
   100b0:       00000793                li      a5,0
   100b4:       c799                    beqz    a5,100c2 <register_fini+0x12>
   100b6:       00002517                auipc   a0,0x2
   100ba:       62250513                addi    a0,a0,1570 # 126d8 <__libc_fini_array>
   100be:       0d40906f                j       19192 <atexit>
   100c2:       8082                    ret

00000000000100c4 <_start>:
   100c4:       0000f197                auipc   gp,0xf
   100c8:       05c18193                addi    gp,gp,92 # 1f120 <__global_pointer$>
   100cc:       76818513                addi    a0,gp,1896 # 1f888 <_PathLocale>
   100d0:       00010617                auipc   a2,0x10
   100d4:       85060613                addi    a2,a2,-1968 # 1f920 <__BSS_END__>
   100d8:       8e09                    sub     a2,a2,a0
   100da:       4581                    li      a1,0
   100dc:       170000ef                jal     ra,1024c <memset>
   100e0:       00009517                auipc   a0,0x9
   100e4:       0b250513                addi    a0,a0,178 # 19192 <atexit>
   100e8:       c519                    beqz    a0,100f6 <_start+0x32>
   100ea:       00002517                auipc   a0,0x2
   100ee:       5ee50513                addi    a0,a0,1518 # 126d8 <__libc_fini_array>
   100f2:       0a0090ef                jal     ra,19192 <atexit>
   100f6:       0ec000ef                jal     ra,101e2 <__libc_init_array>
```

#### Register names

https://riscv.org/wp-content/uploads/2015/01/riscv-calling.pdf

#### GCC options

https://gcc.gnu.org/onlinedocs/gcc/RISC-V-Options.html

https://www.sifive.com/blog/all-aboard-part-1-compiler-args

#### GCC

https://github.com/riscv/riscv-gnu-toolchain.git

https://stackoverflow.com/a/32538388

https://github.com/riscv/riscv-gnu-toolchain/issues/87

https://github.com/cliffordwolf/picorv32#building-a-pure-rv32i-toolchain
