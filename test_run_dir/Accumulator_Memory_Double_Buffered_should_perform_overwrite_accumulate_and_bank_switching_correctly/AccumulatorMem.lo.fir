FIRRTL version 1.1.0
circuit AccumulatorMem :
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
      reset => (UInt<1>("h0"), mem_buff0_col0) @[accmulator_mem.scala 24:31]
    reg mem_buff0_col1 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), mem_buff0_col1) @[accmulator_mem.scala 25:31]
    reg mem_buff1_col0 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), mem_buff1_col0) @[accmulator_mem.scala 26:31]
    reg mem_buff1_col1 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), mem_buff1_col1) @[accmulator_mem.scala 27:31]
    reg out_col0_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), out_col0_reg) @[accmulator_mem.scala 31:29]
    reg out_col1_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), out_col1_reg) @[accmulator_mem.scala 32:29]
    reg valid_out_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), valid_out_reg) @[accmulator_mem.scala 33:30]
    node in0_s32 = cvt(io_in_col0) @[accmulator_mem.scala 38:28]
    node in1_s32 = cvt(io_in_col1) @[accmulator_mem.scala 39:28]
    node _T = eq(io_buffer_select, UInt<1>("h0")) @[accmulator_mem.scala 50:27]
    node _new_val0_T = mux(io_accumulator_mode, mem_buff0_col0, asSInt(UInt<1>("h0"))) @[accmulator_mem.scala 59:25]
    node _new_val0_T_1 = add(_new_val0_T, in0_s32) @[accmulator_mem.scala 59:62]
    node _new_val0_T_2 = tail(_new_val0_T_1, 1) @[accmulator_mem.scala 59:62]
    node new_val0 = asSInt(_new_val0_T_2) @[accmulator_mem.scala 59:62]
    node _new_val1_T = mux(io_accumulator_mode, mem_buff0_col1, asSInt(UInt<1>("h0"))) @[accmulator_mem.scala 60:25]
    node _new_val1_T_1 = add(_new_val1_T, in1_s32) @[accmulator_mem.scala 60:62]
    node _new_val1_T_2 = tail(_new_val1_T_1, 1) @[accmulator_mem.scala 60:62]
    node new_val1 = asSInt(_new_val1_T_2) @[accmulator_mem.scala 60:62]
    node _new_val0_T_3 = mux(io_accumulator_mode, mem_buff1_col0, asSInt(UInt<1>("h0"))) @[accmulator_mem.scala 83:25]
    node _new_val0_T_4 = add(_new_val0_T_3, in0_s32) @[accmulator_mem.scala 83:62]
    node _new_val0_T_5 = tail(_new_val0_T_4, 1) @[accmulator_mem.scala 83:62]
    node new_val0_1 = asSInt(_new_val0_T_5) @[accmulator_mem.scala 83:62]
    node _new_val1_T_3 = mux(io_accumulator_mode, mem_buff1_col1, asSInt(UInt<1>("h0"))) @[accmulator_mem.scala 84:25]
    node _new_val1_T_4 = add(_new_val1_T_3, in1_s32) @[accmulator_mem.scala 84:62]
    node _new_val1_T_5 = tail(_new_val1_T_4, 1) @[accmulator_mem.scala 84:62]
    node new_val1_1 = asSInt(_new_val1_T_5) @[accmulator_mem.scala 84:62]
    node _GEN_0 = mux(_T, new_val0, mem_buff0_col0) @[accmulator_mem.scala 50:40 63:22 24:31]
    node _GEN_1 = mux(_T, new_val1, mem_buff0_col1) @[accmulator_mem.scala 50:40 64:22 25:31]
    node _GEN_2 = mux(_T, new_val0, new_val0_1) @[accmulator_mem.scala 50:40 69:22 90:22]
    node _GEN_3 = mux(_T, new_val1, new_val1_1) @[accmulator_mem.scala 50:40 70:22 91:22]
    node _GEN_4 = mux(_T, mem_buff1_col0, new_val0_1) @[accmulator_mem.scala 26:31 50:40 86:22]
    node _GEN_5 = mux(_T, mem_buff1_col1, new_val1_1) @[accmulator_mem.scala 27:31 50:40 87:22]
    node _GEN_6 = mux(io_enable, UInt<1>("h1"), UInt<1>("h0")) @[accmulator_mem.scala 46:19 47:19 99:19]
    node _GEN_7 = mux(io_enable, _GEN_0, mem_buff0_col0) @[accmulator_mem.scala 46:19 24:31]
    node _GEN_8 = mux(io_enable, _GEN_1, mem_buff0_col1) @[accmulator_mem.scala 46:19 25:31]
    node _GEN_9 = mux(io_enable, _GEN_2, out_col0_reg) @[accmulator_mem.scala 46:19 31:29]
    node _GEN_10 = mux(io_enable, _GEN_3, out_col1_reg) @[accmulator_mem.scala 46:19 32:29]
    node _GEN_11 = mux(io_enable, _GEN_4, mem_buff1_col0) @[accmulator_mem.scala 46:19 26:31]
    node _GEN_12 = mux(io_enable, _GEN_5, mem_buff1_col1) @[accmulator_mem.scala 46:19 27:31]
    io_valid_out <= valid_out_reg @[accmulator_mem.scala 103:16]
    io_out_col0 <= out_col0_reg @[accmulator_mem.scala 104:15]
    io_out_col1 <= out_col1_reg @[accmulator_mem.scala 105:15]
    mem_buff0_col0 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_7) @[accmulator_mem.scala 24:{31,31}]
    mem_buff0_col1 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_8) @[accmulator_mem.scala 25:{31,31}]
    mem_buff1_col0 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_11) @[accmulator_mem.scala 26:{31,31}]
    mem_buff1_col1 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_12) @[accmulator_mem.scala 27:{31,31}]
    out_col0_reg <= mux(reset, asSInt(UInt<32>("h0")), _GEN_9) @[accmulator_mem.scala 31:{29,29}]
    out_col1_reg <= mux(reset, asSInt(UInt<32>("h0")), _GEN_10) @[accmulator_mem.scala 32:{29,29}]
    valid_out_reg <= mux(reset, UInt<1>("h0"), _GEN_6) @[accmulator_mem.scala 33:{30,30}]
