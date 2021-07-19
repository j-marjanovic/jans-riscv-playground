// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

enum RV32I_INSTR {
  AUIPC = 0b0010111,
  JAL = 0b1101111,
  JALR = 0b1100111,
  // groups
  _BRANCH = 0b1100011,  // BEQ, BNE, BLT, BGE, BLTU, BGEU
  _LOAD = 0b0000011,    // LB, LH, LW, LBU, LHU
  _STORE = 0b0100011,   // SB, SH, SW
  _ALU_IMM = 0b0010011, // ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI
  _ALU_R = 0b0110011,   // ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND
};

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