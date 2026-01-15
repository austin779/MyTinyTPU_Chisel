import chisel3._
import chisel3.util._

class AccumulatorAlign extends Module {
  val io = IO(new Bundle {
    val valid_in      = Input(Bool())
    val raw_col0      = Input(UInt(16.W))
    val raw_col1      = Input(UInt(16.W))

    val aligned_valid = Output(Bool())
    val align_col0    = Output(UInt(16.W))
    val align_col1    = Output(UInt(16.W))
  })

  // --- State Registers ---
  // Initialize to 0 to match Verilog reset block
  val col0_delay_reg    = RegInit(0.U(16.W))
  val pending           = RegInit(false.B)
  
  // --- Output Registers ---
  val aligned_valid_reg = RegInit(false.B)
  val align_col0_reg    = RegInit(0.U(16.W))
  val align_col1_reg    = RegInit(0.U(16.W))

  // --- Logic ---
  
  // Default assignment: Pulse valid low every cycle unless driven high
  aligned_valid_reg := false.B

  when(io.valid_in) {
    when(!pending) {
      // [First Valid] Capture Col 0, wait for Col 1
      col0_delay_reg := io.raw_col0
      pending        := true.B
    } .otherwise {
      // [Subsequent Valids] Continuous Streaming
      
      // 1. Output the pair (Old Col0 + New Col1)
      aligned_valid_reg := true.B
      align_col0_reg    := col0_delay_reg
      align_col1_reg    := io.raw_col1
      
      // 2. Capture the current Col0 for the NEXT pair
      col0_delay_reg    := io.raw_col0
      
      // pending stays true (implicit)
    }
  } .otherwise {
    // [No Valid Input] Reset pending state
    pending := false.B
  }

  // --- Wiring to Output Ports ---
  io.aligned_valid := aligned_valid_reg
  io.align_col0    := align_col0_reg
  io.align_col1    := align_col1_reg
}
