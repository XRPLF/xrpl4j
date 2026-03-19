# XLS-0068 Sponsorship Features Usage Guide

This guide explains how to use the XLS-0068 Sponsored Fees and Reserves features in `xrpl4j`.

## Overview

XLS-0068 introduces two sponsorship models for the XRP Ledger:

1. **Co-signing Model** (`SponsorshipTransfer`): Sponsor co-signs each transaction
2. **Pre-funded Model** (`SponsorshipSet`): Sponsor pre-allocates funds for a sponsee

## Prerequisites

- The `featureSponsorship` amendment must be enabled on the network
- Both sponsor and sponsee accounts must exist and be funded
- Sponsor must have sufficient XRP balance to cover sponsorship amounts

## Pre-funded Sponsorship Model

### Creating a Sponsorship

Use `SponsorshipSet` to create a pre-funded sponsorship that allocates XRP for transaction fees and/or reserve requirements:

```java
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.google.common.primitives.UnsignedInteger;

// Get sponsor and sponsee addresses
Address sponsorAddress = sponsorKeyPair.publicKey().deriveAddress();
Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();

// Get current fee and account info
FeeResult feeResult = xrplClient.fee();
AccountInfoResult sponsorInfo = xrplClient.accountInfo(
  AccountInfoRequestParams.of(sponsorAddress)
);

// Create a SponsorshipSet transaction with fee allocation
SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
  .account(sponsorAddress)
  .fee(feeResult.drops().openLedgerFee())
  .sequence(sponsorInfo.accountData().sequence())
  .sponsee(sponseeAddress)
  .feeAmount(XrpCurrencyAmount.ofDrops(1000000)) // 1 XRP for fees
  .maxFee(XrpCurrencyAmount.ofDrops(100))        // Max 100 drops per tx
  .signingPublicKey(sponsorKeyPair.publicKey())
  .build();

// Sign and submit
SingleSignedTransaction<SponsorshipSet> signedTx = 
  signatureService.sign(sponsorKeyPair.privateKey(), sponsorshipSet);
SubmitResult<SponsorshipSet> result = xrplClient.submit(signedTx);
```

### Sponsoring Reserve Requirements

To sponsor reserve units instead of (or in addition to) transaction fees:

```java
SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
  .account(sponsorAddress)
  .fee(feeResult.drops().openLedgerFee())
  .sequence(sponsorInfo.accountData().sequence())
  .sponsee(sponseeAddress)
  .reserveCount(UnsignedInteger.valueOf(5)) // Sponsor 5 reserve units
  .signingPublicKey(sponsorKeyPair.publicKey())
  .build();
```

### Comprehensive Sponsorship

To sponsor both fees and reserves with a counterparty sponsor:

```java
SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
  .account(sponsorAddress)
  .fee(feeResult.drops().openLedgerFee())
  .sequence(sponsorInfo.accountData().sequence())
  .sponsee(sponseeAddress)
  .counterpartySponsor(alternativeSponsorAddress)
  .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
  .maxFee(XrpCurrencyAmount.ofDrops(100))
  .reserveCount(UnsignedInteger.valueOf(5))
  .signingPublicKey(sponsorKeyPair.publicKey())
  .build();
```

## Querying Sponsorship Information

### Get Sponsorship Ledger Objects

Query all sponsorships owned by an account:

```java
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.ledger.SponsorshipObject;

AccountObjectsResult result = xrplClient.accountObjects(
  AccountObjectsRequestParams.builder()
    .account(sponsorAddress)
    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
    .type(AccountObjectType.SPONSORSHIP)
    .build()
);

// Filter to SponsorshipObject instances
List<SponsorshipObject> sponsorships = result.accountObjects().stream()
  .filter(obj -> obj instanceof SponsorshipObject)
  .map(obj -> (SponsorshipObject) obj)
  .collect(Collectors.toList());

// Examine sponsorship details
for (SponsorshipObject sponsorship : sponsorships) {
  System.out.println("Owner: " + sponsorship.owner());
  System.out.println("Sponsee: " + sponsorship.sponsee());
  sponsorship.feeAmount().ifPresent(amt -> 
    System.out.println("Fee Amount: " + amt.value() + " drops"));
  sponsorship.maxFee().ifPresent(amt -> 
    System.out.println("Max Fee: " + amt.value() + " drops"));
  sponsorship.reserveCount().ifPresent(count -> 
    System.out.println("Reserve Count: " + count));
}
```

### Query Account Sponsoring Information

Use the `account_sponsoring` RPC method to get sponsorship details:

```java
import org.xrpl.xrpl4j.model.client.accounts.AccountSponsoringRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountSponsoringResult;

AccountSponsoringResult result = xrplClient.accountSponsoring(
  AccountSponsoringRequestParams.builder()
    .account(sponsorAddress)
    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
    .build()
);

System.out.println("Account: " + result.account());
System.out.println("Sponsored Objects: " + result.sponsoredObjects());
```

## Important Notes

1. **Beta Status**: All XLS-0068 features are marked with `@Beta` and may change
2. **Amendment Required**: The `featureSponsorship` amendment must be enabled
3. **Network Support**: Currently only supported on networks with the XLS-0068 fork
4. **Balance Requirements**: Sponsor must have sufficient XRP to cover allocations
5. **Ledger Objects**: Each `SponsorshipSet` creates a `Sponsorship` ledger object

## See Also

- [XLS-0068 Specification](https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md)
- [Integration Tests](xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/AccountSponsoringIT.java)
- [SponsorshipSet JavaDoc](xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/transactions/SponsorshipSet.java)
- [SponsorshipObject JavaDoc](xrpl4j-core/src/main/java/org/xrpl/xrpl4j/model/ledger/SponsorshipObject.java)

