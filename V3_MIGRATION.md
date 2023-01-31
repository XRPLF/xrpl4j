# Version 2 to Version 3 Migration Guide

This guide outlines the differences between v2.5.1 and v3.0.0 and aims to provide an upgrade path
from v2.5.1 to v3.0.0.

## Dependency Structure
In v3.0.0, the following modules were condensed into the `xrpl-core` module:
- `xrpl4j-address-codec`
- `xrpl4j-binary-codec`
- `xrpl4j-model`
- `xrpl4j-keypairs`
- `xrpl4j-crypto-core`
- `xrpl4j-crypto-bouncycastle`

As a result, there are only three primary dependencies created by this project: `xrpl4j-bom`, `xrpl4j-core` and 
`xrpl4j-client`. Any projects that previously depended on any of the above artifacts should now simply depend on
`xrpl4j-core` and/or `xrpl4j-client`.

## Migrating from xrpl4j-keypairs
All classes in the former `xrpl4j-keypairs` module have been removed from the library
and should be replaced by the classes found in the `org.xrpl.xrpl4j.crypto` package in 
the `xrpl-core` module. The following section outlines how to migrate from using classes found in `xrpl4j-keypairs`
to classes in v3.0.0.

### wallet package
`Wallet`, `WalletFactory`, `DefaultWalletFactory` and `SeedWalletGenerationResult` have been removed in favor
  of classes in `org.xrpl.xrpl4j.crypto`.

Instead of creating a random wallet via `WalletFactory.randomWallet`, developers should use the 
`org.xrpl.xrpl4j.crypto.keys.Seed` class to generate a random seed, which can then be used to derive a 
`org.xrpl.xrpl4j.crypto.keys.KeyPair`. The `KeyPair`'s `PublicKey` can then be used to derive the wallet's `Address`:

```java
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

Seed randomSeed = Seed.ed25519Seed(); // To generate a random secp256k1 Seed, use Seed.secp256k1Seed()
KeyPair keyPair = randomSeed.deriveKeyPair();
Address = keyPair.publicKey().deriveAddress();
```

Instead of restoring a wallet from a Base58 encoded seed `String`, developers can now restore `Seed`s of various types
and derive a `KeyPair` and `Address` from the `Seed`.

```java
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.Entropy;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;

// From new, random entropy
Seed seedFromRandomEntropy = Seed.ed25519SeedFromEntropy(Entropy.newInstance));

// From existing entropy bytes
Seed seedFromEntropy = Seed.ed25519SeedFromEntropy(BaseEncoding.base16().decode("0102030405060708090A0B0C0D0E0F10"))

// From existing passphrase        
Seed seedFromPassphrase = Seed.ed25519SeedFromPassphrase(Passphrase.of("Hello World"));

// From existing secret
Seed seedFromSecret = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("snoPBrXtMeMyMHUVTgbuqAfg1SUTb"));
```

### keypairs package
All classes in the `org.xrpl.xrpl4j.keypairs` package have been removed. `org.xrpl.xrpl4j.keypairs.KeyPair`
has been replaced by `org.xrpl.xrpl4j.crypto.keys.KeyPair`, and all other classes in the `keypairs` package do not
have a direct replacement. Instead, the functionality contained in these classes is available in 
`org.xrpl.xrpl4j.crypto` through the `Seed`, `KeyPair`, and `SignatureService` interfaces.

The primary interface in the `keypairs` package was `KeyPairService`. The following list describes how to migrate
from each method in `KeyPairService`:
* Instead of `KeyPairService.generateSeed()`, use `Seed.ed25519Seed()` or `Seed.secp256k1Seed()`
* Instead of `KeyPairService.generateSeed(UnsignedByteArray entropy)`, use `Seed.ed25519SeedFromEntropy(Entropy.of(byteArray))`
* Instead of `KeyPairService.deriveKeyPair(String seed)`, use
  `Seed.fromfromBase58EncodedSecret(Base58EncodedSecret.of(secret)).deriveKeyPair()`
