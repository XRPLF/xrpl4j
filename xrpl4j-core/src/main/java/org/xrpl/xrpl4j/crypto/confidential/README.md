# Confidential Transfers for MPTs

## Building the JAR

```bash
mvn clean install -DskipTests
```

JAR location: `~/.m2/repository/org/xrpl/xrpl4j-core/HEAD-SNAPSHOT/xrpl4j-core-HEAD-SNAPSHOT.jar`

---

## Services Overview

All cryptographic operations are exposed through four high-level service classes:

```java
ConfidentialMptConvertService convertService = new ConfidentialMptConvertService();
ConfidentialMptSendService sendService = new ConfidentialMptSendService();
ConfidentialMptConvertBackService convertBackService = new ConfidentialMptConvertBackService();
ConfidentialMptClawbackService clawbackService = new ConfidentialMptClawbackService();

MptAmountEncryptor encryptor = new BcMptAmountEncryptor();
MptAmountDecryptor decryptor = new BcMptAmountDecryptor();
```

---

## 1. ConfidentialMPTConvert

Converts a holder's public MPT balance into confidential (encrypted) form. Also serves as the opt-in
mechanism: the first convert registers the holder's ElGamal public key.

**Step 1: Generate context and ZK proof (if registering a key)**

The context hash binds the proof to a specific transaction. The ZK proof proves knowledge of the
private key for the ElGamal public key being registered.

```java
KeyPair holderElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();

ConfidentialMptConvertContext convertContext = convertService.generateContext(
  holderAddress,      // Address
  sequence,           // UnsignedInteger (from account info)
  mpTokenIssuanceId   // MpTokenIssuanceId
);

ConfidentialMptConvertProof zkProof = convertService.generateProof(
  holderElGamalKeyPair,  // KeyPair
  convertContext         // ConfidentialMptConvertContext
);
```

**Step 2: Encrypt amount for holder and issuer (same blinding factor)**

```java
BlindingFactor blindingFactor = BlindingFactor.generate();

EncryptedAmount holderCiphertext = encryptor.encrypt(amount, holderElGamalKeyPair.publicKey(), blindingFactor);
EncryptedAmount issuerCiphertext = encryptor.encrypt(amount, issuerElGamalKeyPair.publicKey(), blindingFactor);
```

**Step 3: Build and submit the transaction**

```java
ConfidentialMptConvert convert = ConfidentialMptConvert.builder()
  .account(holderAddress)
  .fee(fee)
  .sequence(sequence)
  .signingPublicKey(holderPublicKey)
  .lastLedgerSequence(lastLedgerSeq)
  .mpTokenIssuanceId(mpTokenIssuanceId)
  .mptAmount(MpTokenNumericAmount.of(amount))
  .holderEncryptionKey(holderElGamalKeyPair.publicKey().base16Value())  // Only on first convert
  .holderEncryptedAmount(holderCiphertext.toHex())
  .issuerEncryptedAmount(issuerCiphertext.toHex())
  .blindingFactor(blindingFactor.hexValue())
  .zkProof(zkProof.hexValue())                                         // Only when registering key
  .build();
```

---

## 2. ConfidentialMPTMergeInbox

After `ConfidentialMPTConvert`, tokens land in the holder's **inbox (CB_IN)**. MergeInbox moves them
into the **spending balance (CB_S)** so they can be used in sends. No ZK proof required.

```java
ConfidentialMptMergeInbox mergeInbox = ConfidentialMptMergeInbox.builder()
  .account(holderAddress)
  .fee(fee)
  .sequence(sequence)
  .signingPublicKey(holderPublicKey)
  .lastLedgerSequence(lastLedgerSeq)
  .mpTokenIssuanceId(mpTokenIssuanceId)
  .build();
```

---

## 3. ConfidentialMPTSend

Confidential transfer of tokens between accounts. The send amount is encrypted for all three parties
(sender, destination, issuer) using the **same blinding factor**. ZK proofs prove the amount is valid
and the sender has sufficient balance without revealing actual values.

**Step 1: Generate context hash**

The context includes the sender's confidential balance version (from the MPToken ledger object) to
prevent proof replay.

```java
MpTokenObject holderMpToken = getMpToken(holderKeyPair, mpTokenIssuanceId);
UnsignedInteger version = holderMpToken.confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

ConfidentialMptSendContext sendContext = sendService.generateContext(
  senderAddress,      // Address
  sequence,           // UnsignedInteger
  mpTokenIssuanceId,  // MpTokenIssuanceId
  destAddress,        // Address
  version             // UnsignedInteger
);
```

