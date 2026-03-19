# Lending Protocol (XLS-66) Implementation Plan for xrpl4j

## Implementation Ground Rules

**These rules apply at all times during implementation and bug-fix iterations:**

1. **Rippled is the source of truth.** For any transaction, ledger object, flags, signing behavior, etc., always consult `/Users/rajp/Documents/code/rippled-lending-protocol`. When in doubt, read the C++ source.
2. **Integration tests first, then unit tests.** For each transaction type, write an integration test that talks to Devnet first. Capture the actual JSON request/response from Devnet and use that to write JSON serialization/deserialization unit tests. Never make up JSON — derive it from real Devnet responses.
3. **Use both IOU and MPT assets** in JSON serialization/deserialization tests.
4. **Setup prerequisites properly.** You may need to enable rippling, authorize MPT, authorize IOU trust lines, etc. Refer to `SingleAssetVaultIT.java` and `MpTokenIT.java` for how to do this.
5. **Write MetaObjects** for each new ledger object (e.g., `MetaLoanBrokerObject`, `MetaLoanObject`). Follow the `MetaVaultObject` pattern.
6. **Write LedgerEntryParams** for each new ledger object (e.g., `LoanBrokerLedgerEntryParams`, `LoanLedgerEntryParams`). Follow the `VaultLedgerEntryParams` pattern.
7. **Write tests for every class you create.** No class ships without a corresponding test.
8. **Introduce proper Flags classes with tests.** Both transaction flags and ledger object flags. Reference existing flag classes (e.g., `VaultCreateFlags`, `VaultFlags`).
9. **Write Wrapper classes** for `Data` or any other field that needs one. Follow the `VaultData` convention.
10. **Write precondition checks** (`@Value.Check`) for transactions based on the XLS spec and rippled source. Write unit tests for those preconditions.
11. **Write `assertXxxEntryEqualsObjectFromAccountObjects` helper methods** (like `assertVaultEntryEqualsObjectFromAccountObjects` in `SingleAssetVaultIT`) for `LoanBrokerObject` and `LoanObject` to ensure no fields are missed.
12. **Review the last 15 commits** on this branch to see how SingleAssetVault was implemented end-to-end. Don't miss anything that was done there.
13. **LoanSet dual-signing**: `LoanSet` requires both the LoanBroker and Borrower to sign. Implement the counterparty signing logic in the integration test. Reference the xrpl.js integration test at `/Users/rajp/Documents/code/xrpl.js/packages/xrpl/test/integration/transactions/lendingProtocol.test.ts` to see how `signLoanSetByCounterparty` works — the broker signs first, then the borrower fills in `CounterpartySignature`.
14. **Incremental approach**: Get each piece compiling and passing before moving to the next. Build incrementally.

## Overview

Implement the Lending Protocol (XLS-66) in xrpl4j. This adds support for 9 new transaction types, 2 new ledger objects, associated flags, and unit/integration tests. The full spec is in `Lending Protocol Spec.md`.

## Scope

### New Transaction Types (9)

| Transaction | Spec Section | Description |
|---|---|---|
| `LoanBrokerSet` | 3.3 | Create or update a LoanBroker |
| `LoanBrokerDelete` | 3.4 | Delete a LoanBroker |
| `LoanBrokerCoverDeposit` | 3.5 | Deposit First-Loss Capital |
| `LoanBrokerCoverWithdraw` | 3.6 | Withdraw First-Loss Capital |
| `LoanBrokerCoverClawback` | 3.7 | Clawback First-Loss Capital (issuer only) |
| `LoanSet` | 3.8 | Create a new Loan (dual-signed) |
| `LoanDelete` | 3.9 | Delete a fully-paid or defaulted Loan |
| `LoanManage` | 3.10 | Default, impair, or unimpair a Loan |
| `LoanPay` | 3.11 | Make a payment on a Loan |

### New Ledger Objects (2)

| Ledger Object | Spec Section | Description |
|---|---|---|
| `LoanBrokerObject` | 3.1 | Lending Protocol instance tied to a Vault |
| `LoanObject` | 3.2 | Individual loan agreement |

### New Flags Classes

