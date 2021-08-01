
// Copyright (c) 2021 Jan Marjanovic
// This code is licensed under a 3-clause BSD license - see LICENSE.txt


`timescale 1ns / 1ps

module sim_top;

  import axi_vip_pkg::*;

  system_wrapper DUT ();

  initial begin : proc_main
    reg [31:0] read_data;
    reg resp;

    $display("%t Simulation starting...", $time);
    #(100ns);

    DUT.system_i.zynq_ultra_ps_e_0.inst.por_srstb_reset(1'b1);
    #200;
    DUT.system_i.zynq_ultra_ps_e_0.inst.por_srstb_reset(1'b0);
    DUT.system_i.zynq_ultra_ps_e_0.inst.fpga_soft_reset(4'hF);
    #400;
    //minimum 16 clock pulse width delay
    DUT.system_i.zynq_ultra_ps_e_0.inst.por_srstb_reset(1'b1);
    DUT.system_i.zynq_ultra_ps_e_0.inst.fpga_soft_reset(4'h0);

    // reset 
    DUT.system_i.zynq_ultra_ps_e_0.inst.fpga_soft_reset(4'hF);
    #(100ns);
    DUT.system_i.zynq_ultra_ps_e_0.inst.fpga_soft_reset(4'h0);
    #(500ns);

    #(500ns);

    DUT.system_i.zynq_ultra_ps_e_0.inst.read_data(32'ha1000000, 4, read_data, resp);
    $display("%t ID reg = %x", $time, read_data);
    // DUT.system_i.zynq_ultra_ps_e_0.inst.read_data(32'ha1000004, 4, read_data, resp);
    // $display("%t version = %x", $time, read_data);
    #(500ns);

    $display("%t Simulation done", $time);
    $finish;
  end

endmodule
