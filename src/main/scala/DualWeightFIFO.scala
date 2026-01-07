import chisel3._
import chisel3.util._

class DualWeightFIFO(val depth: Int = 4, val dataWidth: Int = 8) extends Module {
  val io = IO(new Bundle {
    // Push Interface (Shared Bus)
    val push_col0 = Input(Bool())
    val push_col1 = Input(Bool())
    val data_in   = Input(UInt(dataWidth.W))

    // Pop Interface (Shared Pop)
    val pop       = Input(Bool())
    
    // Outputs
    val col0_out  = Output(UInt(dataWidth.W)) // Direct (Combinational)
    val col1_out  = Output(UInt(dataWidth.W)) // Skewed (Registered, 1-cycle delay)
    
    // Debug Output
    val col1_raw  = Output(UInt(dataWidth.W)) // Pre-skew (Combinational)
  })

  // Memory Storage 
  val queue0 = RegInit(VecInit(Seq.fill(depth)(0.U(dataWidth.W))))
  val queue1 = RegInit(VecInit(Seq.fill(depth)(0.U(dataWidth.W))))

  // Pointers
  // log2Ceil(4) = 2 bits
  val ptrWidth = log2Ceil(depth)
  val wr_ptr0 = RegInit(0.U(ptrWidth.W))
  val rd_ptr0 = RegInit(0.U(ptrWidth.W))
  val wr_ptr1 = RegInit(0.U(ptrWidth.W))
  val rd_ptr1 = RegInit(0.U(ptrWidth.W))

  // --- Column 1 Output Register (The Skew) ---
  val col1_out_reg = RegInit(0.U(dataWidth.W))

  // ============================================
  // Logic Implementation
  // ============================================

  // 1. Column 0 Logic
  // Push
  when(io.push_col0) {
    queue0(wr_ptr0) := io.data_in
    wr_ptr0 := wr_ptr0 + 1.U
  }
  // Pop
  when(io.pop) {
    rd_ptr0 := rd_ptr0 + 1.U
  }

  // 2. Column 1 Logic (with Skew)
  // Push
  when(io.push_col1) {
    queue1(wr_ptr1) := io.data_in
    wr_ptr1 := wr_ptr1 + 1.U
  }
  // Pop
  when(io.pop) {
    // Key difference: The output of Col1 is latched in a register, creating a 1-cycle delay
    col1_out_reg := queue1(rd_ptr1)
    
    rd_ptr1 := rd_ptr1 + 1.U
  }

  // ============================================
  // Output Assignments
  // ============================================

  // Column 0: Direct Combinational Read (No Latency)
  // Verilog: assign col0_out = queue0[rd_ptr0];
  io.col0_out := queue0(rd_ptr0)

  // Column 1: Registered Output (Skewed)
  io.col1_out := col1_out_reg

  // Debug: Raw combinational read of Col1
  // Verilog: assign col1_raw = queue1[rd_ptr1];
  io.col1_raw := queue1(rd_ptr1)
}

// Generate Verilog
import chisel3.stage.ChiselStage
object DualWeightFIFOMain extends App {
  (new ChiselStage).emitVerilog(new DualWeightFIFO(), Array("--target-dir", "generated"))
}