| Flags Class | Used By | Flags |
|---|---|---|
| `LoanSetFlags` | `LoanSet` transaction | `tfLoanOverpayment` (0x00010000) |
| `LoanManageFlags` | `LoanManage` transaction | `tfLoanDefault` (0x00010000), `tfLoanImpair` (0x00020000), `tfLoanUnimpair` (0x00040000) |
| `LoanPayFlags` | `LoanPay` transaction | `tfLoanOverpayment` (0x00010000), `tfLoanFullPayment` (0x00020000), `tfLoanLatePayment` (0x00040000) |
| `LoanObjectFlags` | `LoanObject` ledger entry | `lsfLoanDefault` (0x00010000), `lsfLoanImpaired` (0x00020000), `lsfLoanOverpayment` (0x00040000) |

> Note: `LoanBrokerSet`, `LoanBrokerDelete`, `LoanBrokerCoverDeposit`, `LoanBrokerCoverWithdraw`, `LoanBrokerCoverClawback`, and `LoanDelete` have **no transaction-specific flags** (use `TransactionFlags` directly). `LoanBrokerObject` has **no object-specific flags** (Flags field is always 0).

### New Inner Object

| Class | Used By | Description |
|---|---|---|
| `CounterpartySignature` | `LoanSet` | Inner object containing counterparty's signature (SigningPubKey + TxnSignature, or Signers array) |

---

## Implementation Steps

Execute each step fully (code + tests) before moving to the next. Follow the patterns established by the Vault (XLS-65) implementation.

### Step 1: Binary Codec Definitions (`definitions.json`)

**File:** `xrpl4j-core/src/main/resources/definitions.json`

The transaction types and ledger entry types are already defined. Verify all required field definitions exist. Fields that may need adding:

**New fields to verify/add:**
- `LoanBrokerID` (Hash256) - already present (nth: 37)
- `LoanID` (Hash256) - already present (nth: 38)
- `LoanSequence` (UInt32) - verify present
- `LoanBrokerNode` (UInt64) - verify present
- `VaultNode` (UInt64) - verify present
- `Borrower` (AccountID) - verify present
- `Counterparty` (AccountID) - verify present
- `CounterpartySignature` (STObject) - verify present
- `LoanOriginationFee` (Number/Amount) - verify present
- `LoanServiceFee` (Number/Amount) - verify present
- `LatePaymentFee` (Number/Amount) - verify present
- `ClosePaymentFee` (Number/Amount) - verify present
- `OverpaymentFee` (UInt32) - verify present
- `InterestRate` (UInt32) - verify present
- `LateInterestRate` (UInt32) - verify present
- `CloseInterestRate` (UInt32) - verify present
- `OverpaymentInterestRate` (UInt32) - verify present
- `StartDate` (UInt32) - verify present
- `PaymentInterval` (UInt32) - verify present
- `GracePeriod` (UInt32) - verify present
- `PreviousPaymentDueDate` (UInt32) - verify present
- `NextPaymentDueDate` (UInt32) - verify present
- `PaymentRemaining` (UInt32) - verify present
- `PaymentTotal` (UInt32) - verify present
- `TotalValueOutstanding` (Number) - verify present
- `PrincipalOutstanding` (Number) - verify present
- `PrincipalRequested` (Number) - verify present
- `ManagementFeeOutstanding` (Number) - verify present
- `ManagementFeeRate` (UInt16) - verify present
- `PeriodicPayment` (Number) - verify present
- `LoanScale` (Int32) - verify present
- `DebtTotal` (Number) - verify present
- `DebtMaximum` (Number) - verify present
- `CoverAvailable` (Number) - verify present
- `CoverRateMinimum` (UInt32) - verify present
- `CoverRateLiquidation` (UInt32) - verify present

### Step 2: Enums and Registry Updates

**Files to modify:**

1. **`TransactionType.java`** — Add 9 new enum constants (all `@Beta`):
   ```
   LOAN_BROKER_SET("LoanBrokerSet"),
   LOAN_BROKER_DELETE("LoanBrokerDelete"),
   LOAN_BROKER_COVER_DEPOSIT("LoanBrokerCoverDeposit"),
   LOAN_BROKER_COVER_WITHDRAW("LoanBrokerCoverWithdraw"),
   LOAN_BROKER_COVER_CLAWBACK("LoanBrokerCoverClawback"),
   LOAN_SET("LoanSet"),
   LOAN_DELETE("LoanDelete"),
   LOAN_MANAGE("LoanManage"),
   LOAN_PAY("LoanPay"),
   ```
   Add before `UNKNOWN`.

