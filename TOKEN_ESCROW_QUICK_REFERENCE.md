# Token Escrow Quick Reference Guide

## Overview
This guide provides a quick reference for implementing XLS-0085 Token Escrow support in xrpl4j.

## Key Files to Modify

### Transaction Models (Phase 1)
```
xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/
├── EscrowCreate.java       - Change amount() from XrpCurrencyAmount to CurrencyAmount
├── EscrowFinish.java       - Verify CurrencyAmount compatibility
└── EscrowCancel.java       - Verify CurrencyAmount compatibility
```

### Ledger Object Models (Phase 2)
```
xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/
├── EscrowObject.java       - Change amount(), add transferRate(), add issuerNode()
└── metadata/
    └── MetaEscrowObject.java - Same changes as EscrowObject
```

### AccountSet Flags (Phase 3)
```
xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/
├── transactions/AccountSet.java - Add ALLOW_TRUSTLINE_LOCKING(17) to enum
└── flags/AccountRootFlags.java  - Add lsfAllowTrustLineLocking flag
```

### MPT Models (Phase 4)
```
xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/
├── MpTokenObject.java          - Add lockedAmount field
└── MpTokenIssuanceObject.java  - Add lockedAmount field
```

### Unit Tests (Phase 5)
```
xrpl4j-core/src/test/java/org/xrpl/xrpl4j/model/
├── transactions/
│   ├── json/EscrowCreateJsonTest.java  - Add IOU/MPT tests
│   ├── json/EscrowFinishJsonTest.java  - Add token tests
│   ├── json/EscrowCancelJsonTest.java  - Add token tests
│   └── AccountSetTests.java            - Add flag tests
└── ledger/
    ├── EscrowObjectTest.java           - Add token tests
    ├── MpTokenObjectTest.java          - Add lockedAmount tests
    └── MpTokenIssuanceObjectTest.java  - Add lockedAmount tests
```

### Integration Tests (Phase 6)
```
xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/
├── EscrowIT.java      - Add IOU and MPT escrow tests
└── AccountSetIT.java  - Add ALLOW_TRUSTLINE_LOCKING tests
```

## Code Changes Summary

### 1. EscrowCreate.java
```java
// BEFORE
@JsonProperty("Amount")
XrpCurrencyAmount amount();

// AFTER
@JsonProperty("Amount")
CurrencyAmount amount();
```

### 2. EscrowObject.java
```java
// BEFORE
@JsonProperty("Amount")
XrpCurrencyAmount amount();

// AFTER
@JsonProperty("Amount")
CurrencyAmount amount();

// NEW FIELDS
@JsonProperty("TransferRate")
Optional<TransferRate> transferRate();

@JsonProperty("IssuerNode")
Optional<String> issuerNode();
```

### 3. AccountSet.java (AccountSetFlag enum)
```java
// ADD TO ENUM
/**
 * Allow trust line tokens (IOUs) issued by this account to be held in escrow.
 * (Requires the TokenEscrow amendment.) Can only be enabled by the issuer account.
 */
ALLOW_TRUSTLINE_LOCKING(17),
```

### 4. AccountRootFlags.java
```java
// ADD FLAG
/**
 * Constant {@link AccountRootFlags} for the {@code lsfAllowTrustLineLocking} flag.
 */
public static final AccountRootFlags ALLOW_TRUSTLINE_LOCKING = 
    new AccountRootFlags(0x40000000);

// ADD METHOD
public boolean lsfAllowTrustLineLocking() {
  return this.isSet(AccountRootFlags.ALLOW_TRUSTLINE_LOCKING);
}
```

### 5. MpTokenObject.java
```java
// ADD FIELD
/**
 * The amount of this MPToken that is locked in escrows.
 *
 * @return An optionally-present {@link MpTokenNumericAmount}.
 */
@JsonProperty("LockedAmount")
Optional<MpTokenNumericAmount> lockedAmount();
```

### 6. MpTokenIssuanceObject.java
```java
// ADD FIELD
/**
 * The total amount of this MPT that is locked in escrows across all holders.
 *
 * @return An optionally-present {@link MpTokenNumericAmount}.
 */
@JsonProperty("LockedAmount")
Optional<MpTokenNumericAmount> lockedAmount();
```

## Test Examples

### Unit Test Example (EscrowCreateJsonTest)
```java
@Test
public void testEscrowCreateWithIOU() throws JsonProcessingException, JSONException {
  EscrowCreate escrowCreate = EscrowCreate.builder()
    .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
    .fee(XrpCurrencyAmount.ofDrops(12))
    .sequence(UnsignedInteger.ONE)
    .amount(IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXEkk"))
      .value("100")
      .build())
    .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
    .cancelAfter(UnsignedLong.valueOf(533257958))
    .finishAfter(UnsignedLong.valueOf(533171558))
    .signingPublicKey(PublicKey.fromBase16EncodedPublicKey("..."))
    .build();

  String json = "{\n" +
    "  \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
    "  \"TransactionType\": \"EscrowCreate\",\n" +
    "  \"Amount\": {\n" +
    "    \"currency\": \"USD\",\n" +
    "    \"issuer\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXEkk\",\n" +
    "    \"value\": \"100\"\n" +
    "  },\n" +
    "  \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
    "  \"CancelAfter\": 533257958,\n" +
    "  \"FinishAfter\": 533171558,\n" +
    "  \"Fee\": \"12\",\n" +
    "  \"Sequence\": 1,\n" +
    "  \"SigningPubKey\": \"...\"\n" +
    "}";

  assertCanSerializeAndDeserialize(escrowCreate, json);
}
```

