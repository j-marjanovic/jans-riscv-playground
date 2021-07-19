
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "cpu_types.h"

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

// opcode: 0b1100011
enum BRANCH_INSTR_FUNC {
  BEQ = 0b000,
  BNE = 0b001,
  BLT = 0b100,
  BGE = 0b101,
  BLTU = 0b110,
  BGEU = 0b111
};

const char* branch_instr_to_str(enum BRANCH_INSTR_FUNC funct3) {
  switch (funct3) {
    case BEQ: return "BEQ";
    case BNE: return "BNE";
    case BLT: return "BLT";
    case BGE: return "BGE";
    case BLTU: return "BLTU";
    case BGEU: return "BGEU";
    default: abort();
  }
}

void op_branch(t_cpu *cpu, uint32_t instr_raw) {
  instr_Btype instr;
  memcpy(&instr, &instr_raw, 4);

  uint32_t imm = instr_btype_imm(&instr);

  printf("[op]        group BRANCH - %s, funct3 = %d, rs1 = %d, imm = 0x%x\n",
         branch_instr_to_str(instr.funct3), instr.funct3, instr.rs1, imm);


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
