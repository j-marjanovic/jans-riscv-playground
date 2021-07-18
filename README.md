# Jan's RISC-V Playground

Welcome to my playground for the RISC-V!

## Notes

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
