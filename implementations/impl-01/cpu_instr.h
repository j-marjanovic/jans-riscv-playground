
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

#pragma once

#include "cpu_types.h"
#include "instr_defs.h"
#include "instr_types.h"

int32_t sign_extend_8bit(uint32_t data) {
  const uint32_t mask_const = ((1 << 24) - 1) << 8;
  uint32_t bit = (data >> 7) & 1;
  uint32_t mask = mask_const * bit;

  return data | mask;
}

int32_t sign_extend_12bit(uint32_t imm) {
  const uint32_t mask_const = ((1 << 20) - 1) << 12;
  uint32_t bit = (imm >> 11) & 1;
  uint32_t mask = mask_const * bit;

  return imm | mask;
}

int32_t sign_extend_16bit(uint32_t data) {
  const uint32_t mask_const = ((1 << 16) - 1) << 16;
  uint32_t bit = (data >> 15) & 1;
  uint32_t mask = mask_const * bit;

  return data | mask;
}

int32_t sign_extend_21bit(uint32_t imm) {
  const uint32_t mask_const = ((1 << 11) - 1) << 21;
  uint32_t bit = (imm >> 20) & 1;
  uint32_t mask = mask_const * bit;

  return imm | mask;
}

uint32_t sign_extend_12bit_unsigned(uint32_t imm) {
  return (uint32_t)sign_extend_12bit(imm);
}

void op_lui(t_cpu *cpu, uint32_t instr_raw) {
  instr_Utype instr;
  memcpy(&instr, &instr_raw, 4);
  printf("[op]        LUI, rd = %d, imm = 0x%x\n", instr.rd, instr.imm);

  cpu->regs.x[instr.rd] = instr.imm << 12;
  cpu->regs.pc += 4;
}

void op_auipc(t_cpu *cpu, uint32_t instr_raw) {
  instr_Utype instr;
  memcpy(&instr, &instr_raw, 4);
  printf("[op]        AUIPC, rd = %d, imm = 0x%x\n", instr.rd, instr.imm);

  assert(instr.rd != 0);

  cpu->regs.x[instr.rd] = cpu->regs.pc + (instr.imm << 12);
  cpu->regs.pc += 4;
}

void op_jal(t_cpu *cpu, uint32_t instr_raw) {
  instr_Jtype instr;
  memcpy(&instr, &instr_raw, 4);

  uint32_t imm = instr_jtype_imm(&instr);
  printf("[op]        JAL, rd = %d, imm = 0x%x\n", instr.rd, imm);

  cpu->regs.x[1] = cpu->regs.pc + 4;
  cpu->regs.pc += sign_extend_21bit(imm);
}

