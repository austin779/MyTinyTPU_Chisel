import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PETest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Processing Element (PE)"

  // 定義一個輔助函數來初始化所有輸入為 0
  def initSignals(dut: PE): Unit = {
    dut.io.en_weight_pass.poke(false.B)
    dut.io.en_weight_capture.poke(false.B)
    dut.io.in_act.poke(0.U)
    dut.io.in_psum.poke(0.U)
  }

  // 對應 Python: test_pe_reset
  it should "initialize all outputs to zero after reset" in {
    test(new PE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // ChiselTest 啟動時預設會做一次 reset，但我們也可以手動測試
      initSignals(dut)
      dut.reset.poke(true.B)
      dut.clock.step(2) // 等待 2 個 cycle
      dut.reset.poke(false.B)

      // 檢查輸出是否為 0
      dut.io.out_act.expect(0.U, "out_act should be 0 after reset")
      dut.io.out_psum.expect(0.U, "out_psum should be 0 after reset")
    }
  }

  // 對應 Python: test_pe_weight_passthrough
  it should "pass psum through during weight loading mode" in {
    test(new PE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1) // 讓 reset 穩定

      // Enable weight pass mode
      // Python: dut.en_weight_pass.value = 1
      //         dut.in_psum.value = 42
      dut.io.en_weight_pass.poke(true.B)
      dut.io.en_weight_capture.poke(false.B)
      dut.io.in_psum.poke(42.U)
      
      // 在 Python 中你等了 2 個 cycle，在 Chisel 我們的 PE 延遲是 1 個 cycle
      // T=0 輸入 -> T=1 輸出
      dut.clock.step(1) 

      // Check passthrough
      dut.io.out_psum.expect(42.U, "Expected psum passthrough of 42")
      dut.io.out_act.expect(0.U, "out_act should be 0 during weight load")
    }
  }

  // 對應 Python: test_pe_weight_capture
  it should "capture weight and perform MAC operation" in {
    test(new PE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      // --- Phase 1: Load weight = 5 ---
      // Python: dut.en_weight_pass.value = 1; dut.en_weight_capture.value = 1; dut.in_psum.value = 5
      println("Loading weight 5...")
      dut.io.en_weight_pass.poke(true.B)
      dut.io.en_weight_capture.poke(true.B)
      dut.io.in_psum.poke(5.U)
      dut.clock.step(1) // 鎖存權重

      // --- Phase 2: Compute Mode ---
      // Switch to compute mode: activation=2, psum_in=10
      // Expected: out_psum = (2 * 5) + 10 = 20
      println("Computing: 2 * 5 + 10...")
      dut.io.en_weight_pass.poke(false.B)
      dut.io.en_weight_capture.poke(false.B)
      dut.io.in_act.poke(2.U)
      dut.io.in_psum.poke(10.U)
      
      dut.clock.step(1) // 等待計算結果

      // Check result
      dut.io.out_psum.expect(20.U, "Expected MAC result 20")
    }
  }

  // 對應 Python: test_pe_mac_operation (更完整的 MAC 測試)
  it should "perform continuous MAC operations correctly" in {
    test(new PE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      // Load weight = 7
      dut.io.en_weight_pass.poke(true.B)
      dut.io.en_weight_capture.poke(true.B)
      dut.io.in_psum.poke(7.U)
      dut.clock.step(1)

      // 進入計算模式
      dut.io.en_weight_pass.poke(false.B)
      dut.io.en_weight_capture.poke(false.B)

      // Test case 1: 3 * 7 + 0 = 21
      dut.io.in_act.poke(3.U)
      dut.io.in_psum.poke(0.U)
      dut.clock.step(1)
      dut.io.out_psum.expect(21.U, "Expected 21 (3*7+0)")

      // Test case 2: 4 * 7 + 100 = 128
      // 注意：這是在下一個 cycle 接著測試
      dut.io.in_act.poke(4.U)
      dut.io.in_psum.poke(100.U)
      dut.clock.step(1)
      dut.io.out_psum.expect(128.U, "Expected 128 (4*7+100)")
    }
  }

  // 對應 Python: test_pe_activation_propagation
  it should "propagate activations correctly" in {
    test(new PE).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      // Load arbitrary weight (doesn't matter for activation flow, but good practice)
      dut.io.en_weight_pass.poke(true.B)
      dut.io.en_weight_capture.poke(true.B)
      dut.io.in_psum.poke(1.U)
      dut.clock.step(1)

      // Compute mode
      dut.io.en_weight_pass.poke(false.B)
      dut.io.en_weight_capture.poke(false.B)

      // Test value 99
      dut.io.in_act.poke(99.U)
      dut.clock.step(1)
      dut.io.out_act.expect(99.U, "Expected out_act=99")

      // Test value 42
      dut.io.in_act.poke(42.U)
      dut.clock.step(1)
      dut.io.out_act.expect(42.U, "Expected out_act=42")
    }
  }
}
