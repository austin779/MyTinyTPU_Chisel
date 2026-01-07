FIRRTL version 1.1.0
circuit DualWeightFIFO :
  module DualWeightFIFO :
    input clock : Clock
    input reset : UInt<1>
    input io_push_col0 : UInt<1>
    input io_push_col1 : UInt<1>
    input io_data_in : UInt<8>
    input io_pop : UInt<1>
    output io_col0_out : UInt<8>
    output io_col1_out : UInt<8>
    output io_col1_raw : UInt<8>

    reg queue0_0 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue0_0) @[DualWeightFIFO.scala 26:23]
    reg queue0_1 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue0_1) @[DualWeightFIFO.scala 26:23]
    reg queue0_2 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue0_2) @[DualWeightFIFO.scala 26:23]
    reg queue0_3 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue0_3) @[DualWeightFIFO.scala 26:23]
    reg queue1_0 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue1_0) @[DualWeightFIFO.scala 27:23]
    reg queue1_1 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue1_1) @[DualWeightFIFO.scala 27:23]
    reg queue1_2 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue1_2) @[DualWeightFIFO.scala 27:23]
    reg queue1_3 : UInt<8>, clock with :
      reset => (UInt<1>("h0"), queue1_3) @[DualWeightFIFO.scala 27:23]
    reg wr_ptr0 : UInt<2>, clock with :
      reset => (UInt<1>("h0"), wr_ptr0) @[DualWeightFIFO.scala 32:24]
    reg rd_ptr0 : UInt<2>, clock with :
      reset => (UInt<1>("h0"), rd_ptr0) @[DualWeightFIFO.scala 33:24]
    reg wr_ptr1 : UInt<2>, clock with :
      reset => (UInt<1>("h0"), wr_ptr1) @[DualWeightFIFO.scala 34:24]
    reg rd_ptr1 : UInt<2>, clock with :
      reset => (UInt<1>("h0"), rd_ptr1) @[DualWeightFIFO.scala 35:24]
    reg col1_out_reg : UInt<8>, clock with :
      reset => (UInt<1>("h0"), col1_out_reg) @[DualWeightFIFO.scala 39:29]
    node _queue0_wr_ptr0 = io_data_in @[DualWeightFIFO.scala 48:{21,21}]
    node _GEN_0 = mux(eq(UInt<1>("h0"), wr_ptr0), _queue0_wr_ptr0, queue0_0) @[DualWeightFIFO.scala 48:{21,21} 26:23]
    node _GEN_1 = mux(eq(UInt<1>("h1"), wr_ptr0), _queue0_wr_ptr0, queue0_1) @[DualWeightFIFO.scala 48:{21,21} 26:23]
    node _GEN_2 = mux(eq(UInt<2>("h2"), wr_ptr0), _queue0_wr_ptr0, queue0_2) @[DualWeightFIFO.scala 48:{21,21} 26:23]
    node _GEN_3 = mux(eq(UInt<2>("h3"), wr_ptr0), _queue0_wr_ptr0, queue0_3) @[DualWeightFIFO.scala 48:{21,21} 26:23]
    node _wr_ptr0_T = add(wr_ptr0, UInt<1>("h1")) @[DualWeightFIFO.scala 49:24]
    node _wr_ptr0_T_1 = tail(_wr_ptr0_T, 1) @[DualWeightFIFO.scala 49:24]
    node _GEN_4 = mux(io_push_col0, _GEN_0, queue0_0) @[DualWeightFIFO.scala 47:22 26:23]
    node _GEN_5 = mux(io_push_col0, _GEN_1, queue0_1) @[DualWeightFIFO.scala 47:22 26:23]
    node _GEN_6 = mux(io_push_col0, _GEN_2, queue0_2) @[DualWeightFIFO.scala 47:22 26:23]
    node _GEN_7 = mux(io_push_col0, _GEN_3, queue0_3) @[DualWeightFIFO.scala 47:22 26:23]
    node _GEN_8 = mux(io_push_col0, _wr_ptr0_T_1, wr_ptr0) @[DualWeightFIFO.scala 47:22 49:13 32:24]
    node _rd_ptr0_T = add(rd_ptr0, UInt<1>("h1")) @[DualWeightFIFO.scala 53:24]
    node _rd_ptr0_T_1 = tail(_rd_ptr0_T, 1) @[DualWeightFIFO.scala 53:24]
    node _GEN_9 = mux(io_pop, _rd_ptr0_T_1, rd_ptr0) @[DualWeightFIFO.scala 52:16 53:13 33:24]
    node _queue1_wr_ptr1 = io_data_in @[DualWeightFIFO.scala 59:{21,21}]
    node _GEN_10 = mux(eq(UInt<1>("h0"), wr_ptr1), _queue1_wr_ptr1, queue1_0) @[DualWeightFIFO.scala 59:{21,21} 27:23]
    node _GEN_11 = mux(eq(UInt<1>("h1"), wr_ptr1), _queue1_wr_ptr1, queue1_1) @[DualWeightFIFO.scala 59:{21,21} 27:23]
    node _GEN_12 = mux(eq(UInt<2>("h2"), wr_ptr1), _queue1_wr_ptr1, queue1_2) @[DualWeightFIFO.scala 59:{21,21} 27:23]
    node _GEN_13 = mux(eq(UInt<2>("h3"), wr_ptr1), _queue1_wr_ptr1, queue1_3) @[DualWeightFIFO.scala 59:{21,21} 27:23]
    node _wr_ptr1_T = add(wr_ptr1, UInt<1>("h1")) @[DualWeightFIFO.scala 60:24]
    node _wr_ptr1_T_1 = tail(_wr_ptr1_T, 1) @[DualWeightFIFO.scala 60:24]
    node _GEN_14 = mux(io_push_col1, _GEN_10, queue1_0) @[DualWeightFIFO.scala 58:22 27:23]
    node _GEN_15 = mux(io_push_col1, _GEN_11, queue1_1) @[DualWeightFIFO.scala 58:22 27:23]
    node _GEN_16 = mux(io_push_col1, _GEN_12, queue1_2) @[DualWeightFIFO.scala 58:22 27:23]
    node _GEN_17 = mux(io_push_col1, _GEN_13, queue1_3) @[DualWeightFIFO.scala 58:22 27:23]
    node _GEN_18 = mux(io_push_col1, _wr_ptr1_T_1, wr_ptr1) @[DualWeightFIFO.scala 58:22 60:13 34:24]
    node _GEN_19 = validif(eq(UInt<1>("h0"), rd_ptr1), queue1_0) @[DualWeightFIFO.scala 66:{18,18}]
    node _GEN_20 = mux(eq(UInt<1>("h1"), rd_ptr1), queue1_1, _GEN_19) @[DualWeightFIFO.scala 66:{18,18}]
    node _GEN_21 = mux(eq(UInt<2>("h2"), rd_ptr1), queue1_2, _GEN_20) @[DualWeightFIFO.scala 66:{18,18}]
    node _GEN_22 = mux(eq(UInt<2>("h3"), rd_ptr1), queue1_3, _GEN_21) @[DualWeightFIFO.scala 66:{18,18}]
    node _rd_ptr1_T = add(rd_ptr1, UInt<1>("h1")) @[DualWeightFIFO.scala 68:24]
    node _rd_ptr1_T_1 = tail(_rd_ptr1_T, 1) @[DualWeightFIFO.scala 68:24]
    node _queue1_rd_ptr1 = _GEN_22 @[DualWeightFIFO.scala 66:18]
    node _GEN_23 = mux(io_pop, _queue1_rd_ptr1, col1_out_reg) @[DualWeightFIFO.scala 63:16 66:18 39:29]
    node _GEN_24 = mux(io_pop, _rd_ptr1_T_1, rd_ptr1) @[DualWeightFIFO.scala 63:16 68:13 35:24]
    node _GEN_25 = validif(eq(UInt<1>("h0"), rd_ptr0), queue0_0) @[DualWeightFIFO.scala 77:{15,15}]
    node _GEN_26 = mux(eq(UInt<1>("h1"), rd_ptr0), queue0_1, _GEN_25) @[DualWeightFIFO.scala 77:{15,15}]
    node _GEN_27 = mux(eq(UInt<2>("h2"), rd_ptr0), queue0_2, _GEN_26) @[DualWeightFIFO.scala 77:{15,15}]
    node _GEN_28 = mux(eq(UInt<2>("h3"), rd_ptr0), queue0_3, _GEN_27) @[DualWeightFIFO.scala 77:{15,15}]
    node _queue0_WIRE_0 = UInt<8>("h0") @[DualWeightFIFO.scala 26:{31,31}]
    node _queue0_WIRE_1 = UInt<8>("h0") @[DualWeightFIFO.scala 26:{31,31}]
    node _queue0_WIRE_2 = UInt<8>("h0") @[DualWeightFIFO.scala 26:{31,31}]
    node _queue0_WIRE_3 = UInt<8>("h0") @[DualWeightFIFO.scala 26:{31,31}]
    node _queue1_WIRE_0 = UInt<8>("h0") @[DualWeightFIFO.scala 27:{31,31}]
    node _queue1_WIRE_1 = UInt<8>("h0") @[DualWeightFIFO.scala 27:{31,31}]
    node _queue1_WIRE_2 = UInt<8>("h0") @[DualWeightFIFO.scala 27:{31,31}]
    node _queue1_WIRE_3 = UInt<8>("h0") @[DualWeightFIFO.scala 27:{31,31}]
    node _queue0_rd_ptr0 = _GEN_28 @[DualWeightFIFO.scala 77:15]
    node _queue1_rd_ptr1_0 = _GEN_22 @[DualWeightFIFO.scala 84:15]
    io_col0_out <= _queue0_rd_ptr0 @[DualWeightFIFO.scala 77:15]
    io_col1_out <= col1_out_reg @[DualWeightFIFO.scala 80:15]
    io_col1_raw <= _queue1_rd_ptr1_0 @[DualWeightFIFO.scala 84:15]
    queue0_0 <= mux(reset, _queue0_WIRE_0, _GEN_4) @[DualWeightFIFO.scala 26:{23,23}]
    queue0_1 <= mux(reset, _queue0_WIRE_1, _GEN_5) @[DualWeightFIFO.scala 26:{23,23}]
    queue0_2 <= mux(reset, _queue0_WIRE_2, _GEN_6) @[DualWeightFIFO.scala 26:{23,23}]
    queue0_3 <= mux(reset, _queue0_WIRE_3, _GEN_7) @[DualWeightFIFO.scala 26:{23,23}]
    queue1_0 <= mux(reset, _queue1_WIRE_0, _GEN_14) @[DualWeightFIFO.scala 27:{23,23}]
    queue1_1 <= mux(reset, _queue1_WIRE_1, _GEN_15) @[DualWeightFIFO.scala 27:{23,23}]
    queue1_2 <= mux(reset, _queue1_WIRE_2, _GEN_16) @[DualWeightFIFO.scala 27:{23,23}]
    queue1_3 <= mux(reset, _queue1_WIRE_3, _GEN_17) @[DualWeightFIFO.scala 27:{23,23}]
    wr_ptr0 <= mux(reset, UInt<2>("h0"), _GEN_8) @[DualWeightFIFO.scala 32:{24,24}]
    rd_ptr0 <= mux(reset, UInt<2>("h0"), _GEN_9) @[DualWeightFIFO.scala 33:{24,24}]
    wr_ptr1 <= mux(reset, UInt<2>("h0"), _GEN_18) @[DualWeightFIFO.scala 34:{24,24}]
    rd_ptr1 <= mux(reset, UInt<2>("h0"), _GEN_24) @[DualWeightFIFO.scala 35:{24,24}]
    col1_out_reg <= mux(reset, UInt<8>("h0"), _GEN_23) @[DualWeightFIFO.scala 39:{29,29}]
