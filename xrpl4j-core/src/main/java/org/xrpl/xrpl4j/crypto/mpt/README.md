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
BlindingFactor nonce = BlindingFactor.generate();

SecretKeyProof zkProof = proofGenerator.generateProof(
  holderElGamalKeyPair.privateKey(),  // ElGamalPrivateKey
  context,                             // ConfidentialMPTConvertContext
  nonce                                // BlindingFactor
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

BlindingFactor nonceKm = BlindingFactor.generate();
BlindingFactor nonceKrSender = BlindingFactor.generate();
BlindingFactor nonceKrDest = BlindingFactor.generate();
BlindingFactor nonceKrIssuer = BlindingFactor.generate();

SamePlaintextParticipant senderParticipant = SamePlaintextParticipant.forProofGeneration(
  senderCiphertext, senderPublicKey, sendBlindingFactorSender, nonceKrSender
);
SamePlaintextParticipant destParticipant = SamePlaintextParticipant.forProofGeneration(
  destCiphertext, destPublicKey, sendBlindingFactorDest, nonceKrDest
);
SamePlaintextParticipant issuerParticipant = SamePlaintextParticipant.forProofGeneration(
  issuerCiphertext, issuerPublicKey, sendBlindingFactorIssuer, nonceKrIssuer
);

SamePlaintextMultiProof samePlaintextProof = samePlaintextGen.generateProof(
  sendAmount,           // UnsignedLong
  senderParticipant,    // SamePlaintextParticipant
  destParticipant,      // SamePlaintextParticipant
  issuerParticipant,    // SamePlaintextParticipant
  Optional.empty(),     // Optional<SamePlaintextParticipant> auditor
  sendContext,          // ConfidentialMPTSendContext
  nonceKm               // BlindingFactor
);
```

**Step 4: Generate Pedersen commitments**

```java
JavaPedersenCommitmentGenerator pedersenGen = new JavaPedersenCommitmentGenerator();

BlindingFactor amountPedersenRho = BlindingFactor.generate();
BlindingFactor balancePedersenRho = BlindingFactor.generate();

PedersenCommitment amountCommitment = pedersenGen.generateCommitment(sendAmount, amountPedersenRho);
PedersenCommitment balanceCommitment = pedersenGen.generateCommitment(senderCurrentBalance, balancePedersenRho);
```

**Step 5: Generate Amount Linkage Proof**

```java
JavaElGamalPedersenLinkProofGenerator linkProofGen = new JavaElGamalPedersenLinkProofGenerator();

ElGamalPedersenLinkProof amountLinkageProof = linkProofGen.generateProof(
  LinkageProofType.AMOUNT_COMMITMENT,
  senderCiphertext,            // ElGamalCiphertext
  senderPublicKey,             // ElGamalPublicKey
  amountCommitment,            // PedersenCommitment
  sendAmount,                  // UnsignedLong
  sendBlindingFactorSender,    // BlindingFactor - same used for encryption
  amountPedersenRho,           // BlindingFactor - Pedersen blinding factor
  BlindingFactor.generate(),   // nonceKm
  BlindingFactor.generate(),   // nonceKr
  BlindingFactor.generate(),   // nonceKrho
  sendContext                  // ConfidentialMPTSendContext
);
```

**Step 6: Generate Balance Linkage Proof**

```java
// Get current balance ciphertext from ledger
ElGamalCiphertext currentBalanceCiphertext = ElGamalCiphertext.fromBytes(
    BaseEncoding.base16().decode(mpToken.confidentialBalanceSpending().get())
  );

// IMPORTANT: Use private key as blinding factor for BALANCE_COMMITMENT
BlindingFactor privateKeyAsBlindingFactor = BlindingFactor.fromBytes(
  senderElGamalKeyPair.privateKey().naturalBytes().toByteArray()
);

ElGamalPedersenLinkProof balanceLinkageProof = linkProofGen.generateProof(
  LinkageProofType.BALANCE_COMMITMENT,
  currentBalanceCiphertext,      // ElGamalCiphertext - from ledger
  senderPublicKey,               // ElGamalPublicKey
  balanceCommitment,             // PedersenCommitment
  senderCurrentBalance,          // UnsignedLong
  privateKeyAsBlindingFactor,    // BlindingFactor - PRIVATE KEY
  balancePedersenRho,            // BlindingFactor
  BlindingFactor.generate(),     // nonceKm
  BlindingFactor.generate(),     // nonceKr
  BlindingFactor.generate(),     // nonceKrho
  sendContext                    // ConfidentialMPTSendContext
);
```

**Step 7: Combine proofs**

```java
String fullZkProofHex = ZKProofUtils.combineSendProofsHex(
  samePlaintextProof, amountLinkageProof, balanceLinkageProof
);

String amountCommitmentHex = amountCommitment.toReversedHex64();
String balanceCommitmentHex = balanceCommitment.toReversedHex64();
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
BlindingFactor convertBackPedersenRho = BlindingFactor.generate();
PedersenCommitment balanceCommitment = pedersenGen.generateCommitment(currentBalance, convertBackPedersenRho);
```

**Step 4: Generate Balance Linkage Proof**

```java
ElGamalCiphertext currentBalanceCiphertext = ElGamalCiphertext.fromBytes(
  BaseEncoding.base16().decode(mpToken.confidentialBalanceSpending().get())
);

BlindingFactor privateKeyAsBlindingFactor = BlindingFactor.fromBytes(
  holderElGamalKeyPair.privateKey().naturalBytes().toByteArray()
);

ElGamalPedersenLinkProof balanceLinkageProof = linkProofGen.generateProof(
  LinkageProofType.BALANCE_COMMITMENT,
  currentBalanceCiphertext,
  holderPublicKey,
  balanceCommitment,
  currentBalance,
  privateKeyAsBlindingFactor,
  convertBackPedersenRho,
  BlindingFactor.generate(),
  BlindingFactor.generate(),
  BlindingFactor.generate(),
  context
);

String zkProofHex = balanceLinkageProof.hexValue();
String balanceCommitmentHex = balanceCommitment.toReversedHex64();
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

BlindingFactor issuerPrivateKeyAsBlindingFactor = BlindingFactor.fromBytes(
  issuerElGamalKeyPair.privateKey().naturalBytes().toByteArray()
);
BlindingFactor clawbackNonce = BlindingFactor.generate();

EqualityPlaintextProof clawbackProof = equalityProofGen.generateProof(
  issuerBalanceCiphertext,           // ElGamalCiphertext - from MPToken.issuerEncryptedBalance
  issuerElGamalKeyPair.publicKey(),  // ElGamalPublicKey
  clawbackAmount,                    // UnsignedLong
  issuerPrivateKeyAsBlindingFactor,  // BlindingFactor - issuer's PRIVATE KEY
  clawbackNonce,                     // BlindingFactor
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
long decryptedAmount = decryptor.decrypt(ciphertext, privateKey);
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

