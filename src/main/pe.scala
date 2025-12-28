import chisel3._
import chisel3.util._

class PE extends Module {
  val io = IO(new Bundle {
    val en_weight_pass    = Input(Bool())
    val en_weight_capture = Input(Bool())
    val in_act            = Input(UInt(8.W))
    val in_psum           = Input(UInt(16.W))
    val out_act           = Output(UInt(8.W))
    val out_psum          = Output(UInt(16.W))
  })

  // DFF in PE
  val weight_reg   = RegInit(0.U(8.W))
  val out_act_reg  = RegInit(0.U(8.W))
  val out_psum_reg = RegInit(0.U(16.W))


  when(io.en_weight_pass) {
    // Weight loading mode
    // Pass psum through, and reset activation value
    out_psum_reg := io.in_psum
    out_act_reg  := 0.U

    // Capture weight logic
    when(io.en_weight_capture) {
      // 對應 Verilog: weight <= in_psum[7:0];
      weight_reg := io.in_psum(7, 0)
    }
  } .otherwise { 
    // main task
    // Computation of Multiply and add   
    out_act_reg := io.in_act
    
    // MAC: (in_act * weight) + in_psum
    // Chisel 會自動推斷乘法後的位寬，這裡我們讓它自然運算後賦值給 16-bit 暫存器
    out_psum_reg := (io.in_act * weight_reg) + io.in_psum
  }

  // DFF output 
  io.out_act  := out_act_reg
  io.out_psum := out_psum_reg
}

// 產生 Verilog 的物件 (用於測試生成)
object PEMain extends App {
  emitVerilog(new PE(), Array("--target-dir", "generated"))
}
