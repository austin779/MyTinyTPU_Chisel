# Final Project:Implement tiny TPU using Chisel

## Introduction
Nowadays AI computing often use matrix operations that require high memory bandwidth to fetch parameters and input data from off-chip memory (typically DRAM),which means that Computer CPU must to execute load instruction to load element in Matrix before any arithmetic operation (Multiplication, Addition) can occur in the ALU .This leads to low arithmetic intensity and tremendous latency for dense matrix operations.
