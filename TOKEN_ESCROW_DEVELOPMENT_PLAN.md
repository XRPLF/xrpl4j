# Token Escrow Development Plan (XLS-0085)

## Overview

This document outlines the development plan for implementing XLS-0085 Token Escrow support in xrpl4j. The Token Escrow
amendment enhances existing Escrow functionality to support both Trustline-based tokens (IOUs) and Multi-Purpose
Tokens (MPTs), in addition to XRP.

**Specification
**: [XLS-0085 Token-Enabled Escrows](https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0085-token-escrow)

## Key Changes Summary

### What's Changing

- **Existing transactions** (EscrowCreate, EscrowFinish, EscrowCancel) will be **extended**, not replaced
- The `Amount` field will change from `XrpCurrencyAmount` to `CurrencyAmount` to support XRP, IOU, and MPT
- New fields added to support token-specific features (transfer rates, issuer nodes, locked amounts)
- New AccountSet flag for IOU escrow permission
- MPT objects gain locked amount tracking

### What's NOT Changing

- No new transaction types are being created
- Existing XRP escrow functionality remains backward compatible
- Transaction structure and validation patterns remain consistent

## Development Phases

### Phase 1: Update Core Transaction Models

**Goal**: Modify escrow transactions to accept any CurrencyAmount type

#### Tasks

1. **Update EscrowCreate.amount() to accept CurrencyAmount**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowCreate.java`
    - Change: `XrpCurrencyAmount amount()` → `CurrencyAmount amount()`
    - Impact: Breaking change for existing code using EscrowCreate

2. **Update EscrowCreate documentation**
    - Update JavaDoc to reflect:
        - Support for XRP, IOU, and MPT tokens
        - New validation rules per XLS-0085
        - Failure conditions (tecNO_PERMISSION, tecFROZEN, etc.)
        - State changes for different token types

3. **Update EscrowFinish transaction**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowFinish.java`
    - Verify compatibility with CurrencyAmount
    - Update documentation for token-specific behavior

4. **Update EscrowCancel transaction**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowCancel.java`
    - Verify compatibility with CurrencyAmount
    - Update documentation for token-specific behavior

**Files to Modify**:

- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowCreate.java`
- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowFinish.java`
- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/EscrowCancel.java`

### Phase 2: Update Ledger Object Models

**Goal**: Extend EscrowObject to support token amounts and new fields

#### Tasks

1. **Update EscrowObject.amount() to CurrencyAmount**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/EscrowObject.java`
    - Change: `XrpCurrencyAmount amount()` → `CurrencyAmount amount()`

2. **Add EscrowObject.transferRate() field**
    - Add: `Optional<TransferRate> transferRate()`
    - Purpose: Store transfer rate (IOU) or transfer fee (MPT) at escrow creation
    - Used during settlement even if issuer changes rate later

3. **Add EscrowObject.issuerNode() field**
    - Add: `Optional<String> issuerNode()`
    - Purpose: Reference to issuer's directory node when issuer is neither source nor destination
    - Type: UInt64 in protocol, String in Java (hex representation)

4. **Update MetaEscrowObject similarly**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/metadata/MetaEscrowObject.java`
    - Apply same changes: amount, transferRate, issuerNode

5. **Update EscrowObject documentation**
    - Document token escrow support
    - Explain new fields and their purpose
    - Update examples

**Files to Modify**:

- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/EscrowObject.java`
- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/metadata/MetaEscrowObject.java`

**New Types Needed**:

- `TransferRate` wrapper class (if not already exists) for UInt32 transfer rate value

### Phase 3: Add AccountSet Flag Support

**Goal**: Enable IOU issuers to allow their tokens in escrows

#### Tasks