2. **`LedgerEntryType` enum** (in `LedgerObject.java`) — Add 2 new constants (all `@Beta`):
   ```
   LOAN_BROKER("LoanBroker"),
   LOAN("Loan"),
   ```

3. **`Transaction.typeMap`** (in `Transaction.java`) — Add 9 new entries mapping `ImmutableXxx.class` to `TransactionType.XXX`.

4. **`@JsonSubTypes`** (in `LedgerObject.java`) — Add 2 new subtypes:
   ```
   @JsonSubTypes.Type(value = ImmutableLoanBrokerObject.class, name = "LoanBroker"),
   @JsonSubTypes.Type(value = ImmutableLoanObject.class, name = "Loan"),
   ```

### Step 3: Flags Classes

**Directory:** `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/flags/`

Follow the pattern from `VaultCreateFlags.java`:

1. **`LoanSetFlags.java`** — extends `TransactionFlags`
   - `tfLoanOverpayment` = 0x00010000

2. **`LoanManageFlags.java`** — extends `TransactionFlags`
   - `tfLoanDefault` = 0x00010000
   - `tfLoanImpair` = 0x00020000
   - `tfLoanUnimpair` = 0x00040000
   - Add `@Value.Check` or builder validation: flags are mutually exclusive

3. **`LoanPayFlags.java`** — extends `TransactionFlags`
   - `tfLoanOverpayment` = 0x00010000
   - `tfLoanFullPayment` = 0x00020000
   - `tfLoanLatePayment` = 0x00040000
   - Add validation: flags are mutually exclusive

4. **`LoanObjectFlags.java`** — extends `Flags` (ledger object flags, not TransactionFlags)
   - `lsfLoanDefault` = 0x00010000
   - `lsfLoanImpaired` = 0x00020000
   - `lsfLoanOverpayment` = 0x00040000

### Step 4: Transaction Model Classes

**Directory:** `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/`

All classes follow the `@Value.Immutable` + Jackson pattern from `VaultCreate.java`. All should be `@Beta`.

#### 4a. `LoanBrokerSet.java`
- Extends: `Transaction`
- Fields:
  - `VaultID` (Hash256, required)
  - `LoanBrokerID` (Optional<Hash256>) — present when modifying existing
  - `Data` (Optional<VaultData>) — reuse `VaultData` type (256-byte hex blob)
  - `ManagementFeeRate` (Optional<UnsignedInteger>) — 0-10000
  - `DebtMaximum` (Optional<AssetAmount>)
  - `CoverRateMinimum` (Optional<UnsignedInteger>) — 0-100000
  - `CoverRateLiquidation` (Optional<UnsignedInteger>) — 0-100000
- No custom flags (use `TransactionFlags`)
- `@Value.Check`: If CoverRateMinimum or CoverRateLiquidation is present, validate both-or-neither non-zero logic. ManagementFeeRate 0-10000. CoverRateMinimum/CoverRateLiquidation 0-100000.

#### 4b. `LoanBrokerDelete.java`
- Extends: `Transaction`
- Fields:
  - `LoanBrokerID` (Hash256, required)
- No custom flags

#### 4c. `LoanBrokerCoverDeposit.java`
- Extends: `Transaction`
- Fields:
  - `LoanBrokerID` (Hash256, required)
  - `Amount` (CurrencyAmount, required)
- No custom flags

#### 4d. `LoanBrokerCoverWithdraw.java`
- Extends: `Transaction`
- Fields:
  - `LoanBrokerID` (Hash256, required)
  - `Amount` (CurrencyAmount, required)
  - `Destination` (Optional<Address>)
  - `DestinationTag` (Optional<UnsignedInteger>)
- No custom flags

#### 4e. `LoanBrokerCoverClawback.java`
- Extends: `Transaction`
- Fields:
  - `LoanBrokerID` (Optional<Hash256>)
  - `Amount` (Optional<CurrencyAmount>)
