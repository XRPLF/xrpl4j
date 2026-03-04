# Confidential Transfers for MPTs

## Building the JAR

```bash
mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true
```

JAR location: `~/.m2/repository/org/xrpl/xrpl4j-core/HEAD-SNAPSHOT/xrpl4j-core-HEAD-SNAPSHOT.jar`

---

## 1. ConfidentialMPTConvert

**Step 1: Encrypt amount for holder and issuer (use SAME blindingFactor)**

```java
BlindingFactor blindingFactor = BlindingFactor.generate();
JavaElGamalBalanceEncryptor encryptor = new JavaElGamalBalanceEncryptor();

ElGamalCiphertext holderCiphertext = encryptor.encrypt(amount, holderElGamalKeyPair.publicKey(), blindingFactor);
ElGamalCiphertext issuerCiphertext = encryptor.encrypt(amount, issuerElGamalKeyPair.publicKey(), blindingFactor);

String holderEncryptedAmount = holderCiphertext.hexValue();
String issuerEncryptedAmount = issuerCiphertext.hexValue();
```

**Step 2: Generate context**

```java
ConfidentialMPTConvertContext context = ConfidentialMPTConvertContext.generate(
  holderAddress,      // Address
  sequence,           // UnsignedInteger
  mpTokenIssuanceId,  // MpTokenIssuanceId
  amount              // UnsignedLong
);
```

**Step 3: Generate ZKProof**

```java
JavaSecretKeyProofGenerator proofGenerator = new JavaSecretKeyProofGenerator();

SecretKeyProof zkProof = proofGenerator.generateProof(
  holderElGamalKeyPair.privateKey(),  // ElGamalPrivateKey
  context                              // ConfidentialMPTConvertContext
);

String zkProofHex = zkProof.hexValue();
```

---

## 2. ConfidentialMPTMergeInbox

After `ConfidentialMPTConvert`, tokens go to the **inbox**. You must merge them to the **spending balance** before sending.

**No ZKProof required** - just build and submit the transaction:

```java
ConfidentialMPTMergeInbox mergeInbox = ConfidentialMPTMergeInbox.builder()
  .account(holderAddress)
  .fee(fee)
  .sequence(sequence)
  .signingPublicKey(holderPublicKey)
  .lastLedgerSequence(lastLedgerSequence)
  .mpTokenIssuanceId(mpTokenIssuanceId)
  .build();
```

---

## 3. ConfidentialMPTSend

**Step 1: Encrypt amount for sender, destination, and issuer**

```java
BlindingFactor sendBlindingFactorSender = BlindingFactor.generate();
BlindingFactor sendBlindingFactorDest = BlindingFactor.generate();
BlindingFactor sendBlindingFactorIssuer = BlindingFactor.generate();

ElGamalCiphertext senderCiphertext = encryptor.encrypt(sendAmount, senderPublicKey, sendBlindingFactorSender);
ElGamalCiphertext destCiphertext = encryptor.encrypt(sendAmount, destPublicKey, sendBlindingFactorDest);
ElGamalCiphertext issuerCiphertext = encryptor.encrypt(sendAmount, issuerPublicKey, sendBlindingFactorIssuer);
```

**Step 2: Generate context**

```java
UnsignedInteger version = mpToken.confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

ConfidentialMPTSendContext sendContext = ConfidentialMPTSendContext.generate(
  senderAddress,      // Address
  sequence,           // UnsignedInteger
  mpTokenIssuanceId,  // MpTokenIssuanceId
  destAddress,        // Address
  version             // UnsignedInteger - from MPToken.confidentialBalanceVersion
);
```

**Step 3: Generate SamePlaintextMultiProof**

```java
JavaSamePlaintextMultiProofGenerator samePlaintextGen = new JavaSamePlaintextMultiProofGenerator();

// Create participants (nonces are generated internally)
SamePlaintextParticipant senderParticipant = SamePlaintextParticipant.forProofGeneration(
  senderCiphertext, senderPublicKey, sendBlindingFactorSender
);
SamePlaintextParticipant destParticipant = SamePlaintextParticipant.forProofGeneration(
  destCiphertext, destPublicKey, sendBlindingFactorDest
);
SamePlaintextParticipant issuerParticipant = SamePlaintextParticipant.forProofGeneration(
  issuerCiphertext, issuerPublicKey, sendBlindingFactorIssuer
);

// Generate proof (all nonces are generated internally)
SamePlaintextMultiProof samePlaintextProof = samePlaintextGen.generateProof(
  sendAmount,           // UnsignedLong
  senderParticipant,    // SamePlaintextParticipant
  destParticipant,      // SamePlaintextParticipant
  issuerParticipant,    // SamePlaintextParticipant
  Optional.empty(),     // Optional<SamePlaintextParticipant> auditor
  sendContext           // ConfidentialMPTSendContext
);
```

**Step 4: Generate Pedersen commitments**

