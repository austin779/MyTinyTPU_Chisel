# Final Project:Implement tiny TPU using Chisel
This project will implement a 2x2 systolic array TPU.
## Introduction
Nowadays AI computing often use matrix operations that require high memory bandwidth to fetch parameters and input data from off-chip memory (typically DRAM),which means that Computer CPU must to execute load instruction to load element in Matrix before any arithmetic operation (Multiplication, Addition) can occur in the ALU .This leads to low arithmetic intensity and tremendous latency for dense matrix operations.
## 2.What is Tensor Processing Unit (TPU)
TPU was designed to handle complex machine learning processes that involve huge matrix operation .It only use one instruction to operate a matrix computeation instead of executing several ordered instruction to load, to compute ,and to write back . 
## 3. TPU vs. GPU: The Systolic Array
The key difference lies in how they handle data flow:

* GPU (Parallel Arithmetic): Utilizes massive parallelism to achieve a high number of Multiply and Accumulate (MAC) operations per cycle. However, it still relies heavily on accessing memory registers frequently.

* TPU (Systolic Array): Uses a Systolic Array architecture. Instead of reading from memory for every operation, input datas and weights respectively flow "DIAGNOSELY and vertically" through the array of PE
    * Benefit: 
        1. This reduces DRAM access because data is passed directly from one Processing Element (PE) to its neighbor, maximizing data reuse.
        2. higher throughput
        
## Matrix Multiply Unit
The core of the TPU is the Matrix Multiply Unit, composed of a grid of Processing Elements.

* Processing Element (PE): The basic unit that performs the multiplication and addition.

* Weight Stationary Data Flow: Each PE holds a specific weight value in a local register (stationary) for the duration of a matrix multiplication process. Inputs flow past these stationary weights.

* Data Skewing: Input data does not enter the array all at once. It is "skewed" (delayed by clock cycles) so that the correct input meets the correct weight at the exact right time as it flows through the array.

* Systolic Data Flow: Activations flow from left to right, while partial sums flow from top to bottom, accumulating results as they pass through the PEs.
```
MyTinyTPU/
├── src/
│ ├── main/
│ │ └── scala/
│ │ ├── accmulator_align.scala # Align accmulator logic
│ │ ├── accmulator_mem.scala #Accumulator memory module
│ │ ├── ActivationFunc.scala # Enable function module (e.g., ReLU)
│ │ ├── DualWeightFIFO.scala # Dual-weighted FIFO buffer
│ │ ├── mmu.scala # Matrix Multiply Unit (main module)
│ │ └── pe.scala # Processing Element
│ |
│ ├── test/
│ │ └── scala/
│ │ ├── test_accumulator_mem.scala # Test the accumulator memory module
│ │ ├── test_accumulator_align.scala # Test accumulator alignment logic
│ │ ├── test_ActivationFunc.scala # Test activation function module
│ │ ├── test_DualWeightFIFO.scala # Test FIFO buffer
│ │ ├── test_mmu.scala # Test matrix multiplication module
│ │ └── test_pe.scala # Test processing unit module
```
