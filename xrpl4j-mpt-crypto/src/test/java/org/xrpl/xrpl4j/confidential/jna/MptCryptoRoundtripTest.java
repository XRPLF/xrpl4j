package org.xrpl.xrpl4j.confidential.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: mpt-crypto
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.util.Arrays;

/**
 * Roundtrip tests for the mpt-crypto native library via JNA. These tests verify that the native
 * shared library loads correctly and produces valid results on the current platform.
 *
 * <p>No BouncyCastle or pure-Java implementations are involved here — all operations go through
 * the native C library via {@link MptCryptoImpl}.</p>
 */
class MptCryptoRoundtripTest {

  private static final int BLINDING_FACTOR_SIZE = 32;
  private static final int PUBKEY_SIZE = 33;
  private static final int PRIVKEY_SIZE = 32;
  private static final int CIPHERTEXT_SIZE = 66;
  private static final int COMMITMENT_SIZE = 33;
  private static final int CONVERT_PROOF_SIZE = 65;
  private static final int CLAWBACK_PROOF_SIZE = 98;
  private static final int CONVERT_BACK_PROOF_SIZE = 883;

  private static MptCryptoImpl crypto;
  private static byte[] pubkey;
  private static byte[] privkey;

  @BeforeAll
  static void setUp() {
    crypto = new MptCryptoImpl();

    KeyPair keyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    pubkey = keyPair.publicKey().value().toByteArray();
    privkey = keyPair.privateKey().naturalBytes().toByteArray();

    assertThat(pubkey).hasSize(PUBKEY_SIZE);
    assertThat(privkey).hasSize(PRIVKEY_SIZE);
  }

  @Test
  void nativeLibraryLoads() {
    // If we get here without UnsatisfiedLinkError, the native library loaded successfully.
    assertThat(MptCryptoLibrary.INSTANCE).isNotNull();
  }

  @Test
  void blindingFactorGeneration() {
    byte[] factor = new byte[BLINDING_FACTOR_SIZE];
    int result = crypto.generateBlindingFactor(factor);

    assertThat(result).isZero();
    // Should not be all zeros (astronomically unlikely for a random 32-byte value)
    assertThat(factor).isNotEqualTo(new byte[BLINDING_FACTOR_SIZE]);
  }

  @Test
  void blindingFactorGenerationProducesDifferentValues() {
    byte[] factor1 = new byte[BLINDING_FACTOR_SIZE];
    byte[] factor2 = new byte[BLINDING_FACTOR_SIZE];

    assertThat(crypto.generateBlindingFactor(factor1)).isZero();
    assertThat(crypto.generateBlindingFactor(factor2)).isZero();

    assertThat(factor1).isNotEqualTo(factor2);
  }

  @Test
  void encryptDecryptRoundtrip() {
    long amount = 12345L;

    byte[] blindingFactor = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(blindingFactor)).isZero();

    // Encrypt
    byte[] ciphertext = new byte[CIPHERTEXT_SIZE];
    int encResult = crypto.encryptAmount(amount, pubkey, blindingFactor, ciphertext);
    assertThat(encResult).isZero();
    assertThat(ciphertext).isNotEqualTo(new byte[CIPHERTEXT_SIZE]);

