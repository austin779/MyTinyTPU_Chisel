import chisel3._
import chisel3.util._

class AccumulatorAlign extends Module {
  val io = IO(new Bundle {
    val valid_in  = Input(Bool())
    val raw_col0  = Input(UInt(16.W))
    val raw_col1  = Input(UInt(16.W))

    val aligned_valid = Output(Bool())
    val align_col0    = Output(UInt(16.W))
    val align_col1    = Output(UInt(16.W))
  })

  // Internal Registers 
  // Used to temporarily store the value of Col 0, waiting for Col 1 in the next cycle.
  val col0_delay_reg = RegInit(0.U(16.W))
  
  // Status flag: Indicates whether the first Col 0 has been captured and is waiting for a match.
  val pending = RegInit(false.B)

  // Output register
  val align_col0_reg    = RegInit(0.U(16.W))
  val align_col1_reg    = RegInit(0.U(16.W))
  
  // 預設每個 Cycle valid 都會歸零 (除非被下面的邏輯拉高)
  aligned_valid_reg := false.B

  when(io.valid_in) {
    when(!pending) {
      // [Case 1] First Valid: 
      // 這是串流的第一筆數據。
      // Col 0 先到了，我們先把它存起來 (Capture)，不輸出。
      // Col 1 此時還是無效的 (因為它慢一拍)，所以我們什麼都不送出。
      col0_delay_reg := io.raw_col0
      pending := true.B
    } .otherwise {
      // [Case 2] Subsequent Valids (Continuous Stream):
      // 這不是第一筆了，pending 已經是 true。

      // 1. col0_delay_reg (上一拍的 Col 0)
      // 2. io.raw_col1    (這一拍的 Col 1) -> They are in the same group
      // 3. io.raw_col0    (這一拍的 Col 0) -> This is the next group, save it.

      // 觸發輸出
      aligned_valid_reg := true.B
      
      // 配對輸出
      align_col0_reg := col0_delay_reg // 舊的 Col 0
      align_col1_reg := io.raw_col1    // 新的 Col 1 (剛好對齊)

      // Preparing for the next pairing
      col0_delay_reg := io.raw_col0
      
      // pending 保持為 true，繼續接收
    }
  } .otherwise {
    // [Case 3] No Valid Input:
    // Data flow interrupted. Resetting the 'pending' state to prepare for the next wave of new data.
    pending := false.B
  }

  // --- Output Assignments ---
  io.aligned_valid := aligned_valid_reg
  io.align_col0    := align_col0_reg
  io.align_col1    := align_col1_reg
}

// Generate Verilog
import chisel3.stage.ChiselStage
object AccumulatorAlignMain extends App {
  (new ChiselStage).emitVerilog(new AccumulatorAlign(), Array("--target-dir", "generated"))
}

/*


analysis and validate
假設 MMU 輸出序列如下 (Col 1 比 Col 0 晚 1 Cycle)：

T0: raw_col0 = A, raw_col1 = X (無效), valid = 1

T1: raw_col0 = B, raw_col1 = A (Col 1 的 A 到了), valid = 1

T2: raw_col0 = C, raw_col1 = B, valid = 1

T3: raw_col0 = X, raw_col1 = X, valid = 0

Chisel 電路行為：

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

Output (T1晚/T2早): {A, A} Valid! -> 成功對齊

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
