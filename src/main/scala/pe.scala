import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage

class PE extends Module {
  val io = IO(new Bundle {
    val en_weight_pass    = Input(Bool())
    val en_weight_capture = Input(Bool())
    val in_act            = Input(UInt(8.W))
    val in_psum           = Input(UInt(16.W))
    val out_act           = Output(UInt(8.W))
    val out_psum          = Output(UInt(16.W))
  })

  // --- Internal Registers (DFFs) ---
  val weight_reg   = RegInit(0.U(8.W))
  val out_act_reg  = RegInit(0.U(8.W))
  val out_psum_reg = RegInit(0.U(16.W))

  // --- Logic Description ---
  when(io.en_weight_pass) {
    // [Mode 1: Weight Loading]
    // 1. Vertical Shift: Pass psum from top to bottom
    out_psum_reg := io.in_psum
    
    // 2. Clear horizontal activation during loading (optional but clean)
    out_act_reg  := 0.U

    // 3. Weight Capture Logic (Nested inside Pass mode)
    // Only capture when BOTH pass=1 AND capture=1
    when(io.en_weight_capture) {
      weight_reg := io.in_psum(7, 0)
    }
  } .otherwise { 
    // [Mode 2: Computation (MAC)]
    // 1. Horizontal Shift: Pass activation from left to right
    out_act_reg := io.in_act
    
    // 2. Multiply-Accumulate
    // Formula: out = (act * weight) + psum
    // Note: (8-bit * 8-bit) + 16-bit = 17-bit result.
    // Assigning to 16-bit register implicitly truncates the MSB (overflow is ignored).
    out_psum_reg := (io.in_act * weight_reg) + io.in_psum
  }

  // --- Output Assignments ---
  io.out_act  := out_act_reg
  io.out_psum := out_psum_reg
}

// Generate Verilog
object PEMain extends App {
  (new ChiselStage).emitVerilog(new PE(), Array("--target-dir", "generated"))
}