void op_jalr(t_cpu *cpu, uint32_t instr_raw) {
  instr_Itype instr;
  memcpy(&instr, &instr_raw, 4);

  printf("[op]        JALR, rd = %d, funct3 = %d, rs1 = %d, imm = 0x%x (%d)\n",
         instr.rd, instr.funct3, instr.rs1, instr.imm,
         sign_extend_12bit(instr.imm));

  cpu->regs.x[instr.rd] = cpu->regs.pc + 4;
  cpu->regs.pc = cpu->regs.x[instr.rs1] + sign_extend_12bit(instr.imm);
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
  case BGE:
    if ((int32_t)cpu->regs.x[instr.rs1] >= (int32_t)cpu->regs.x[instr.rs2]) {
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
  case BEQ:
    if (cpu->regs.x[instr.rs1] == cpu->regs.x[instr.rs2]) {
      printf("[branch]    branch taken\n");
      cpu->regs.pc += imm;
    } else {
      printf("[branch]    branch skipped\n");
      cpu->regs.pc += 4;
    }
    break;
  case BLT:
    if ((int32_t)cpu->regs.x[instr.rs1] < (int32_t)cpu->regs.x[instr.rs2]) {
      printf("[branch]    branch taken\n");
      cpu->regs.pc += imm;
    } else {
      printf("[branch]    branch skipped\n");
      cpu->regs.pc += 4;
    }
    break;
  case BLTU:
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

void op_load(t_cpu *cpu, uint32_t instr_raw) {
  instr_Itype instr;
  memcpy(&instr, &instr_raw, 4);

  printf("[op]        LOAD, rd = %d, funct3 = %d, rs1 = %d, imm = 0x%x\n",
         instr.rd, instr.funct3, instr.rs1, instr.imm);

  uint32_t load_addr = cpu->regs.x[instr.rs1] + sign_extend_12bit(instr.imm);
  printf("[load]      addr = %08x\n", load_addr);

  uint32_t data;

  switch (instr.funct3) {
  case 0: // LB
    data = sign_extend_8bit(cpu->mem_ops->read8(cpu->mem_impl, load_addr));
    break;
  case 1: // LH
    data = sign_extend_16bit(cpu->mem_ops->read16(cpu->mem_impl, load_addr));
    break;
  case 2: // LW
    data = cpu->mem_ops->read32(cpu->mem_impl, load_addr, 0);
    break;
  case 4: // LBU
    data = cpu->mem_ops->read8(cpu->mem_impl, load_addr);
    break;
  case 5: // LHU
    data = cpu->mem_ops->read16(cpu->mem_impl, load_addr);
    break;
  default:
    printf("ERROR: unrecognized LOAD instruction");
    abort();
  }

  cpu->regs.x[instr.rd] = data;
  cpu->regs.pc += 4;
}

void op_store(t_cpu *cpu, uint32_t instr_raw) {
  instr_Stype instr;
  memcpy(&instr, &instr_raw, 4);

  uint32_t imm = instr_stype_imm(&instr);
  printf("[op]        STORE, funct3 = %d, rs1 = %d, rs2 = %d, imm = 0x%x\n",
         instr.funct3, instr.rs1, instr.rs2, imm);

  uint32_t store_addr = cpu->regs.x[instr.rs1] + sign_extend_12bit(imm);
  printf("[store]     addr = %08x\n", store_addr);

  uint32_t data = cpu->regs.x[instr.rs2];

  switch (instr.funct3) {
  case 0: // SB
    cpu->mem_ops->write8(cpu->mem_impl, store_addr, data);
    break;
  case 1: // SH
    cpu->mem_ops->write16(cpu->mem_impl, store_addr, data);
    break;
  case 2: // SW
    cpu->mem_ops->write32(cpu->mem_impl, store_addr, data);
    break;
  default:
    printf("ERROR: unrecognized STORE instruction");
    abort();
  }
  cpu->regs.pc += 4;
}

void op_alu_imm(t_cpu *cpu, uint32_t instr_raw) {
  instr_Itype instr;
  memcpy(&instr, &instr_raw, 4);

  printf("[op]        group ALU IMM - %s, rd = %d, funct3 = %d, rs1 = %d, imm "
         "= 0x%x (%d)\n",
         alu_imm_instr_to_str(instr.funct3), instr.rd, instr.funct3, instr.rs1,
         instr.imm, sign_extend_12bit(instr.imm));

  uint32_t shamt;
  uint32_t funct6;

  switch (instr.funct3) {
  case ADDI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] + sign_extend_12bit(instr.imm);
    break;
  case SLTI:
    cpu->regs.x[instr.rd] =
        (int32_t)cpu->regs.x[instr.rs1] > sign_extend_12bit(instr.imm);
    break;
  case SLTIU:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] > sign_extend_12bit_unsigned(instr.imm);
    break;
  case XORI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] ^ sign_extend_12bit(instr.imm);
    break;
  case ORI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] | sign_extend_12bit(instr.imm);
    break;
  case ANDI:
    cpu->regs.x[instr.rd] =
        cpu->regs.x[instr.rs1] & sign_extend_12bit(instr.imm);
    break;
  case SLLI:
    cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] << instr.imm;
    break;
  case SRxI:
    shamt = instr.imm & 0x3F;
    funct6 = instr.imm >> 6;

    if (funct6 == 0b0100000) { // SRAI
      uint32_t sign_bit = (cpu->regs.x[instr.rs1] & 0x80000000) >> 31;
      uint32_t mask = ((1 << (32 - shamt)) - 1) << (32 - shamt);
      printf("shamt = %x, funct6 = %x\n", shamt, funct6);
      printf("sign_bit = %d, mask = %x\n", sign_bit, mask);
      printf("TODO - check this instruction\n");
      abort();
      cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] >> shamt;
    } else if (funct6 == 0b0000000) { // SRLI
      cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] >> shamt;
    } else {
      printf("ERROR: unrecognized SRxI instruction");
      abort();
    }
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
  case OR:
    cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] | cpu->regs.x[instr.rs2];
    break;
  case AND:
    cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] & cpu->regs.x[instr.rs2];
    break;
  case XOR:
    cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1] ^ cpu->regs.x[instr.rs2];
    break;
  case SLL:
    cpu->regs.x[instr.rd] = cpu->regs.x[instr.rs1]
                            << (cpu->regs.x[instr.rs2] & 0x1F);
    break;
  case SLT:
  case SLTU:
  case SRL_SRA:
    printf("ALU instr not yet implemented\n"); // TODO: implement
    abort(); // unsupported instr // TODO: move to default
  }

  cpu->regs.pc += 4;
}

t_cpu_op cpu_ops[] = {
    [LUI] = op_lui,      [AUIPC] = op_auipc,      [JAL] = op_jal,
    [JALR] = op_jalr,    [_BRANCH] = op_branch,   [_LOAD] = op_load,
    [_STORE] = op_store, [_ALU_IMM] = op_alu_imm, [_ALU_R] = op_alu,
};
