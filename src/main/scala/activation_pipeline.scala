import chisel3._
import chisel3.util._

class ActivationPipeline extends Module {
  val io = IO(new Bundle {
    val valid_in    = Input(Bool())
    val acc_in      = Input(SInt(32.W))    // Input From Accumulator
    val target_in   = Input(SInt(32.W))    // Optional target for loss

    val activation_mode = Input(UInt(2.W)) // select mode :0 for pass ,1 for ReLU, 2 for ReLU6 

    val norm_gain   = Input(SInt(16.W))
    val norm_bias   = Input(SInt(32.W))
    val norm_shift  = Input(UInt(5.W))

    val q_inv_scale = Input(SInt(16.W))    // 1/S in Q8.8
    val q_zero_point= Input(SInt(8.W))     // Signed 8-bit

    // --- Outputs ---
    val valid_out   = Output(Bool())
    val ub_data_out = Output(SInt(8.W))    // To Unified Buffer
    
    val loss_valid  = Output(Bool())
    val loss_out    = Output(SInt(32.W))
  })

  // ============================================================
  // Stage 1: Activation
  // ============================================================
  val u_act = Module(new ActivationFunc)
  
  u_act.io.valid_in := io.valid_in
  u_act.io.data_in  := io.acc_in

  u_act.io.mode     := io.activation_mode.asTypeOf(ActivationOp())
  
  val s1_valid = u_act.io.valid_out
  val s1_data  = u_act.io.data_out

  // ============================================================
  // Target Alignment Logic
  // ============================================================
  // Matches Verilog: target_d1 <= target_in if valid_in is high
  val target_d1 = RegInit(0.S(32.W))
  
  when(io.valid_in) {
    target_d1 := io.target_in
  }

  // ============================================================
  // Stage 2: Normalization
  // ============================================================
  val u_norm = Module(new Normalizer)

  u_norm.io.valid_in := s1_valid
  u_norm.io.data_in  := s1_data
  u_norm.io.gain     := io.norm_gain
  u_norm.io.bias     := io.norm_bias
  u_norm.io.shift    := io.norm_shift

  val s2_valid = u_norm.io.valid_out
  val s2_data  = u_norm.io.data_out

  // ============================================================
  // Stage 3a: Loss (Parallel computation)
  // ============================================================
  val u_loss = Module(new LossBlock)

  u_loss.io.valid_in  := s2_valid
  u_loss.io.data_in   := s2_data
  u_loss.io.target_in := target_d1 // Connects to the aligned target register

  io.loss_valid := u_loss.io.valid_out
  io.loss_out   := u_loss.io.loss_out

  // ============================================================
  // Stage 3b: Quantization (Inlined)
  // Formula: x_q = clip( round(x * (1/S)) + Z , -128, 127 )
  // ============================================================
  
  // 1. Multiplication (32-bit * 16-bit -> 48-bit)
  val mult = s2_data * io.q_inv_scale

  // 2. Rounding (Add 0.5 * 2^8 => 128)
  val mult_rounded = mult + 128.S

  // 3. Scaling (Shift right by 8 to return to Q0)
  // We keep it as 32-bit width for the subsequent addition
  val scaled = (mult_rounded >> 8).asSInt 

  // 4. Zero Point Addition
  // Chisel handles width extension automatically, but we ensure q_zero_point is treated as signed
  val biased = scaled + io.q_zero_point

  // 5. Saturation Logic (Clamp to int8 range: -128 to 127)
  val sat_max = 127.S
  val sat_min = -128.S
  
  // Logic: if > 127 then 127, else if < -128 then -128, else lower 8 bits
  val saturated = Mux(biased > sat_max, sat_max, 
                  Mux(biased < sat_min, sat_min, biased(7,0).asSInt))

  // 6. Output Register
  val valid_reg = RegInit(false.B)
  val ub_q_reg  = RegInit(0.S(8.W))

  // Note: The verilog uses 'if (reset)... else' logic implicitly handled by RegInit
  // We update registers on every cycle, but data only changes meaningfully when valid is tracked?
  // Your Verilog updates ub_q_reg *every* cycle based on s2_valid logic.
  valid_reg := s2_valid
  ub_q_reg  := saturated

  // Output Assignments
  io.valid_out   := valid_reg
  io.ub_data_out := ub_q_reg
}
