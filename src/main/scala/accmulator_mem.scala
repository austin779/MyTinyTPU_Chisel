import chisel3._

class AccumulatorMem(val bypassReadNew: Boolean = true) extends Module {
  val io = IO(new Bundle {
    val enable           = Input(Bool())
    val accumulator_mode = Input(Bool()) // 0: Overwrite, 1: Add
    val buffer_select    = Input(Bool()) // 0: Buffer A, 1: Buffer B
    
    // Inputs are 16-bit UInt
    val in_col0          = Input(UInt(16.W))
    val in_col1          = Input(UInt(16.W))
    
    val valid_out        = Output(Bool())
    val out_col0         = Output(SInt(32.W))
    val out_col1         = Output(SInt(32.W))
  })

  // --- Double Buffer Registers ---
  val mem_buff0_col0 = RegInit(0.S(32.W))
  val mem_buff0_col1 = RegInit(0.S(32.W))
  val mem_buff1_col0 = RegInit(0.S(32.W))
  val mem_buff1_col1 = RegInit(0.S(32.W))

  // Output Registers
  val valid_out_reg = RegInit(false.B)
  val out_col0_reg  = RegInit(0.S(32.W))
  val out_col1_reg  = RegInit(0.S(32.W))

  // --- Logic Implementation ---
  
  // Default: Valid is low (pulse behavior)
  valid_out_reg := false.B

  when(io.enable) {
    // Verilog: $signed({16'b0, in_col0})
    // This zero-extends the 16-bit input to 32-bit positive signed integer
    val val0 = io.in_col0.zext 
    val val1 = io.in_col1.zext 

    // Helper to calculate next value based on mode
    def nextVal(current: SInt, input: SInt): SInt = {
      Mux(io.accumulator_mode, current + input, input)
    }

    when(io.buffer_select === false.B) {
      // --- Buffer 0 Logic ---
      val next0 = nextVal(mem_buff0_col0, val0)
      val next1 = nextVal(mem_buff0_col1, val1)

      // Update Buffer
      mem_buff0_col0 := next0
      mem_buff0_col1 := next1

      // Update Output (Bypass Logic)
      if (bypassReadNew) {
        out_col0_reg := next0
        out_col1_reg := next1
      } else {
        out_col0_reg := mem_buff0_col0
        out_col1_reg := mem_buff0_col1
      }
    } .otherwise {
      // --- Buffer 1 Logic ---
      val next0 = nextVal(mem_buff1_col0, val0)
      val next1 = nextVal(mem_buff1_col1, val1)

      // Update Buffer
      mem_buff1_col0 := next0
      mem_buff1_col1 := next1

      // Update Output (Bypass Logic)
      if (bypassReadNew) {
        out_col0_reg := next0
        out_col1_reg := next1
      } else {
        out_col0_reg := mem_buff1_col0
        out_col1_reg := mem_buff1_col1
      }
    }
    
    // Assert Valid
    valid_out_reg := true.B
  }

  // --- Output Wiring ---
  io.valid_out := valid_out_reg
  io.out_col0  := out_col0_reg
  io.out_col1  := out_col1_reg
}
