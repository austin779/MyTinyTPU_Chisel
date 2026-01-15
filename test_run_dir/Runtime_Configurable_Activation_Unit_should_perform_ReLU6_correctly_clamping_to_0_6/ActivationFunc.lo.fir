FIRRTL version 1.1.0
circuit ActivationFunc :
  module ActivationFunc :
    input clock : Clock
    input reset : UInt<1>
    input io_valid_in : UInt<1>
    input io_data_in : SInt<32>
    input io_mode : UInt<2>
    output io_valid_out : UInt<1>
    output io_data_out : SInt<32>

    reg valid_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), valid_reg) @[ActivationFunc.scala 21:26]
    reg data_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), data_reg) @[ActivationFunc.scala 22:26]
    node _T = asUInt(UInt<1>("h0")) @[ActivationFunc.scala 31:19]
    node _T_1 = asUInt(io_mode) @[ActivationFunc.scala 31:19]
    node _T_2 = eq(_T, _T_1) @[ActivationFunc.scala 31:19]
    node _T_3 = asUInt(UInt<1>("h1")) @[ActivationFunc.scala 31:19]
    node _T_4 = asUInt(io_mode) @[ActivationFunc.scala 31:19]
    node _T_5 = eq(_T_3, _T_4) @[ActivationFunc.scala 31:19]
    node _processed_data_T = gt(io_data_in, asSInt(UInt<1>("h0"))) @[ActivationFunc.scala 36:40]
    node _processed_data_T_1 = mux(_processed_data_T, io_data_in, asSInt(UInt<1>("h0"))) @[ActivationFunc.scala 36:28]
    node _T_6 = asUInt(UInt<2>("h2")) @[ActivationFunc.scala 31:19]
    node _T_7 = asUInt(io_mode) @[ActivationFunc.scala 31:19]
    node _T_8 = eq(_T_6, _T_7) @[ActivationFunc.scala 31:19]
    node _relu_T = gt(io_data_in, asSInt(UInt<1>("h0"))) @[ActivationFunc.scala 39:33]
    node relu = mux(_relu_T, io_data_in, asSInt(UInt<1>("h0"))) @[ActivationFunc.scala 39:21]
    node _processed_data_T_2 = gt(relu, asSInt(UInt<4>("h6"))) @[ActivationFunc.scala 40:34]
    node _processed_data_T_3 = mux(_processed_data_T_2, asSInt(UInt<4>("h6")), relu) @[ActivationFunc.scala 40:28]
    node _GEN_0 = mux(_T_8, _processed_data_T_3, io_data_in) @[ActivationFunc.scala 28:18 31:19 40:22]
    node _GEN_1 = mux(_T_5, _processed_data_T_1, _GEN_0) @[ActivationFunc.scala 31:19 36:22]
    node _GEN_2 = mux(_T_2, io_data_in, _GEN_1) @[ActivationFunc.scala 31:19 33:22]
    node processed_data = _GEN_2 @[ActivationFunc.scala 25:28]
    io_valid_out <= valid_reg @[ActivationFunc.scala 48:16]
    io_data_out <= data_reg @[ActivationFunc.scala 49:16]
    valid_reg <= mux(reset, UInt<1>("h0"), io_valid_in) @[ActivationFunc.scala 21:{26,26} 45:13]
    data_reg <= mux(reset, asSInt(UInt<32>("h0")), processed_data) @[ActivationFunc.scala 22:{26,26} 46:13]
