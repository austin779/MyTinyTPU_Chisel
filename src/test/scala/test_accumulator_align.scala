import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AccumulatorAlignTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Accumulator Align (Deskew)"

  // 輔助函式：初始化
  def initSignals(dut: AccumulatorAlign): Unit = {
    dut.io.valid_in.poke(false.B)
    dut.io.raw_col0.poke(0.U)
    dut.io.raw_col1.poke(0.U)
  }

  // --- Test Case 1: Continuous Streaming ---
  // 模擬標準的 MMU 輸出：Col 1 比 Col 0 晚 1 Cycle
  it should "align skewed column data correctly" in {
    test(new AccumulatorAlign).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      println("--- Test 1: Continuous Stream ---")
      // Data Sequence:
      // Row 0 Result: [10, 20]
      // Row 1 Result: [30, 40]
      
      // T=0: MMU Output cycle 0
      // Col 0 出現 Row 0 的結果 (10)
      // Col 1 還是無效值 (0)
      dut.io.valid_in.poke(true.B)
      dut.io.raw_col0.poke(10.U)
      dut.io.raw_col1.poke(0.U) // Invalid/Garbage
      dut.clock.step(1)

      // T=1: MMU Output cycle 1
      // Col 0 出現 Row 1 的結果 (30)
      // Col 1 出現 Row 0 的結果 (20) -> 這應該跟上一拍的 10 配對
      dut.io.valid_in.poke(true.B)
      dut.io.raw_col0.poke(30.U)
      dut.io.raw_col1.poke(20.U) 
      
      // Check Output at T=1 (從 Register 出來的結果反映的是 T=0 的輸入狀態)
      // T=0 輸入觸發了 pending=true，但 aligned_valid_reg 還是 false
      dut.io.aligned_valid.expect(false.B) 
      
      dut.clock.step(1)

      // T=2: MMU Output cycle 2
      // Col 0 結束了 (0)
      // Col 1 出現 Row 1 的結果 (40) -> 這應該跟上一拍的 30 配對
      dut.io.valid_in.poke(true.B)
      dut.io.raw_col0.poke(0.U) // Garbage
      dut.io.raw_col1.poke(40.U)
      
      // Check Output at T=2 (反映 T=1 的輸入操作)
      // 應該看到第一組配對成功 [10, 20]
      dut.io.aligned_valid.expect(true.B)
      dut.io.align_col0.expect(10.U)
      dut.io.align_col1.expect(20.U)
      println(f"T=2 Output: Valid=${dut.io.aligned_valid.peek().litValue}, Col0=${dut.io.align_col0.peek().litValue}, Col1=${dut.io.align_col1.peek().litValue}")

      dut.clock.step(1)

      // T=3: End of Stream
      dut.io.valid_in.poke(false.B)
      
      // Check Output at T=3 (反映 T=2 的輸入操作)
      // 應該看到第二組配對成功 [30, 40]
      dut.io.aligned_valid.expect(true.B)
      dut.io.align_col0.expect(30.U)
      dut.io.align_col1.expect(40.U)
      println(f"T=3 Output: Valid=${dut.io.aligned_valid.peek().litValue}, Col0=${dut.io.align_col0.peek().litValue}, Col1=${dut.io.align_col1.peek().litValue}")

      dut.clock.step(1)
      
      // T=4: Idle
      // Output should be invalid now
      dut.io.aligned_valid.expect(false.B)
      println("PASS: Continuous stream aligned correctly")
    }
  }

  // --- Test Case 2: Interrupted Stream ---
  // 測試如果在傳輸中間斷開 (valid=0)，狀態機是否能正確重置，不會亂配對
  it should "handle interrupted streams (resetting pending state)" in {
    test(new AccumulatorAlign).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      println("--- Test 2: Interrupted Stream ---")
      
      // --- Batch 1: Single Row [55, 66] ---
      // T=0: Col 0 arrives (55)
      dut.io.valid_in.poke(true.B)
      dut.io.raw_col0.poke(55.U)
      dut.io.raw_col1.poke(0.U)
      dut.clock.step(1)

      // T=1: Col 1 arrives (66)
      dut.io.valid_in.poke(true.B)
      dut.io.raw_col0.poke(0.U) // Next col0 is empty/garbage
      dut.io.raw_col1.poke(66.U)
      dut.clock.step(1)

      // T=2: Stream Stops! (Valid goes low)
      dut.io.valid_in.poke(false.B)
      
      // Check Output (Result of T=1 operation) -> Should have [55, 66]
      dut.io.aligned_valid.expect(true.B)
      dut.io.align_col0.expect(55.U)
      dut.io.align_col1.expect(66.U)
      dut.clock.step(1)

      // T=3: Idle (State machine should define pending=false here)
      dut.io.aligned_valid.expect(false.B)
      dut.clock.step(1)

      // --- Batch 2: New Row [77, 88] ---
      // KEY-> The pending status of T=3 with valid=0 should be cleared.
      // So when the new 77 comes in at T=4, it should be treated as the "new Col 0" instead of being paired with the old residual value.
      
      // T=4: Col 0 arrives (77)
      dut.io.valid_in.poke(true.B)
      dut.io.raw_col0.poke(77.U)
      dut.io.raw_col1.poke(99.U) // 這是dirty，不應該被輸出
      dut.clock.step(1)

      // T=5: Col 1 arrives (88)
      dut.io.valid_in.poke(true.B)
      dut.io.raw_col0.poke(0.U)
      dut.io.raw_col1.poke(88.U)
      
      // Check Output of T=4 logic -> Should be INVALID (accumulating 77)
      dut.io.aligned_valid.expect(false.B)
      dut.clock.step(1)

      // T=6: Check Output of T=5 logic -> Should be [77, 88]
      dut.io.aligned_valid.expect(true.B)
      dut.io.align_col0.expect(77.U)
      dut.io.align_col1.expect(88.U)
      
      println("PASS: Interrupted stream handled correctly")
    }
  }
}

/*

T0 (First Valid):

valid_in=1, pending=0

進入 when(!pending)

col0_delay_reg <= A

pending <= 1

Output: aligned_valid=0 (無輸出)

T1 (Subsequent):

valid_in=1, pending=1

進入 .otherwise

aligned_valid_reg <= 1

align_col0_reg <= A (來自 delay_reg)

align_col1_reg <= A (來自 input raw_col1)

col0_delay_reg <= B (存新的 Col 0)

Output (T1晚/T2早): {A, A} Valid! -> 成功對齊！

T2 (Subsequent):

valid_in=1, pending=1

align_col0_reg <= B

align_col1_reg <= B

Output (T2晚/T3早): {B, B} Valid!

T3 (Invalid):

valid_in=0

pending <= 0

Output: aligned_valid=0


*/
