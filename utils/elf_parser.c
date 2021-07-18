
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

const uint32_t ELF_HEADER_MAG = 0x464c457F;
const uint8_t ELF_HEADER_MACHINE_RISCV = 0xf3;

struct __attribute__((packed)) elf_header {
    uint32_t e_ident_mag;
    uint8_t e_ident_class;
    uint8_t e_ident_data;
    uint8_t e_ident_version;
    uint8_t e_ident_abi;
    uint8_t e_ident_abiversion;
    uint8_t e_ident_pad[7];
    uint16_t e_type;
    uint16_t e_machine;
    uint32_t e_version;
    uint64_t e_entry;
    uint64_t e_phoff;
    uint64_t e_shoff;
    uint32_t e_flags;
    uint16_t e_ehsize;
    uint16_t e_phentsize;
    uint16_t e_phnum;
    uint16_t e_shentsize;
    uint16_t e_shnum;
    uint16_t e_shstrndx;
};

struct ph_entry {
    uint32_t p_type;
    uint32_t p_flags;
    uint64_t p_offset;
    uint64_t p_vaddr;
    uint64_t p_paddr;
    uint64_t p_filesz;
    uint64_t p_memsz;
    uint64_t p_align;
};

struct sh_entry {
    uint32_t sh_name;
    uint32_t sh_type;
    uint64_t sh_flags;
    uint64_t sh_addr;
    uint64_t sh_offset;
    uint64_t sh_size;
    uint32_t sh_link;
    uint32_t sh_info;
    uint64_t sh_addralign;
    uint64_t sh_entsize;
};

void print_header(struct elf_header* hdr)
{
    printf("ELF header\n");

    printf("  magic number = %x ", hdr->e_ident_mag);
    printf("%s", hdr->e_ident_mag == ELF_HEADER_MAG ? "(OK)" : "(ERROR)");
    printf("\n");

    // clang-format off
    printf("  class = %s\n",
        hdr->e_ident_class == 1 ? "32b" :
        hdr->e_ident_class == 2 ? "64b" :
        "ERROR: unrecognized"
    );
    // clang-format ong

    printf("  ABI = 0x%x\n",
        hdr->e_ident_abi);

    printf("  version = %d\n",
        hdr->e_version);

    printf("  machine = 0x%x (%s)\n",
        hdr->e_machine,
        hdr->e_machine == ELF_HEADER_MACHINE_RISCV ? "RISC-V" : "unsupported");

    printf("  entry = 0x%lx\n", hdr->e_entry);

    printf("\n");
    printf("  prog header offs = 0x%lx\n", hdr->e_phoff);
    printf("  ph entry size = %d\n", hdr->e_phentsize);
    printf("  ph num = %d\n", hdr->e_phnum);

    printf("\n");
    printf("  sect header offs = 0x%lx\n", hdr->e_shoff);
    printf("  sh entry size = %d\n", hdr->e_shentsize);
    printf("  sh num = %d\n", hdr->e_shnum);
    printf("  sh str index = %d\n", hdr->e_shstrndx);
}

void print_prog_header(struct ph_entry* entry)
{
    printf("    type   = 0x%x [", entry->p_type);
    switch (entry->p_type) {
    case 1:
        printf("LOAD");
        break;
    case 2:
        printf("DYNAMIC");
        break;
    case 3:
        printf("INTERP");
        break;
    case 4:
        printf("NOTE");
        break;
    case 5:
        printf("SHLIB");
        break;
    case 6:
        printf("PHDR");
        break;
    case 7:
        printf("TLS");
        break;
    case 0x6474e553:
        printf("GNU_PROPERTY");
        break;
    case 0x6474e550:
        printf("GNU_EH_FRAME");
        break;
    case 0x6474e551:
        printf("GNU_STACK");
        break;
    case 0x6474e552:
        printf("GNU_RELRO");
        break;
    }
    printf("]\n");

    printf("    flags  = 0x%x (", entry->p_flags);
    printf("%c", (entry->p_flags & (1 << 2)) ? 'R' : '-');
    printf("%c", (entry->p_flags & (1 << 1)) ? 'W' : '-');
    printf("%c", (entry->p_flags & (1 << 0)) ? 'X' : '-');
    printf(")\n");

    printf("    offset = 0x%lx\n", entry->p_offset);
    printf("    vaddr  = 0x%lx\n", entry->p_vaddr);
    printf("    paddr  = 0x%lx\n", entry->p_paddr);
    printf("    filesz = 0x%lx\n", entry->p_filesz);
    printf("    memsz  = 0x%lx\n", entry->p_memsz);
    printf("    align  = 0x%lx\n", entry->p_align);
}

void print_prog_headers(int fd, struct elf_header* hdr)
{
    printf("Program headers\n");

    lseek(fd, hdr->e_phoff, SEEK_SET);

    struct ph_entry entry;
    for (int i = 0; i < hdr->e_phnum; i++) {
        read(fd, &entry, sizeof(entry));
        printf("  Prog header %d\n", i);
        print_prog_header(&entry);
    }
}