- No custom flags
- `@Value.Check`: Either LoanBrokerID or Amount must be present

#### 4f. `CounterpartySignature.java`
- `@Value.Immutable` with Jackson serialization
- Fields:
  - `SigningPubKey` (Optional<String>) — hex public key
  - `TxnSignature` (Optional<String>) — hex signature
  - `Signers` (Optional<List<SignerWrapper>>) — reuse existing signer types
- Validation: Must have either (SigningPubKey + TxnSignature) or Signers

#### 4g. `LoanSet.java`
- Extends: `Transaction`
- Flags: `LoanSetFlags`
- Fields:
  - `LoanBrokerID` (Hash256, required)
  - `Counterparty` (Optional<Address>)
  - `CounterpartySignature` (CounterpartySignature, required)
  - `Data` (Optional<VaultData>)
  - `LoanOriginationFee` (Optional<AssetAmount>) — defaults 0
  - `LoanServiceFee` (Optional<AssetAmount>) — defaults 0
  - `LatePaymentFee` (Optional<AssetAmount>) — defaults 0
  - `ClosePaymentFee` (Optional<AssetAmount>) — defaults 0
  - `OverpaymentFee` (Optional<UnsignedInteger>) — 0-100000
  - `InterestRate` (Optional<UnsignedInteger>) — 0-100000
  - `LateInterestRate` (Optional<UnsignedInteger>) — 0-100000
  - `CloseInterestRate` (Optional<UnsignedInteger>) — 0-100000
  - `OverpaymentInterestRate` (Optional<UnsignedInteger>) — 0-100000
  - `PrincipalRequested` (AssetAmount, required)
  - `PaymentTotal` (Optional<UnsignedInteger>) — defaults 1
  - `PaymentInterval` (Optional<UnsignedInteger>) — defaults 60
  - `GracePeriod` (Optional<UnsignedInteger>) — defaults 60
- `@Value.Check`: GracePeriod >= 60. PaymentInterval >= 60.

#### 4h. `LoanDelete.java`
- Extends: `Transaction`
- Fields:
  - `LoanID` (Hash256, required)
- No custom flags

#### 4i. `LoanManage.java`
- Extends: `Transaction`
- Flags: `LoanManageFlags`
- Fields:
  - `LoanID` (Hash256, required)

#### 4j. `LoanPay.java`
- Extends: `Transaction`
- Flags: `LoanPayFlags`
- Fields:
  - `LoanID` (Hash256, required)
  - `Amount` (CurrencyAmount, required)

### Step 5: Ledger Object Model Classes

**Directory:** `xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/`

Follow `VaultObject.java` pattern.

#### 5a. `LoanBrokerObject.java`
- Extends: `LedgerObject`
- `@Value.Derived` ledgerEntryType → `LOAN_BROKER`
- Fields (from spec section 3.1.2):
  - `Flags` (UnsignedInteger, default 0) — no object-specific flags
  - `PreviousTxnID` (Hash256)
  - `PreviousTxnLgrSeq` (UnsignedInteger)
  - `Sequence` (UnsignedInteger)
  - `LoanSequence` (UnsignedInteger)
  - `OwnerNode` (String) — UINT64 serialized as string
  - `VaultNode` (String) — UINT64 serialized as string
  - `VaultID` (Hash256)
  - `Account` (Address) — pseudo-account
  - `Owner` (Address) — broker owner
  - `Data` (Optional<VaultData>)
  - `ManagementFeeRate` (Optional<UnsignedInteger>) — UInt16
  - `OwnerCount` (UnsignedInteger)
  - `DebtTotal` (AssetAmount)
  - `DebtMaximum` (Optional<AssetAmount>)
  - `CoverAvailable` (AssetAmount)
  - `CoverRateMinimum` (Optional<UnsignedInteger>)
  - `CoverRateLiquidation` (Optional<UnsignedInteger>)
  - `index` (Hash256)

