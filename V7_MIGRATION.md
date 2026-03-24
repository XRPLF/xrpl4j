# Version 6 to Version 7 Migration Guide

This guide outlines the breaking changes between v6.x.x and v7.0.0 and provides an upgrade path for applications using
xrpl4j.

## TODO: Add migration guide for #712 and #715 after rebase

## Overview

Version 7.0.0 introduces support for
the [Single Asset Vault](https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0065-single-asset-vault) amendment,
which adds Multi-Purpose Token (MPT) support to the `Issue` type. The previously concrete `Issue` immutable has been
refactored into a polymorphic interface with three concrete subtypes: `XrpIssue`, `IouIssue`, and `MptIssue`.

## Breaking Changes

### Issue Model

#### Issue is now an interface

`Issue` was previously an `@Value.Immutable` with a `builder()` method, a `currency()` field, and an optional
`issuer()` field. It is now a plain interface with three concrete implementations: `XrpIssue`, `IouIssue`, and
`MptIssue`. The `Issue` interface provides `handle()` and `map()` methods for type-safe polymorphic dispatch across the
three subtypes.

#### Issue.builder() removed

`Issue.builder()` and `ImmutableIssue.Builder` no longer exist. Use the concrete subtype builders instead.

**Migration:**

```java
// Before (v6.x.x): XRP Issue
Issue xrp = Issue.builder().currency("XRP").build();

// After (v7.0.0): XRP Issue
Issue xrp = Issue.XRP; // or XrpIssue.XRP

// Before (v6.x.x): IOU Issue
Issue usd = Issue.builder()
    .currency("USD")
    .issuer(issuerAddress)
    .build();

// After (v7.0.0): IOU Issue
Issue usd = IouIssue.builder()
    .currency("USD")
    .issuer(issuerAddress)
    .build();
```

You can now also create MPT issues:

```java
// New in v7.0.0: MPT Issue
Issue mpt = MptIssue.builder()
    .mptIssuanceId(mptIssuanceId)
    .build();
```

#### Issue.currency() and Issue.issuer() removed

The `currency()` and `issuer()` accessors are no longer on the `Issue` interface. They have moved to the concrete
subtypes where they apply:

- `XrpIssue.currency()` — always returns `"XRP"`
- `IouIssue.currency()` — returns the currency code
- `IouIssue.issuer()` — returns the issuer `Address`
- `MptIssue.mptIssuanceId()` — returns the `MpTokenIssuanceId`

**Migration:**

Use the `handle()` or `map()` methods to work with `Issue` instances polymorphically. For example, in v6.x.x you could
access currency and issuer directly from an `AmmObject`:

```java
// Before (v6.x.x)
Issue issue = ammObject.asset();
String currency = issue.currency();
Optional<Address> issuer = issue.issuer();
```

In v7.0.0, objects like `VaultObject` can hold any asset type (XRP, IOU, or MPT). Use `handle()` to dispatch based on
the concrete type:

```java
// After (v7.0.0)
Issue issue = vaultObject.asset();
issue.handle(
  // Handle XRP
  xrpIssue -> {
    String currency = xrpIssue.currency(); // "XRP"
  },
  // Handle IOU
  iouIssue -> {
    String currency = iouIssue.currency();
    Address issuer = iouIssue.issuer();
  },
  // Handle MPT
  mptIssue -> {
    MpTokenIssuanceId issuanceId = mptIssue.mptIssuanceId();
  }
);
```

Or use `map()` to transform an `Issue` into a value:

```java
Issue issue = vaultObject.asset();
String description = issue.map(
  xrpIssue -> "XRP",
  iouIssue -> iouIssue.currency() + "/" + iouIssue.issuer(),
  mptIssue -> "MPT:" + mptIssue.mptIssuanceId()
);
```

### JSON Serialization

JSON serialization and deserialization remain compatible. The `IssueDeserializer` automatically selects the correct
subtype based on the JSON structure:

- `{"currency": "XRP"}` → `XrpIssue`
- `{"currency": "USD", "issuer": "rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"}` → `IouIssue`
- `{"mpt_issuance_id": "00000001A407AF5856CFF3379945D823561023E8E5CED9C9"}` → `MptIssue`

## Backward Compatibility

- JSON serialization and deserialization remain compatible with the same JSON structure.
- The `Issue.XRP` constant is still available and works the same way.

## Additional Resources

- **XLS-0065 Specification**: https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0065-single-asset-vault
