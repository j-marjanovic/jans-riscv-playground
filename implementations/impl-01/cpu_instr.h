
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "cpu_types.h"
#include "instr_types.h"

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

const char *branch_instr_to_str(enum BRANCH_INSTR_FUNC funct3) {
  switch (funct3) {
  case BEQ:
    return "BEQ";
  case BNE:
    return "BNE";
  case BLT:
    return "BLT";
  case BGE:
    return "BGE";
  case BLTU:
    return "BLTU";
  case BGEU:
    return "BGEU";
  default:
    abort();
  }
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

// opcode: 0b0010011
enum ALU_IMM_INSTR_FUNC {
  ADDI = 0b000,
  SLTI = 0b010,
  SLTIU = 0b011,
  XORI = 0b100,
  ORI = 0b110,
  ANDI = 0b111,
  SLLI = 0b001,
  SRxI = 0b101,
};

const char *alu_imm_instr_to_str(enum ALU_IMM_INSTR_FUNC funct3) {
  switch (funct3) {
  case ADDI:
    return "ADDI";
  case SLTI:
    return "SLTI";
  case SLTIU:
    return "SLTIU";
  case XORI:
    return "XORI";
  case ORI:
    return "ORI";
  case ANDI:
    return "ANDI";
  case SLLI:
    return "SLLI";
  case SRxI:
    return "SRxI";
  default:
    abort();
  }
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

// opcode: 0b0110011
enum ALU_INSTR_FUNC {
  ADD_SUB = 0b000,
  SLL = 0b001,
  SLT = 0b010,
  SLTU = 0b011,
  XOR = 0b100,
  SRL_SRA = 0b101,
  OR = 0b110,
  AND = 0b111,
};

const char *alu_instr_to_str(enum ALU_IMM_INSTR_FUNC funct3, uint8_t funct7) {

  switch (funct3) {
  case ADD_SUB:
    if (funct7 == 0b0000000) {
      return "ADD";
    } else if (funct7 == 0b0100000) {
      return "SUB";
    } else {
      printf("ERROR: unrecognized ALU instruction");
      abort();
    }
  case SLL:
    return "SLL";
  case SLT:
    return "SLT";
  case SLTU:
    return "SLTU";
  case XOR:
    return "XOR";
  case SRL_SRA:
    if (funct7 == 0b0000000) {
      return "SRL";
    } else if (funct7 == 0b0100000) {
      return "SRA";
    } else {
      printf("ERROR: unrecognized ALU instruction");
      abort();
    }
  case OR:
    return "OR";
  case AND:
    return "AND";

  default:
    abort();
  }
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
    [AUIPC] = op_auipc,      [JAL] = op_jal,    [_BRANCH] = op_branch,
    [_ALU_IMM] = op_alu_imm, [_ALU_R] = op_alu,
};
