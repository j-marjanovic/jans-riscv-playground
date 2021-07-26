// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt

package ervp02.pc8250

import chisel3._
import chisel3.util._

class RBR extends Bundle {
  val data = UInt(8.W)
}

class THR extends Bundle {
  val data = UInt(8.W)
}

class IER extends Bundle {
  val rsvd7_4 = UInt(4.W)
  val MS = Bool()
  val RLS = Bool()
  val THRE = Bool()
  val RDE = Bool()
}

class IIR extends Bundle {
  val rsvd7_3 = UInt(5.W)
  val IID = UInt(2.W)
  val IP = Bool()
}

class LCR extends Bundle {
  val DLAB = Bool()
  val SBC = Bool()
  val SPAR = Bool()
  val EPS = Bool()
  val PEN = Bool()
  val STB = Bool()
  val WSL = UInt(2.W)
}

class MCR extends Bundle {
  val rsvd7_5 = UInt(3.W)
  val loop = Bool()
  val out = UInt(2.W)
  val RTS = Bool()
  val DTR = Bool()
}

class LSR extends Bundle {
  val rsvd7 = Bool()
  val TEMP = Bool()
  val THRE = Bool()
  val BI = Bool()
  val FE = Bool()
  val PE = Bool()
  val OE = Bool()
  val DR = Bool()
}

class MSR extends Bundle {
  val DCD = Bool()
  val RI = Bool()
  val DSR = Bool()
  val CTS = Bool()
  val DDCD = Bool()
  val TERI = Bool()
  val DDSR = Bool()
  val DCTS = Bool()
}

class SCR extends Bundle {
  val data = UInt(8.W)
}

class DL extends Bundle {
  val L = UInt(8.W)
  val H = UInt(8.W)
}

class Registers(val DL_INIT: Int) extends MultiIOModule {
  val mem = IO(new MemoryInterface)

  // 0
  val RBR = IO(Input(Valid(new RBR)))

  val THR = IO(Output(Valid(new THR)))
  val reg_THR = Reg(Valid(new THR))
  THR := reg_THR
  reg_THR.valid := false.B

  val DL = IO(Output(UInt(16.W)))
  val reg_DL = RegInit(new DL, DL_INIT.U(16.W).asTypeOf(new DL))
  DL := reg_DL.asUInt()

  // 2 - not yet impl

  // 3
  val LCR = IO(Output(new LCR))
  val reg_LCR = Reg(new LCR)
  LCR := reg_LCR

  // 4
  val MCR = IO(Output(new MCR))
  val reg_MCR = Reg(new MCR)
  MCR := reg_MCR

  // 5
  val LSR = IO(Input(new LSR))
  val reg_LSR = Wire(new LSR)
  val reg_LSR_DR = RegInit(false.B)
  reg_LSR.rsvd7 := false.B
  reg_LSR.TEMP := LSR.TEMP
  reg_LSR.THRE := LSR.THRE
  reg_LSR.BI := LSR.BI
  reg_LSR.FE := LSR.FE
  reg_LSR.PE := LSR.PE
  reg_LSR.OE := LSR.OE
  reg_LSR.DR := reg_LSR_DR

  // 6
  val MSR = IO(Input(new MSR))

  // 7
  val reg_SCR = Reg(new SCR)

  // write logic
  when(mem.we) {
    switch(mem.addr) {
      is(0.U) {
        when(reg_LCR.DLAB) {
          reg_DL.L := mem.din
        }.otherwise {
          reg_THR.bits := mem.din.asTypeOf(new THR)
          reg_THR.valid := true.B
        }
      }
      is(1.U) {
        when(reg_LCR.DLAB) {
          reg_DL.H := mem.din
        }
      }
      is(3.U) {
        reg_LCR := mem.din.asTypeOf(new LCR)
      }
      is(4.U) {
        reg_MCR := mem.din.asTypeOf(new MCR)
      }
      // 5: read only
      // 6: read only
      is(7.U) {
        reg_SCR := mem.din.asTypeOf(new SCR)
      }
    }
  }

  // read logic
  val dout_reg = Reg(UInt(8.W))
  mem.dout := dout_reg

  switch(mem.addr) {
    is(0.U) {
      when(reg_LCR.DLAB) {
        dout_reg := reg_DL.L
      }.otherwise {
        dout_reg := RBR.asUInt()
      }
    }
    is(1.U) {
      when(reg_LCR.DLAB) {
        dout_reg := reg_DL.H
      }.otherwise {
        dout_reg := 0.U
      }
    }
    is(2.U) {
      dout_reg := 0.U
    }
    is(3.U) {
      dout_reg := reg_LCR.asUInt()
    }
    is(4.U) {
      dout_reg := reg_MCR.asUInt()
    }
    is(5.U) {
      dout_reg := LSR.asUInt()
    }
    is(6.U) {
      dout_reg := MSR.asUInt()
    }
    is(7.U) {
      dout_reg := reg_SCR.asUInt()
    }
  }

  // DE logic
  when(RBR.valid) {
    reg_LSR_DR := true.B
  }.elsewhen(mem.addr === 0.U && !reg_LCR.DLAB && !mem.we) {
    reg_LSR_DR := false.B
  }

}
