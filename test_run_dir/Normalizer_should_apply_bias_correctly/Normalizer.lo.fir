FIRRTL version 1.1.0
circuit Normalizer :
  module Normalizer :
    input clock : Clock
    input reset : UInt<1>
    input io_valid_in : UInt<1>
    input io_data_in : SInt<32>
    input io_gain : SInt<16>
    input io_bias : SInt<32>
    input io_shift : UInt<5>
    output io_valid_out : UInt<1>
    output io_data_out : SInt<32>

    node mult = mul(io_data_in, io_gain) @[Normalizer.scala 20:25]
    node shifted = dshr(mult, io_shift) @[Normalizer.scala 25:22]
    reg valid_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), valid_reg) @[Normalizer.scala 28:26]
    reg data_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), data_reg) @[Normalizer.scala 29:26]
    node _data_reg_T = bits(shifted, 31, 0) @[Normalizer.scala 38:22]
    node _data_reg_T_1 = asSInt(_data_reg_T) @[Normalizer.scala 38:30]
    node _data_reg_T_2 = add(_data_reg_T_1, io_bias) @[Normalizer.scala 38:37]
    node _data_reg_T_3 = tail(_data_reg_T_2, 1) @[Normalizer.scala 38:37]
    node _data_reg_T_4 = asSInt(_data_reg_T_3) @[Normalizer.scala 38:37]
    io_valid_out <= valid_reg @[Normalizer.scala 41:16]
    io_data_out <= data_reg @[Normalizer.scala 42:16]
    valid_reg <= mux(reset, UInt<1>("h0"), io_valid_in) @[Normalizer.scala 28:{26,26} 32:13]
    data_reg <= mux(reset, asSInt(UInt<32>("h0")), _data_reg_T_4) @[Normalizer.scala 29:{26,26} 38:12]