1. **Add ALLOW_TRUSTLINE_LOCKING to AccountSetFlag enum**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/AccountSet.java`
    - Add to enum: `ALLOW_TRUSTLINE_LOCKING(17)`
    - This is `asfAllowTrustLineLocking` in the protocol

2. **Update AccountSetFlag documentation**
    - JavaDoc explaining:
        - Enables IOU tokens to be held in escrow
        - Must be set by issuer before escrows can be created
        - Only applies to IOU tokens (not MPTs)

3. **Add corresponding AccountRootFlags entry**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/flags/AccountRootFlags.java`
    - Add: `lsfAllowTrustLineLocking` flag (0x40000000)
    - Used to check if account has enabled trustline locking

**Files to Modify**:

- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/AccountSet.java`
- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/flags/AccountRootFlags.java`

### Phase 4: Update MPT Models

**Goal**: Add locked amount tracking to MPT objects

#### Tasks

1. **Add lockedAmount field to MpTokenObject**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/MpTokenObject.java`
    - Add: `Optional<MpTokenNumericAmount> lockedAmount()`
    - Tracks amount locked in escrows for this holder

2. **Add lockedAmount field to MpTokenIssuanceObject**
    - File: `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/MpTokenIssuanceObject.java`
    - Add: `Optional<MpTokenNumericAmount> lockedAmount()`
    - Tracks total amount locked in escrows for this issuance

3. **Update MPT model documentation**
    - Explain lockedAmount purpose
    - Document how it changes during escrow operations
    - Note: OutstandingAmount remains unchanged during escrow

**Files to Modify**:

- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/MpTokenObject.java`
- `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/MpTokenIssuanceObject.java`

### Phase 5: Add Unit Tests

**Goal**: Comprehensive unit test coverage for all model changes

#### Test Files to Create/Update

1. **EscrowCreateJsonTest for token escrows**
    - File: `xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/transactions/json/EscrowCreateJsonTest.java`
    - Tests:
        - EscrowCreate with IssuedCurrencyAmount
        - EscrowCreate with MptCurrencyAmount
        - JSON serialization/deserialization
        - Backward compatibility with XrpCurrencyAmount

2. **EscrowFinishJsonTest for token escrows**
    - Update existing or create new tests
    - Verify JSON handling with token escrows

3. **EscrowCancelJsonTest for token escrows**
    - Update existing or create new tests
    - Verify JSON handling with token escrows

4. **EscrowObjectTest for token escrows**
    - File: Update `xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/ledger/EscrowObjectTest.java`
    - Tests:
        - EscrowObject with CurrencyAmount variants
        - transferRate field serialization
        - issuerNode field serialization
        - Complete JSON round-trip tests

5. **AccountSetFlag unit tests**
    - File: Update `xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/transactions/AccountSetTests.java`
    - Test ALLOW_TRUSTLINE_LOCKING flag
    - Verify flag value (17)

6. **MPT lockedAmount unit tests**
    - Files:
        - `xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/ledger/MpTokenObjectTest.java`
        - `xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/ledger/MpTokenIssuanceObjectTest.java`
    - Test lockedAmount field
    - JSON serialization with lockedAmount

**Test Patterns to Follow**:

- Use existing `AbstractJsonTest` base class
- Follow pattern from `xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/transactions/json/PaymentJsonTests.java`
- Test both serialization and deserialization
- Include edge cases and optional fields

### Phase 6: Add Integration Tests

**Goal**: End-to-end testing of token escrow functionality

#### Integration Test Scenarios

1. **IOU Escrow Integration Tests**
    - File: Extend `xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/EscrowIT.java`
    - Test scenarios:
        - Create IOU escrow with authorized issuer
        - Finish IOU escrow successfully
        - Cancel IOU escrow
        - Test with RequireAuth enabled
        - Test with freeze conditions (global, individual, deep freeze)
        - Verify transfer rate is locked and applied

2. **MPT Escrow Integration Tests**
    - File: Extend `EscrowIT.java` or create separate test class
    - Test scenarios:
        - Create MPT escrow with lsfMPTCanEscrow flag
        - Finish MPT escrow successfully
        - Cancel MPT escrow
        - Test with RequireAuth enabled
        - Test with lock conditions
        - Verify transfer fee is locked and applied
        - Verify lockedAmount tracking

