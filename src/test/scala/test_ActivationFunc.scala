import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
// Import your main package if needed, e.g., import mytinytpu._

class ActivationFuncSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Runtime Configurable Activation Unit"

  // Helper to reset signals
  def initSignals(dut: ActivationFunc): Unit = {
    dut.io.valid_in.poke(false.B)
    dut.io.data_in.poke(0.S)
    dut.io.mode.poke(ActivationOp.Passthrough) // Default to Passthrough
    dut.clock.step(1)
  }

  // --- Test Case 1: ReLU Mode ---
  it should "perform ReLU correctly (clamping negatives to 0)" in {
    // FIX: Remove argument from constructor -> new ActivationFunc
    test(new ActivationFunc).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      
      // FIX: Select Mode via IO
      dut.io.mode.poke(ActivationOp.ReLU)
      println("--- Testing ReLU Mode ---")

      // Case A: Positive Input
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(10.S)
      dut.clock.step(1)
      dut.io.data_out.expect(10.S)

      // Case B: Negative Input
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(-5.S)
      dut.clock.step(1)
      dut.io.data_out.expect(0.S)
      
      println("PASS: ReLU Logic Verified")
    }
  }

  // --- Test Case 2: ReLU6 Mode ---
  it should "perform ReLU6 correctly (clamping to [0, 6])" in {
    // FIX: Remove argument
    test(new ActivationFunc).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      
      // FIX: Select Mode via IO
      dut.io.mode.poke(ActivationOp.ReLU6)
      println("--- Testing ReLU6 Mode ---")

      // Case A: Normal Range (3 -> 3)
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(3.S)
      dut.clock.step(1)
      dut.io.data_out.expect(3.S)

      // Case B: Overflow (10 -> 6)
      dut.io.data_in.poke(10.S)
      dut.clock.step(1)
      dut.io.data_out.expect(6.S)

      println("PASS: ReLU6 Logic Verified")
    }
  }

  // --- Test Case 3: Passthrough Mode ---
  it should "perform Passthrough correctly (no modification)" in {
    // FIX: Remove argument
    test(new ActivationFunc).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)

      // FIX: Select Mode via IO
      dut.io.mode.poke(ActivationOp.Passthrough)
      println("--- Testing Passthrough Mode ---")

      // Case A: Negative should stay Negative
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(-50.S)
      dut.clock.step(1)
      dut.io.data_out.expect(-50.S)

      println("PASS: Passthrough Logic Verified")
    }
  }
}