* Instead of `KeyPairService.sign(UnsignedByteArray message, String privateKey)` or `KeyPairService.sign(String message, String privateKey)`,
  use `BcSignatureService.sign(PrivateKey privateKey, T transaction)`
* Instead of `KeyPairService.verify(UnsignedByteArray message, String signature, String publicKey)` or 
  `KeyPairService.verify(String message, String signature, String publicKey)`, use 
  `BcSignatureService.verify(Signer signatureWithPublicKey, T transaction)` 
* Instead of `KeyPairService.deriveAddress(String publicKey)` or 
  `KeyPairService.deriveAddress(UnsignedByteAray publicKey)`, use `PublicKey.deriveAddress()`

## Migrating from xrpl4j-crypto-core and xrpl4j-crypto-bouncycastle
All cryptography operations have been moved to the `xrpl4j-core` artifact and a majority of the classes
found in `xrpl4j-crypto-core` and `xrpl4j-crypto-bouncycastle` have been replaced or rewritten. The following
section describes how to achieve the same functionality in v3.0.0 as was available in v2.5.1.

### Managing Keys
`BcKeyUtils` has been moved from `org.xrpl.xrpl4j.crypto` to `org.xrpl.xrpl4j.crypto.bc.keys`.

`org.xrpl.xrpl4j.crypto.Seed`, `org.xrpl.xrpl4j.crypto.PrivateKey`, and `org.xrpl4j.crypto.PublicKey` have been 
replaced by `org.xrpl.xrpl4j.crypto.keys.Seed`, `org.xrpl.xrpl4j.crypto.keys.PrivateKey`, and 
`org.xrpl4j.xrpl4j.crypto.keys.PublicKey` respectively. All seed/private key generation and restoration, as well as
public key and address derivation functionality is now available in these classes. The following example shows
how to generate a new random ED25519 `Seed`, derive a `KeyPair` from the `Seed`, and derive an `Address` from the 
derived `PublicKey` in v3.0.0:

```java
Seed seed = Seed.ed25519Seed();
KeyPair keyPair = seed.deriveKeyPair();
PublicKey publicKey = keyPair.publicKey();
PrivateKey privateKey = keyPair.privateKey();
Address = publicKey.deriveAddress();
```

`KeyMetadata` has been removed altogether. Instead, all private  keys are now represented by extensions of 
`org.xrpl.xrpl4j.crypto.keys.PrivateKeyable`. `PrivateKeyable` has two child implementations/extensions: `PrivateKey` 
and `PrivateKeyReference`. `PrivateKey` represents a private key held in memory in the same runtime. This variant could
be useful in the context of an android or native application, but should be avoided in server-side applications as 
holding private keys in-memory poses a large security threat to applications.

`PrivateKeyReference` on the other hand is, as the name suggests, a reference to a private key that is held in another 
system. For example, keys held in an HSM or other custody service can be referenced by a `PrivateKeyReference`. 
Different custody systems require different identifying information of private keys. Therefore, developers will likely
need to extend or implement `PrivateKeyReference` for each custody provider they use. In turn, developers must 
implement `SignatureService<MyPrivateKeyReference>` to interact with any given custody service, although
`AbstracSignatureService` can be extended to reduce a large portion of this effort.

### Signing and Verifying Transactions
The main interface used to sign and verify transactions in version 2 was 
`org.xrpl.xrpl4j.crypto.signing.SignatureService`. In version 3, `SignatureService` has been completely rewritten,
though it remains in the same package.

The version 3 `SignatureService` now contains a generic type extending `PrivateKeyable`. Developers
can now implement the same interface (`SignatureService`) for both in-memory `PrivateKey`s and private keys managed
by external services (referenced by `PrivateKeyReference`). This was possible in version 2, but the interface was 
clunky and unintuitive for in-memory private keys, ie calls to `SignatureService.sign` required passing in 
`KeyMetadata.EMPTY` for all in-memory private keys.

