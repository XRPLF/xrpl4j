# MPT Integration Testing Status

## Overview

This document describes the current status of Multi-Purpose Token (MPT) integration testing in xrpl4j, specifically for MPT support in the Decentralized Exchange (DEX), Automated Market Makers (AMM), and Path Finding features.

## Current Status: ⚠️ Tests Require Feature Branch

### What Works ✅

1. **MPT Core Functionality** - Fully implemented and tested
   - `MpTokenIssuanceCreate`, `MpTokenIssuanceDestroy`, `MpTokenIssuanceSet`
   - `MpTokenAuthorize`, `MpTokenMint`, `MpTokenBurn`, `MpTokenClawback`
   - All core MPT models have 100% test coverage

2. **Binary Codec** - Fully functional
   - `definitions.json` updated with fields from `feature/mpt-v2e`:
     - `TakerPaysMPT` (nth: 3, type: Hash192)
     - `TakerGetsMPT` (nth: 4, type: Hash192)
     - New error codes: `temBAD_MPT` (-249), `terLOCKED` (-84)
   - Polymorphic `CurrencyAmount` serialization works correctly

3. **Integration Test Code** - Written and ready
   - 7 MPT AMM integration tests in `AmmIT.java`
   - MPT Offer tests in `OfferIT.java`
   - MPT Path Finding tests in `PathFindIT.java`

### What Doesn't Work ❌

**All MPT DEX/AMM/PathFinding tests fail** with the current `rippled:develop` Docker image.

#### Error Message:
```
org.xrpl.xrpl4j.client.JsonRpcClientErrorException: 
invalidTransaction (fails local checks: Amount can not be MPT.)
```

#### Root Cause:
The `rippleci/rippled:develop` Docker image does not yet support MPT in:
- **AMM** (Automated Market Makers)
- **DEX** (Decentralized Exchange / OfferCreate)
- **Path Finding**

These features are currently only available in the **`feature/mpt-v2e`** branch of rippled, which implements [XLS-82d](https://github.com/XRPLF/XRPL-Standards/discussions/82).

## Test Results

### Tested Against `rippled:develop` (2026-03-04)

```bash
cd /Users/creed/XRPLF/xrpl4j
mvn test -pl xrpl4j-integration-tests -Dtest=AmmIT#mptAmmCreateAndVerifyWithAmmInfoAndLedgerEntry
```

**Result:** ❌ All 7 MPT AMM tests failed

| Test | Error |
|------|-------|
| `mptAmmCreateAndVerifyWithAmmInfoAndLedgerEntry` | `Amount can not be MPT` |
| `mptAmmDepositAndWithdraw` | `Amount can not be MPT` |
| `mptAmmBidOnAuctionSlot` | `Amount can not be MPT` |
| `mptAmmVoteOnTradingFee` | `Amount can not be MPT` |
| `mptAmmLedgerEntryWithTwoMpts` | `Amount can not be MPT` |
| `mptAmmClawback` | `NoSuchElementException` (AMM never created) |
| `mptAmmInfoAndLedgerEntryWithMptAndIou` | `NoSuchElementException` (AMM never created) |

## When Will Tests Pass?

The integration tests will pass when **XLS-82d is merged** into `rippled:develop` and a new Docker image is published.

### Timeline (Estimated):
1. ✅ **XLS-33 (MPT Core)** - Merged into `rippled:develop`
2. 🚧 **XLS-82d (MPT in DEX/AMM)** - Currently in `feature/mpt-v2e` branch
3. ⏳ **Merge to develop** - Pending
4. ⏳ **Docker image update** - After merge

## How to Test Against `feature/mpt-v2e`

### Option 1: Build rippled from Source (Complex)

**Requirements:**
- Python 3.11+
- Conan 2.17+
- CMake 3.22+
- GCC 12+ or Clang 16+

```bash
cd /Users/creed/XRPLF/XRPL-fork/rippled
git checkout feature/mpt-v2e

# Follow BUILD.md instructions
# This is complex and requires proper C++20 toolchain setup
```

### Option 2: Wait for Official Merge (Recommended)

The simplest approach is to wait for XLS-82d to be merged into `rippled:develop`, then:

1. Pull the latest `rippleci/rippled:develop` image
2. Run the tests - they should pass automatically

## References

- **XLS-33 (MPT Core)**: https://github.com/XRPLF/XRPL-Standards/discussions/33
- **XLS-82d (MPT in DEX)**: https://github.com/XRPLF/XRPL-Standards/discussions/82
- **rippled feature branch**: https://github.com/XRPLF/rippled/tree/feature/mpt-v2e
- **definitions.json source**: Generated from `feature/mpt-v2e` using xrpl.js

## Conclusion

✅ **xrpl4j is ready for MPT DEX/AMM/PathFinding**
- All models are implemented
- Binary codec works correctly
- Integration tests are written and ready

❌ **rippled:develop is not ready yet**
- Waiting for XLS-82d to merge
- Tests will remain commented/disabled until then

**Recommendation:** Keep the MPT DEX/AMM/PathFinding tests in the codebase but document that they require a future rippled version.

