
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

typedef struct __attribute__((packed)) {
  uint8_t opcode : 7;
  uint8_t rd : 5;
  uint8_t funct3 : 3;
  uint8_t rs1 : 5;
  uint8_t rs2 : 5;
  uint8_t funct7 : 7;
} instr_Rtype;

typedef struct __attribute__((packed)) {
  uint8_t opcode : 7;
  uint8_t rd : 5;
  uint8_t funct3 : 3;
  uint8_t rs1 : 5;
  uint32_t imm : 12;
} instr_Itype;

typedef struct __attribute__((packed)) {
  uint8_t opcode : 7;
  uint32_t imm11 : 1;
  uint32_t imm4_1 : 4;
  uint8_t funct3 : 3;
  uint8_t rs1 : 5;
  uint32_t imm10_5 : 4;
  uint32_t imm12 : 4;
} instr_Btype;

uint32_t instr_btype_imm(instr_Btype *instr) {
  uint32_t tmp = 0;

  tmp |= instr->imm4_1 << 1;
  tmp |= instr->imm11 << 11;
  tmp |= instr->imm10_5 << 5;
  tmp |= instr->imm12 << 12;

  return tmp;
}

typedef struct __attribute__((packed)) {
  uint8_t opcode : 7;
  uint8_t rd : 5;
  uint32_t imm : 20;
} instr_Utype;

typedef struct __attribute__((packed)) {
  uint8_t opcode : 7;
  uint8_t rd : 5;
  uint32_t imm19_12 : 8;
  uint32_t imm11 : 1;
  uint32_t imm10_1 : 10;
  uint32_t imm20 : 1;
} instr_Jtype;

uint32_t instr_jtype_imm(instr_Jtype *instr) {
  uint32_t tmp = 0;

  tmp |= instr->imm10_1 << 1;
  tmp |= instr->imm11 << 11;
  tmp |= instr->imm19_12 << 12;
  tmp |= instr->imm20 << 20;

  return tmp;
}

_Static_assert(sizeof(instr_Rtype) == 4, "size of Rtype");
_Static_assert(sizeof(instr_Itype) == 4, "size of Itype");
_Static_assert(sizeof(instr_Btype) == 4, "size of Btype");
_Static_assert(sizeof(instr_Utype) == 4, "size of Utype");
_Static_assert(sizeof(instr_Jtype) == 4, "size of Jtype");
