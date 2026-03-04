# Smart Escrow Soak Test

This document describes the Smart Escrow soak test harness for XLS-0100 testing.

## Overview

The Smart Escrow soak test is a comprehensive, long-running test designed to validate the Smart Escrow feature under continuous load. It consists of two main components:

1. **Rust WASM Functions** (`smart-escrow-wasm-functions/`) - WebAssembly functions that implement various escrow release conditions
2. **Java Soak Test** (`SmartEscrowSoakTest.java`) - Multi-threaded Java application that continuously creates and finishes Smart Escrows

## Prerequisites

### For WASM Functions
- Rust toolchain (1.70+)
- `wasm32-unknown-unknown` target
- Optional: `wasm-opt` from binaryen for optimization

### For Java Soak Test
- Java 8+
- Maven 3.6+
- Access to an XRPL network with SmartEscrow amendment enabled (local rippled, testnet, or devnet)

## Building WASM Functions

1. Navigate to the WASM functions directory:
```bash
cd smart-escrow-wasm-functions
```

2. Build the WASM module:
```bash
./build.sh
```

This will:
- Compile the Rust code to WebAssembly
- Optionally optimize with `wasm-opt`
- Copy the WASM files to the Java test resources directory

## Running the Soak Test

### Quick Start

Run the soak test with default configuration (1 hour, 10 threads):

```bash
mvn test -Dtest=SmartEscrowSoakTest -pl xrpl4j-integration-tests
```

### Configuration

The soak test can be configured by modifying constants in `SmartEscrowSoakTest.java`:

- `NUM_WORKER_THREADS` - Number of concurrent worker threads (default: 10)
- `FAUCET_INITIAL_BALANCE_DROPS` - Initial XRP balance for faucet account (default: 100 XRP)
- `ESCROW_AMOUNT_DROPS` - Amount per escrow (default: 0.01 XRP)
- `TEST_DURATION_MINUTES` - How long to run the test (default: 60 minutes)
- `METRICS_REPORT_INTERVAL_SECONDS` - How often to report metrics (default: 30 seconds)

### Environment Variables

Set the XRPL network to test against:

```bash
# Local rippled
export XRPL_ENVIRONMENT=LOCAL

# Testnet
export XRPL_ENVIRONMENT=TESTNET

# Devnet
export XRPL_ENVIRONMENT=DEVNET
```

## Test Workflow

The soak test follows this workflow:

1. **Initialization**
   - Creates and funds a faucet account with initial XRP balance
   - Loads WASM bytecode from resources (or uses minimal stub if not found)
   - Starts metrics reporter thread
   - Spawns worker threads

2. **Worker Thread Loop** (each thread independently):
   - Creates a new destination account
   - Selects a random WASM function to test
   - Creates a Smart Escrow with:
     - WASM function as `FinishFunction`
     - Function-specific data in `Data` field
     - 10-minute `CancelAfter` timeout
   - Waits 5 seconds for escrow to be in ledger
   - Attempts to finish the escrow with appropriate `ComputationAllowance`
   - Records success/failure and gas consumption
   - Repeats

3. **Metrics Collection**
   - Escrows created
   - Escrows finished successfully
   - Escrows finished with failure
   - Escrows canceled
   - Total errors
   - Total gas consumed
   - Success rate per function type
   - Average gas per successful finish

4. **Shutdown**
   - After configured duration, stops all workers
   - Reports final metrics
   - Exits

## WASM Functions Tested

The soak test exercises 8 different WASM function types:

1. **always_succeed** - Always returns success (1)
2. **always_fail** - Always returns failure (0)
3. **balance_check** - Checks destination account balance
4. **time_window** - Only succeeds within time window
5. **data_counter** - Increments counter, succeeds after N attempts
6. **oracle_price_check** - Checks oracle price feed
7. **credential_check** - Verifies finisher has credentials
8. **gas_stress_test** - Intensive computation to test gas limits

## Metrics Output

Example metrics output:

```
=== METRICS ===
Escrows Created: 1250
Escrows Finished (Success): 625
Escrows Finished (Failed): 600
Escrows Canceled: 0
Errors: 25
Total Gas Used: 62500000
Success Rate: 50.00%
Average Gas per Success: 100000
Function Success Counts:
  always_succeed: 156
  balance_check: 78
  time_window: 79
  data_counter: 77
  oracle_price_check: 80
  credential_check: 75
  gas_stress_test: 80
Function Failure Counts:
  always_fail: 150
  balance_check: 75
  ...
```

## Troubleshooting

### WASM File Not Found
If the WASM file is not found in resources, the test will use a minimal stub that only exports `always_succeed`. To use full functionality, build the WASM functions first.

### Amendment Not Enabled
If the SmartEscrow amendment is not enabled on the target network, transactions will fail with `temDISABLED` or similar error codes.

### Insufficient Faucet Balance
If the faucet runs out of XRP, increase `FAUCET_INITIAL_BALANCE_DROPS` or reduce `ESCROW_AMOUNT_DROPS`.

### High Error Rate
- Check network connectivity
- Verify amendment is enabled
- Check rippled logs for transaction failures
- Reduce number of worker threads if overwhelming the network

## Future Enhancements

- [ ] Add support for custom WASM function parameters
- [ ] Implement escrow cancellation testing
- [ ] Add support for multi-signature escrows
- [ ] Implement gas consumption benchmarking
- [ ] Add Prometheus metrics export
- [ ] Create dashboard for real-time monitoring
- [ ] Add support for testing against multiple networks simultaneously
- [ ] Implement fuzzing for edge cases

## References

- [XLS-0100 Smart Escrows Specification](https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0100-smart-escrows)
- [XRPL WASM Standard Library](https://github.com/ripple/xrpl-wasm-stdlib)
- [WebAssembly Specification](https://webassembly.github.io/spec/)

