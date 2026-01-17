import chisel3._

// --- Main Accumulator Module ---
class Accumulator extends Module {
  val io = IO(new Bundle {
    // Inputs
    val valid_in           = Input(Bool())
    val accumulator_enable = Input(Bool()) // 1: add, 0: overwrite
    val addr_sel           = Input(Bool()) // buffer selection

    // right now is Unsigned number, alterly choose SInt(16.W)
    val mmu_col0_in        = Input(UInt(16.W))  // MAC result from column 0 and column 1
    val mmu_col1_in        = Input(UInt(16.W)) 

    // Outputs
    val acc_col0_out       = Output(SInt(32.W)) //for 16bit accumulation, the largest additin need 17bit to store result, for the memory alignment , use 32bit
    val acc_col1_out       = Output(SInt(32.W))
    val valid_out          = Output(Bool()) // if accumulation finish, valid is 1 , othewise 0
  })

  // 1. Instantiate the Alignment Module
  val u_align = Module(new AccumulatorAlign) 

  // 2. Instantiate the Memory/Accumulation Module
  val u_mem = Module(new AccumulatorMem)

  // 3. Wiring: Inputs into u_align
  u_align.io.valid_in := io.valid_in
  u_align.io.raw_col0 := io.mmu_col0_in
  u_align.io.raw_col1 := io.mmu_col1_in

  // 4. Wiring: u_align into  u_mem
  // Note: clk and reset are implicitly connected.
  u_mem.io.enable           := u_align.io.aligned_valid // u_align tell mem to send data using valid bit
  u_mem.io.accumulator_mode := io.accumulator_enable 	// addition :1 , ovverwrite: 0
  u_mem.io.buffer_select    := io.addr_sel		
  u_mem.io.in_col0          := u_align.io.align_col0
  u_mem.io.in_col1          := u_align.io.align_col1

  // 5. Wiring: u_mem -> Outputs
  io.valid_out    := u_mem.io.valid_out
  io.acc_col0_out := u_mem.io.out_col0
  io.acc_col1_out := u_mem.io.out_col1
}
