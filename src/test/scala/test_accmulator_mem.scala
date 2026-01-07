import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AccumulatorMemTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Accumulator Memory (Double Buffered)"

  // 輔助函式：初始化
  def initSignals(dut: AccumulatorMem): Unit = {
    dut.io.enable.poke(false.B)
    dut.io.accumulator_mode.poke(false.B) // Default: Overwrite
    dut.io.buffer_select.poke(false.B)    // Default: Bank 0
    dut.io.in_col0.poke(0.U)
    dut.io.in_col1.poke(0.U)
  }

  // 設定：使用預設的 bypassReadNew = true
  // 這表示寫入當下，Output 就會呈現剛算好的新值 (延遲 1 Cycle)
  it should "perform overwrite, accumulate, and bank switching correctly" in {
    test(new AccumulatorMem(bypassReadNew = true)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      println("--- Phase 1: Overwrite Mode (Bank 0) ---")
      // 情境：矩陣乘法的第一個 Tile，需要覆蓋掉記憶體裡的垃圾值
      // Operation: Mem[0] = Input
      
      dut.io.enable.poke(true.B)
      dut.io.buffer_select.poke(false.B)    // Select Bank 0
      dut.io.accumulator_mode.poke(false.B) // Mode 0: Overwrite
      
      // Inputs: [100, 200]
      dut.io.in_col0.poke(100.U)
      dut.io.in_col1.poke(200.U)
      
      dut.clock.step(1)

      // Check Output (T+1)
      // 預期：直接輸出輸入值
      dut.io.valid_out.expect(true.B)
      dut.io.out_col0.expect(100.S)
      dut.io.out_col1.expect(200.S)
      println("Phase 1 Pass: Initial write [100, 200]")


      println("--- Phase 2: Accumulate Mode (Bank 0) ---")
      // 情境：矩陣乘法的後續 Tile，需要累加
      // Operation: Mem[0] = Mem[0] + Input
      // Current Mem: [100, 200]
      // Input: [10, 20]
      // Result should be: [110, 220]

      dut.io.accumulator_mode.poke(true.B) // Mode 1: Accumulate
      dut.io.in_col0.poke(10.U)
      dut.io.in_col1.poke(20.U)
      
      dut.clock.step(1)

      // Check Output
      dut.io.out_col0.expect(110.S)
      dut.io.out_col1.expect(220.S)
      println("Phase 2 Pass: Accumulated [10, 20] -> Result [110, 220]")


      println("--- Phase 3: Bank Switching (Switch to Bank 1) ---")
      // 情境：Bank 0 正在被讀取(或保留)，我們切換到 Bank 1 算新的東西
      // Operation: Mem[1] = Input (Overwrite)
      // Bank 0 should remain [110, 220] internally

      dut.io.buffer_select.poke(true.B)      // Select Bank 1
      dut.io.accumulator_mode.poke(false.B)  // Mode 0: Overwrite
      
      // Input: [500, 600]
      dut.io.in_col0.poke(500.U)
      dut.io.in_col1.poke(600.U)
      
      dut.clock.step(1)

      // Check Output (Should show Bank 1 values)
      dut.io.out_col0.expect(500.S)
      dut.io.out_col1.expect(600.S)
      println("Phase 3 Pass: Bank 1 written [500, 600]")

      
      println("--- Phase 4: Accumulate on Bank 1 ---")
      // Operation: Mem[1] = Mem[1] + Input
      // Current Mem[1]: [500, 600]
      // Input: [1, 1]
      // Result: [501, 601]
      
      dut.io.accumulator_mode.poke(true.B) // Accumulate
      dut.io.in_col0.poke(1.U)
      dut.io.in_col1.poke(1.U)
      
      dut.clock.step(1)
      
      dut.io.out_col0.expect(501.S)
      dut.io.out_col1.expect(601.S)
      println("Phase 4 Pass: Bank 1 Accumulated to [501, 601]")


      println("--- Phase 5: Switch Back to Bank 0 (Verification) ---")
      // condition：切回 Bank 0，確認之前的 [110, 220] 還在
      // tip：為了「只讀取不修改」，我們可以使用 Accumulate 模式並輸入 0
      // Operation: Mem[0] = Mem[0] + 0
      
      dut.io.buffer_select.poke(false.B)   // Select Bank 0
      dut.io.accumulator_mode.poke(true.B) // Accumulate
      dut.io.in_col0.poke(0.U)             // Add 0
      dut.io.in_col1.poke(0.U)             // Add 0
      
      dut.clock.step(1)
      
      // Check Output (Should be the value from Phase 2)
      dut.io.out_col0.expect(110.S)
      dut.io.out_col1.expect(220.S)
      println("Phase 5 Pass: Bank 0 value preserved [110, 220]")
      
      // Cleanup
      dut.io.enable.poke(false.B)
      dut.clock.step(1)
      dut.io.valid_out.expect(false.B)
    }
  }
}
/*
Surfer 波形圖


Phase 1 (Overwrite):

buffer_select 為 Low。

out_col0 變成 100。這證明了新值直接覆蓋了初始的 0。

Phase 2 (Accumulate):

accumulator_mode 拉高。

out_col0 變成 110 (100 + 10)。這證明了加法器正常工作。

Phase 3 (Switch):

buffer_select 拉高。

out_col0 變成 500。這證明我們現在操作的是另一塊獨立的記憶體。

Phase 5 (Switch Back):

buffer_select 拉回 Low。

out_col0 變回 110。這證明了 Bank 0 的資料在我們操作 Bank 1 的時候被完好地保存了下來（Double Buffering 成功）。
*/
