import chisel3._

class Normalizer extends Module {
  val io = IO(new Bundle {
    // Inputs
    val valid_in = Input(Bool())
    val data_in  = Input(SInt(32.W))
    val gain     = Input(SInt(16.W))
    val bias     = Input(SInt(32.W))
    val shift    = Input(UInt(5.W)) 

    // Outputs
    val valid_out = Output(Bool())
    val data_out  = Output(SInt(32.W))
  })

  // 1. Multiplication
  // SInt(32.W) * SInt(16.W) results in SInt(48.W)
  // Matches Verilog: logic signed [47:0] mult;
  val mult = io.data_in * io.gain

  // 2. Arithmetic Shift
  // The >> operator on SInt in Chisel performs an arithmetic shift (preserves sign).
  // Matches Verilog: mult >>> shift;
  val shifted = mult >> io.shift

  // 3. Sequential Logic (Registers)
  val valid_reg = RegInit(false.B)
  val data_reg  = RegInit(0.S(32.W))

  // Update Valid
  valid_reg := io.valid_in

  // Update Data
  // Verilog: data_out <= shifted[31:0] + bias;
  // logic: We extract the lower 32 bits of the shifted result, treat them as SInt, and add bias.
  // Note: We use .asSInt to ensure the bit extraction is treated as a signed number for the addition.
  data_reg := shifted(31, 0).asSInt + io.bias

  // 4. Output Assignments
  io.valid_out := valid_reg
  io.data_out  := data_reg
}

// Generate Verilog
import chisel3.stage.ChiselStage
object NormalizerMain extends App {
  (new ChiselStage).emitVerilog(new Normalizer(), Array("--target-dir", "generated"))
}
