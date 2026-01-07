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
      reset => (UInt<1>("h0"), col0_delay_reg) @[accmulator_align.scala 17:31]
    reg pending : UInt<1>, clock with :
      reset => (UInt<1>("h0"), pending) @[accmulator_align.scala 20:24]
    reg aligned_valid_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), aligned_valid_reg) @[accmulator_align.scala 23:34]
    reg align_col0_reg : UInt<16>, clock with :
      reset => (UInt<1>("h0"), align_col0_reg) @[accmulator_align.scala 24:34]
    reg align_col1_reg : UInt<16>, clock with :
      reset => (UInt<1>("h0"), align_col1_reg) @[accmulator_align.scala 25:34]
    node _T = eq(pending, UInt<1>("h0")) @[accmulator_align.scala 33:10]
    node _GEN_0 = mux(_T, io_raw_col0, io_raw_col0) @[accmulator_align.scala 33:20 38:22 56:22]
    node _GEN_1 = mux(_T, UInt<1>("h1"), pending) @[accmulator_align.scala 33:20 39:15 20:24]
    node _GEN_2 = mux(_T, UInt<1>("h0"), UInt<1>("h1")) @[accmulator_align.scala 33:20 30:21 49:25]
    node _GEN_3 = mux(_T, align_col0_reg, col0_delay_reg) @[accmulator_align.scala 33:20 24:34 52:22]
    node _GEN_4 = mux(_T, align_col1_reg, io_raw_col1) @[accmulator_align.scala 33:20 25:34 53:22]
    node _GEN_5 = mux(io_valid_in, _GEN_0, col0_delay_reg) @[accmulator_align.scala 32:21 17:31]
    node _GEN_6 = mux(io_valid_in, _GEN_1, UInt<1>("h0")) @[accmulator_align.scala 32:21 63:13]
    node _GEN_7 = mux(io_valid_in, _GEN_2, UInt<1>("h0")) @[accmulator_align.scala 30:21 32:21]
    node _GEN_8 = mux(io_valid_in, _GEN_3, align_col0_reg) @[accmulator_align.scala 32:21 24:34]
    node _GEN_9 = mux(io_valid_in, _GEN_4, align_col1_reg) @[accmulator_align.scala 32:21 25:34]
    io_aligned_valid <= aligned_valid_reg @[accmulator_align.scala 67:20]
    io_align_col0 <= align_col0_reg @[accmulator_align.scala 68:20]
    io_align_col1 <= align_col1_reg @[accmulator_align.scala 69:20]
    col0_delay_reg <= mux(reset, UInt<16>("h0"), _GEN_5) @[accmulator_align.scala 17:{31,31}]
    pending <= mux(reset, UInt<1>("h0"), _GEN_6) @[accmulator_align.scala 20:{24,24}]
    aligned_valid_reg <= mux(reset, UInt<1>("h0"), _GEN_7) @[accmulator_align.scala 23:{34,34}]
    align_col0_reg <= mux(reset, UInt<16>("h0"), _GEN_8) @[accmulator_align.scala 24:{34,34}]
    align_col1_reg <= mux(reset, UInt<16>("h0"), _GEN_9) @[accmulator_align.scala 25:{34,34}]
