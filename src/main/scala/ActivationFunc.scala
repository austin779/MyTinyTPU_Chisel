import chisel3._
import chisel3.util._

// Define the Enum for the modes
object ActivationOp extends ChiselEnum {
  val Passthrough, ReLU, ReLU6 = Value
}

// Define the Hardware Module
class ActivationFunc extends Module {
  val io = IO(new Bundle {
    val valid_in  = Input(Bool())
    val data_in   = Input(SInt(32.W))
    val mode      = Input(ActivationOp()) // Runtime input to switch modes
    
    val valid_out = Output(Bool())
    val data_out  = Output(SInt(32.W))
  })

  // Pipeline Registers
  val valid_reg = RegInit(false.B)
  val data_reg  = RegInit(0.S(32.W))

  // Wire for logic calculation
  val processed_data = Wire(SInt(32.W))

  // Default
  processed_data := io.data_in

  // Logic Switching
  switch(io.mode) {
    is(ActivationOp.Passthrough) {
      processed_data := io.data_in
    }
    is(ActivationOp.ReLU) {
      processed_data := Mux(io.data_in > 0.S, io.data_in, 0.S)
    }
    is(ActivationOp.ReLU6) {
      val relu = Mux(io.data_in > 0.S, io.data_in, 0.S)
      processed_data := Mux(relu > 6.S, 6.S, relu)
    }
  }

  // Updates
  valid_reg := io.valid_in
  data_reg  := processed_data

  io.valid_out := valid_reg
  io.data_out  := data_reg
}