The new `SignatureService` interface has two implementations: `org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService<PrivateKey>`
and `org.xrpl.xrpl4j.crypto.signing.bc.BcDerivedKeySignatureService<PrivateKeyReference>`. In version 2, the 
analogous classes were `org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService` and 
`org.xrpl.xrpl4j.crypto.signing.DerivedKeySignatureService`, respectively. However, `BcSignatureService` can now
be used as a singleton and sign transactions for any number of `PrivateKey`s, unlike `SingleKeySignatureService` which
held a singular `PrivateKey`.

## Changes to Model Objects
v3.0.0 introduces many changes to the objects previously found in the `xrpl4j-model` dependency. While most of the 
changes involved removing previously deprecated fields and methods, some changes were not deprecated in v2.5.1.

* `Transaction.computeMultiSigFee` has been removed in favor of `FeeUtils.computeMultisigNetworkFees(FeeResult, SignerListObject)`
* `Transaction.closeDate()` has been removed in favor of  `TransactionResult.closeDate()` as this field is only present in
  responses to RPC calls.
* Similarly, `Transaction.closeDateHuman()` has been removed in favor of  `TransactionResult.closeDateHuman()`.
* `Transaction.hash()` has been removed in favor of `AccountTransactionsTransactionsTransaction.hash()` which are
  present in `AccountTransactionResult.transactions()`.
* `Transaction.ledgerIndex()` has been removed in favor of `AccountTransactionsTransactionsTransaction.ledgerIndex()`
  which are present in `AccountTransactionResult.transactions()`.
* `AccountTransactionsResult.ledgerIndexMin()` and `AccountTransactionsResult.ledgerIndexMax` have been removed
  in favor of `AccountTransactionsResult.ledgerIndexMinimum()` and `AccountTransactionsResult.ledgerIndexMaximum()`
  respectively
* `AccountTransactionsTransactionResult.transaction()` has been removed in favor of
  `AccountTransactionsTransactionResult.resultTransaction()`. The transaction's hash and ledgerIndex can be found in
  `resultTransaction().hash()` and `resultTransaction().ledgerIndex()`, respectively.
* `AccountTransactionsRequestParams.builder()` has been renamed to `AccountTransactionsRequestParams.unboundedBuilder()`
* `AccountTransactionsRequestParams.ledgerIndexMin()` has been removed in favor of
  `AccountTransactionsRequestParams.ledgerIndexMinimum()`
* `AccountTransactionsRequestParams.ledgerIndexMax()` has been removed in favor of
  `AccountTransactionsRequestParams.ledgerIndexMaximum()`
* The `LedgerIndex(String value)` constructor, `LedgerIndex.of(String value)`, and `LedgerIndex.of(UnsignedLong value)`
  have been removed from the public API. `LedgerIndex`es should only be constructed via the
  `LedgerIndex.of(UnsignedInteger value)` static constructor.
* `LedgerIndex.CURRENT`, `LedgerIndex.CLOSED`, and `LedgerIndex.VALIDATED` have been removed. Ledger index shortcut
  values can be specified via the `LedgerSpecifier` class.
* `LedgerIndex.unsignedLongValue()` and `LedgerIndex.value()` have been removed. To get the underlying value of
  the `LedgerIndex`, use `LedgerIndex.unsignedIntegerValue()`
* `LedgerIndex.plus(UnsignedLong other)` has been removed. Only the `UnsignedInteger` variant is still supported.
* `SubmitMultiSignedResult.result()` has been removed in favor of `SubmitMultiSignedResult.engineResult()` whose type
  has changed from `Optional<String>` to `String`
* `SubmitMultiSignedResult.resultCode()` has been removed in favor of `SubmitMultiSignedResult.engineResultCode()` whose type
  has changed from `Optional<Integer>` to `Integer`
