;buildInfoPackage: chisel3, version: 3.3.1, scalaVersion: 2.12.11, sbtVersion: 1.3.10
circuit PE : 
  module PE : 
    input clock : Clock
    input reset : UInt<1>
    output io : {flip en_weight_pass : UInt<1>, flip en_weight_capture : UInt<1>, flip in_act : UInt<8>, flip in_psum : UInt<16>, out_act : UInt<8>, out_psum : UInt<16>}
    
    reg weight_reg : UInt<8>, clock with : (reset => (reset, UInt<8>("h00"))) @[pe.scala 15:29]
    reg out_act_reg : UInt<8>, clock with : (reset => (reset, UInt<8>("h00"))) @[pe.scala 16:29]
    reg out_psum_reg : UInt<16>, clock with : (reset => (reset, UInt<16>("h00"))) @[pe.scala 17:29]
    when io.en_weight_pass : @[pe.scala 20:27]
      out_psum_reg <= io.in_psum @[pe.scala 23:18]
      out_act_reg <= UInt<1>("h00") @[pe.scala 24:18]
      when io.en_weight_capture : @[pe.scala 27:32]
        node _T = bits(io.in_psum, 7, 0) @[pe.scala 29:31]
        weight_reg <= _T @[pe.scala 29:18]
        skip @[pe.scala 27:32]
      skip @[pe.scala 20:27]
    else : @[pe.scala 31:16]
      out_act_reg <= io.in_act @[pe.scala 34:17]
      node _T_1 = mul(io.in_act, weight_reg) @[pe.scala 38:32]
      node _T_2 = add(_T_1, io.in_psum) @[pe.scala 38:46]
      node _T_3 = tail(_T_2, 1) @[pe.scala 38:46]
      out_psum_reg <= _T_3 @[pe.scala 38:18]
      skip @[pe.scala 31:16]
    io.out_act <= out_act_reg @[pe.scala 42:15]
    io.out_psum <= out_psum_reg @[pe.scala 43:15]
    