**Step 2: Encrypt send amount for all parties (same blinding factor)**

All three encryptions use the **same** `sendBlindingFactor`, producing a shared C1 point (`r*G`).
This is required for the equality-shared-r proof.

```java
BlindingFactor sendBlindingFactor = BlindingFactor.generate();

EncryptedAmount senderCiphertext = encryptor.encrypt(sendAmount, senderElGamalPublicKey, sendBlindingFactor);
EncryptedAmount destCiphertext = encryptor.encrypt(sendAmount, destElGamalPublicKey, sendBlindingFactor);
EncryptedAmount issuerCiphertext = encryptor.encrypt(sendAmount, issuerElGamalPublicKey, sendBlindingFactor);
```

**Step 3: Decrypt sender's current balance and generate Pedersen proof params**

The sender's spending balance must be decrypted to generate range proofs that prove the send amount
and remaining balance are non-negative.

```java
EncryptedAmount senderBalanceCiphertext = EncryptedAmount.fromHex(
  holderMpToken.confidentialBalanceSpending().orElseThrow(...)
);
UnsignedLong senderCurrentBalance = decryptor.decrypt(
  senderBalanceCiphertext, senderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
);

BlindingFactor amountBlindingFactor = BlindingFactor.generate();
BlindingFactor balanceBlindingFactor = BlindingFactor.generate();

PedersenProofParams amountParams = sendService.generatePedersenProofParams(
  sendAmount, senderCiphertext, amountBlindingFactor
);
PedersenProofParams balanceParams = sendService.generatePedersenProofParams(
  senderCurrentBalance, senderBalanceCiphertext, balanceBlindingFactor
);
```

**Step 4: Generate the combined ZK proof**

The proof bundles four components:
1. **Equality-shared-r proof** — proves all ciphertexts encrypt the same amount with shared randomness
2. **Amount linkage proof** — links ElGamal ciphertext to Pedersen commitment for the amount
3. **Balance linkage proof** — links ElGamal ciphertext to Pedersen commitment for the balance
4. **Aggregated bulletproof** — proves amount and remaining balance are in range [0, 2^64)

```java
ConfidentialMptSendProof sendProof = sendService.generateProof(
  senderElGamalKeyPair,     // KeyPair (secp256k1)
  sendAmount,               // UnsignedLong
  Arrays.asList(
    MptConfidentialParty.of(senderElGamalPublicKey, senderCiphertext),
    MptConfidentialParty.of(destElGamalPublicKey, destCiphertext),
    MptConfidentialParty.of(issuerElGamalPublicKey, issuerCiphertext)
  ),
  sendBlindingFactor,       // BlindingFactor (shared across all recipients)
  sendContext,              // ConfidentialMptSendContext
  amountParams,             // PedersenProofParams
  balanceParams             // PedersenProofParams
);
```

**Step 5: Build and submit the transaction**

```java
ConfidentialMptSend send = ConfidentialMptSend.builder()
  .account(senderAddress)
  .fee(fee)
  .sequence(sequence)
  .signingPublicKey(senderPublicKey)
  .lastLedgerSequence(lastLedgerSeq)
  .destination(destAddress)
  .mpTokenIssuanceId(mpTokenIssuanceId)
  .senderEncryptedAmount(senderCiphertext.toHex())
  .destinationEncryptedAmount(destCiphertext.toHex())
  .issuerEncryptedAmount(issuerCiphertext.toHex())
  .zkProof(sendProof.hexValue())
  .amountCommitment(amountParams.pedersenCommitment().hexValue())
  .balanceCommitment(balanceParams.pedersenCommitment().hexValue())
  .build();
```

---

## 4. ConfidentialMPTConvertBack

Converts confidential MPTs back to public balance. Requires a balance linkage proof and range proof
to show the holder has sufficient confidential funds.

**Step 1: Encrypt amount for holder and issuer (same blinding factor)**

```java
BlindingFactor convertBackBlindingFactor = BlindingFactor.generate();

EncryptedAmount holderCiphertext = encryptor.encrypt(convertBackAmount, holderElGamalPublicKey, convertBackBlindingFactor);
EncryptedAmount issuerCiphertext = encryptor.encrypt(convertBackAmount, issuerElGamalPublicKey, convertBackBlindingFactor);
```

**Step 2: Generate context and Pedersen params for the spending balance**

