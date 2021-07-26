OUTPUT_ARCH("riscv")
ENTRY(_start)

SECTIONS
{
  . = 0x200;
  .text.init : { *(.text.init) }
  . = ALIGN(0x10);
  .tohost : { *(.tohost) }
  . = ALIGN(0x10);
  .text : { *(.text) }
  . = ALIGN(0x10);
  .data : { *(.data) }
  .data.string : { *(.data.string)}
  .bss : { *(.bss) }
  .sdata          :
  {
    __global_pointer$ = . + 0x800;
    *(.srodata.cst16) *(.srodata.cst8) *(.srodata.cst4) *(.srodata.cst2) *(.srodata .srodata.*)
    *(.sdata .sdata.* .gnu.linkonce.s.*)
  }
  _edata = .; PROVIDE (edata = .);
  . = .;
  __bss_start = .;
  _end = .;
}
