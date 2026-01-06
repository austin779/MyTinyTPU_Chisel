import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage
//MMU: 2x2 PE Array

class MMU(val rows: Int = 2, val cols: Int = 2) extends Module {
  val io = IO(new Bundle {
    val en_weight_pass = Input(Bool())
    val en_capture_cols = Input(Vec(cols, Bool()))
    val row_in = Input(Vec(rows, UInt(8.W)))
    val col_in = Input(Vec(cols, UInt(8.W)))
    val acc_out = Output(Vec(cols, UInt(16.W)))
  })

  // 1. 2D PE array (rows x cols)
  // pe00, pe01
  // pe10, pe11
  val pes = Seq.fill(rows, cols)(Module(new PE))

  // 2. for loop iteration for wire connection
  for (r <- 0 until rows) {
    for (c <- 0 until cols) { 
      // --- pass through weight signal connction ---
      pes(r)(c).io.en_weight_pass := io.en_weight_pass
      
      // Column Weight Capture
      // pes[0][0].en_weight_capture(en_capture_col0)
      // pes[0][1].en_weight_capture(en_capture_col1)
      pes(r)(c).io.en_weight_capture := io.en_capture_cols(c)

      // Activations horizontal flow
      // pe00 -> pe01 ,flow from left to right
      if (c == 0) {
        pes(r)(c).io.in_act := io.row_in(r)
      } else {
      	// connect pe01 act wire to pe00, and pe11 act wire to pe10
        pes(r)(c).io.in_act := pes(r)(c - 1).io.out_act
      }

      // --- Vertical flow of Partial Sums / Weights
      // pe00 -> pe10 (flow from top to bottom)
      if (r == 0) {
        // zero extension to 16-bit
        pes(r)(c).io.in_psum := Cat(0.U(8.W), io.col_in(c))
      } else {
      	// partial sum propagate vertically 
        pes(r)(c).io.in_psum := pes(r - 1)(c).io.out_psum
      }
    }
  }

  // 3. Output Connection
 // Connect index 'c' of acc_out to the PE at the last row, column 'c'
for (c <- 0 until cols) {
  io.acc_out(c) := pes(rows - 1)(c).io.out_psum
}
}



object MMUMain extends App {
  println("Generating Verilog for MMU...")
  // 這裡生成 2x2 的 MMU
  (new ChiselStage).emitVerilog(new MMU(rows = 2, cols = 2), Array("--target-dir", "generated"))
}
