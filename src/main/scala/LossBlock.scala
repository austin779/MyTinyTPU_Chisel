import chisel3._
import chisel3.util._

class LossBlock extends Module {
  val io = IO(new Bundle {
    val valid_in  = Input(Bool())
    val data_in   = Input(SInt(32.W))
    val target_in = Input(SInt(32.W))
    val valid_out = Output(Bool())
    val loss_out  = Output(SInt(32.W))
  })

  // Combinational logic
  val diff = io.data_in - io.target_in
  val abs_diff = Mux(diff < 0.S, -diff, diff)

  // Sequential logic
  when (reset.asBool) {
    io.valid_out := false.B
    io.loss_out  := 0.S
  } .otherwise {
    io.valid_out := io.valid_in
    io.loss_out  := abs_diff
  }
}

