import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ActivationFuncTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Activation Function Unit"

  // 輔助函式：初始化
  def initSignals(dut: ActivationFunc): Unit = {
    dut.io.valid_in.poke(false.B)
    dut.io.data_in.poke(0.S)
  }

  // --- Test Case 1: ReLU Mode (Default) ---
  // 邏輯: y = max(0, x)
  it should "perform ReLU correctly (clamping negatives to 0)" in {
    // 在這裡指定參數為 ReLU
    test(new ActivationFunc(ActivationConfig.ReLU)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      println("--- Testing ReLU Mode ---")

      // Case A: Positive Input (Should pass)
      // Input: 10 -> Output: 10
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(10.S)
      dut.clock.step(1) // 等待 1 cycle (因為有 Reg)
      
      dut.io.valid_out.expect(true.B)
      dut.io.data_out.expect(10.S)

      // Case B: Negative Input (Should be 0)
      // Input: -5 -> Output: 0
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(-5.S)
      dut.clock.step(1)

      dut.io.valid_out.expect(true.B)
      dut.io.data_out.expect(0.S)

      // Case C: Zero Input (Should be 0)
      // Input: 0 -> Output: 0
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(0.S)
      dut.clock.step(1)

      dut.io.valid_out.expect(true.B)
      dut.io.data_out.expect(0.S)
      
      println("PASS: ReLU Logic Verified")
    }
  }

  // --- Test Case 2: ReLU6 Mode ---
  // 邏輯: y = min(6, max(0, x))
  it should "perform ReLU6 correctly (clamping to [0, 6])" in {
    // 在這裡指定參數為 ReLU6
    test(new ActivationFunc(ActivationConfig.ReLU6)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      println("--- Testing ReLU6 Mode ---")

      // Case A: Normal Range (0 < x < 6)
      // Input: 3 -> Output: 3
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(3.S)
      dut.clock.step(1)
      dut.io.data_out.expect(3.S)

      // Case B: Overflow (x > 6)
      // Input: 10 -> Output: 6
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(10.S)
      dut.clock.step(1)
      dut.io.data_out.expect(6.S)

      // Case C: Underflow (x < 0)
      // Input: -10 -> Output: 0
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(-10.S)
      dut.clock.step(1)
      dut.io.data_out.expect(0.S)

      // Case D: Boundary (x = 6)
      // Input: 6 -> Output: 6
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(6.S)
      dut.clock.step(1)
      dut.io.data_out.expect(6.S)

      println("PASS: ReLU6 Logic Verified")
    }
  }

  // --- Test Case 3: Passthrough Mode ---
  // 邏輯: y = x (直接通過)
  it should "perform Passthrough correctly (no modification)" in {
    // 在這裡指定參數為 Passthrough
    test(new ActivationFunc(ActivationConfig.Passthrough)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      initSignals(dut)
      dut.clock.step(1)

      println("--- Testing Passthrough Mode ---")

      // Case A: Positive
      // Input: 100 -> Output: 100
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(100.S)
      dut.clock.step(1)
      dut.io.data_out.expect(100.S)

      // Case B: Negative
      // Input: -50 -> Output: -50
      dut.io.valid_in.poke(true.B)
      dut.io.data_in.poke(-50.S)
      dut.clock.step(1)
      dut.io.data_out.expect(-50.S)

      println("PASS: Passthrough Logic Verified")
    }
  }
}