3. **AccountSet with ALLOW_TRUSTLINE_LOCKING**
    - File: Extend `xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/AccountSetIT.java`
    - Test scenarios:
        - Set asfAllowTrustLineLocking flag
        - Clear asfAllowTrustLineLocking flag
        - Verify flag appears in AccountRoot

4. **Transfer Rate/Fee Locking Tests**
    - Verify rates are captured at escrow creation
    - Change rate after creation
    - Verify original rate is used at settlement

5. **Error Condition Tests**
    - Test all failure scenarios from XLS-0085:
        - tecNO_PERMISSION (issuer is source, flag not set)
        - tecFROZEN (token frozen/locked)
        - tecNO_AUTH (not authorized)
        - tecUNFUNDED (insufficient balance)
        - tecNO_LINE / tecNO_ENTRY (no trustline/MPToken)
        - tecINSUFFICIENT_RESERVE (can't create trustline/MPToken)

6. **Auto-creation Tests**
    - Test automatic trustline creation during EscrowFinish
    - Test automatic MPToken creation during EscrowFinish
    - Test automatic creation during EscrowCancel
    - Verify reserve requirements

**Test Patterns to Follow**:

- Use existing `AbstractIT` base class
- Follow patterns from `EscrowIT.java`
- Use `scanForResult()` and `scanForFinality()` helpers
- Create test accounts with `createRandomAccountEd25519()`
- Clean up test data appropriately

## Implementation Order

Recommended implementation order to minimize breaking changes:

1. **Phase 3** (AccountSet Flag) - Independent, can be done first
2. **Phase 4** (MPT Models) - Independent, can be done in parallel with Phase 3
3. **Phase 1** (Transaction Models) - Core changes, do before Phase 2
4. **Phase 2** (Ledger Objects) - Depends on Phase 1
5. **Phase 5** (Unit Tests) - After each phase, add corresponding unit tests
6. **Phase 6** (Integration Tests) - Final validation after all model changes

## Breaking Changes

### API Breaking Changes

1. **EscrowCreate.amount()** changes from `XrpCurrencyAmount` to `CurrencyAmount`
    - **Impact**: Code using `EscrowCreate.builder().amount(XrpCurrencyAmount.ofDrops(...))` will still work
    - **Migration**: No code changes needed due to polymorphism

2. **EscrowObject.amount()** changes from `XrpCurrencyAmount` to `CurrencyAmount`
    - **Impact**: Code expecting `XrpCurrencyAmount` will need to handle `CurrencyAmount`
    - **Migration**: Use `amount().handle()` or `amount().map()` to handle different types

### Backward Compatibility

- All existing XRP escrow functionality remains unchanged
- Existing tests should continue to pass
- JSON serialization remains compatible

## Testing Strategy

### Unit Test Coverage

- All new fields must have JSON serialization tests
- All enum values must be tested
- Edge cases for optional fields

### Integration Test Coverage

- Happy path for IOU escrows
- Happy path for MPT escrows
- All error conditions from XLS-0085
- Authorization and freeze scenarios
- Transfer rate/fee locking

### Manual Testing Checklist

- [ ] Create IOU escrow on testnet
- [ ] Finish IOU escrow on testnet
- [ ] Cancel IOU escrow on testnet
- [ ] Create MPT escrow on testnet
- [ ] Finish MPT escrow on testnet
- [ ] Cancel MPT escrow on testnet
- [ ] Verify locked amounts update correctly
- [ ] Verify transfer rates are locked

## Documentation Updates

### JavaDoc Updates Required

- EscrowCreate transaction
- EscrowFinish transaction
- EscrowCancel transaction
- EscrowObject ledger object
- MetaEscrowObject
- AccountSet.AccountSetFlag
- AccountRootFlags
- MpTokenObject
- MpTokenIssuanceObject

### README Updates

- Update examples to show token escrow usage
- Add section on token escrow support
- Document new AccountSet flag

## Dependencies

### External Dependencies

- No new external dependencies required
- Uses existing CurrencyAmount, IssuedCurrencyAmount, MptCurrencyAmount types

### Internal Dependencies

- Requires existing MPT support in xrpl4j
- Requires existing IOU/trustline support

## Risk Assessment

### Low Risk

- Adding new optional fields to ledger objects
- Adding new AccountSet flag
- Unit tests

### Medium Risk

- Changing amount field type in transactions
- Integration tests (requires testnet with amendment enabled)

### High Risk

- None identified

## Timeline Estimate

- **Phase 1**: 2-3 days
- **Phase 2**: 2-3 days
- **Phase 3**: 1 day
- **Phase 4**: 1 day
- **Phase 5**: 3-4 days
- **Phase 6**: 4-5 days

**Total Estimated Time**: 13-17 days

## Success Criteria

- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Existing XRP escrow tests still pass
- [ ] Can create, finish, and cancel IOU escrows on testnet
- [ ] Can create, finish, and cancel MPT escrows on testnet
- [ ] All error conditions properly handled
- [ ] Documentation updated
- [ ] Code review completed

## Appendix: Key XLS-0085 Specification Details

### IOU vs MPT Differences

| Aspect            | IOU Tokens                           | MPT Tokens                  |
|-------------------|--------------------------------------|-----------------------------|
| **Trustlines**    | Required                             | Not used                    |
| **Issuer Flag**   | `lsfAllowTrustLineLocking` (account) | `tfMPTCanEscrow` (token)    |
| **Transfer Flag** | N/A                                  | `tfMPTCanTransfer` required |
| **Require Auth**  | `lsfRequireAuth`                     | `tfMPTRequireAuth`          |
| **Freeze/Lock**   | Deep Freeze prevents finish          | Lock prevents finish        |
| **Transfer Rate** | `TransferRate`                       | `TransferFee`               |
| **Outstanding**   | Unchanged during escrow              | Unchanged during escrow     |

### Failure Conditions

#### EscrowCreate Failures

- `tecNO_PERMISSION`: Issuer is source OR issuer hasn't enabled escrow flag
- `tecNO_AUTH`: Source not authorized to hold token
- `tecUNFUNDED`: Source lacks trustline OR insufficient balance
- `tecOBJECT_NOT_FOUND`: Source doesn't hold MPT
- `tecFROZEN`: Token is frozen/locked for source

#### EscrowFinish Failures

- `tecNO_AUTH`: Destination not authorized
- `tecNO_LINE` / `tecNO_ENTRY`: Destination lacks trustline/MPT
- `tecINSUFFICIENT_RESERVE`: Can't create trustline/MPT
- `tecFROZEN`: Deep freeze/lock condition (but allows cancel)

#### EscrowCancel Failures

- `tecNO_AUTH`: Source not authorized
- `tecNO_LINE` / `tecNO_ENTRY`: Source lacks trustline/MPT
- `tecINSUFFICIENT_RESERVE`: Can't create trustline/MPT
- Note: Freeze/lock does NOT prevent cancel

### State Changes

#### EscrowCreate

- **IOU**: Deduct from source's trustline balance
- **MPT**: Deduct from source's MPT balance, increase LockedAmount on both MPToken and MPTokenIssuance

#### EscrowFinish

- **IOU**: Add to destination's trustline balance
- **MPT**: Complex logic based on whether source/destination is issuer (see spec section 1.2.2)

#### EscrowCancel

- **IOU**: Add back to source's trustline balance
- **MPT**: Add back to source's MPT balance, decrease LockedAmount

## References

- [XLS-0085 Specification](https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0085-token-escrow)
- [XLS-0033 Multi-Purpose Tokens](https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0033-multi-purpose-tokens)
- [XRPL Escrow Documentation](https://xrpl.org/escrow.html)
- [XRPL Transaction Types](https://xrpl.org/transaction-types.html)
