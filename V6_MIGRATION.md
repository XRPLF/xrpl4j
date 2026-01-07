# Version 5 to Version 6 Migration Guide

This guide outlines the breaking changes between v5.x.x and v6.0.0 and provides an upgrade path for applications using
xrpl4j.

## Overview

Version 6.0.0 introduces support for
the [XLS-0085 Token Escrow](https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0085-token-escrow) amendment, which
extends the existing Escrow functionality to support Trustline-based tokens (IOUs) and Multi-Purpose Tokens (MPTs), in
addition to XRP.

## Breaking Changes

### Escrow Transaction Models

#### EscrowCreate.amount()

The `amount()` field in `EscrowCreate` has changed from `XrpCurrencyAmount` to `CurrencyAmount` to support XRP, IOU, and
MPT tokens.

**Before (v5.x.x):**

```java

@JsonProperty("Amount")
XrpCurrencyAmount amount();
```

**After (v6.0.0):**

```java

@JsonProperty("Amount")
CurrencyAmount amount();
```

**Migration:**

Due to polymorphism, existing code using `XrpCurrencyAmount` will continue to work without changes:

```java
// This still works in v6.0.0
EscrowCreate escrowCreate = EscrowCreate.builder()
    .account(sourceAddress)
    .destination(destinationAddress)
    .amount(XrpCurrencyAmount.ofDrops(1000000))
    .fee(XrpCurrencyAmount.ofDrops(12))
    .sequence(UnsignedInteger.ONE)
    .build();
```

You can now also create escrows with IOU or MPT tokens:

```java
// New in v6.0.0: IOU escrow
EscrowCreate iouEscrow = EscrowCreate.builder()
    .account(sourceAddress)
    .destination(destinationAddress)
    .amount(IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuerAddress)
      .value("100")
      .build())
    .fee(XrpCurrencyAmount.ofDrops(12))
    .sequence(UnsignedInteger.ONE)
    .build();

// New in v6.0.0: MPT escrow
EscrowCreate mptEscrow = EscrowCreate.builder()
  .account(sourceAddress)
  .destination(destinationAddress)
  .amount(MptCurrencyAmount.builder()
    .mptIssuanceId(mptIssuanceId)
    .value(UnsignedLong.valueOf(1000))
    .build())
  .fee(XrpCurrencyAmount.ofDrops(12))
  .sequence(UnsignedInteger.ONE)
  .build();
```

### Escrow Ledger Objects

#### EscrowObject.amount()

The `amount()` field in `EscrowObject` has changed from `XrpCurrencyAmount` to `CurrencyAmount`.

**Before (v5.x.x):**

```java

@JsonProperty("Amount")
XrpCurrencyAmount amount();
```

**After (v6.0.0):**

```java

@JsonProperty("Amount")
CurrencyAmount amount();
```

**Migration:**

Code that expects `XrpCurrencyAmount` will need to handle the `CurrencyAmount` type. Use the `handle()` or `map()`
methods to work with different currency types:

```java
// Before (v5.x.x)
XrpCurrencyAmount amount = escrowObject.amount();
UnsignedLong drops = amount.toDrops();

// After (v6.0.0)
CurrencyAmount amount = escrowObject.amount();
amount.handle(
  // Handle XRP
  xrpAmount ->{
    UnsignedLong drops = xrpAmount.toDrops();
  },
  // Handle IOU
  issuedCurrencyAmount ->{
    String value = issuedCurrencyAmount.value();
  },
  // Handle MPT
  mptAmount ->{
    UnsignedLong value = mptAmount.value();
  }
);
```

#### New EscrowObject Fields

`EscrowObject` and `MetaEscrowObject` now include two new optional fields:

- **`transferRate()`**: Stores the transfer rate (for IOUs) or transfer fee (for MPTs) at escrow creation time. This
  rate is locked and used during settlement even if the issuer changes the rate later.
- **`issuerNode()`**: A reference to the issuer's directory node when the issuer is neither the source nor destination.

```java
Optional<TransferRate> transferRate();

Optional<String> issuerNode();
```

These fields are automatically populated by the XRPL when creating token escrows and do not require any action from
developers.

## New Features

### AccountSet Flag for IOU Escrows

A new `AccountSetFlag` has been added to allow IOU issuers to enable their tokens to be held in escrows.

**New Flag:**

```java
/**
 * Allow trust line tokens (IOUs) issued by this account to be held in escrow (requires the TokenEscrow amendment) 
 * and can only be enabled by the issuer account.
 */
ALLOW_TRUSTLINE_LOCKING(17)
```

**Usage:**

IOU issuers must set this flag before their tokens can be used in escrows:

```java
AccountSet accountSet = AccountSet.builder()
  .account(issuerAddress)
  .fee(XrpCurrencyAmount.ofDrops(12))
  .sequence(accountInfo.accountData().sequence())
  .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_LOCKING)
  .signingPublicKey(issuerKeyPair.publicKey())
  .build();
```

A corresponding `AccountRootFlags` entry has also been added:

```java
public static final AccountRootFlags ALLOW_TRUSTLINE_LOCKING =
  new AccountRootFlags(0x40000000);

public boolean lsfAllowTrustLineLocking() {
  return this.isSet(AccountRootFlags.ALLOW_TRUSTLINE_LOCKING);
}
```

### MPT Locked Amount Tracking

`MpTokenObject` and `MpTokenIssuanceObject` now include an optional `lockedAmount()` field to track amounts locked in
escrows:

```java
/**
 * The amount of this MPToken that is locked in escrows.
 */
@JsonProperty("LockedAmount")
Optional<MpTokenNumericAmount> lockedAmount();
```

This field is automatically updated by the XRPL when MPT escrows are created, finished, or cancelled.

## Backward Compatibility

All existing XRP escrow functionality remains fully backward compatible:

- Existing code using `EscrowCreate`, `EscrowFinish`, and `EscrowCancel` with XRP will continue to work without
  modification
- All existing unit and integration tests should continue to pass
- JSON serialization and deserialization remain compatible

## Additional Resources

- **XLS-0085 Specification**: https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0085-token-escrow
- **Development Plan**: See `TOKEN_ESCROW_DEVELOPMENT_PLAN.md` for implementation details
- **Quick Reference**: See `TOKEN_ESCROW_QUICK_REFERENCE.md` for code examples and validation rules
