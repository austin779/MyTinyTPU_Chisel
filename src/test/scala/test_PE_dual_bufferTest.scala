import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PE_dual_bufferTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "PE_dual_buffer"

  it should "perform computation while background loading weights" in {
    test(new PE_dual_buffer) { c =>
      // ==========================================
      // 初始化
      // ==========================================
      c.io.en_weight_load.poke(false.B)
      c.io.en_weight_swap.poke(false.B)
      c.io.valid_in.poke(false.B)
      c.io.in_act.poke(0.S)
      c.io.in_psum.poke(0.S)
      c.io.in_weight.poke(0.S)
      c.clock.step(1)

      // ==========================================
      // Phase 1: 載入第一組權重 (Weight = 5)
      // ==========================================
      println("--- Phase 1: Load Weight 5 ---")
      
      // Step 1: Load into Shadow
      c.io.en_weight_load.poke(true.B)
      c.io.in_weight.poke(5.S)
      c.clock.step(1)

      // Step 2: Swap Shadow to Active
      // 此時 Active 應該變成 5
      c.io.en_weight_load.poke(false.B)
      c.io.en_weight_swap.poke(true.B) 
      c.clock.step(1)
      c.io.en_weight_swap.poke(false.B)

      // ==========================================
      // Phase 2: 運算 + 背景載入 (Critical Test)
      // 目標：Active 保持 5 (運算用)，但 Shadow 變成 10 (背景載入)
      // ==========================================
      println("--- Phase 2: Compute (with W=5) & Background Load (W=10) ---")

      // [Cycle 1 Input]
      // 輸入: Act=2, PSum=10. 預期結果: 2 * 5 + 10 = 20 (2 cycle後出現)
      // 動作: 開始背景載入 Weight = 10
      c.io.valid_in.poke(true.B)
      c.io.in_act.poke(2.S)
      c.io.in_psum.poke(10.S)
      
      c.io.en_weight_load.poke(true.B) // <--- 開啟背景載入
      c.io.in_weight.poke(10.S)        // <--- 偷偷塞入新權重 10
      c.clock.step(1)

      // [Cycle 2 Input]
      // 輸入: Act=3, PSum=20. 預期結果: 3 * 5 + 20 = 35 (驗證 Active 沒變)
      // 動作: 關閉 Load
      c.io.en_weight_load.poke(false.B)
      c.io.in_act.poke(3.S)
      c.io.in_psum.poke(20.S)
      c.clock.step(1)

      // ==========================================
      // 驗證 Phase 2 的第一個輸出 (Latency = 2)
      // 這時候應該看到 Cycle 1 的結果: 2 * 5 + 10 = 20
      // ==========================================
      c.io.out_psum.expect(20.S) 
      println(f"Check 1: Output 20 matched (Used Weight 5)")

      // ==========================================
      // Phase 3: 切換權重 (Swap)
      // ==========================================
      println("--- Phase 3: Swap to Weight 10 ---")

      // [Cycle 3 Input] 
      // 動作: 發送 Swap 訊號! 下一筆運算應該要用新權重 10 了
      // 輸入: Act=4, PSum=0. 預期結果: 4 * 10 + 0 = 40 (注意權重變了)
      c.io.en_weight_swap.poke(true.B)
      c.io.in_act.poke(4.S)
      c.io.in_psum.poke(0.S)
      c.clock.step(1)
      c.io.en_weight_swap.poke(false.B)

      // ==========================================
      // 驗證 Phase 2 的第二個輸出
      // 這時候應該看到 Cycle 2 的結果: 3 * 5 + 20 = 35
      // 證明剛剛背景載入的時候，並沒有影響到當時正在算的數據
      // ==========================================
      c.io.out_psum.expect(35.S)
      println(f"Check 2: Output 35 matched (Still used Weight 5 during load)")

      // [Cycle 4 Input] - 隨便給，為了推動 Pipeline
      c.io.in_act.poke(1.S)
      c.io.in_psum.poke(1.S)
      c.clock.step(1)

      // ==========================================
      // 驗證 Phase 3 (Swap 後) 的輸出
      // 這時候應該看到 Cycle 3 的結果: 4 * 10 + 0 = 40
      // ==========================================
      c.io.out_psum.expect(40.S)
      println(f"Check 3: Output 40 matched (Swapped to Weight 10)")
    }
  }
}
