import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MMU_dual_buffer_Test extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "MMU_double_buffer"

  it should "perform matrix multiplication with background loading and swapping" in {
    test(new MMU_double_buffer(rows = 2, cols = 2))
      .withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      
      def resetInputs(): Unit = {
        c.io.en_weight_load.poke(false.B)
        c.io.en_weight_swap.poke(false.B)
        c.io.in_act.foreach(_.poke(0.S))
        c.io.in_psum.foreach(_.poke(0.S))
        c.io.in_valid.foreach(_.poke(false.B))
        c.io.in_weight.foreach(_.poke(0.S))
      }

      resetInputs()
      c.clock.step(1)
      
      // ... (Phase 1: Loading W1 保持不變，略過以節省篇幅) ...
      // 假設這裡已經跑完 Phase 1，W1 載入完成，W2 準備載入
      println("=== Phase 1 Done: W1 Loaded ===")
      
      // 模擬 Phase 1 結束後的 Swap
      c.io.en_weight_swap.poke(true.B)
      c.clock.step(1)
      c.io.en_weight_swap.poke(false.B)

      // ==========================================
      // Phase 2: Compute A x W1 (Expect 8, 12)
      // 同時背景載入 W2
      // ==========================================
      println("\n=== Phase 2: Compute (A x W1) + Bg Load (W2) ===")
      
      // Cycle T+0: Input Row 0 Start
      c.io.in_act(0).poke(2.S) 
      // Bg Load Start
      c.io.en_weight_load.poke(true.B)
      c.io.in_weight(0).poke(30.S) 
      c.clock.step(1)

      // Cycle T+1: Input Row 0 Next, Row 1 Start
      c.io.in_act(0).poke(2.S)
      c.io.in_act(1).poke(2.S)
      // Bg Load Next
      c.io.in_weight(0).poke(10.S)
      c.io.in_weight(1).poke(40.S)
      c.clock.step(1)

      // Cycle T+2: Input Row 1 Next
      c.io.in_act(0).poke(0.S)
      c.io.in_act(1).poke(2.S)
      // Bg Load Next
      c.io.in_weight(0).poke(0.S)
      c.io.in_weight(1).poke(20.S)
      c.clock.step(1)

      // Cycle T+3: Input Done
      c.io.in_act(1).poke(0.S)
      c.io.en_weight_load.poke(false.B)
      c.io.in_weight(1).poke(0.S)
      
      // *** 關鍵驗證點 1 (T+3) ***
      // 根據 Latency = 4，第一筆結果應該還沒出來 (或剛出來)，我們先不做檢查
      c.clock.step(1)

      // Cycle T+4: 這裡應該要有第一筆輸出 (Row 0 的結果)
      // Result Matrix Row 0: [8, 12]
      // 由於 MMU 輸出也是 Skewed，通常 Col 0 先出來
      c.io.out_psum(0).expect(8.S, "Phase 2 Row 0 Col 0 Failed")
      println("Checked: Output 8 (Col 0) OK")
      c.clock.step(1)

      // Cycle T+5: 
      // Result Matrix Row 1: [8, 12] (Col 0 的第二個輸出)
      // Result Matrix Row 0: [12]    (Col 1 的第一個輸出 - Skewed)
      c.io.out_psum(0).expect(8.S, "Phase 2 Row 1 Col 0 Failed")
      c.io.out_psum(1).expect(12.S, "Phase 2 Row 0 Col 1 Failed")
      println("Checked: Output 8 (Col 0) & 12 (Col 1) OK")
      c.clock.step(1)

      // Cycle T+6:
      // Result Matrix Row 1: [12] (Col 1 的第二個輸出)
      c.io.out_psum(1).expect(12.S, "Phase 2 Row 1 Col 1 Failed")
      println("Checked: Output 12 (Col 1) OK")
      c.clock.step(1)


      // ==========================================
      // Phase 3: Swap & Compute B x W2 (Expect 40, 60)
      // ==========================================
      println("\n=== Phase 3: Swap & Compute (B x W2) ===")
      c.io.en_weight_swap.poke(true.B)
      c.clock.step(1)
      c.io.en_weight_swap.poke(false.B)

      // Input B (全為 1)
      // Cycle T+0
      c.io.in_act(0).poke(1.S)
      c.clock.step(1)

      // Cycle T+1
      c.io.in_act(0).poke(1.S)
      c.io.in_act(1).poke(1.S)
      c.clock.step(1)

      // Cycle T+2
      c.io.in_act(0).poke(0.S)
      c.io.in_act(1).poke(1.S)
      c.clock.step(1)
      
      // Cycle T+3
      c.io.in_act(1).poke(0.S)
      c.clock.step(1)

      // Cycle T+4: Check Output [40, 60]
      // 第一筆輸出: Col 0 應該是 40
      c.io.out_psum(0).expect(40.S, "Phase 3 Row 0 Col 0 Failed (Did Swap work?)")
      println("Checked: Output 40 (Col 0) - SWAP SUCCESS!")
      c.clock.step(1)

      // Cycle T+5
      c.io.out_psum(0).expect(40.S)
      c.io.out_psum(1).expect(60.S)
      println("Checked: Output 40 & 60 OK")
      c.clock.step(1)

      // Cycle T+6
      c.io.out_psum(1).expect(60.S)
      println("Checked: Output 60 OK")
      c.clock.step(1)
      
      println("ALL CHECKS PASSED! Dual Buffer Logic is Solid.")
    }
  }
}
