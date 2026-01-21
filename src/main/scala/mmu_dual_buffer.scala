import chisel3._
import chisel3.util._

// 假設這是您之前的 PE 定義 (放在同一個 package 或檔案中)
// class PE_dual_buffer extends Module { ... }

class MMU_double_buffer(val rows: Int = 2, val cols: Int = 2) extends Module {
  val io = IO(new Bundle {
    // ================= Global Control =================
    // 廣播給所有 PE
    val en_weight_load = Input(Bool())
    val en_weight_swap = Input(Bool())

    // ================= Inputs (Skewed) ================
    // Activations: 從左側進入，每一列 (Row) 一個輸入
    val in_act = Input(Vec(rows, SInt(8.W)))

    // Partial Sums: 從上方進入，每一行 (Col) 一個輸入 (通常是 0)
    val in_psum = Input(Vec(cols, SInt(32.W)))
    
    // Valid 訊號: 伴隨 PSum 從上方進入
    val in_valid = Input(Vec(cols, Bool()))

    // Weights: 從上方載入 (Background Loading)，每一行一個輸入
    val in_weight = Input(Vec(cols, SInt(8.W)))

    // ================= Outputs ========================
    // Partial Sums: 從最下方流出 (這就是我們要的矩陣運算結果)
    val out_psum = Output(Vec(cols, SInt(32.W)))
    
    // Valid 訊號: 從最下方流出
    val out_valid = Output(Vec(cols, Bool()))
    
    // Debug用: 也可以把最右邊的 Activation 接出來看 (Optional)
    // val out_act = Output(Vec(rows, SInt(8.W))) 
  })

  // 1. 產生 PE 二維陣列
  // 使用 Seq.fill 快速生成 2x2 的 PE 實例
  val PEs = Seq.fill(rows)(Seq.fill(cols)(Module(new PE_dual_buffer)))

  // 2. 透過雙重迴圈進行連線 (Mesh Connection)
  for (r <- 0 until rows) {
    for (c <- 0 until cols) {
      // --- 2.1 廣播控制訊號 ---
      PEs(r)(c).io.en_weight_load := io.en_weight_load
      PEs(r)(c).io.en_weight_swap := io.en_weight_swap

      // --- 2.2 水平連接 (Activations) ---
      if (c == 0) {
        // 最左邊：接外部輸入 (來自 Testbench 或 Buffer)
        PEs(r)(c).io.in_act := io.in_act(r)
      } else {
        // 中間：接左邊 PE 的輸出
        PEs(r)(c).io.in_act := PEs(r)(c-1).io.out_act
      }

      // --- 2.3 垂直連接 (Partial Sums & Valid) ---
      if (r == 0) {
        // 最上面：接外部輸入 (通常是 0)
        PEs(r)(c).io.in_psum  := io.in_psum(c)
        PEs(r)(c).io.valid_in := io.in_valid(c)
      } else {
        // 中間：接上面 PE 的輸出
        PEs(r)(c).io.in_psum  := PEs(r-1)(c).io.out_psum
        PEs(r)(c).io.valid_in := PEs(r-1)(c).io.valid_out
      }

      // --- 2.4 垂直連接 (Weights - Background Chain) ---
      // 這就是實現 "由上而下" 載入權重的關鍵
      if (r == 0) {
        // 最上面：接外部 Weight Feeder
        PEs(r)(c).io.in_weight := io.in_weight(c)
      } else {
        // 中間：接上面 PE 的 Shadow 傳遞線
        PEs(r)(c).io.in_weight := PEs(r-1)(c).io.out_weight
      }
    }
  }

  // 3. 處理模組輸出
  // MMU 的輸出就是「最下面一排 PE」的 psum 和 valid
  for (c <- 0 until cols) {
    io.out_psum(c)  := PEs(rows-1)(c).io.out_psum
    io.out_valid(c) := PEs(rows-1)(c).io.valid_out
  }
}
