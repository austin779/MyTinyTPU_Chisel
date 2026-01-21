FIRRTL version 1.1.0
circuit PE :
  module PE :
    input clock : Clock
    input reset : UInt<1>
    input io_en_weight_pass : UInt<1>
    input io_en_weight_capture : UInt<1>
    input io_in_act : UInt<8>
    input io_in_psum : UInt<16>
    output io_out_act : UInt<8>
    output io_out_psum : UInt<16>

    reg weight_reg : UInt<8>, clock with :
      reset => (UInt<1>("h0"), weight_reg) @[pe.scala 15:29]
    reg out_act_reg : UInt<8>, clock with :
      reset => (UInt<1>("h0"), out_act_reg) @[pe.scala 16:29]
    reg out_psum_reg : UInt<16>, clock with :
      reset => (UInt<1>("h0"), out_psum_reg) @[pe.scala 17:29]
    node _weight_reg_T = bits(io_in_psum, 7, 0) @[pe.scala 37:31]
    node _GEN_0 = mux(io_en_weight_capture, _weight_reg_T, weight_reg) @[pe.scala 36:32 37:18 15:29]
    node _out_psum_reg_T = mul(io_in_act, weight_reg) @[pe.scala 48:32]
    node _out_psum_reg_T_1 = add(_out_psum_reg_T, io_in_psum) @[pe.scala 48:46]
    node _out_psum_reg_T_2 = tail(_out_psum_reg_T_1, 1) @[pe.scala 48:46]
    node _GEN_1 = mux(io_en_weight_pass, io_in_psum, _out_psum_reg_T_2) @[pe.scala 27:27 30:18 48:18]
    node _GEN_2 = mux(io_en_weight_pass, UInt<1>("h0"), io_in_act) @[pe.scala 27:27 32:18 42:17]
    node _GEN_3 = mux(io_en_weight_pass, _GEN_0, weight_reg) @[pe.scala 27:27 15:29]
    io_out_act <= out_act_reg @[pe.scala 52:15]
    io_out_psum <= out_psum_reg @[pe.scala 53:15]
    weight_reg <= mux(reset, UInt<8>("h0"), _GEN_3) @[pe.scala 15:{29,29}]
    out_act_reg <= mux(reset, UInt<8>("h0"), _GEN_2) @[pe.scala 16:{29,29}]
    out_psum_reg <= mux(reset, UInt<16>("h0"), _GEN_1) @[pe.scala 17:{29,29}]
