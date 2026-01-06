FIRRTL version 1.1.0
circuit MMU :
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
      reset => (UInt<1>("h0"), weight_reg) @[pe.scala 16:29]
    reg out_act_reg : UInt<8>, clock with :
      reset => (UInt<1>("h0"), out_act_reg) @[pe.scala 17:29]
    reg out_psum_reg : UInt<16>, clock with :
      reset => (UInt<1>("h0"), out_psum_reg) @[pe.scala 18:29]
    node _weight_reg_T = bits(io_in_psum, 7, 0) @[pe.scala 32:31]
    node _GEN_0 = mux(io_en_weight_capture, _weight_reg_T, weight_reg) @[pe.scala 31:32 32:18 16:29]
    node _out_psum_reg_T = mul(io_in_act, weight_reg) @[pe.scala 43:32]
    node _out_psum_reg_T_1 = add(_out_psum_reg_T, io_in_psum) @[pe.scala 43:46]
    node _out_psum_reg_T_2 = tail(_out_psum_reg_T_1, 1) @[pe.scala 43:46]
    node _GEN_1 = mux(io_en_weight_pass, io_in_psum, _out_psum_reg_T_2) @[pe.scala 21:27 24:18 43:18]
    node _GEN_2 = mux(io_en_weight_pass, UInt<1>("h0"), io_in_act) @[pe.scala 21:27 27:18 37:17]
    node _GEN_3 = mux(io_en_weight_pass, _GEN_0, weight_reg) @[pe.scala 21:27 16:29]
    io_out_act <= out_act_reg @[pe.scala 47:15]
    io_out_psum <= out_psum_reg @[pe.scala 48:15]
    weight_reg <= mux(reset, UInt<8>("h0"), _GEN_3) @[pe.scala 16:{29,29}]
    out_act_reg <= mux(reset, UInt<8>("h0"), _GEN_2) @[pe.scala 17:{29,29}]
    out_psum_reg <= mux(reset, UInt<16>("h0"), _GEN_1) @[pe.scala 18:{29,29}]

  module MMU :
    input clock : Clock
    input reset : UInt<1>
    input io_en_weight_pass : UInt<1>
    input io_en_capture_cols_0 : UInt<1>
    input io_en_capture_cols_1 : UInt<1>
    input io_row_in_0 : UInt<8>
    input io_row_in_1 : UInt<8>
    input io_col_in_0 : UInt<8>
    input io_col_in_1 : UInt<8>
    output io_acc_out_0 : UInt<16>
    output io_acc_out_1 : UInt<16>

    inst pes_0_0 of PE @[mmu.scala 18:40]
    inst pes_0_1 of PE @[mmu.scala 18:40]
    inst pes_1_0 of PE @[mmu.scala 18:40]
    inst pes_1_1 of PE @[mmu.scala 18:40]
    node _pes_0_0_io_in_psum_T = cat(UInt<8>("h0"), io_col_in_0) @[Cat.scala 33:92]
    node _pes_0_1_io_in_psum_T = cat(UInt<8>("h0"), io_col_in_1) @[Cat.scala 33:92]
    io_acc_out_0 <= pes_1_0.io_out_psum @[mmu.scala 55:17]
    io_acc_out_1 <= pes_1_1.io_out_psum @[mmu.scala 55:17]
    pes_0_0.clock <= clock
    pes_0_0.reset <= reset
    pes_0_0.io_en_weight_pass <= io_en_weight_pass @[mmu.scala 24:35]
    pes_0_0.io_en_weight_capture <= io_en_capture_cols_0 @[mmu.scala 29:38]
    pes_0_0.io_in_act <= io_row_in_0 @[mmu.scala 34:29]
    pes_0_0.io_in_psum <= _pes_0_0_io_in_psum_T @[mmu.scala 44:30]
    pes_0_1.clock <= clock
    pes_0_1.reset <= reset
    pes_0_1.io_en_weight_pass <= io_en_weight_pass @[mmu.scala 24:35]
    pes_0_1.io_en_weight_capture <= io_en_capture_cols_1 @[mmu.scala 29:38]
    pes_0_1.io_in_act <= pes_0_0.io_out_act @[mmu.scala 37:29]
    pes_0_1.io_in_psum <= _pes_0_1_io_in_psum_T @[mmu.scala 44:30]
    pes_1_0.clock <= clock
    pes_1_0.reset <= reset
    pes_1_0.io_en_weight_pass <= io_en_weight_pass @[mmu.scala 24:35]
    pes_1_0.io_en_weight_capture <= io_en_capture_cols_0 @[mmu.scala 29:38]
    pes_1_0.io_in_act <= io_row_in_1 @[mmu.scala 34:29]
    pes_1_0.io_in_psum <= pes_0_0.io_out_psum @[mmu.scala 47:30]
    pes_1_1.clock <= clock
    pes_1_1.reset <= reset
    pes_1_1.io_en_weight_pass <= io_en_weight_pass @[mmu.scala 24:35]
    pes_1_1.io_en_weight_capture <= io_en_capture_cols_1 @[mmu.scala 29:38]
    pes_1_1.io_in_act <= pes_1_0.io_out_act @[mmu.scala 37:29]
    pes_1_1.io_in_psum <= pes_0_1.io_out_psum @[mmu.scala 47:30]
