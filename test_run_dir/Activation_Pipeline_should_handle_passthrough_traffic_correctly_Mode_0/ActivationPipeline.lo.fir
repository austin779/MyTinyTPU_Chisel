FIRRTL version 1.1.0
circuit ActivationPipeline :
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

  module LossBlock :
    input clock : Clock
    input reset : UInt<1>
    input io_valid_in : UInt<1>
    input io_data_in : SInt<32>
    input io_target_in : SInt<32>
    output io_valid_out : UInt<1>
    output io_loss_out : SInt<32>

    node _diff_T = sub(io_data_in, io_target_in) @[LossBlock.scala 14:25]
    node _diff_T_1 = tail(_diff_T, 1) @[LossBlock.scala 14:25]
    node diff = asSInt(_diff_T_1) @[LossBlock.scala 14:25]
    node _abs_diff_T = lt(diff, asSInt(UInt<1>("h0"))) @[LossBlock.scala 15:27]
    node _abs_diff_T_1 = sub(asSInt(UInt<1>("h0")), diff) @[LossBlock.scala 15:34]
    node _abs_diff_T_2 = tail(_abs_diff_T_1, 1) @[LossBlock.scala 15:34]
    node _abs_diff_T_3 = asSInt(_abs_diff_T_2) @[LossBlock.scala 15:34]
    node abs_diff = mux(_abs_diff_T, _abs_diff_T_3, diff) @[LossBlock.scala 15:21]
    node _T = asUInt(reset) @[LossBlock.scala 18:15]
    node _GEN_0 = mux(_T, UInt<1>("h0"), io_valid_in) @[LossBlock.scala 18:23 19:18 22:18]
    node _GEN_1 = mux(_T, asSInt(UInt<1>("h0")), abs_diff) @[LossBlock.scala 18:23 20:18 23:18]
    io_valid_out <= _GEN_0
    io_loss_out <= _GEN_1

  module ActivationPipeline :
    input clock : Clock
    input reset : UInt<1>
    input io_valid_in : UInt<1>
    input io_acc_in : SInt<32>
    input io_target_in : SInt<32>
    input io_activation_mode : UInt<2>
    input io_norm_gain : SInt<16>
    input io_norm_bias : SInt<32>
    input io_norm_shift : UInt<5>
    input io_q_inv_scale : SInt<16>
    input io_q_zero_point : SInt<8>
    output io_valid_out : UInt<1>
    output io_ub_data_out : SInt<8>
    output io_loss_valid : UInt<1>
    output io_loss_out : SInt<32>

    inst u_act of ActivationFunc @[activation_pipeline.scala 33:21]
    inst u_norm of Normalizer @[activation_pipeline.scala 56:22]
    inst u_loss of LossBlock @[activation_pipeline.scala 70:22]
    reg target_d1 : SInt<32>, clock with :
      reset => (UInt<1>("h0"), target_d1) @[activation_pipeline.scala 47:26]
    node _GEN_0 = mux(io_valid_in, io_target_in, target_d1) @[activation_pipeline.scala 49:21 50:15 47:26]
    node mult = mul(u_norm.io_data_out, io_q_inv_scale) @[activation_pipeline.scala 85:22]
    node _mult_rounded_T = add(mult, asSInt(UInt<9>("h80"))) @[activation_pipeline.scala 88:27]
    node _mult_rounded_T_1 = tail(_mult_rounded_T, 1) @[activation_pipeline.scala 88:27]
    node mult_rounded = asSInt(_mult_rounded_T_1) @[activation_pipeline.scala 88:27]
    node scaled = shr(mult_rounded, 8) @[activation_pipeline.scala 92:30]
    node _biased_T = add(scaled, io_q_zero_point) @[activation_pipeline.scala 96:23]
    node _biased_T_1 = tail(_biased_T, 1) @[activation_pipeline.scala 96:23]
    node biased = asSInt(_biased_T_1) @[activation_pipeline.scala 96:23]
    node _saturated_T = gt(biased, asSInt(UInt<8>("h7f"))) @[activation_pipeline.scala 103:30]
    node _saturated_T_1 = lt(biased, asSInt(UInt<8>("h80"))) @[activation_pipeline.scala 104:30]
    node _saturated_T_2 = bits(biased, 7, 0) @[activation_pipeline.scala 104:56]
    node _saturated_T_3 = asSInt(_saturated_T_2) @[activation_pipeline.scala 104:62]
    node _saturated_T_4 = mux(_saturated_T_1, asSInt(UInt<8>("h80")), _saturated_T_3) @[activation_pipeline.scala 104:22]
    node saturated = mux(_saturated_T, asSInt(UInt<8>("h7f")), _saturated_T_4) @[activation_pipeline.scala 103:22]
    reg valid_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), valid_reg) @[activation_pipeline.scala 107:26]
    reg ub_q_reg : SInt<8>, clock with :
      reset => (UInt<1>("h0"), ub_q_reg) @[activation_pipeline.scala 108:26]
    node _u_act_io_mode_WIRE_1 = io_activation_mode @[activation_pipeline.scala 38:{51,51}]
    node _u_act_io_mode_WIRE_2 = _u_act_io_mode_WIRE_1 @[activation_pipeline.scala 38:{51,51}]
    node _u_act_io_mode_WIRE = _u_act_io_mode_WIRE_2 @[activation_pipeline.scala 38:{51,51}]
    io_valid_out <= valid_reg @[activation_pipeline.scala 117:18]
    io_ub_data_out <= ub_q_reg @[activation_pipeline.scala 118:18]
    io_loss_valid <= u_loss.io_valid_out @[activation_pipeline.scala 76:17]
    io_loss_out <= u_loss.io_loss_out @[activation_pipeline.scala 77:17]
    u_act.clock <= clock
    u_act.reset <= reset
    u_act.io_valid_in <= io_valid_in @[activation_pipeline.scala 35:21]
    u_act.io_data_in <= io_acc_in @[activation_pipeline.scala 36:21]
    u_act.io_mode <= _u_act_io_mode_WIRE @[activation_pipeline.scala 38:21]
    target_d1 <= mux(reset, asSInt(UInt<32>("h0")), _GEN_0) @[activation_pipeline.scala 47:{26,26}]
    u_norm.clock <= clock
    u_norm.reset <= reset
    u_norm.io_valid_in <= u_act.io_valid_out @[activation_pipeline.scala 58:22]
    u_norm.io_data_in <= u_act.io_data_out @[activation_pipeline.scala 59:22]
    u_norm.io_gain <= io_norm_gain @[activation_pipeline.scala 60:22]
    u_norm.io_bias <= io_norm_bias @[activation_pipeline.scala 61:22]
    u_norm.io_shift <= io_norm_shift @[activation_pipeline.scala 62:22]
    u_loss.clock <= clock
    u_loss.reset <= reset
    u_loss.io_valid_in <= u_norm.io_valid_out @[activation_pipeline.scala 72:23]
    u_loss.io_data_in <= u_norm.io_data_out @[activation_pipeline.scala 73:23]
    u_loss.io_target_in <= target_d1 @[activation_pipeline.scala 74:23]
    valid_reg <= mux(reset, UInt<1>("h0"), u_norm.io_valid_out) @[activation_pipeline.scala 107:{26,26} 113:13]
    ub_q_reg <= mux(reset, asSInt(UInt<8>("h0")), saturated) @[activation_pipeline.scala 108:{26,26} 114:13]
