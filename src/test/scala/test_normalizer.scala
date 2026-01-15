import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class NormalizerSpec extends AnyFlatSpec with ChiselScalatestTester {
  
  behavior of "Normalizer"

  // Helper function to reset signals to a known state
  def initSignals(c: Normalizer): Unit = {
    c.io.valid_in.poke(false.B)
    c.io.data_in.poke(0.S)
    c.io.gain.poke(0.S)
    c.io.bias.poke(0.S)
    c.io.shift.poke(0.U)
    c.clock.step(1)
  }

  // --- Test Case 1: Passthrough Mode ---
  // Python: gain=256 (1.0 in Q8), shift=8, bias=0
  it should "perform passthrough correctly (gain=1.0, bias=0)" in {
    test(new Normalizer).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      initSignals(c)

      // Set Inputs
      c.io.valid_in.poke(true.B)
      c.io.data_in.poke(100.S)
      c.io.gain.poke(256.S)   // 1.0 * 2^8
      c.io.bias.poke(0.S)
      c.io.shift.poke(8.U)

      // Step Clock (Module has 1 cycle latency due to output registers)
      c.clock.step(1) 
      
      // Verify
      // Logic: (100 * 256) >> 8 + 0 = 100
      c.io.valid_out.expect(true.B)
      c.io.data_out.expect(100.S)
      
      println(f"Normalizer passthrough: input=100, output=${c.io.data_out.peek().litValue}")
    }
  }

  // --- Test Case 2: Bias Mode ---
  // Python: gain=256, shift=8, bias=50
  it should "apply bias correctly" in {
    test(new Normalizer).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      initSignals(c)

      // Set Inputs
      c.io.valid_in.poke(true.B)
      c.io.data_in.poke(100.S)
      c.io.gain.poke(256.S)
      c.io.bias.poke(50.S)
      c.io.shift.poke(8.U)

      // Step Clock
      c.clock.step(1)

      // Verify
      // Logic: (100 * 256) >> 8 + 50 = 100 + 50 = 150
      c.io.data_out.expect(150.S)
      
      println(f"Normalizer with bias: input=100, bias=50, output=${c.io.data_out.peek().litValue}")
    }
  }

  // --- Test Case 3: Scaling Mode ---
  // Python: gain=512 (2.0 in Q8), shift=8, bias=0
  it should "scale data correctly (gain=2.0)" in {
    test(new Normalizer).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      initSignals(c)

      // Set Inputs
      c.io.valid_in.poke(true.B)
      c.io.data_in.poke(50.S)
      c.io.gain.poke(512.S)   // 2.0 * 2^8
      c.io.bias.poke(0.S)
      c.io.shift.poke(8.U)

      // Step Clock
      c.clock.step(1)

      // Verify
      // Logic: (50 * 512) >> 8 = (25600 >> 8) = 100
      c.io.data_out.expect(100.S)

      println(f"Normalizer scaling: input=50, gain=2.0, output=${c.io.data_out.peek().litValue}")
    }
  }
}
