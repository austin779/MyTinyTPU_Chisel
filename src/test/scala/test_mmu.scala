import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


class MMUTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "MMU Systolic Array"

  // parameters setup
  val ROWS = 2
  val COLS = 2

  it should "perform full 2x2 matrix multiplication correctly" in {
    test(new MMU(rows = ROWS, cols = COLS)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      
      // ==========================================
      // Test Vectors
      // ==========================================
      // Matrix A (Inputs):
      // [1, 2]
      // [3, 4]
      val matrixA = Seq(
        Seq(1, 2),
        Seq(3, 4)
      )

      // Matrix B (Weights):
      // [5, 6]
      // [7, 8]
      val matrixB = Seq(
        Seq(5, 6),
        Seq(7, 8)
      )

      // Expected Result C = A x B:
      // [ (1*5 + 2*7), (1*6 + 2*8) ]  = [ 19, 22 ]
      // [ (3*5 + 4*7), (3*6 + 4*8) ]  = [ 43, 50 ]
      val expectedC = Seq(
        Seq(19, 22),
        Seq(43, 50)
      )

      // initialization
      dut.io.en_weight_pass.poke(false.B)
      dut.io.en_capture_cols.foreach(_.poke(false.B))
      dut.io.row_in.foreach(_.poke(0.U))
      dut.io.col_in.foreach(_.poke(0.U))
      dut.clock.step(1) // Reset

      println("=== Phase 1: Loading Weights (Matrix B) ===")
      // Weight Stationary!!
      // 我們需要把 Matrix B 的數據從 col_in 灌入。
      // 由於是垂直傳遞，我們先送入 Row 1 (底部) 的權重，再送入 Row 0 (頂部) 的權重。
      
      dut.io.en_weight_pass.poke(true.B) // 開啟穿透模式，讓權重流到底部

      // Step 1.1: 送入 Row 1 的權重 [7, 8]
      // 這些數據會進入 Row 0 的 PE
      for (c <- 0 until COLS) {
        dut.io.col_in(c).poke(matrixB(1)(c).U)
      }
      dut.clock.step(1) 

      // Step 1.2: 送入 Row 0 的權重 [5, 6]
      // 同時，上一步的 [7, 8] 會流到 Row 1 的 PE
      for (c <- 0 until COLS) {
        dut.io.col_in(c).poke(matrixB(0)(c).U)
      }
      dut.clock.step(1)

      // Step 1.3: 鎖存權重 (Capture)
      // 此時 [5,6] 在 Row 0, [7,8] 在 Row 1，正是我們想要的位置
      dut.io.en_capture_cols.foreach(_.poke(true.B))
      dut.clock.step(1)

      // Step 1.4: 結束載入
      dut.io.en_capture_cols.foreach(_.poke(false.B))
      dut.io.en_weight_pass.poke(false.B)
      dut.io.col_in.foreach(_.poke(0.U)) // 清除輸入，避免影響 partial sum
      println(" -> Weights Loaded.")


      println("=== Phase 2: Streaming Activations (Matrix A) ===")
      // 策略：Systolic Skewing (傾斜輸入)
      // Row 0 在 T=0 開始
      // Row 1 在 T=1 開始 (因為它需要跟上方流下來的 partial sum 對齊)
      
      // 輸入序列規劃：
      // Cycle 0: In0=A00(1), In1=0
      // Cycle 1: In0=A01(2), In1=A10(3)
      // Cycle 2: In0=0,      In1=A11(4)
      // Cycle 3: In0=0,      In1=0

      val maxCycles = ROWS + COLS + 5 // 足夠的週期讓數據流完

      // 我們使用一個 map 來追蹤什麼時候預期會出現輸出
      // Output Skew:
      // Col 0 的結果會在 Row 0 輸入後的 (Rows + 0) 延遲出現？這取決於 PE 的 Reg 數量。
      // 假設 PE 每個方向都有 1 cycle delay。
      
      for (i <- 0 until maxCycles) {
        // --- Drive Inputs (Skew Logic) ---
        // Row 0 Logic
        val input0 = if (i < COLS) matrixA(0)(i) else 0
        dut.io.row_in(0).poke(input0.U)

        // Row 1 Logic (Delayed by 1 cycle)
        val input1 = if (i >= 1 && i < 1 + COLS) matrixA(1)(i-1) else 0
        dut.io.row_in(1).poke(input1.U)

        // --- Monitor Outputs ---
        // 在每個 Cycle 檢查輸出，或者我們等到特定 Cycle 再檢查
        // 這裡為了 Debug 方便，我們印出非零輸出
        val out0 = dut.io.acc_out(0).peek().litValue
        val out1 = dut.io.acc_out(1).peek().litValue
        
        println(f"Cycle $i: Inputs=($input0, $input1) | Outputs=($out0, $out1)")

        // --- Validation Logic (Check specific cycles) ---
        // 根據 PE 的 Reg 數量，Latency 通常是 Rows + Cols 的組合
        // 這裡假設:
        // C(0,0) 出現在 Cycle 3 (19)
        // C(1,0) 出現在 Cycle 4 (43)
        // C(0,1) 出現在 Cycle 4 (22)
        // C(1,1) 出現在 Cycle 5 (50)
        
        // 注意：這些 Cycle 數字取決於你的 PE 內部有多少個 Register。
        // 如果你的 PE output_act 和 output_psum 都有 Reg，那上面的推測是合理的。
        
        if (i == 3) {
           assert(out0 == expectedC(0)(0), f"Error C(0,0): Expected ${expectedC(0)(0)}, got $out0")
        }
        if (i == 4) {
           assert(out0 == expectedC(1)(0), f"Error C(1,0): Expected ${expectedC(1)(0)}, got $out0")
           assert(out1 == expectedC(0)(1), f"Error C(0,1): Expected ${expectedC(0)(1)}, got $out1")
        }
        if (i == 5) {
           assert(out1 == expectedC(1)(1), f"Error C(1,1): Expected ${expectedC(1)(1)}, got $out1")
        }

        dut.clock.step(1)
      }
      
      println("=== Test Passed: All values matched expected matrix multiplication ===")
    }
  }
}