#### 5b. `LoanObject.java`
- Extends: `LedgerObject`
- `@Value.Derived` ledgerEntryType → `LOAN`
- Flags: `LoanObjectFlags`
- Fields (from spec section 3.2.2):
  - `Flags` (LoanObjectFlags, default empty)
  - `PreviousTxnID` (Hash256)
  - `PreviousTxnLgrSeq` (UnsignedInteger)
  - `LoanSequence` (UnsignedInteger)
  - `OwnerNode` (String)
  - `LoanBrokerNode` (String)
  - `LoanBrokerID` (Hash256)
  - `Borrower` (Address)
  - `LoanOriginationFee` (AssetAmount)
  - `LoanServiceFee` (AssetAmount)
  - `LatePaymentFee` (AssetAmount)
  - `ClosePaymentFee` (AssetAmount)
  - `OverpaymentFee` (UnsignedInteger)
  - `InterestRate` (UnsignedInteger)
  - `LateInterestRate` (UnsignedInteger)
  - `CloseInterestRate` (UnsignedInteger)
  - `OverpaymentInterestRate` (UnsignedInteger)
  - `StartDate` (UnsignedInteger)
  - `PaymentInterval` (UnsignedInteger)
  - `GracePeriod` (UnsignedInteger)
  - `PreviousPaymentDueDate` (UnsignedInteger)
  - `NextPaymentDueDate` (UnsignedInteger)
  - `PaymentRemaining` (UnsignedInteger)
  - `TotalValueOutstanding` (AssetAmount)
  - `PrincipalOutstanding` (AssetAmount)
  - `ManagementFeeOutstanding` (Optional<AssetAmount>)
  - `PeriodicPayment` (AssetAmount)
  - `LoanScale` (Optional<Integer>) — Int32
  - `Data` (Optional<VaultData>)
  - `index` (Hash256)

### Step 6: Unit Tests for Models

**Directory:** `xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/`

Follow patterns from `VaultCreateTest.java` and `VaultObjectTest.java`:

1. **Transaction tests** — For each transaction type, write a test class that:
   - Builds a transaction with all fields populated using the builder
   - Serializes to JSON and verifies the output matches the expected JSON from the spec examples
   - Deserializes from JSON and verifies all fields
   - Tests `@Value.Check` validation (invalid field values throw `IllegalArgumentException`)

2. **Ledger object tests** — For each ledger object:
   - Builds with all fields and verifies serialization/deserialization
   - Uses the example JSON from spec sections 3.1.9 and 3.2.8

3. **Flags tests** — For each flags class:
   - Tests individual flag setting/reading
   - Tests flag combinations (and mutual exclusivity where applicable)
   - Tests `empty()` and builder

Test files to create:
- `transactions/LoanBrokerSetTest.java`
- `transactions/LoanBrokerDeleteTest.java`
- `transactions/LoanBrokerCoverDepositTest.java`
- `transactions/LoanBrokerCoverWithdrawTest.java`
- `transactions/LoanBrokerCoverClawbackTest.java`
- `transactions/LoanSetTest.java`
- `transactions/LoanDeleteTest.java`
- `transactions/LoanManageTest.java`
- `transactions/LoanPayTest.java`
- `ledger/LoanBrokerObjectTest.java`
- `ledger/LoanObjectTest.java`
- `flags/LoanSetFlagsTest.java`
- `flags/LoanManageFlagsTest.java`
- `flags/LoanPayFlagsTest.java`
- `flags/LoanObjectFlagsTest.java`

### Step 7: Integration Tests

**File:** `xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/LendingProtocolIT.java`

Follow `SingleAssetVaultIT.java` pattern. Test the full lifecycle:

1. Create a Vault (prerequisite)
2. Create a LoanBroker via `LoanBrokerSet`
3. Deposit into the Vault
4. Deposit First-Loss Capital via `LoanBrokerCoverDeposit`
5. Create a Loan via `LoanSet` (dual-signed)
6. Make a payment via `LoanPay`
7. Delete a fully-paid Loan via `LoanDelete`
8. Withdraw First-Loss Capital via `LoanBrokerCoverWithdraw`
9. Delete the LoanBroker via `LoanBrokerDelete`
10. Test default flow: Create loan, default via `LoanManage`, delete
11. **Freeze/unfreeze test**: Issuer freezes the Borrower's account, verify that the Borrower cannot repay the loan (`LoanPay` fails). Then the Issuer removes the freeze, and verify the Borrower can successfully pay the loan.

---

## Key Reference Files

