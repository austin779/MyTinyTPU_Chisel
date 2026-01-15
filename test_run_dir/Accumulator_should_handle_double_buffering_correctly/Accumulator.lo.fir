FIRRTL version 1.1.0
circuit Accumulator :
  module AccumulatorAlign :
    input clock : Clock
    input reset : UInt<1>
    input io_valid_in : UInt<1>
    input io_raw_col0 : UInt<16>
    input io_raw_col1 : UInt<16>
    output io_aligned_valid : UInt<1>
    output io_align_col0 : UInt<16>
    output io_align_col1 : UInt<16>

    reg col0_delay_reg : UInt<16>, clock with :
      reset => (UInt<1>("h0"), col0_delay_reg) @[accmulator_align.scala 17:34]
    reg pending : UInt<1>, clock with :
      reset => (UInt<1>("h0"), pending) @[accmulator_align.scala 18:34]
    reg aligned_valid_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), aligned_valid_reg) @[accmulator_align.scala 21:34]
    reg align_col0_reg : UInt<16>, clock with :
      reset => (UInt<1>("h0"), align_col0_reg) @[accmulator_align.scala 22:34]
    reg align_col1_reg : UInt<16>, clock with :
      reset => (UInt<1>("h0"), align_col1_reg) @[accmulator_align.scala 23:34]
    node _T = eq(pending, UInt<1>("h0")) @[accmulator_align.scala 31:10]
    node _GEN_0 = mux(_T, io_raw_col0, io_raw_col0) @[accmulator_align.scala 31:20 33:22 44:25]
    node _GEN_1 = mux(_T, UInt<1>("h1"), pending) @[accmulator_align.scala 31:20 34:22 18:34]
    node _GEN_2 = mux(_T, UInt<1>("h0"), UInt<1>("h1")) @[accmulator_align.scala 31:20 28:21 39:25]
    node _GEN_3 = mux(_T, align_col0_reg, col0_delay_reg) @[accmulator_align.scala 31:20 22:34 40:25]
    node _GEN_4 = mux(_T, align_col1_reg, io_raw_col1) @[accmulator_align.scala 31:20 23:34 41:25]
    node _GEN_5 = mux(io_valid_in, _GEN_0, col0_delay_reg) @[accmulator_align.scala 30:21 17:34]
    node _GEN_6 = mux(io_valid_in, _GEN_1, UInt<1>("h0")) @[accmulator_align.scala 30:21 50:13]
    node _GEN_7 = mux(io_valid_in, _GEN_2, UInt<1>("h0")) @[accmulator_align.scala 28:21 30:21]
    node _GEN_8 = mux(io_valid_in, _GEN_3, align_col0_reg) @[accmulator_align.scala 30:21 22:34]
    node _GEN_9 = mux(io_valid_in, _GEN_4, align_col1_reg) @[accmulator_align.scala 30:21 23:34]
    io_aligned_valid <= aligned_valid_reg @[accmulator_align.scala 54:20]
    io_align_col0 <= align_col0_reg @[accmulator_align.scala 55:20]
    io_align_col1 <= align_col1_reg @[accmulator_align.scala 56:20]
    col0_delay_reg <= mux(reset, UInt<16>("h0"), _GEN_5) @[accmulator_align.scala 17:{34,34}]
    pending <= mux(reset, UInt<1>("h0"), _GEN_6) @[accmulator_align.scala 18:{34,34}]
    aligned_valid_reg <= mux(reset, UInt<1>("h0"), _GEN_7) @[accmulator_align.scala 21:{34,34}]
    align_col0_reg <= mux(reset, UInt<16>("h0"), _GEN_8) @[accmulator_align.scala 22:{34,34}]
    align_col1_reg <= mux(reset, UInt<16>("h0"), _GEN_9) @[accmulator_align.scala 23:{34,34}]

  module AccumulatorMem :
    input clock : Clock
    input reset : UInt<1>
    input io_enable : UInt<1>
    input io_accumulator_mode : UInt<1>
    input io_buffer_select : UInt<1>
    input io_in_col0 : UInt<16>
    input io_in_col1 : UInt<16>
    output io_valid_out : UInt<1>
    output io_out_col0 : SInt<32>
    output io_out_col1 : SInt<32>

    reg mem_buff0_col0 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), mem_buff0_col0) @[accmulator_mem.scala 19:31]
    reg mem_buff0_col1 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), mem_buff0_col1) @[accmulator_mem.scala 20:31]
    reg mem_buff1_col0 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), mem_buff1_col0) @[accmulator_mem.scala 21:31]
    reg mem_buff1_col1 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), mem_buff1_col1) @[accmulator_mem.scala 22:31]
    reg valid_out_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), valid_out_reg) @[accmulator_mem.scala 25:30]
    reg out_col0_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), out_col0_reg) @[accmulator_mem.scala 26:30]
    reg out_col1_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), out_col1_reg) @[accmulator_mem.scala 27:30]
    node val0 = cvt(io_in_col0) @[accmulator_mem.scala 37:27]
    node val1 = cvt(io_in_col1) @[accmulator_mem.scala 38:27]
    node _T = eq(io_buffer_select, UInt<1>("h0")) @[accmulator_mem.scala 45:27]
    node _next0_T = add(mem_buff0_col0, val0) @[accmulator_mem.scala 42:40]
    node _next0_T_1 = tail(_next0_T, 1) @[accmulator_mem.scala 42:40]
    node _next0_T_2 = asSInt(_next0_T_1) @[accmulator_mem.scala 42:40]
    node next0 = mux(io_accumulator_mode, _next0_T_2, val0) @[accmulator_mem.scala 42:10]
    node _next1_T = add(mem_buff0_col1, val1) @[accmulator_mem.scala 42:40]
    node _next1_T_1 = tail(_next1_T, 1) @[accmulator_mem.scala 42:40]
    node _next1_T_2 = asSInt(_next1_T_1) @[accmulator_mem.scala 42:40]
    node next1 = mux(io_accumulator_mode, _next1_T_2, val1) @[accmulator_mem.scala 42:10]
    node _next0_T_3 = add(mem_buff1_col0, val0) @[accmulator_mem.scala 42:40]
    node _next0_T_4 = tail(_next0_T_3, 1) @[accmulator_mem.scala 42:40]
    node _next0_T_5 = asSInt(_next0_T_4) @[accmulator_mem.scala 42:40]
    node next0_1 = mux(io_accumulator_mode, _next0_T_5, val0) @[accmulator_mem.scala 42:10]
    node _next1_T_3 = add(mem_buff1_col1, val1) @[accmulator_mem.scala 42:40]
    node _next1_T_4 = tail(_next1_T_3, 1) @[accmulator_mem.scala 42:40]
    node _next1_T_5 = asSInt(_next1_T_4) @[accmulator_mem.scala 42:40]
    node next1_1 = mux(io_accumulator_mode, _next1_T_5, val1) @[accmulator_mem.scala 42:10]
    node _GEN_0 = mux(_T, next0, mem_buff0_col0) @[accmulator_mem.scala 45:40 51:22 19:31]
    node _GEN_1 = mux(_T, next1, mem_buff0_col1) @[accmulator_mem.scala 45:40 52:22 20:31]
    node _GEN_2 = mux(_T, next0, next0_1) @[accmulator_mem.scala 45:40 56:22 73:22]
    node _GEN_3 = mux(_T, next1, next1_1) @[accmulator_mem.scala 45:40 57:22 74:22]
    node _GEN_4 = mux(_T, mem_buff1_col0, next0_1) @[accmulator_mem.scala 21:31 45:40 68:22]
    node _GEN_5 = mux(_T, mem_buff1_col1, next1_1) @[accmulator_mem.scala 22:31 45:40 69:22]
    node _GEN_6 = mux(io_enable, _GEN_0, mem_buff0_col0) @[accmulator_mem.scala 34:19 19:31]
    node _GEN_7 = mux(io_enable, _GEN_1, mem_buff0_col1) @[accmulator_mem.scala 34:19 20:31]
    node _GEN_8 = mux(io_enable, _GEN_2, out_col0_reg) @[accmulator_mem.scala 34:19 26:30]
    node _GEN_9 = mux(io_enable, _GEN_3, out_col1_reg) @[accmulator_mem.scala 34:19 27:30]
    node _GEN_10 = mux(io_enable, _GEN_4, mem_buff1_col0) @[accmulator_mem.scala 34:19 21:31]
    node _GEN_11 = mux(io_enable, _GEN_5, mem_buff1_col1) @[accmulator_mem.scala 34:19 22:31]
    node _GEN_12 = mux(io_enable, UInt<1>("h1"), UInt<1>("h0")) @[accmulator_mem.scala 32:17 34:19 82:19]
    io_valid_out <= valid_out_reg @[accmulator_mem.scala 86:16]
    io_out_col0 <= out_col0_reg @[accmulator_mem.scala 87:16]
    io_out_col1 <= out_col1_reg @[accmulator_mem.scala 88:16]
    mem_buff0_col0 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_6) @[accmulator_mem.scala 19:{31,31}]
    mem_buff0_col1 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_7) @[accmulator_mem.scala 20:{31,31}]
    mem_buff1_col0 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_10) @[accmulator_mem.scala 21:{31,31}]
    mem_buff1_col1 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_11) @[accmulator_mem.scala 22:{31,31}]
    valid_out_reg <= mux(reset, UInt<1>("h0"), _GEN_12) @[accmulator_mem.scala 25:{30,30}]
    out_col0_reg <= mux(reset, asSInt(UInt<32>("h0")), _GEN_8) @[accmulator_mem.scala 26:{30,30}]
    out_col1_reg <= mux(reset, asSInt(UInt<32>("h0")), _GEN_9) @[accmulator_mem.scala 27:{30,30}]

  module Accumulator :
    input clock : Clock
    input reset : UInt<1>
    input io_valid_in : UInt<1>
    input io_accumulator_enable : UInt<1>
    input io_addr_sel : UInt<1>
    input io_mmu_col0_in : UInt<16>
    input io_mmu_col1_in : UInt<16>
    output io_acc_col0_out : SInt<32>
    output io_acc_col1_out : SInt<32>
    output io_valid_out : UInt<1>

    inst u_align of AccumulatorAlign @[accmulator.scala 23:23]
    inst u_mem of AccumulatorMem @[accmulator.scala 26:21]
    io_acc_col0_out <= u_mem.io_out_col0 @[accmulator.scala 43:19]
    io_acc_col1_out <= u_mem.io_out_col1 @[accmulator.scala 44:19]
    io_valid_out <= u_mem.io_valid_out @[accmulator.scala 42:19]
    u_align.clock <= clock
    u_align.reset <= reset
    u_align.io_valid_in <= io_valid_in @[accmulator.scala 29:23]
    u_align.io_raw_col0 <= io_mmu_col0_in @[accmulator.scala 30:23]
    u_align.io_raw_col1 <= io_mmu_col1_in @[accmulator.scala 31:23]
    u_mem.clock <= clock
    u_mem.reset <= reset
    u_mem.io_enable <= u_align.io_aligned_valid @[accmulator.scala 35:29]
    u_mem.io_accumulator_mode <= io_accumulator_enable @[accmulator.scala 36:29]
    u_mem.io_buffer_select <= io_addr_sel @[accmulator.scala 37:29]
    u_mem.io_in_col0 <= u_align.io_align_col0 @[accmulator.scala 38:29]
    u_mem.io_in_col1 <= u_align.io_align_col1 @[accmulator.scala 39:29]
