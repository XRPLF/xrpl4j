# Smart Escrow WASM Functions

This directory contains WebAssembly (WASM) functions for testing XLS-0100 Smart Escrows functionality.

## Overview

These WASM functions are designed to be used with the Smart Escrow feature on the XRPL. They demonstrate various use cases and edge cases for programmable escrow release conditions.

## Prerequisites

- Rust toolchain (1.70+)
- `wasm32-unknown-unknown` target: `rustup target add wasm32-unknown-unknown`
- `wasm-opt` (optional, for optimization): Install via `binaryen` package

## Building

Build all WASM functions:

```bash
cargo build --release --target wasm32-unknown-unknown
```

The compiled WASM files will be in `target/wasm32-unknown-unknown/release/`.

## Optimizing (Optional)

For smaller WASM binaries, use `wasm-opt`:

```bash
wasm-opt -Oz -o optimized.wasm target/wasm32-unknown-unknown/release/smart_escrow_wasm_functions.wasm
```

## Available Functions

### 1. `always_succeed`
- **Purpose**: Always returns success (positive value)
- **Use Case**: Testing basic Smart Escrow execution
- **Return**: 1

### 2. `balance_check`
- **Purpose**: Checks if destination account has minimum balance
- **Use Case**: Conditional release based on account state
- **Logic**: Reads destination account balance from ledger, returns success if >= threshold

### 3. `time_window`
- **Purpose**: Only succeeds within a specific time window
- **Use Case**: Time-based conditional release
- **Logic**: Checks current ledger close time against configured window

### 4. `data_counter`
- **Purpose**: Stateful counter using escrow data - fails on first call, succeeds on second
- **Use Case**: Multi-attempt escrow release, demonstrates persistent state management
- **Logic**:
  - Reads a u32 counter from the escrow data field (4 bytes, little-endian)
  - First call (counter=0): Increments counter to 1, writes back to escrow data, returns 0 (tecWASM_REJECTED)
  - Second call (counter>=1): Returns 1 (tesSUCCESS), escrow released
- **Note**: This demonstrates true stateful behavior using the escrow data field, replacing the previous `always_fail` function

### 5. `oracle_price_check`
- **Purpose**: Checks oracle price feed before releasing
- **Use Case**: Price-conditional escrow release
- **Logic**: Reads oracle price, compares to threshold

### 6. `credential_check`
- **Purpose**: Verifies finisher has required credentials
- **Use Case**: Permission-based escrow release
- **Logic**: Checks if finisher account has specific credential

### 7. `gas_stress_test`
- **Purpose**: Consumes significant gas to test limits
- **Use Case**: Testing gas metering and limits
- **Logic**: Performs intensive computation

## Integration with Java Soak Test

The compiled WASM binaries are used by the Java soak test application in `xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/SmartEscrowSoakTest.java`.

The Java application:
1. Loads the WASM bytecode from the compiled `.wasm` files
2. Hex-encodes the bytecode
3. Creates `EscrowCreate` transactions with the `FinishFunction` field set to the hex-encoded WASM
4. Attempts to finish the escrows with `EscrowFinish` transactions
5. Monitors success/failure rates and gas consumption

## Notes

- **xrpl-wasm-stdlib**: This project depends on the XRPL WASM standard library, which provides functions for:
  - Reading ledger state (account balances, objects, etc.)
  - Reading escrow data
  - Writing to escrow data
  - Accessing transaction context (finisher account, etc.)
  - Reading oracle data
  - Checking credentials

- **Size Limits**: Smart Escrow functions must be under the `ExtensionSizeLimit` (configured via `SetFee`)

- **Gas Limits**: Execution must complete within the `ComputationAllowance` specified in `EscrowFinish`

## Development Workflow

1. Modify function implementations in `src/lib.rs`
2. Build: `cargo build --release --target wasm32-unknown-unknown`
3. Copy WASM files to Java test resources (or configure Java to read from Rust target directory)
4. Run Java soak test to validate behavior

## Future Enhancements

- Add more complex multi-signature scenarios
- Implement cross-chain bridge integration tests
- Add fuzzing for edge cases
- Implement benchmark suite for gas consumption analysis