| Purpose | Path |
|---|---|
| Spec | `Lending Protocol Spec.md` |
| Rippled source (source of truth) | `/Users/rajp/Documents/code/rippled-lending-protocol/src/xrpld/app/tx/detail/Loan*.cpp/.h` |
| xrpl.js IT (signing reference) | `/Users/rajp/Documents/code/xrpl.js/packages/xrpl/test/integration/transactions/lendingProtocol.test.ts` |
| Transaction base | `xrpl4j-core/.../model/transactions/Transaction.java` |
| Transaction type enum | `xrpl4j-core/.../model/transactions/TransactionType.java` |
| Ledger object base | `xrpl4j-core/.../model/ledger/LedgerObject.java` |
| Example transaction | `xrpl4j-core/.../model/transactions/VaultCreate.java` |
| Example ledger object | `xrpl4j-core/.../model/ledger/VaultObject.java` |
| Example flags | `xrpl4j-core/.../model/flags/VaultCreateFlags.java` |
| Example MetaObject | `xrpl4j-core/.../model/transactions/metadata/MetaVaultObject.java` |
| Example LedgerEntryParams | `xrpl4j-core/.../model/client/ledger/VaultLedgerEntryParams.java` |
| Example transaction test | `xrpl4j-core/.../model/transactions/VaultCreateTest.java` |
| Example ledger test | `xrpl4j-core/.../model/ledger/VaultObjectTest.java` |
| Example IT | `xrpl4j-integration-tests/.../tests/SingleAssetVaultIT.java` |
| Binary codec defs | `xrpl4j-core/src/main/resources/definitions.json` |

## Type Mapping Notes

- `NUMBER` fields in the spec → use `AssetAmount` (string-serialized number type used by Vault)
- `UINT16` / `UINT32` → `UnsignedInteger` (from Guava)
- `UINT64` → `String` (too large for UnsignedInteger; follows existing pattern for OwnerNode)
- `HASH256` → `Hash256`
- `ACCOUNTID` → `Address`
- `AMOUNT` → `CurrencyAmount`
- `BLOB` → `VaultData` (reuse existing 256-byte hex type) or `String` for raw hex
- `INT32` → `Integer` or a wrapper (for `LoanScale`)
- `STOBJECT` → Custom `@Value.Immutable` class (`CounterpartySignature`)

## Execution Order

### Phase 1: Foundation (get it compiling)
1. Verify `definitions.json` has all fields
2. Add enum constants and registry entries (TransactionType, LedgerEntryType, typeMap, JsonSubTypes)
3. Create flags classes (transaction flags + ledger object flags)
4. Create wrapper classes (e.g., for `Data` field if needed)
5. Create transaction model interfaces (all 9 transactions)
6. Create `CounterpartySignature` inner object for `LoanSet`
7. Create ledger object model interfaces (`LoanBrokerObject`, `LoanObject`)
8. Create MetaObjects (`MetaLoanBrokerObject`, `MetaLoanObject`)
9. Create LedgerEntryParams (`LoanBrokerLedgerEntryParams`, `LoanLedgerEntryParams`)
10. Build (`mvn clean install -DskipITs -DskipTests`) and fix compilation errors

### Phase 2: Integration tests against Devnet (get real JSON)
11. Write `LendingProtocolIT.java` with tests for each transaction type against Devnet
12. Implement counterparty signing logic for `LoanSet` dual-signature
13. Run each IT individually against Devnet: `mvn verify -pl xrpl4j-integration-tests -Dit.test=LendingProtocolIT -DskipTests -DuseDevnet`
14. Capture actual JSON request/response from Devnet for use in unit tests
15. Write `assertLoanBrokerEntryEqualsObjectFromAccountObjects` and `assertLoanEntryEqualsObjectFromAccountObjects` helpers

### Phase 3: Unit tests (derived from Devnet responses)
16. Write JSON serialization/deserialization tests for all 9 transaction types (use IOU + MPT)
17. Write JSON serialization/deserialization tests for both ledger objects (use IOU + MPT)
18. Write flags tests for all flags classes
19. Write precondition check (`@Value.Check`) unit tests
20. Write MetaObject tests
21. Write LedgerEntryParams tests
22. Run full build: `mvn clean install -DskipITs`
