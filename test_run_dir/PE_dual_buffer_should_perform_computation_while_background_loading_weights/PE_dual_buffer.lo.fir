FIRRTL version 1.1.0
circuit PE_dual_buffer :
  module PE_dual_buffer :
    input clock : Clock
    input reset : UInt<1>
    input io_en_weight_load : UInt<1>
    input io_en_weight_swap : UInt<1>
    input io_valid_in : UInt<1>
    output io_valid_out : UInt<1>
    input io_in_act : SInt<8>
    output io_out_act : SInt<8>
    input io_in_psum : SInt<32>
    output io_out_psum : SInt<32>
    input io_in_weight : SInt<8>
    output io_out_weight : SInt<8>

    reg weight_active : SInt<8>, clock with :
      reset => (UInt<1>("h0"), weight_active) @[PE_dualBuffer.scala 29:30]
    reg weight_shadow : SInt<8>, clock with :
      reset => (UInt<1>("h0"), weight_shadow) @[PE_dualBuffer.scala 32:30]
    node _GEN_0 = mux(io_en_weight_load, io_in_weight, weight_shadow) @[PE_dualBuffer.scala 36:27 37:19 32:30]
    node _GEN_1 = mux(io_en_weight_swap, weight_shadow, weight_active) @[PE_dualBuffer.scala 42:27 43:19 29:30]
    reg act_reg : SInt<8>, clock with :
      reset => (UInt<1>("h0"), act_reg) @[PE_dualBuffer.scala 55:24]
    reg psum_in_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), psum_in_reg) @[PE_dualBuffer.scala 60:28]
    node _mac_result_T = mul(act_reg, weight_active) @[PE_dualBuffer.scala 66:29]
    node _mac_result_T_1 = add(_mac_result_T, psum_in_reg) @[PE_dualBuffer.scala 66:53]
    node _mac_result_T_2 = tail(_mac_result_T_1, 1) @[PE_dualBuffer.scala 66:53]
    node mac_result = asSInt(_mac_result_T_2) @[PE_dualBuffer.scala 66:53]
    reg psum_out_reg : SInt<32>, clock with :
      reset => (UInt<1>("h0"), psum_out_reg) @[PE_dualBuffer.scala 68:29]
    reg valid_out_reg_REG : UInt<1>, clock with :
      reset => (UInt<1>("h0"), valid_out_reg_REG) @[PE_dualBuffer.scala 69:38]
    reg valid_out_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), valid_out_reg) @[PE_dualBuffer.scala 69:30]
    io_valid_out <= valid_out_reg @[PE_dualBuffer.scala 72:16]
    io_out_act <= act_reg @[PE_dualBuffer.scala 56:14]
    io_out_psum <= psum_out_reg @[PE_dualBuffer.scala 71:16]
    io_out_weight <= weight_shadow @[PE_dualBuffer.scala 48:17]
    weight_active <= mux(reset, asSInt(UInt<8>("h0")), _GEN_1) @[PE_dualBuffer.scala 29:{30,30}]
    weight_shadow <= mux(reset, asSInt(UInt<8>("h0")), _GEN_0) @[PE_dualBuffer.scala 32:{30,30}]
    act_reg <= io_in_act @[PE_dualBuffer.scala 55:24]
    psum_in_reg <= io_in_psum @[PE_dualBuffer.scala 60:28]
    psum_out_reg <= mac_result @[PE_dualBuffer.scala 68:29]
    valid_out_reg_REG <= io_valid_in @[PE_dualBuffer.scala 69:38]
    valid_out_reg <= valid_out_reg_REG @[PE_dualBuffer.scala 69:30]
