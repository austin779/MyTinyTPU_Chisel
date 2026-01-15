import chisel3._
import chisel3.util._

class UnifiedBuffer(
  val WIDTH: Int = 8,
  val DEPTH: Int = 256
) extends Module {

  // Address width = clog2(DEPTH)
  val ADDR_W: Int = log2Ceil(DEPTH)

  val io = IO(new Bundle {
    // Write side
    val wr_valid = Input(Bool())
    val wr_data  = Input(UInt(WIDTH.W))
    val wr_ready = Output(Bool())

    // Read side
    val rd_ready = Input(Bool())
    val rd_valid = Output(Bool())
    val rd_data  = Output(UInt(WIDTH.W))

    // Status
    val full  = Output(Bool())
    val empty = Output(Bool())
    val count = Output(UInt((ADDR_W + 1).W))
  })

  // ------------------------------------------------------------
  // Storage
  // ------------------------------------------------------------
  val mem = Reg(Vec(DEPTH, UInt(WIDTH.W)))

  val wr_ptr = RegInit(0.U(ADDR_W.W))
  val rd_ptr = RegInit(0.U(ADDR_W.W))
  val count  = RegInit(0.U((ADDR_W + 1).W))

  io.count := count

  // ------------------------------------------------------------
  // Status signals
  // ------------------------------------------------------------
  io.full  := count === DEPTH.U
  io.empty := count === 0.U

  io.wr_ready := !io.full

  // ------------------------------------------------------------
  // Combinational read (zero latency)
  // ------------------------------------------------------------
  io.rd_data  := mem(rd_ptr)
  io.rd_valid := !io.empty && io.rd_ready

  // ------------------------------------------------------------
  // Read / write enables
  // ------------------------------------------------------------
  val doWrite = io.wr_valid && io.wr_ready
  val doRead  = io.rd_ready && !io.empty

  // ------------------------------------------------------------
  // Sequential logic
  // ------------------------------------------------------------
  when (reset.asBool) {
    wr_ptr := 0.U
    rd_ptr := 0.U
    count  := 0.U
  } .otherwise {

    // Write path
    when (doWrite) {
      mem(wr_ptr) := io.wr_data
      wr_ptr := wr_ptr + 1.U
    }

    // Read path (pointer advance only)
    when (doRead) {
      rd_ptr := rd_ptr + 1.U
    }

    // Count update
    when (doWrite && !doRead) {
      count := count + 1.U
    } .elsewhen (doRead && !doWrite) {
      count := count - 1.U
    }
    // simultaneous read/write → count unchanged
  }
}

