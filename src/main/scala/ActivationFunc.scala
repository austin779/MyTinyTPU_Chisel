import chisel3._
import chisel3.util._


object ActivationCONSTANT {
  val Passthrough = 0 // y = x
  val ReLU        = 1
  val ReLU6       = 2
}

class ActivationFunc(val defaultAct: Int = ActivationCONSTANT.ReLU) extends Module {
  val io = IO(new Bundle {
    val valid_in  = Input(Bool())
    val data_in   = Input(SInt(32.W))
    val valid_out = Output(Bool())
    val data_out  = Output(SInt(32.W))
  })

  // DFF
  // init 0/false
  val valid_reg = RegInit(false.B)
  val data_reg  = RegInit(0.S(32.W))

//---------------------------------------

  // 1. Valid bit propagation (delay 1 Cycle)
  valid_reg := io.valid_in

  // 2. data process (The type of hardware to be generated is determined by the Scala parameters)
  // 這對應 Verilog 的 unique case (DEFAULT_ACT)
  defaultAct match {
    case ActivationCONSTANT.Passthrough => 
      // mode 00: Passthrough
      data_reg := io.data_in

    case ActivationCONSTANT.ReLU => 
      // mode 01: ReLU (Max(0, in))
      // 檢查是否小於 0 (符號位為 1)
      when(io.data_in < 0.S) {
        data_reg := 0.S
      } .otherwise {
        data_reg := io.data_in
      }

    case ActivationCONSTANT.ReLU6 => 
      // 模式 10: ReLU6 (Clamp to [0, 6])
      val const_six = 6.S(32.W)
      
      when(io.data_in < 0.S) {
        // 小於 0 變 0
        data_reg := 0.S
      } .elsewhen(io.data_in > const_six) {
        // 大於 6 變 6
        data_reg := const_six
      } .otherwise {
        // 中間值保持不變
        data_reg := io.data_in
      }

    case _ => 
      // Default: Passthrough
      data_reg := io.data_in
  }

  // --- 輸出連接 ---
  io.valid_out := valid_reg
  io.data_out  := data_reg
}



import chisel3.stage.ChiselStage
object ActivationFuncMain extends App {
  // 這裡可以修改傳入的參數來生成不同版本的硬體
  // 例如: new ActivationFunc(ActivationCONSTANT.ReLU6)
  (new ChiselStage).emitVerilog(
    new ActivationFunc(ActivationCONSTANT.ReLU), 
    Array("--target-dir", "generated")
  )
}
