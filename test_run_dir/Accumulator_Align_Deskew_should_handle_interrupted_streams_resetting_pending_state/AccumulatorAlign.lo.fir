FIRRTL version 1.1.0
circuit AccumulatorAlign :
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