```java
MpTokenObject mpToken = getMpToken(holderKeyPair, mpTokenIssuanceId);
UnsignedInteger version = mpToken.confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

ConfidentialMptConvertBackContext convertBackContext = convertBackService.generateContext(
  holderAddress, sequence, mpTokenIssuanceId, version
);

// Decrypt current spending balance
EncryptedAmount currentBalanceCiphertext = EncryptedAmount.fromHex(
  mpToken.confidentialBalanceSpending().orElseThrow(...)
);
UnsignedLong currentBalance = decryptor.decrypt(
  currentBalanceCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
);

BlindingFactor balanceBlindingFactor = BlindingFactor.generate();
PedersenProofParams balanceParams = convertBackService.generatePedersenProofParams(
  currentBalance, currentBalanceCiphertext, balanceBlindingFactor
);
```

**Step 3: Generate proof and build transaction**

```java
ConfidentialMptConvertBackProof proof = convertBackService.generateProof(
  holderElGamalKeyPair, convertBackAmount, convertBackContext, balanceParams
);

PedersenCommitment balanceCommitment = PedersenCommitment.of(balanceParams.pedersenCommitment());

ConfidentialMptConvertBack convertBack = ConfidentialMptConvertBack.builder()
  .account(holderAddress)
  .fee(fee)
  .sequence(sequence)
  .signingPublicKey(holderPublicKey)
  .lastLedgerSequence(lastLedgerSeq)
  .mpTokenIssuanceId(mpTokenIssuanceId)
  .mptAmount(MpTokenNumericAmount.of(convertBackAmount))
  .holderEncryptedAmount(holderCiphertext.toHex())
  .issuerEncryptedAmount(issuerCiphertext.toHex())
  .blindingFactor(convertBackBlindingFactor.hexValue())
  .balanceCommitment(balanceCommitment.hexValue())
  .zkProof(proof.hexValue())
  .build();
```

---

## 5. ConfidentialMPTClawback

Issuer-only transaction to forcibly convert a holder's confidential balance back to the issuer's public
reserve. The issuer uses their encrypted mirror of the holder's balance (`IssuerEncryptedBalance` on
the holder's MPToken) to generate a plaintext equality proof.

**Step 1: Read and decrypt the issuer's mirror balance**

```java
MpTokenObject holderMpToken = getMpToken(holderKeyPair, mpTokenIssuanceId);
EncryptedAmount issuerBalanceCiphertext = EncryptedAmount.fromHex(
  holderMpToken.issuerEncryptedBalance().orElseThrow(...)
);

UnsignedLong issuerDecryptedBalance = decryptor.decrypt(
  issuerBalanceCiphertext, issuerElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
);
```

**Step 2: Generate context and proof**

```java
ConfidentialMptClawbackContext clawbackContext = clawbackService.generateContext(
  issuerAddress, sequence, mpTokenIssuanceId, holderAddress
);

ConfidentialMptClawbackProof clawbackProof = clawbackService.generateProof(
  issuerBalanceCiphertext,           // EncryptedAmount from MPToken.issuerEncryptedBalance
  issuerElGamalKeyPair.publicKey(),  // PublicKey
  clawbackAmount,                    // UnsignedLong
  issuerElGamalKeyPair.privateKey(), // PrivateKey (used as blinding factor)
  clawbackContext                    // ConfidentialMptClawbackContext
);
```

**Step 3: Build and submit**

```java
ConfidentialMptClawback clawback = ConfidentialMptClawback.builder()
  .account(issuerAddress)
  .fee(fee)
  .sequence(sequence)
  .signingPublicKey(issuerPublicKey)
  .lastLedgerSequence(lastLedgerSeq)
  .mpTokenIssuanceId(mpTokenIssuanceId)
  .holder(holderAddress)
  .mptAmount(MpTokenNumericAmount.of(clawbackAmount))
  .zkProof(clawbackProof.hexValue())
  .build();
```

---

## Decryption

```java
MptAmountDecryptor decryptor = new BcMptAmountDecryptor();

EncryptedAmount ciphertext = EncryptedAmount.fromHex(encryptedHex);

// Decrypt with search range [minAmount, maxAmount]
// Uses baby-step giant-step algorithm to find the plaintext
UnsignedLong amount = decryptor.decrypt(ciphertext, privateKey, UnsignedLong.ZERO, UnsignedLong.valueOf(1_000_000));
```

---

## Full Integration Test

See `xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/ConfidentialTransfersIT.java`

## Using in your project

Run `mvn clean install -DskipTests` to build the JAR. Then add the
following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-core</artifactId>
  <version>HEAD-SNAPSHOT</version>
</dependency>
```