### Integration Test Example (EscrowIT)
```java
@Test
public void createAndFinishIouEscrow() throws JsonRpcClientErrorException {
  // 1. Create issuer account and enable ALLOW_TRUSTLINE_LOCKING
  KeyPair issuerKeyPair = createRandomAccountEd25519();
  AccountSet accountSet = AccountSet.builder()
    .account(issuerKeyPair.publicKey().deriveAddress())
    .fee(feeResult.drops().openLedgerFee())
    .sequence(issuerAccountInfo.accountData().sequence())
    .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_LOCKING)
    .signingPublicKey(issuerKeyPair.publicKey())
    .build();
  // Submit and wait for validation...

  // 2. Create trustlines for sender and receiver
  // ... (create trustlines)

  // 3. Create escrow with IOU
  EscrowCreate escrowCreate = EscrowCreate.builder()
    .account(senderKeyPair.publicKey().deriveAddress())
    .sequence(senderAccountInfo.accountData().sequence())
    .fee(feeResult.drops().openLedgerFee())
    .amount(IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .value("100")
      .build())
    .destination(receiverKeyPair.publicKey().deriveAddress())
    .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
    .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
    .signingPublicKey(senderKeyPair.publicKey())
    .build();
  // Submit and wait for validation...

  // 4. Wait for finishAfter time
  // ... (wait)

  // 5. Finish escrow
  EscrowFinish escrowFinish = EscrowFinish.builder()
    .account(receiverKeyPair.publicKey().deriveAddress())
    .fee(feeResult.drops().openLedgerFee())
    .owner(senderKeyPair.publicKey().deriveAddress())
    .offerSequence(escrowCreate.sequence())
    .signingPublicKey(receiverKeyPair.publicKey())
    .build();
  // Submit and verify...
}
```

## Validation Rules from XLS-0085

### EscrowCreate Validation
- ✅ Amount must be positive
- ✅ Issuer cannot be the source
- ✅ For IOUs: Issuer must have `lsfAllowTrustLineLocking` flag set
- ✅ For MPTs: Token must have `lsfMPTCanEscrow` flag set
- ✅ For MPTs: Token must have `lsfMPTCanTransfer` flag (unless destination is issuer)
- ✅ Source must be authorized if token requires auth
- ✅ Source must have trustline (IOU) or MPToken (MPT)
- ✅ Token must not be frozen/locked for source
- ✅ Source must have sufficient balance

### EscrowFinish Validation
- ✅ Destination must be authorized if token requires auth
- ✅ Destination must have trustline (IOU) or MPToken (MPT), or can auto-create
- ✅ Deep freeze/lock prevents finish (but allows cancel)
- ✅ Global/individual freeze allows finish for IOUs

### EscrowCancel Validation
- ✅ Source must be authorized if token requires auth
- ✅ Source must have trustline (IOU) or MPToken (MPT), or can auto-create
- ✅ Freeze/lock does NOT prevent cancel

## Error Codes to Test

| Error Code | Condition |
|------------|-----------|
| `tecNO_PERMISSION` | Issuer is source OR issuer hasn't enabled escrow flag |
| `tecNO_AUTH` | Account not authorized to hold token |
| `tecUNFUNDED` | Source lacks trustline OR insufficient balance |
| `tecOBJECT_NOT_FOUND` | Source doesn't hold MPT |
| `tecFROZEN` | Token is frozen/locked |
| `tecNO_LINE` | Destination lacks trustline (IOU) |
| `tecNO_ENTRY` | Destination lacks MPToken (MPT) |
| `tecINSUFFICIENT_RESERVE` | Can't create trustline/MPToken due to reserves |

## Implementation Checklist

### Phase 1: Transaction Models
- [ ] Update EscrowCreate.amount() to CurrencyAmount
- [ ] Update EscrowCreate JavaDoc
- [ ] Update EscrowFinish JavaDoc
- [ ] Update EscrowCancel JavaDoc
- [ ] Verify backward compatibility

### Phase 2: Ledger Objects
- [ ] Update EscrowObject.amount() to CurrencyAmount
- [ ] Add EscrowObject.transferRate()
- [ ] Add EscrowObject.issuerNode()
- [ ] Update MetaEscrowObject similarly
- [ ] Update JavaDoc

### Phase 3: AccountSet Flags
- [ ] Add ALLOW_TRUSTLINE_LOCKING(17) to AccountSetFlag
- [ ] Add lsfAllowTrustLineLocking to AccountRootFlags
- [ ] Update JavaDoc

### Phase 4: MPT Models
- [ ] Add lockedAmount to MpTokenObject
- [ ] Add lockedAmount to MpTokenIssuanceObject
- [ ] Update JavaDoc

### Phase 5: Unit Tests
- [ ] EscrowCreate with IOU JSON test
- [ ] EscrowCreate with MPT JSON test
- [ ] EscrowObject with tokens JSON test
- [ ] AccountSetFlag test
- [ ] MPT lockedAmount tests

### Phase 6: Integration Tests
- [ ] IOU escrow create/finish/cancel
- [ ] MPT escrow create/finish/cancel
- [ ] AccountSet flag integration test
- [ ] Transfer rate/fee locking test
- [ ] Error condition tests
- [ ] Auto-creation tests

## Resources

- **Specification**: https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0085-token-escrow
- **Development Plan**: See `TOKEN_ESCROW_DEVELOPMENT_PLAN.md`
- **Task List**: Run task management tools to view detailed task breakdown
