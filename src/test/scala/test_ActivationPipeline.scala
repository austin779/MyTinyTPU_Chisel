import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ActivationPipelineSpec extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "Activation Pipeline"

  // Helper function to initialize signals to default state
  def initSignals(c: ActivationPipeline): Unit = {
    c.io.valid_in.poke(false.B)
    c.io.acc_in.poke(0.S)
    c.io.target_in.poke(0.S)
    
    // Default Configuration: Passthrough (Mode 0)
    // Matches Python: dut.activation_mode.value = MODE_PASS
    c.io.activation_mode.poke(0.U) 
    
    // Standard Normalization Config (1.0 gain, no bias)
    c.io.norm_gain.poke(256.S)   // 1.0 in Q8.8
    c.io.norm_bias.poke(0.S)
    c.io.norm_shift.poke(8.U)
    
    // Standard Quantization Config (1.0 scale, 0 zero point)
    c.io.q_inv_scale.poke(256.S) // 1.0 in Q8.8
    c.io.q_zero_point.poke(0.S)
    
    c.clock.step(1)
  }

  // 1. Test Reset
  it should "reset correctly" in {
    test(new ActivationPipeline).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      initSignals(c)
      
      c.clock.step(2)

      // Verify outputs are zero
      c.io.valid_out.expect(false.B)
      c.io.ub_data_out.expect(0.S)
      
      println("PASS: Activation pipeline reset test")
    }
  }

 // 2. Test Passthrough (Mode 0)
  it should "handle passthrough traffic correctly (Mode 0)" in {
    test(new ActivationPipeline).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      initSignals(c)

      // Configuration
      c.io.activation_mode.poke(0.U)

      // Send Input
      c.io.valid_in.poke(true.B)
      c.io.acc_in.poke(42.S)
      c.clock.step(1) // Cycle 1 (Input captured by Activation)

      c.io.valid_in.poke(false.B)
      
      // --- FIX: Change step(3) to step(2) ---
      // We already stepped 1. We need 2 more to complete the 3-cycle pipeline.
      // Cycle 2: Act -> Norm
      // Cycle 3: Norm -> Quant (Output becomes Valid HERE)
      c.clock.step(2) 

      // Verify
      c.io.valid_out.expect(true.B)
      c.io.ub_data_out.expect(42.S)
      
      println(f"Pipeline passthrough: input=42, output=${c.io.ub_data_out.peek().litValue}")
    }
  }

  // 3. Test ReLU Clipping (Mode 1)
  it should "clip negative values in ReLU mode (Mode 1)" in {
    test(new ActivationPipeline).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      initSignals(c)
      
      // --- Configuration ---
      c.io.activation_mode.poke(1.U) // Explicitly Set Mode 1 (ReLU)

      // --- Send Negative Input ---
      c.io.valid_in.poke(true.B)
      c.io.acc_in.poke(-50.S)
      c.clock.step(1)

      c.io.valid_in.poke(false.B)
      
      // --- Wait for Latency ---
      c.clock.step(2)

      // --- Verify ---
      c.io.valid_out.expect(true.B)
      c.io.ub_data_out.expect(0.S) // Should be clipped to 0
      
      println(f"Pipeline ReLU: input=-50, output=${c.io.ub_data_out.peek().litValue}")
    }
  }

  // 4. Test Quantization Saturation
  it should "saturate large values to int8 range" in {
    test(new ActivationPipeline).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      initSignals(c)
      
      // Mode doesn't strictly matter for +200, but let's keep it clean
      c.io.activation_mode.poke(1.U) 

      // --- Send Large Input ---
      // Input 200 > Max Int8 (127)
      c.io.valid_in.poke(true.B)
      c.io.acc_in.poke(200.S) 
      c.clock.step(1)

      c.io.valid_in.poke(false.B)
      
      // --- Wait for Latency ---
      c.clock.step(2)

      // --- Verify ---
      // Logic: Saturation clamps 200 to 127
      c.io.valid_out.expect(true.B)
      c.io.ub_data_out.expect(127.S)
      
      println(f"Pipeline saturation: input=200, output=${c.io.ub_data_out.peek().litValue}")
    }
  }
}
