
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "cpu_types.h"
#include "instr_defs.h"
#include "instr_types.h"

int32_t sign_extend_12bit(uint32_t imm) {
  const uint32_t mask_const = ((1 << 20) - 1) << 12;
  uint32_t bit = (imm >> 11) & 1;

  uint32_t mask = mask_const * bit;
  ;

  return imm | mask;
}

uint32_t sign_extend_12bit_unsigned(uint32_t imm) {
  return (uint32_t)sign_extend_12bit(imm);
}

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
  cpu->regs.pc += imm; // TODO: sign extend
}

void op_jalr(t_cpu *cpu, uint32_t instr_raw) {
  instr_Itype instr;
  memcpy(&instr, &instr_raw, 4);

  printf("[op]        JALR, rd = %d, funct3 = %d, rs1 = %d, imm = 0x%x\n",
         instr.rd, instr.funct3, instr.rs1, instr.imm);

  cpu->regs.x[1] = cpu->regs.pc + 4;
  cpu->regs.pc += cpu->regs.x[instr.rs1] + sign_extend_12bit(instr.imm);
}

void op_branch(t_cpu *cpu, uint32_t instr_raw) {
  instr_Btype instr;
  memcpy(&instr, &instr_raw, 4);

  uint32_t imm = instr_btype_imm(&instr);

  printf("[op]        group BRANCH - %s, funct3 = %d, rs1 = %d, rs2 = %d, imm "
         "= 0x%x\n",
         branch_instr_to_str(instr.funct3), instr.funct3, instr.rs1, instr.rs2,
         imm);

  switch (instr.funct3) {
  case BNE:
    if (cpu->regs.x[instr.rs1] != cpu->regs.x[instr.rs2]) {
      printf("[branch]    branch taken\n");
      cpu->regs.pc += imm;
    } else {
      printf("[branch]    branch skipped\n");
      cpu->regs.pc += 4;
    }
    break;
  case BGEU:
    if (cpu->regs.x[instr.rs1] >= cpu->regs.x[instr.rs2]) {
      printf("[branch]    branch taken\n");
      cpu->regs.pc += imm;
    } else {
      printf("[branch]    branch skipped\n");
      cpu->regs.pc += 4;
    }
    break;
  default:
    // TODO: implement
    abort();
  }
}

void op_store(t_cpu *cpu, uint32_t instr_raw) {
  instr_Stype instr;
  memcpy(&instr, &instr_raw, 4);

  uint32_t imm = instr_stype_imm(&instr);
  printf("[op]        group STORE, funct3 = %d, rs1 = %d, rs2 = %d, imm "
         "= 0x%x\n",
         instr.funct3, instr.rs1, instr.rs2, imm);

  // funct3: 0 = bytes, 1 - halfword, 2 - word
  // TODO: implement the store

  cpu->regs.pc += 4;
}

void op_alu_imm(t_cpu *cpu, uint32_t instr_raw) {
  instr_Itype instr;
  memcpy(&instr, &instr_raw, 4);

  printf("[op]        group ALU IMM - %s, rd = %d, funct3 = %d, rs1 = %d, imm "
         "= 0x%x\n",
         alu_imm_instr_to_str(instr.funct3), instr.rd, instr.funct3, instr.rs1,
         instr.imm);

  switch (instr.funct3) {
  case ADDI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] + instr.imm; // TODO: sign extend
    break;
  case SLTI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] > instr.imm; // TODO: sign extend
    break;
  case SLTIU:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] > instr.imm; // TODO: sign extend, unsigned
    break;
  case XORI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] ^ instr.imm; // TODO: sign extend
    break;
  case ORI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] | instr.imm; // TODO: sign extend
    break;
  case ANDI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] & instr.imm; // TODO: sign extend
    break;
  case SLLI:
    cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1]
                            << instr.imm; // TODO: check details
    break;
  case SRxI:
    abort(); // TODO
  }

  cpu->regs.pc += 4;
}

void op_alu(t_cpu *cpu, uint32_t instr_raw) {
  instr_Rtype instr;
  memcpy(&instr, &instr_raw, 4);

  printf(
      "[op]        group ALU - %s, rd = %d, funct3 = %d, rs1 = %d, rs2 = %d, "
      "funct7 = %d\n",
      alu_instr_to_str(instr.funct3, instr.funct7), instr.rd, instr.funct3,
      instr.rs1, instr.rs2, instr.funct7);

  switch (instr.funct3) {
  case ADD_SUB:
    if (instr.funct7 == 0b0100000) {
      cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] - cpu->regs.x[instr.rs2];
    } else {
      cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] + cpu->regs.x[instr.rs2];
    }
    break;
  case SLL:
  case SLT:
  case SLTU:
  case XOR:
  case SRL_SRA:
  case OR:
  case AND:
    printf("ALU instr not yet implemented\n"); // TODO: implement
    abort(); // unsupported instr // TODO: move to default
  }

  cpu->regs.pc += 4;
}

t_cpu_op cpu_ops[] = {
    [AUIPC] = op_auipc,    [JAL] = op_jal,      [JALR] = op_jalr,
    [_BRANCH] = op_branch, [_STORE] = op_store, [_ALU_IMM] = op_alu_imm,
    [_ALU_R] = op_alu,
};
