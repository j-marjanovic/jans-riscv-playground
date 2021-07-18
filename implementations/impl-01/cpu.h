
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include <stdint.h>
#include <stdio.h>

#include "instr_types.h"

typedef struct {
  uint32_t x[32];
  uint32_t pc;
} t_regs;

typedef struct {
  void (*write)(void *, uint32_t, uint32_t);
  uint32_t (*read)(void *, uint32_t);
} t_mem_ops;

typedef struct {
  t_regs regs;
  t_mem_ops *mem_ops;
  void *mem_impl;
} t_cpu;

void create_cpu(t_cpu *cpu, void *mem_impl, t_mem_ops *mem_ops) {
  for (unsigned int i = 0; i < 32; i++) {
    cpu->regs.x[i] = 0;
  }
  cpu->regs.pc = 0;

  cpu->mem_ops = mem_ops;
  cpu->mem_impl = mem_impl;
}

enum RV32I_INSTR {
  AUIPC = 0b0010111,
  JAL = 0b1101111,
  // groups
  _BRANCH = 0b1100011,  // BEQ, BNE, BLT, BGE, BLTU, BGEU
  _ALU_IMM = 0b0010011, // ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI
  _ALU_R = 0b0110011,   // ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND
};

typedef void (*t_cpu_op)(t_cpu *cpu, uint32_t instr);

void op_auipc(t_cpu *cpu, uint32_t instr_raw) {
  instr_Utype instr;
  memcpy(&instr, &instr_raw, 4);
  printf("[op]        AUIPC, rd = %d, imm = 0x%x\n", instr.rd, instr.imm);

  assert(instr.rd != 0);

  cpu->regs.x[instr.rd] += instr.imm << 12;
  cpu->regs.pc += 4;
}

void op_jal(t_cpu *cpu, uint32_t instr_raw) {
  instr_Jtype instr;
  memcpy(&instr, &instr_raw, 4);

  uint32_t imm = instr_jtype_imm(&instr);
  printf("[op]        JAL, rd = %d, imm = 0x%x\n", instr.rd, imm);

  cpu->regs.x[1] = cpu->regs.pc + 4;
  cpu->regs.pc += imm;
}

void op_branch(t_cpu *cpu, uint32_t instr_raw) {
  instr_Btype instr;
  memcpy(&instr, &instr_raw, 4);

  uint32_t imm = instr_btype_imm(&instr);

  printf("[op]        group BRANCH, funct3 = %d, rs1 = %d, imm = 0x%x\n",
         instr.funct3, instr.rs1, imm);

  // TODO - finish instruction
  abort();
}

void op_alu_imm(t_cpu *cpu, uint32_t instr_raw) {
  instr_Itype instr;
  memcpy(&instr, &instr_raw, 4);

  printf(
      "[op]        group ALU IMM, rd = %d, funct3 = %d, rs1 = %d, imm = 0x%x\n",
      instr.rd, instr.funct3, instr.rs1, instr.imm);

  switch (instr.funct3) {
  case 0b000:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] + instr.imm; // TODO: sign extend
    break;
  case 0b010:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] > instr.imm; // TODO: sign extend
    break;
  case 0b011:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] > instr.imm; // TODO: sign extend, unsigned
    break;
  case 0b111:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] & instr.imm; // TODO: sign extend
    break;
  case 0b110:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] & instr.imm; // TODO: sign extend
    break;
  case 0b100:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] & instr.imm; // TODO: sign extend
    break;
  case 0b001: // SLLI
    abort();  // TODO
  case 0b101: // SRLI, SRAI
    abort();  // TODO
  }

  cpu->regs.pc += 4;
}

void op_alu(t_cpu *cpu, uint32_t instr_raw) {
  instr_Rtype instr;
  memcpy(&instr, &instr_raw, 4);

  printf("[op]        ALU, rd = %d, funct3 = %d, rs1 = %d, rs2 = %d, funct7 = "
         "%d\n",
         instr.rd, instr.funct3, instr.rs1, instr.rs2, instr.funct7);

  switch (instr.funct3) {
  case 0b000: // ADD, SUB
    if (instr.funct7 == 0b0100000) {
      cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] - cpu->regs.x[instr.rs2];
    } else {
      cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] + cpu->regs.x[instr.rs2];
    }
    break;
  case 0b001: // SLL
  case 0b010: // SLT
  case 0b011: // SLTU
  case 0b100: // XOR
  case 0b101: // SRL, SRA
  case 0b110: // OR
  case 0b111: // AND
    abort();  // unsupported instr
  }

  cpu->regs.pc += 4;
}

t_cpu_op cpu_ops[] = {
    [AUIPC] = op_auipc,      [JAL] = op_jal,    [_BRANCH] = op_branch,
    [_ALU_IMM] = op_alu_imm, [_ALU_R] = op_alu,
};

#define KNRM "\x1B[0m"
#define KGRY "\x1B[38;5;240m"

void cpu_dump_regs(t_cpu *cpu) {
  printf("----------------------------------------------\n");
  for (int i = 0; i < 32; i++) {
    if ((i != 0) && (i % 8 == 0)) {
      printf("\n");
    }

    if (i % 8 == 0) {
      printf(" %2d | ", i);
    }

    if (cpu->regs.x[i]) {
      printf(KNRM);
    } else {
      printf(KGRY);
    }
    printf("%08x ", cpu->regs.x[i]);
    printf(KNRM);
  }

  printf("\n");
  printf(" pc = %08x\n", cpu->regs.pc);
  printf("----------------------------------------------\n");
}

void cpu_exec_instr(t_cpu *cpu) {
  uint32_t instr = cpu->mem_ops->read(cpu->mem_impl, cpu->regs.pc);
  printf("[decoder]   loaded instr = 0x%08x\n", instr);

  uint8_t opcode = instr & 0x7F;
  printf("[decoder]   opcode = 0x%x\n", opcode);

  cpu_ops[opcode](cpu, instr);

  cpu_dump_regs(cpu);
}
