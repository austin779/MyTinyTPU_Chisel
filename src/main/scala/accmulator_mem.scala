import chisel3._
import chisel3.util._

class AccumulatorMem(val bypassReadNew: Boolean = true) extends Module {
  val io = IO(new Bundle {
    // Control Signals
    val enable           = Input(Bool())
    val accumulator_mode = Input(Bool()) // 0: Overwrite, 1: Accumulate
    val buffer_select    = Input(Bool()) // 0: Bank 0, 1: Bank 1

    // Data Inputs (16-bit Unsigned usually, derived from MMU)
    val in_col0 = Input(UInt(16.W))
    val in_col1 = Input(UInt(16.W))

    // Outputs (32-bit Signed)
    val valid_out = Output(Bool())
    val out_col0  = Output(SInt(32.W))
    val out_col1  = Output(SInt(32.W))
  })

  // --- Internal Storage (Double Buffering) ---
  // Define two sets of temporary registers Bank, each containing col0 and col1

  val mem_buff0_col0 = RegInit(0.S(32.W))
  val mem_buff0_col1 = RegInit(0.S(32.W))
  val mem_buff1_col0 = RegInit(0.S(32.W))
  val mem_buff1_col1 = RegInit(0.S(32.W))

  // --- Output Registers ---

  val out_col0_reg = RegInit(0.S(32.W))
  val out_col1_reg = RegInit(0.S(32.W))
  val valid_out_reg = RegInit(false.B)

  // --- Input Conversion ---
  // 在 Chisel 中，先用 zext 轉為 SInt (會多 1 bit)，運算時會自動適應位寬
  val in0_s32 = io.in_col0.zext 
  val in1_s32 = io.in_col1.zext 

//--------------------------------------------------------------------
  
  // 預設保持原值
  valid_out_reg := false.B

  when(io.enable) {
    valid_out_reg := true.B
    
  // Select the bank for the operation based on buffer_select
    when(io.buffer_select === false.B) { 
      // === Bank 0 Active ===
      
      // 1. Calculate Next Value
      // mode=1 (累加): 原值 + 輸入
      // mode=0 (覆蓋): 0 + 輸入
      val old_val0 = mem_buff0_col0
      val old_val1 = mem_buff0_col1
      
      val new_val0 = Mux(io.accumulator_mode, old_val0, 0.S) + in0_s32
      val new_val1 = Mux(io.accumulator_mode, old_val1, 0.S) + in1_s32
      
      // 2. Update Memory
      mem_buff0_col0 := new_val0
      mem_buff0_col1 := new_val1
      
      // 3. Update Output
      if (bypassReadNew) {
        // Bypass Mode: Write-Through 
        out_col0_reg := new_val0
        out_col1_reg := new_val1
      } else {
        // Standard Mode: 輸出寫入前的舊值 Read-Before-Write behavior
        out_col0_reg := old_val0
        out_col1_reg := old_val1
      }
      
    } .otherwise { 
      // === Bank 1 Active ===
      
      val old_val0 = mem_buff1_col0
      val old_val1 = mem_buff1_col1
      
      val new_val0 = Mux(io.accumulator_mode, old_val0, 0.S) + in0_s32
      val new_val1 = Mux(io.accumulator_mode, old_val1, 0.S) + in1_s32
      
      mem_buff1_col0 := new_val0
      mem_buff1_col1 := new_val1
      
      if (bypassReadNew) {
        out_col0_reg := new_val0
        out_col1_reg := new_val1
      } else {
        out_col0_reg := old_val0
        out_col1_reg := old_val1
      }
    }
  } .otherwise {
    // Enable low: Output Valid 為 0，Data Output 保持最後一筆的值 (Latch behavior in Verilog reg)
    valid_out_reg := false.B
  }

  // --- Output Assignment ---
  io.valid_out := valid_out_reg
  io.out_col0 := out_col0_reg
  io.out_col1 := out_col1_reg
}

// Generate Verilog
import chisel3.stage.ChiselStage
object AccumulatorMemMain extends App {
  (new ChiselStage).emitVerilog(new AccumulatorMem(), Array("--target-dir", "generated"))
}
