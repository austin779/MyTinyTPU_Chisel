module PE(
  input         clock,
  input         reset,
  input         io_en_weight_pass,
  input         io_en_weight_capture,
  input  [7:0]  io_in_act,
  input  [15:0] io_in_psum,
  output [7:0]  io_out_act,
  output [15:0] io_out_psum
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
`endif // RANDOMIZE_REG_INIT
  reg [7:0] weight_reg; // @[pe.scala 15:29]
  reg [7:0] out_act_reg; // @[pe.scala 16:29]
  reg [15:0] out_psum_reg; // @[pe.scala 17:29]
  wire [15:0] _T_1 = io_in_act * weight_reg; // @[pe.scala 38:32]
  wire [15:0] _T_3 = _T_1 + io_in_psum; // @[pe.scala 38:46]
  assign io_out_act = out_act_reg; // @[pe.scala 42:15]
  assign io_out_psum = out_psum_reg; // @[pe.scala 43:15]
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  weight_reg = _RAND_0[7:0];
  _RAND_1 = {1{`RANDOM}};
  out_act_reg = _RAND_1[7:0];
  _RAND_2 = {1{`RANDOM}};
  out_psum_reg = _RAND_2[15:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
  always @(posedge clock) begin
    if (reset) begin
      weight_reg <= 8'h0;
    end else if (io_en_weight_pass) begin
      if (io_en_weight_capture) begin
        weight_reg <= io_in_psum[7:0];
      end
    end
    if (reset) begin
      out_act_reg <= 8'h0;
    end else if (io_en_weight_pass) begin
      out_act_reg <= 8'h0;
    end else begin
      out_act_reg <= io_in_act;
    end
    if (reset) begin
      out_psum_reg <= 16'h0;
    end else if (io_en_weight_pass) begin
      out_psum_reg <= io_in_psum;
    end else begin
      out_psum_reg <= _T_3;
    end
  end
endmodule