* `SubmitMultiSignedResult.resultMessage()` has been removed in favor of `SubmitMultiSignedResult.engineResultMessage()` whose type
  has changed from `Optional<String>` to `String`
* `SubmitResult.result()` has been removed in favor of `SubmitResult.engineResult()` whose type
  has changed from `Optional<String>` to `String`
* `SubmitResult.resultCode()` has been removed in favor of `SubmitResult.engineResultCode()` whose type
  has changed from `Optional<Integer>` to `Integer`
* `VersionType` has been renamed to `KeyType`
* `Transaction.publicKey()` is now typed as a `PublicKey` instead of an `Optional<String>`

The following classes' `.ledgerHash()` and `.ledgerIndex()` methods have been removed in favor of `.ledgerSpecifier()`:
* `AccountChannelsRequestParams`
* `AccountCurrenciesRequestParams`
* `AccountInfoRequestParams`
* `AccountLinesRequestParams`
* `AccountObjectsRequestParams`
* `AccountOffersRequestParams`
* `AccountTransactionsRequestParams`
* `LedgerRequestParams`
* `RipplePathFindRequestParams`

Additionally, `.ledgerSpecifier()` no longer defaults to `LedgerSpecifier.CURRENT`, so developers must explicitly
set `LedgerSpecifier` in each of the classes above.

The following classes `.ledgerIndex()` and `.ledgerHash()` methods have been changed from `@Nullable` to `Optional`
* `AccountChannelsResult`
* `AccountCurrenciesResult`

## Changes to XrplClient
Some methods have been removed from `XrplClient`.

#### Submission Methods
`XrplClient.submit(Wallet wallet, T unsignedTransaction)`, 
`XrplClient.submit(org.xrpl.xrpl4j.crypto.signing.SignedTransaction)`, and
`XrplClient.submit(org.xrpl.xrpl4j.model.client.transactions.SignedTransaction)` have been removed in favor of 
`XrplClient.submit(SingleSignedTransaction signedTransaction)`.

To create a `SingleSignedTransaction`, use `BcSignatureService` like this:

```java
KeyPair keyPair = Seed.ed25519Seed().deriveKeyPair(); 
BcSignatureService signatureService = new BcSignatureService();
SingleSignedTransaction<Transaction> signedTransaction = signatureService.sign(keyPair.privateKey(), transaction);
xrplClient.submit(signedTransaction);
```

`XrplClient.submitMultisigned(T transaction)` has been removed in favor of 
`XrplClient.submitMultisigned(MultiSignedTransaction<T> transaction)`.

To create a `MultiSignedTransaction`, use `BcSignatureService` like this:

```java
Set<Signer> signers = Lists.newArrayList(aliceKeyPair, bobKeyPair).stream()
  .map(keyPair -> {
      Signature signedPayment = signatureService.multiSign(keyPair.privateKey(), unsignedPayment);
      return Signer.builder()
        .signingPublicKey(keyPair.publicKey())
        .transactionSignature(signedPayment)
        .build();
    }
  )
  .collect(Collectors.toSet());

/////////////////////////////
// Then we add the signatures to the Payment object and submit it
MultiSignedTransaction<Payment> multiSigPayment = MultiSignedTransaction.<Payment>builder()
  .unsignedTransaction(unsignedPayment)
  .signerSet(signers)
  .build();

SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSigPayment);
```

For a more complete example, see `SubmitMultiSignedIT`.

#### Signing Methods
`XrplClient.signTransaction(Wallet wallet, T unsignedTransaction)` has also been removed. A `SignatureService` 
should be used instead to sign transactions.

#### Query Methods
`XrplClient.serverInfo()` has been removed in favor of `XrplClient.serverInformation()`. `XrplClient.serverInformation()`
returns an `org.xrpo.xrpl4j.model.client.serverinfo.ServerInfoResult`, which could contain one of 
`RippledServerInfo`, `ReportingModeServerInfo`, or `ClioServerInfo` depending on which type of XRPL server was
queried.