// caller is responsible for free-ing the returned buffer
char* get_sh_name_buffer(int fd, struct elf_header* hdr) {
    off_t cur_pos = lseek(fd, 0, SEEK_CUR);

    struct sh_entry entry;
    pread(fd, &entry, sizeof(entry), hdr->e_shoff + hdr->e_shentsize*hdr->e_shstrndx);

    off_t str_pos = entry.sh_offset;
    size_t str_len = entry.sh_size;
    char* str = malloc(str_len);
    assert(str != NULL);

    pread(fd, str, str_len, str_pos);

    lseek(fd, cur_pos, SEEK_SET);

    return str;
}

void print_section_header(struct sh_entry *entry, const char* section_names) {
    printf("    name   = %s (%d)\n", section_names + entry->sh_name, entry->sh_name);

    printf("    type   = 0x%x [", entry->sh_type);
    switch (entry->sh_type) {
        case 0    : printf("NULL"); break;
        case 1    : printf("PROGBITS"); break;
        case 2    : printf("SYMTAB"); break;
        case 3    : printf("STRTAB"); break;
        case 4    : printf("RELA"); break;
        case 5    : printf("HASH"); break;
        case 6    : printf("DYNAMIC"); break;
        case 7    : printf("NOTE"); break;
        case 8    : printf("NOBITS"); break;
        case 9    : printf("REL"); break;
        case 10   : printf("SHLIB"); break;
        case 11   : printf("DYNSYM"); break;
        case 14   : printf("INIT_ARRAY"); break;
        case 15   : printf("FINI_ARRAY"); break;
        case 16   : printf("PREINIT_ARRAY"); break;
        case 17   : printf("GROUP"); break;
        case 18   : printf("SYMTAB_SHNDX"); break;
        case 19   : printf("NUM"); break;
        default:
            if ((entry->sh_type >= 0x70000000) && (entry->sh_type <= 0x7fffffff)) {
                printf("PROC"); break;
            }
    }
    printf("]\n");

    printf("    flags  = 0x%lx [", entry->sh_flags);
    printf("%s", entry->sh_flags & (1 << 0)  ? "WRITE, " : "");
    printf("%s", entry->sh_flags & (1 << 1)  ? "ALLOC, " : "");
    printf("%s", entry->sh_flags & (1 << 2)  ? "EXECINSTR, " : "");
    printf("%s", entry->sh_flags & (1 << 4)  ? "MERGE, " : "");
    printf("%s", entry->sh_flags & (1 << 5)  ? "STRINGS, " : "");
    printf("%s", entry->sh_flags & (1 << 6)  ? "INFO_LINK, " : "");
    printf("%s", entry->sh_flags & (1 << 7)  ? "LINK_ORDER, " : "");
    printf("%s", entry->sh_flags & (1 << 8)  ? "OS_NONCONFORMING, " : "");
    printf("%s", entry->sh_flags & (1 << 9)  ? "GROUP, " : "");
    printf("%s", entry->sh_flags & (1 << 10) ? "TLS, " : "");
    printf("%s", entry->sh_flags & (1 << 11) ? "COMPRESSED, " : "");
    if (entry->sh_flags) {
        printf("\b\b]\n");
    } else {
        printf("]\n");
    }

    printf("    addr   = 0x%0lx\n", entry->sh_addr);
    printf("    offset = 0x%0lx\n", entry->sh_offset);
    printf("    size   = 0x%0lx\n", entry->sh_size);
    printf("    link   = 0x%0x\n", entry->sh_link);
    printf("    info   = 0x%0x\n", entry->sh_info);
    printf("    aalign = 0x%0lx\n", entry->sh_addralign);
    printf("    ent sz = 0x%0lx\n", entry->sh_entsize);
}

void print_section_headers(int fd, struct elf_header* hdr)
{
    printf("Section headers\n");

    lseek(fd, hdr->e_shoff, SEEK_SET);

    char* section_names = get_sh_name_buffer(fd, hdr);

    struct sh_entry entry;
    for (int i = 0; i < hdr->e_shnum; i++) {
        read(fd, &entry, sizeof(entry));
        printf("  Section header %d\n", i);
        print_section_header(&entry, section_names);
    }

    free(section_names);
}

int main(int argc, char* argv[])
{
    if (argc != 2) {
        printf("Usage: %s ELF_FILE\n", argv[0]);
        return EXIT_FAILURE;
    }

    int fd = open(argv[1], O_RDWR);
    if (fd < 0) {
        printf("Error opening file\n");
        return EXIT_FAILURE;
    }

    struct elf_header hdr;
    read(fd, &hdr, sizeof(hdr));

    print_header(&hdr);
    print_prog_headers(fd, &hdr);
    print_section_headers(fd, &hdr);
}
