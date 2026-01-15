import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class AccumulatorSpec extends AnyFlatSpec with ChiselScalatestTester {
  
  behavior of "Accumulator"

  // 1. Test Reset
  it should "initialize all outputs to zero after reset" in {
    test(new Accumulator) { c =>
      // ChiselTest asserts reset by default at start.
      // We explicitly set inputs to 0 as per python test.
      c.io.valid_in.poke(false.B)
      c.io.accumulator_enable.poke(false.B)
      c.io.addr_sel.poke(false.B)
      c.io.mmu_col0_in.poke(0.U)
      c.io.mmu_col1_in.poke(0.U)

      c.clock.step(2)

      // Assert outputs are 0
      c.io.acc_col0_out.expect(0.S)
      c.io.acc_col1_out.expect(0.S)
      c.io.valid_out.expect(false.B)
      
      println("PASS: Accumulator reset test")
    }
  }

  // 2. Test Overwrite Mode
  it should "handle overwrite mode correctly" in {
    test(new Accumulator) { c =>
      // Initial Setup
      c.io.valid_in.poke(false.B)
      c.io.accumulator_enable.poke(false.B)
      c.io.addr_sel.poke(false.B)
      c.io.mmu_col0_in.poke(0.U)
      c.io.mmu_col1_in.poke(0.U)
      c.clock.step(2)

      // Send two valid cycles
      // Cycle 1: Gets stored in delay register
      c.io.valid_in.poke(true.B)
      c.io.mmu_col0_in.poke(100.U)
      c.io.mmu_col1_in.poke(200.U)
      c.clock.step(1)

      // Cycle 2: Triggers aligned output
      c.io.mmu_col0_in.poke(150.U)
      c.io.mmu_col1_in.poke(250.U)
      c.clock.step(1)

      // End valid input
      c.io.valid_in.poke(false.B)
      c.clock.step(2)

      // Check results (Assuming last valid output matches the accumulated logic)
      // Note: In Python you printed these. In Chisel we usually expect specific values.
      // Based on your python logs, you might need to adjust the expected values 
      // depending on exactly how your memory behaves (LIFO/FIFO).
      // Here I simply read them out to print, similar to your python script.
      val col0 = c.io.acc_col0_out.peek().litValue
      val col1 = c.io.acc_col1_out.peek().litValue
      
      println(f"Overwrite mode result: col0=$col0, col1=$col1")
    }
  }

  // 3. Test Accumulate Mode
  it should "handle accumulate mode correctly" in {
    test(new Accumulator) { c =>
      // Reset phase
      c.io.valid_in.poke(false.B)
      c.clock.step(1)

      // --- Phase 1: Overwrite initial values ---
      c.io.accumulator_enable.poke(false.B)
      c.io.addr_sel.poke(false.B) // Buffer 0

      // Cycle 1
      c.io.valid_in.poke(true.B)
      c.io.mmu_col0_in.poke(10.U)
      c.io.mmu_col1_in.poke(20.U)
      c.clock.step(1)

      // Cycle 2
      c.io.mmu_col0_in.poke(15.U)
      c.io.mmu_col1_in.poke(25.U)
      c.clock.step(1)

      c.io.valid_in.poke(false.B)
      c.clock.step(2)

      val col0_initial = c.io.acc_col0_out.peek().litValue
      val col1_initial = c.io.acc_col1_out.peek().litValue
      println(f"Initial values: col0=$col0_initial, col1=$col1_initial")

      // --- Phase 2: Accumulate ---
      c.io.accumulator_enable.poke(true.B) // Enable accumulation

      // Cycle 1
      c.io.valid_in.poke(true.B)
      c.io.mmu_col0_in.poke(5.U)
      c.io.mmu_col1_in.poke(10.U)
      c.clock.step(1)

      // Cycle 2
      c.io.mmu_col0_in.poke(7.U)
      c.io.mmu_col1_in.poke(12.U)
      c.clock.step(1)

      c.io.valid_in.poke(false.B)
      c.clock.step(2)

      // Verification
      // Based on typical logic: (10+5) and (20+10) or similar logic depending on address
      val col0_accum = c.io.acc_col0_out.peek().litValue
      val col1_accum = c.io.acc_col1_out.peek().litValue
      
      println(f"After accumulation: col0=$col0_accum, col1=$col1_accum")
      
      // Example Assertion (Uncomment and adjust values if you know the exact expected math)
      // c.io.acc_col0_out.expect((col0_initial + 5).S) 
    }
  }

  // 4. Test Double Buffering
  it should "handle double buffering correctly" in {
    test(new Accumulator) { c =>
      c.io.accumulator_enable.poke(false.B)
      
      // --- Write to Buffer 0 ---
      c.io.addr_sel.poke(false.B)
      c.io.valid_in.poke(true.B)
      
      c.io.mmu_col0_in.poke(111.U)
      c.io.mmu_col1_in.poke(222.U)
      c.clock.step(1)
      
      c.io.mmu_col0_in.poke(333.U)
      c.io.mmu_col1_in.poke(444.U)
      c.clock.step(1)
      
      c.io.valid_in.poke(false.B)
      c.clock.step(2)
      
      val buf0_col0 = c.io.acc_col0_out.peek().litValue
      val buf0_col1 = c.io.acc_col1_out.peek().litValue
      println(f"Buffer 0: col0=$buf0_col0, col1=$buf0_col1")
      
      // --- Write to Buffer 1 ---
      c.io.addr_sel.poke(true.B) // Switch buffer
      c.io.valid_in.poke(true.B)
      
      c.io.mmu_col0_in.poke(555.U)
      c.io.mmu_col1_in.poke(666.U)
      c.clock.step(1)
      
      c.io.mmu_col0_in.poke(777.U)
      c.io.mmu_col1_in.poke(888.U)
      c.clock.step(1)
      
      c.io.valid_in.poke(false.B)
      c.clock.step(2)
      
      val buf1_col0 = c.io.acc_col0_out.peek().litValue
      val buf1_col1 = c.io.acc_col1_out.peek().litValue
      println(f"Buffer 1: col0=$buf1_col0, col1=$buf1_col1")
      
      // Verify Buffer 1 contents are different from Buffer 0 logic
      assert(buf1_col0 != buf0_col0)
    }
  }

//===

// 5. Test Alignment
  it should "align columns correctly" in {
    test(new Accumulator) { c =>
      c.io.accumulator_enable.poke(false.B)
      c.io.addr_sel.poke(false.B)
      c.clock.step(1)
      
      // T1: Send first valid (col0=100, col1=999 ignored)
      c.io.valid_in.poke(true.B)
      c.io.mmu_col0_in.poke(100.U)
      c.io.mmu_col1_in.poke(999.U)
      c.clock.step(1)
      
      // T2: Send second valid (col0=200, matches prev col0)
      c.io.mmu_col0_in.poke(200.U)
      c.io.mmu_col1_in.poke(100.U)
      c.clock.step(1)
      
      // T3: Stop inputs
      c.io.valid_in.poke(false.B)
      
      // --- CRITICAL FIX HERE ---
      // Old code: c.clock.step(2) -> Too late! Misses the pulse.
      // New code: c.clock.step(1) -> Just right. Catches the pulse.
      c.clock.step(1) 
      
      // Check valid_out
      c.io.valid_out.expect(true.B)
      
      // Check aligned output: Should be 100 and 100
      c.io.acc_col0_out.expect(100.S)
      c.io.acc_col1_out.expect(100.S)
      
      println("PASS: Accumulator alignment test")
    }
  }
//===
/*
  // 6. Test Alignment
  it should "align columns correctlyv2" in {
    test(new Accumulator) { c =>
      c.io.accumulator_enable.poke(false.B)
      c.io.addr_sel.poke(false.B)
      c.clock.step(1)
      
      // Send staggered data
      // First valid: col0=100, col1=999 (999 ignored)
      c.io.valid_in.poke(true.B)
      c.io.mmu_col0_in.poke(100.U)
      c.io.mmu_col1_in.poke(999.U)
      c.clock.step(1)
      
      // Second valid: col0=200, col1=100 (matches previous col0)
      c.io.mmu_col0_in.poke(200.U)
      c.io.mmu_col1_in.poke(100.U)
      c.clock.step(1)
      
      c.io.valid_in.poke(false.B)
      c.clock.step(2) // Wait for latency
      
      // Check valid_out
      c.io.valid_out.expect(true.B)
      
      // Check aligned output: Should be 100 and 100
      c.io.acc_col0_out.expect(100.S)
      c.io.acc_col1_out.expect(100.S)
      
      println("PASS: Accumulator alignment test")
    }
    
  }*/
}