```java
JavaPedersenCommitmentGenerator pedersenGen = new JavaPedersenCommitmentGenerator();
JavaElGamalBalanceDecryptor balanceDecryptor = new JavaElGamalBalanceDecryptor();

BlindingFactor amountBlindingFactorForSend = BlindingFactor.generate();
BlindingFactor balanceBlindingFactorForSend = BlindingFactor.generate();

// Get sender's current balance from ledger and decrypt it
ElGamalCiphertext currentBalanceCiphertext = ElGamalCiphertext.fromBytes(
  BaseEncoding.base16().decode(mpToken.confidentialBalanceSpending().get())
);
long senderCurrentBalanceLong = balanceDecryptor.decrypt(
  currentBalanceCiphertext, senderElGamalKeyPair.privateKey(), 0, 1_000_000
);
UnsignedLong senderCurrentBalance = UnsignedLong.valueOf(senderCurrentBalanceLong);

PedersenCommitment amountCommitment = pedersenGen.generateCommitment(sendAmount, amountBlindingFactorForSend);
PedersenCommitment balanceCommitment = pedersenGen.generateCommitment(senderCurrentBalance, balanceBlindingFactorForSend);
```

**Step 5: Generate Amount Linkage Proof**

```java
JavaElGamalPedersenLinkProofGenerator linkProofGen = new JavaElGamalPedersenLinkProofGenerator();

// Nonces are generated internally
ElGamalPedersenLinkProof amountLinkageProof = linkProofGen.generateProof(
  LinkageProofType.AMOUNT_COMMITMENT,
  senderCiphertext,            // EncryptedAmount
  senderPublicKey,             // ElGamalPublicKey
  amountCommitment,            // PedersenCommitment
  sendAmount,                  // UnsignedLong
  sendBlindingFactorSender,    // BlindingFactor - same used for encryption
  amountBlindingFactorForSend, // BlindingFactor - Pedersen blinding factor
  sendContext                  // ConfidentialMPTSendContext
);
```

**Step 6: Generate Balance Linkage Proof**

```java
// IMPORTANT: Use private key as blinding factor for BALANCE_COMMITMENT
BlindingFactor privateKeyAsBlindingFactor = BlindingFactor.fromBytes(
  senderElGamalKeyPair.privateKey().naturalBytes().toByteArray()
);

// Nonces are generated internally
ElGamalPedersenLinkProof balanceLinkageProof = linkProofGen.generateProof(
  LinkageProofType.BALANCE_COMMITMENT,
  currentBalanceCiphertext,      // EncryptedAmount - from ledger
  senderPublicKey,               // ElGamalPublicKey
  balanceCommitment,             // PedersenCommitment
  senderCurrentBalance,          // UnsignedLong
  privateKeyAsBlindingFactor,    // BlindingFactor - PRIVATE KEY
  balanceBlindingFactorForSend,  // BlindingFactor
  sendContext                    // ConfidentialMPTSendContext
);
```

**Step 7: Generate Bulletproof Range Proof**

The bulletproof proves that both the send amount and remaining balance are non-negative (in range [0, 2^64)).

```java
// Compute remaining balance: senderCurrentBalance - sendAmount
UnsignedLong remainingBalance = UnsignedLong.valueOf(
  senderCurrentBalance.longValue() - sendAmount.longValue()
);

// Compute blinding factor for remaining balance: rho_rem = rho_balance - rho_amount
byte[] negAmountBlinding = Secp256k1Operations.scalarNegate(amountBlindingFactorForSend.toBytes());
byte[] rhoRemBytes = Secp256k1Operations.scalarAdd(balanceBlindingFactorForSend.toBytes(), negAmountBlinding);
BlindingFactor rhoRem = BlindingFactor.fromBytes(rhoRemBytes);

// Generate aggregated bulletproof for {amount, remainingBalance}
BulletproofRangeProofGenerator bulletproofGen = new JavaBulletproofRangeProofGenerator();
BulletproofRangeProof bulletproof = bulletproofGen.generateProof(
  Arrays.asList(sendAmount, remainingBalance),        // List of values
  Arrays.asList(amountBlindingFactorForSend, rhoRem), // List of blinding factors
  sendContext
);
```

**Step 8: Combine all proofs**

```java
// SamePlaintextMultiProof (359) + Amount Linkage (195) + Balance Linkage (195) + Bulletproof (754) = 1503 bytes
String fullZkProofHex = ZKProofUtils.combineSendProofsWithBulletproofHex(
  samePlaintextProof, amountLinkageProof, balanceLinkageProof, bulletproof
);

String amountCommitmentHex = amountCommitment.hexValue();
String balanceCommitmentHex = balanceCommitment.hexValue();
```

---

## 4. ConfidentialMPTConvertBack

**Step 1: Encrypt amount for holder and issuer (use SAME blindingFactor)**

```java
BlindingFactor convertBackBlindingFactor = BlindingFactor.generate();

ElGamalCiphertext holderCiphertext = encryptor.encrypt(convertBackAmount, holderPublicKey, convertBackBlindingFactor);
ElGamalCiphertext issuerCiphertext = encryptor.encrypt(convertBackAmount, issuerPublicKey, convertBackBlindingFactor);
```

**Step 2: Generate context**