    // Decrypt
    long[] outAmount = new long[1];
    int decResult = crypto.decryptAmount(ciphertext, privkey, outAmount);
    assertThat(decResult).isZero();
    assertThat(outAmount[0]).isEqualTo(amount);
  }

  @Test
  void encryptDecryptZeroAmount() {
    long amount = 0L;

    byte[] blindingFactor = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(blindingFactor)).isZero();

    byte[] ciphertext = new byte[CIPHERTEXT_SIZE];
    assertThat(crypto.encryptAmount(amount, pubkey, blindingFactor, ciphertext)).isZero();

    long[] outAmount = new long[1];
    assertThat(crypto.decryptAmount(ciphertext, privkey, outAmount)).isZero();
    assertThat(outAmount[0]).isEqualTo(amount);
  }

  @Test
  void encryptDecryptLargeAmount() {
    long amount = 1_000_000L;

    byte[] blindingFactor = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(blindingFactor)).isZero();

    byte[] ciphertext = new byte[CIPHERTEXT_SIZE];
    assertThat(crypto.encryptAmount(amount, pubkey, blindingFactor, ciphertext)).isZero();

    long[] outAmount = new long[1];
    assertThat(crypto.decryptAmount(ciphertext, privkey, outAmount)).isZero();
    assertThat(outAmount[0]).isEqualTo(amount);
  }

  @Test
  void differentBlindingFactorsProduceDifferentCiphertexts() {
    long amount = 500L;

    byte[] bf1 = new byte[BLINDING_FACTOR_SIZE];
    byte[] bf2 = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(bf1)).isZero();
    assertThat(crypto.generateBlindingFactor(bf2)).isZero();

    byte[] ct1 = new byte[CIPHERTEXT_SIZE];
    byte[] ct2 = new byte[CIPHERTEXT_SIZE];
    assertThat(crypto.encryptAmount(amount, pubkey, bf1, ct1)).isZero();
    assertThat(crypto.encryptAmount(amount, pubkey, bf2, ct2)).isZero();

    // Same amount but different blinding factors should produce different ciphertexts
    assertThat(ct1).isNotEqualTo(ct2);
  }

  @Test
  void pedersenCommitmentGeneration() {
    long amount = 42L;

    byte[] blindingFactor = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(blindingFactor)).isZero();

    byte[] commitment = new byte[COMMITMENT_SIZE];
    int result = crypto.generatePedersenCommitment(amount, blindingFactor, commitment);
    assertThat(result).isZero();
    assertThat(commitment).isNotEqualTo(new byte[COMMITMENT_SIZE]);
  }

  @Test
  void pedersenCommitmentDeterministic() {
    long amount = 100L;

    byte[] blindingFactor = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(blindingFactor)).isZero();

    byte[] commitment1 = new byte[COMMITMENT_SIZE];
    byte[] commitment2 = new byte[COMMITMENT_SIZE];
    assertThat(crypto.generatePedersenCommitment(amount, blindingFactor, commitment1)).isZero();
    assertThat(crypto.generatePedersenCommitment(amount, blindingFactor, commitment2)).isZero();

    // Same inputs must produce the same commitment
    assertThat(commitment1).isEqualTo(commitment2);
  }

  @Test
  void convertProofGenerateAndVerify() {
    byte[] ctxHash = new byte[32];
    Arrays.fill(ctxHash, (byte) 0x42);

    byte[] proof = new byte[CONVERT_PROOF_SIZE];
    int genResult = crypto.generateConvertProof(pubkey, privkey, ctxHash, proof);
    assertThat(genResult).isZero();

    int verifyResult = crypto.verifyConvertProof(proof, pubkey, ctxHash);
    assertThat(verifyResult).isZero();
  }

  @Test
  void convertProofFailsWithWrongKey() {
    byte[] ctxHash = new byte[32];
    Arrays.fill(ctxHash, (byte) 0x42);

    byte[] proof = new byte[CONVERT_PROOF_SIZE];
    assertThat(crypto.generateConvertProof(pubkey, privkey, ctxHash, proof)).isZero();

    // Verify with a different key pair should fail
    KeyPair otherKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    byte[] otherPubkey = otherKeyPair.publicKey().value().toByteArray();

    int verifyResult = crypto.verifyConvertProof(proof, otherPubkey, ctxHash);
    assertThat(verifyResult).isNotZero();
  }

  @Test
  void clawbackProofGenerateAndVerify() {
    long amount = 500L;
    byte[] ctxHash = new byte[32];
    Arrays.fill(ctxHash, (byte) 0xAB);

    // Encrypt the amount first
    byte[] blindingFactor = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(blindingFactor)).isZero();

    byte[] ciphertext = new byte[CIPHERTEXT_SIZE];
    assertThat(crypto.encryptAmount(amount, pubkey, blindingFactor, ciphertext)).isZero();

    // Generate clawback proof
    byte[] proof = new byte[CLAWBACK_PROOF_SIZE];
    int genResult = crypto.generateClawbackProof(privkey, pubkey, ctxHash, amount, ciphertext, proof);
    assertThat(genResult).isZero();

    // Split ciphertext into c1 and c2 for verification
    int verifyResult = crypto.verifyClawbackProof(proof,
      Arrays.copyOfRange(ciphertext, 0, 33),
      Arrays.copyOfRange(ciphertext, 33, 66),
      pubkey, amount, ctxHash
    );
    assertThat(verifyResult).isZero();
  }

  @Test
  void convertBackProofGenerateAndVerify() {
    long balance = 5000L;
    byte[] ctxHash = new byte[32];
    Arrays.fill(ctxHash, (byte) 0xCD);

    // Set up balance state: encrypt balance and generate commitment
    byte[] encBf = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(encBf)).isZero();

    byte[] balanceCiphertext = new byte[CIPHERTEXT_SIZE];
    assertThat(crypto.encryptAmount(balance, pubkey, encBf, balanceCiphertext)).isZero();

    byte[] commitBf = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(commitBf)).isZero();

    byte[] balanceCommitment = new byte[COMMITMENT_SIZE];
    assertThat(crypto.generatePedersenCommitment(balance, commitBf, balanceCommitment)).isZero();

    // Generate convert-back proof
    long convertBackAmount = 1000L;
    byte[] proof = new byte[CONVERT_BACK_PROOF_SIZE];
    int genResult = crypto.generateConvertBackProof(
      privkey, pubkey, ctxHash, convertBackAmount,
      balanceCommitment, balance, balanceCiphertext, commitBf,
      proof
    );
    assertThat(genResult).isZero();

    // Verify
    int verifyResult = crypto.verifyConvertBackProof(
      pubkey, ctxHash, convertBackAmount,
      balanceCiphertext, balanceCommitment, proof
    );
    assertThat(verifyResult).isZero();
  }

  @Test
  void sendProofGenerateAndVerify() {
    long sendAmount = 100L;
    byte[] ctxHash = new byte[32];
    Arrays.fill(ctxHash, (byte) 0xEF);

    // Generate recipient key pairs
    KeyPair destKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    KeyPair issuerKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();

    // Transaction blinding factor (shared across all recipient encryptions)
    byte[] txBf = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(txBf)).isZero();

    // Encrypt the amount for each party (sender, dest, issuer) using the same blinding factor
    byte[] senderCt = new byte[CIPHERTEXT_SIZE];
    byte[] destCt = new byte[CIPHERTEXT_SIZE];
    byte[] issuerCt = new byte[CIPHERTEXT_SIZE];
    assertThat(crypto.encryptAmount(sendAmount, pubkey, txBf, senderCt)).isZero();
    assertThat(crypto.encryptAmount(sendAmount, destKeyPair.publicKey().value().toByteArray(), txBf, destCt)).isZero();
    assertThat(crypto.encryptAmount(sendAmount, issuerKeyPair.publicKey().value().toByteArray(), txBf, issuerCt))
      .isZero();

    // Build recipient arrays (sender + dest + issuer = 3 recipients)
    int numRecipients = 3;
    byte[] recipientPubkeys = new byte[numRecipients * PUBKEY_SIZE];
    byte[] recipientCiphertexts = new byte[numRecipients * CIPHERTEXT_SIZE];

    System.arraycopy(pubkey, 0, recipientPubkeys, 0, PUBKEY_SIZE);
    System.arraycopy(destKeyPair.publicKey().value().toByteArray(), 0, recipientPubkeys, PUBKEY_SIZE, PUBKEY_SIZE);
    System.arraycopy(issuerKeyPair.publicKey().value().toByteArray(), 0, recipientPubkeys, 2 * PUBKEY_SIZE,
      PUBKEY_SIZE);

    System.arraycopy(senderCt, 0, recipientCiphertexts, 0, CIPHERTEXT_SIZE);
    System.arraycopy(destCt, 0, recipientCiphertexts, CIPHERTEXT_SIZE, CIPHERTEXT_SIZE);
    System.arraycopy(issuerCt, 0, recipientCiphertexts, 2 * CIPHERTEXT_SIZE, CIPHERTEXT_SIZE);

    // Amount Pedersen commitment
    byte[] amountBf = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(amountBf)).isZero();
    byte[] amountCommitment = new byte[COMMITMENT_SIZE];
    assertThat(crypto.generatePedersenCommitment(sendAmount, amountBf, amountCommitment)).isZero();

    // Balance Pedersen commitment
    long senderBalance = 5000L;
    byte[] balanceBf = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(balanceBf)).isZero();
    byte[] balanceCommitment = new byte[COMMITMENT_SIZE];
    assertThat(crypto.generatePedersenCommitment(senderBalance, balanceBf, balanceCommitment)).isZero();

    // Balance ciphertext
    byte[] balanceEncBf = new byte[BLINDING_FACTOR_SIZE];
    assertThat(crypto.generateBlindingFactor(balanceEncBf)).isZero();
    byte[] balanceCiphertext = new byte[CIPHERTEXT_SIZE];
    assertThat(crypto.encryptAmount(senderBalance, pubkey, balanceEncBf, balanceCiphertext)).isZero();

    // Generate send proof (fixed at 946 bytes)
    int proofSize = 946;
    byte[] proof = new byte[proofSize];
    int[] outLen = new int[]{proofSize};

    int genResult = crypto.generateSendProof(
      privkey, pubkey, sendAmount,
      recipientPubkeys, recipientCiphertexts, numRecipients,
      txBf, ctxHash,
      amountCommitment,
      balanceCommitment, senderBalance, balanceCiphertext, balanceBf,
      proof, outLen
    );
    assertThat(genResult).isZero();
    assertThat(outLen[0]).isEqualTo(proofSize);

    // Verify send proof — senderSpendingCiphertext is the balance ciphertext (what the sender spends from)
    int verifyResult = crypto.verifySendProof(
      recipientPubkeys, recipientCiphertexts, numRecipients,
      balanceCiphertext, ctxHash,
      amountCommitment, balanceCommitment,
      proof
    );
    assertThat(verifyResult).isZero();
  }
}
