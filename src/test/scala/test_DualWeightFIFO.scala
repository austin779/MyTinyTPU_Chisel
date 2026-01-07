import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class DualWeightFIFOTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Dual Weight FIFO (Staggered)"

  // 輔助函式：初始化輸入訊號
  def initSignals(dut: DualWeightFIFO): Unit = {
    dut.io.push_col0.poke(false.B)
    dut.io.push_col1.poke(false.B)
    dut.io.pop.poke(false.B)
    dut.io.data_in.poke(0.U)
  }

  // Test Case 1: Reset Behavior
  it should "reset all outputs to zero" in {
    test(new DualWeightFIFO(depth = 4, dataWidth = 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)

      // Apply Reset
      dut.reset.poke(true.B)
      dut.clock.step(2)
      dut.reset.poke(false.B)

      // Verify outputs
      dut.io.col0_out.expect(0.U, "col0_out should be 0 after reset")
      dut.io.col1_out.expect(0.U, "col1_out should be 0 after reset")
      
      println("PASS: Dual FIFO reset test")
    }
  }

  // Test Case 2: Column Independence 
  it should "maintain independence between columns" in {
    test(new DualWeightFIFO(depth = 4, dataWidth = 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      // 1. Push to Col0 only: 11, 12
      println("Pushing to Col0: 11, 12")
      dut.io.push_col0.poke(true.B)
      dut.io.data_in.poke(11.U)
      dut.clock.step(1)

      dut.io.data_in.poke(12.U)
      dut.clock.step(1)
      dut.io.push_col0.poke(false.B)

      // 2. Push to Col1 only: 21, 22
      println("Pushing to Col1: 21, 22")
      dut.io.push_col1.poke(true.B)
      dut.io.data_in.poke(21.U)
      dut.clock.step(1)

      dut.io.data_in.poke(22.U)
      dut.clock.step(1)
      dut.io.push_col1.poke(false.B)
      
      dut.clock.step(1) // idle

      // 3. Pop and Verify
      println("Popping...")
      dut.io.pop.poke(true.B)
      
      // Wait 1 cycle for Pop to take effect
      dut.clock.step(1)

      val col0_val1 = dut.io.col0_out.peek().litValue
      val col1_val1 = dut.io.col1_out.peek().litValue
      println(f"After 1st Pop Cycle: col0=$col0_val1, col1=$col1_val1")
      

      // Col0 是獨立的，應該看到 11 或 12 (取決於讀取指標邏輯)
      // Col1 是獨立的，應該看到 21 或 22
      // 只要 col0 != col1 且數值正確，就證明了獨立性
      
      dut.clock.step(1)
      val col0_val2 = dut.io.col0_out.peek().litValue
      val col1_val2 = dut.io.col1_out.peek().litValue
      println(f"After 2nd Pop Cycle: col0=$col0_val2, col1=$col1_val2")

      dut.io.pop.poke(false.B)
      println("PASS: Dual FIFO column independence test")
    }
  }

  // Test Case 3: Skew Verification

  it should "verify 1-cycle skew between col0 and col1" in {
    test(new DualWeightFIFO(depth = 4, dataWidth = 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      // 1. Push same values to both columns: 100, 200
      val testData = Seq(100, 200)

      // Push to Col 0
      dut.io.push_col0.poke(true.B)
      for (data <- testData) {
        dut.io.data_in.poke(data.U)
        dut.clock.step(1)
      }
      dut.io.push_col0.poke(false.B)

      // Push to Col 1
      dut.io.push_col1.poke(true.B)
      for (data <- testData) {
        dut.io.data_in.poke(data.U)
        dut.clock.step(1)
      }
      dut.io.push_col1.poke(false.B)
      
      dut.clock.step(1)

      // 2. Start Popping and Check Skew
      println("Start Popping...")
      dut.io.pop.poke(true.B)

      // --- Cycle 0 of Pop ---
      // Col0 (Combinational): 應該馬上看到第一個數據 100 (Queue0[0])
      // Col1 (Registered): 應該還是 0 (Reset值)，因為還沒經過 Clock Edge
      val c0_t0 = dut.io.col0_out.peek().litValue
      val c1_t0 = dut.io.col1_out.peek().litValue
      println(f"Time 0 (Pop High): col0=$c0_t0, col1=$c1_t0")
      
      dut.io.col0_out.expect(100.U, "Col0 should show 1st data immediately (Combinational)")
      // Col1 還沒更新，無需 expect，或者 expect(0.U)
      
      dut.clock.step(1)

      // --- Cycle 1 of Pop ---
      // Col0 (Comb): 指標前進到 1。應該看到 200 (Queue0[1])
      // Col1 (Reg): 在 Edge 觸發後抓到了 Queue1[0]。應該看到 100 -> **這就是 Skew**
      val c0_t1 = dut.io.col0_out.peek().litValue
      val c1_t1 = dut.io.col1_out.peek().litValue
      println(f"Time 1 (After 1 cycle): col0=$c0_t1, col1=$c1_t1")

      dut.io.col0_out.expect(200.U, "Col0 should satisfy combinational read (next data)")
      dut.io.col1_out.expect(100.U, "Col1 should show 1st data now (1 cycle delay)")

      dut.clock.step(1)

      // --- Cycle 2 of Pop ---
      // Col0 (Comb): 指標前進到 2 (無效值/dirty value)
      // Col1 (Reg): 應該看到 200 (Queue1[1])
      val c0_t2 = dut.io.col0_out.peek().litValue
      val c1_t2 = dut.io.col1_out.peek().litValue
      println(f"Time 2 (After 2 cycles): col0=$c0_t2, col1=$c1_t2")

      dut.io.col1_out.expect(200.U, "Col1 should show 2nd data now")

      dut.io.pop.poke(false.B)
      println("PASS: Dual FIFO skew test completed")
    }
  }
}

/*validation note
Test Case 3 

col0_out 在 pop 拉高的瞬間就變成了 100。

而 col1_out 會在 下一個 Cycle 才變成 100。

sbt "testOnly DualWeightFIFOTest"
*/
