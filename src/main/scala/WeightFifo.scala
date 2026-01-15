import chisel3._
import chisel3.util._

class WeightFifo extends Module {
  val io = IO(new Bundle {
    val push     = Input(Bool())     // From DDR controller
    val pop      = Input(Bool())     // From MMU controller
    val data_in  = Input(UInt(8.W))
    val data_out = Output(UInt(8.W))
  })

  // Tiny depth FIFO (4 entries)
  val buffer = Reg(Vec(4, UInt(8.W)))
  val wr_ptr = RegInit(0.U(2.W))
  val rd_ptr = RegInit(0.U(2.W))
  val dataOutReg = RegInit(0.U(8.W))

  io.data_out := dataOutReg

  when (reset.asBool) {
    wr_ptr     := 0.U
    rd_ptr     := 0.U
    dataOutReg := 0.U
  } .otherwise {

    // Write (Push)
    when (io.push) {
      buffer(wr_ptr) := io.data_in
      wr_ptr := wr_ptr + 1.U
    }

    // Read (Pop)
    when (io.pop) {
      dataOutReg := buffer(rd_ptr)
      rd_ptr := rd_ptr + 1.U
    }
  }
}


/*
X No protection against overflow/underflow (by design)
X  No valid/ready signals (matches your original)
*/