```java
UnsignedInteger version = mpToken.confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

ConfidentialMPTConvertBackContext context = ConfidentialMPTConvertBackContext.generate(
  holderAddress,       // Address
  sequence,            // UnsignedInteger
  mpTokenIssuanceId,   // MpTokenIssuanceId
  convertBackAmount,   // UnsignedLong
  version              // UnsignedInteger
);
```

**Step 3: Generate Pedersen commitment for current balance**

```java
BlindingFactor convertBackBalanceBlindingFactor = BlindingFactor.generate();
PedersenCommitment balanceCommitment = pedersenGen.generateCommitment(currentBalance, convertBackBalanceBlindingFactor);
```

**Step 4: Generate Balance Linkage Proof**

```java
ElGamalCiphertext currentBalanceCiphertext = ElGamalCiphertext.fromBytes(
  BaseEncoding.base16().decode(mpToken.confidentialBalanceSpending().get())
);

BlindingFactor privateKeyAsBlindingFactor = BlindingFactor.fromBytes(
  holderElGamalKeyPair.privateKey().naturalBytes().toByteArray()
);

// Nonces are generated internally
ElGamalPedersenLinkProof balanceLinkageProof = linkProofGen.generateProof(
  LinkageProofType.BALANCE_COMMITMENT,
  currentBalanceCiphertext,
  holderPublicKey,
  balanceCommitment,
  currentBalance,
  privateKeyAsBlindingFactor,
  convertBackBalanceBlindingFactor,
  context
);
```

**Step 5: Generate Bulletproof Range Proof**

The bulletproof proves the remaining balance after conversion is non-negative.

```java
// Compute remaining balance: currentBalance - convertBackAmount
UnsignedLong remainingBalance = UnsignedLong.valueOf(
  currentBalance.longValue() - convertBackAmount.longValue()
);

// Generate bulletproof for remaining balance (single value)
BulletproofRangeProofGenerator bulletproofGen = new JavaBulletproofRangeProofGenerator();
BulletproofRangeProof bulletproof = bulletproofGen.generateProof(
  Collections.singletonList(remainingBalance),
  Collections.singletonList(convertBackBalanceBlindingFactor),  // Same blinding factor as balance commitment
  context
);
```

**Step 6: Combine proofs**

```java
// Pedersen linkage proof (195 bytes) + Bulletproof (688 bytes) = 883 bytes total
String zkProofHex = ZKProofUtils.combineConvertBackProofsHex(
  balanceLinkageProof, bulletproof
);

String balanceCommitmentHex = balanceCommitment.hexValue();
```

---

## 5. ConfidentialMPTClawback

**Step 1: Get issuer's encrypted balance from holder's MPToken**

```java
// Use issuerEncryptedBalance (NOT confidentialBalanceSpending)
ElGamalCiphertext issuerBalanceCiphertext = ElGamalCiphertext.fromBytes(
    BaseEncoding.base16().decode(holderMpToken.issuerEncryptedBalance().get())
  );
```

**Step 2: Generate context**

```java
ConfidentialMPTClawbackContext context = ConfidentialMPTClawbackContext.generate(
  issuerAddress,       // Address - issuer's address
  sequence,            // UnsignedInteger
  mpTokenIssuanceId,   // MpTokenIssuanceId
  clawbackAmount,      // UnsignedLong
  holderAddress        // Address - holder's address
);
```

**Step 3: Generate EqualityPlaintextProof**

```java
JavaEqualityPlaintextProofGenerator equalityProofGen = new JavaEqualityPlaintextProofGenerator();

// The nonce is generated internally by the proof generator
BlindingFactor issuerPrivateKeyAsBlindingFactor = BlindingFactor.fromBytes(
  issuerElGamalKeyPair.privateKey().naturalBytes().toByteArray()
);

EqualityPlaintextProof clawbackProof = equalityProofGen.generateProof(
  issuerBalanceCiphertext,           // EncryptedAmount - from MPToken.issuerEncryptedBalance
  issuerElGamalKeyPair.publicKey(),  // ElGamalPublicKey
  clawbackAmount,                    // UnsignedLong
  issuerPrivateKeyAsBlindingFactor,  // BlindingFactor - issuer's PRIVATE KEY
  context                            // ConfidentialMPTClawbackContext
);

String zkProofHex = clawbackProof.hexValue();
```

---

## Decryption

```java
JavaElGamalBalanceDecryptor decryptor = new JavaElGamalBalanceDecryptor();

ElGamalCiphertext ciphertext = ElGamalCiphertext.fromBytes(
  BaseEncoding.base16().decode(encryptedHex)
);

// Decrypt with search range [minAmount, maxAmount]
// The decryptor uses baby-step giant-step algorithm to find the plaintext
long decryptedAmount = decryptor.decrypt(ciphertext, privateKey, 0, 1_000_000);
```

---

## Sample Integration Test

`xrpl4j-integration-tests/src/test/java/org/xrpl/xrpl4j/tests/ConfidentialTransfersIT.java`

## Using in your project

Run `mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true` to build the JAR. Then add the
following dependency to your `pom.xml`:

```xml

<dependency>
  <groupId>org.xrpl</groupId>
  <artifactId>xrpl4j-core</artifactId>
  <version>HEAD-SNAPSHOT</version>
</dependency>
```

