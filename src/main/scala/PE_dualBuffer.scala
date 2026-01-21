import chisel3._
import chisel3.util._

class PE_dual_buffer extends Module {
  val io = IO(new Bundle {
    // Control Signals
    val en_weight_load = Input(Bool()) // 1 = Shadow Chain 流動 (背景載入)
    val en_weight_swap = Input(Bool()) // 1 = Shadow 數值更新到 Active (瞬間切換)

    // Valid Handshake (Optional, but good practice)
    val valid_in  = Input(Bool())
    val valid_out = Output(Bool())

    // Data Paths
    val in_act     = Input(SInt(8.W))
    val out_act    = Output(SInt(8.W))

    val in_psum    = Input(SInt(32.W))
    val out_psum   = Output(SInt(32.W))

    val in_weight  = Input(SInt(8.W))
    val out_weight = Output(SInt(8.W))
  })

  // =========================================================
  // 1. Dual Buffer Logic (The Core Fix)
  // =========================================================
  // Active Register: 參與運算的權重，只有 Swap 時才變動
  val weight_active = RegInit(0.S(8.W))
  
  // Shadow Register: 背景傳輸鏈，Load 時就像 Shift Register
  val weight_shadow = RegInit(0.S(8.W))

  // 邏輯 A: 背景載入 (Shift Chain)
  // 當啟用 Load 時，Shadow 接收輸入；否則保持原值 (或你可以讓它一直流動，視控制邏輯而定)
  when(io.en_weight_load) {
    weight_shadow := io.in_weight
  }

  // 邏輯 B: 權重更新 (Swap)
  // 當運算完成，Swap 訊號來臨，Active 瞬間吃下 Shadow 的值
  when(io.en_weight_swap) {
    weight_active := weight_shadow
  }

  // 關鍵修正：直接將 Shadow 接到輸出，形成單週期傳遞鏈
  // PE0(shadow) -> Next Cycle -> PE1(shadow)
  io.out_weight := weight_shadow 

  // =========================================================
  // 2. Data Pipeline (Systolic Flow)
  // =========================================================
  // Activation Pass-through (Horizontal)
  // 通常 Activation 需要打一拍再傳給右邊 (Output Stationary / Weight Stationary 常見設計)
  val act_reg = RegNext(io.in_act) 
  io.out_act := act_reg

  // PSum Pass-through (Vertical)
  // 這裡假設 Partial Sum 也是打一拍往下傳
  val psum_in_reg = RegNext(io.in_psum)

  // =========================================================
  // 3. MAC Unit
  // =========================================================
  // 使用 weight_active 進行運算，這樣載入 shadow 時不會影響計算結果
  val mac_result = (act_reg * weight_active).asSInt + psum_in_reg
  
  val psum_out_reg = RegNext(mac_result)
  val valid_out_reg = RegNext(RegNext(io.valid_in)) // Delay 需匹配 pipeline stage

  io.out_psum  := psum_out_reg
  io.valid_out := valid_out_reg
}
