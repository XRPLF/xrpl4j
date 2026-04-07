package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptClawbackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptConvertBackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptConvertBackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptSendProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptConvertProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptConvertProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcMptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcMptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcPedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptClawbackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptConvertBackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptConvertBackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptSendProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptConvertProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptConvertProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaPedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Benchmark comparing BouncyCastle (BC) and JNA (native C) implementations of Confidential MPT
 * cryptographic operations. Runs each operation 20 times and reports average timings.
 */
class ConfidentialMptBenchmarkIT {

  private static final int ITERATIONS = 100;
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(1000);
  private static final UnsignedLong BALANCE = UnsignedLong.valueOf(5000);
  private static final UnsignedLong DECRYPT_MAX = UnsignedLong.valueOf(10000);

  // Shared test fixtures
  private static KeyPair senderKeyPair;
  private static KeyPair destKeyPair;
  private static KeyPair issuerKeyPair;
  private static BlindingFactor blindingFactor;
  private static ConfidentialMptConvertContext convertContext;
  private static ConfidentialMptSendContext sendContext;
  private static ConfidentialMptConvertBackContext convertBackContext;
  private static ConfidentialMptClawbackContext clawbackContext;

  @BeforeAll
  static void setUp() {
    senderKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    destKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    issuerKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    blindingFactor = new SecureRandomBlindingFactorGenerator().generate();

    // Create dummy 32-byte context hashes for benchmarking
    byte[] dummyHash = new byte[32];
    Arrays.fill(dummyHash, (byte) 0x42);
    UnsignedByteArray hashBytes = UnsignedByteArray.of(dummyHash);
    convertContext = ConfidentialMptConvertContext.of(hashBytes);
    sendContext = ConfidentialMptSendContext.of(hashBytes);
    convertBackContext = ConfidentialMptConvertBackContext.of(hashBytes);
    clawbackContext = ConfidentialMptClawbackContext.of(hashBytes);
  }

  @Test
  void benchmarkAllOperations() {
    Map<String, long[]> results = new LinkedHashMap<>();

    // --- Encryption ---
    results.put("Encrypt", benchmarkEncrypt());

    // --- Decryption ---
    results.put("Decrypt", benchmarkDecrypt());

    // --- Convert Proof Generate ---
    results.put("Convert Proof Gen", benchmarkConvertProofGen());

    // --- Send Proof Generate ---
    results.put("Send Proof Gen", benchmarkSendProofGen());

    // --- ConvertBack Proof Generate ---
    results.put("ConvertBack Proof Gen", benchmarkConvertBackProofGen());

    // --- Clawback Proof Generate ---
    results.put("Clawback Proof Gen", benchmarkClawbackProofGen());

    // --- Print Summary ---
    printSummary(results);
  }

  // ========================= Benchmark Methods =========================

  private long[] benchmarkEncrypt() {
    MptAmountEncryptor bcEnc = new BcMptAmountEncryptor();
    MptAmountEncryptor jnaEnc = new JnaMptAmountEncryptor();

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      BlindingFactor bf = new SecureRandomBlindingFactorGenerator().generate();

      long start = System.nanoTime();
      bcEnc.encrypt(AMOUNT, senderKeyPair.publicKey(), bf);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaEnc.encrypt(AMOUNT, senderKeyPair.publicKey(), bf);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkDecrypt() {
    MptAmountEncryptor enc = new JnaMptAmountEncryptor();
    MptAmountDecryptor bcDec = new BcMptAmountDecryptor();
    MptAmountDecryptor jnaDec = new JnaMptAmountDecryptor();

    EncryptedAmount ciphertext = enc.encrypt(AMOUNT, senderKeyPair.publicKey(), blindingFactor);

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      long start = System.nanoTime();
      bcDec.decrypt(ciphertext, senderKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaDec.decrypt(ciphertext, senderKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkConvertProofGen() {
    BcConfidentialMptConvertProofGenerator bcGen = new BcConfidentialMptConvertProofGenerator();
    JnaConfidentialMptConvertProofGenerator jnaGen = new JnaConfidentialMptConvertProofGenerator();

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      long start = System.nanoTime();
      bcGen.generateProof(senderKeyPair, convertContext);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaGen.generateProof(senderKeyPair, convertContext);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkConvertProofVerify() {
    JnaConfidentialMptConvertProofGenerator jnaGen = new JnaConfidentialMptConvertProofGenerator();
    BcConfidentialMptConvertProofVerifier bcVerifier = new BcConfidentialMptConvertProofVerifier();
    JnaConfidentialMptConvertProofVerifier jnaVerifier = new JnaConfidentialMptConvertProofVerifier();

    ConfidentialMptConvertProof proof = jnaGen.generateProof(senderKeyPair, convertContext);

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      long start = System.nanoTime();
      bcVerifier.verifyProof(proof, senderKeyPair.publicKey(), convertContext);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaVerifier.verifyProof(proof, senderKeyPair.publicKey(), convertContext);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkSendProofGen() {
    BcConfidentialMptSendProofGenerator bcGen = new BcConfidentialMptSendProofGenerator();
    JnaConfidentialMptSendProofGenerator jnaGen = new JnaConfidentialMptSendProofGenerator();
    BcPedersenCommitmentGenerator bcCommitGen = new BcPedersenCommitmentGenerator();
    JnaPedersenCommitmentGenerator jnaCommitGen = new JnaPedersenCommitmentGenerator();
    JnaMptAmountEncryptor jnaEnc = new JnaMptAmountEncryptor();
    JnaBlindingFactorGenerator jnaBfGen = new JnaBlindingFactorGenerator();

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      BlindingFactor sendBf = jnaBfGen.generate();
      EncryptedAmount senderCt = jnaEnc.encrypt(AMOUNT, senderKeyPair.publicKey(), sendBf);
      EncryptedAmount destCt = jnaEnc.encrypt(AMOUNT, destKeyPair.publicKey(), sendBf);
      EncryptedAmount issuerCt = jnaEnc.encrypt(AMOUNT, issuerKeyPair.publicKey(), sendBf);

      List<MptConfidentialParty> recipients = Arrays.asList(
        MptConfidentialParty.of(senderKeyPair.publicKey(), senderCt),
        MptConfidentialParty.of(destKeyPair.publicKey(), destCt),
        MptConfidentialParty.of(issuerKeyPair.publicKey(), issuerCt)
      );

      BlindingFactor amountBf = jnaBfGen.generate();
      BlindingFactor balanceBf = jnaBfGen.generate();

      // Balance ciphertext (simulated spending balance)
      BlindingFactor balanceEncBf = jnaBfGen.generate();
      EncryptedAmount balanceCt = jnaEnc.encrypt(BALANCE, senderKeyPair.publicKey(), balanceEncBf);

      // BC Pedersen params
      PedersenCommitment bcAmtCommit = bcCommitGen.generateCommitment(AMOUNT, amountBf);
      PedersenProofParams bcAmountParams = PedersenProofParams.builder()
        .pedersenCommitment(bcAmtCommit.value()).amount(AMOUNT)
        .encryptedAmount(senderCt).blindingFactor(amountBf).build();
      PedersenCommitment bcBalCommit = bcCommitGen.generateCommitment(BALANCE, balanceBf);
      PedersenProofParams bcBalanceParams = PedersenProofParams.builder()
        .pedersenCommitment(bcBalCommit.value()).amount(BALANCE)
        .encryptedAmount(balanceCt).blindingFactor(balanceBf).build();

      long start = System.nanoTime();
      bcGen.generateProof(senderKeyPair, AMOUNT, recipients, sendBf, sendContext, bcAmountParams, bcBalanceParams);
      bcTotal += System.nanoTime() - start;

      // JNA Pedersen params
      PedersenCommitment jnaAmtCommit = jnaCommitGen.generateCommitment(AMOUNT, amountBf);
      PedersenProofParams jnaAmountParams = PedersenProofParams.builder()
        .pedersenCommitment(jnaAmtCommit.value()).amount(AMOUNT)
        .encryptedAmount(senderCt).blindingFactor(amountBf).build();
      PedersenCommitment jnaBalCommit = jnaCommitGen.generateCommitment(BALANCE, balanceBf);
      PedersenProofParams jnaBalanceParams = PedersenProofParams.builder()
        .pedersenCommitment(jnaBalCommit.value()).amount(BALANCE)
        .encryptedAmount(balanceCt).blindingFactor(balanceBf).build();

      start = System.nanoTime();
      jnaGen.generateProof(senderKeyPair, AMOUNT, recipients, sendBf, sendContext, jnaAmountParams, jnaBalanceParams);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkSendProofVerify() {
    BcConfidentialMptSendProofVerifier bcVerifier = new BcConfidentialMptSendProofVerifier();
    JnaConfidentialMptSendProofVerifier jnaVerifier = new JnaConfidentialMptSendProofVerifier();
    JnaConfidentialMptSendProofGenerator jnaGen = new JnaConfidentialMptSendProofGenerator();
    JnaPedersenCommitmentGenerator jnaCommitGen = new JnaPedersenCommitmentGenerator();
    JnaMptAmountEncryptor jnaEnc = new JnaMptAmountEncryptor();
    JnaBlindingFactorGenerator jnaBfGen = new JnaBlindingFactorGenerator();

    // Pre-generate a proof to verify repeatedly
    BlindingFactor sendBf = jnaBfGen.generate();
    EncryptedAmount senderCt = jnaEnc.encrypt(AMOUNT, senderKeyPair.publicKey(), sendBf);
    EncryptedAmount destCt = jnaEnc.encrypt(AMOUNT, destKeyPair.publicKey(), sendBf);
    EncryptedAmount issuerCt = jnaEnc.encrypt(AMOUNT, issuerKeyPair.publicKey(), sendBf);
    List<MptConfidentialParty> recipients = Arrays.asList(
      MptConfidentialParty.of(senderKeyPair.publicKey(), senderCt),
      MptConfidentialParty.of(destKeyPair.publicKey(), destCt),
      MptConfidentialParty.of(issuerKeyPair.publicKey(), issuerCt)
    );
    BlindingFactor amountBf = jnaBfGen.generate();
    BlindingFactor balanceBf = jnaBfGen.generate();
    BlindingFactor balanceEncBf = jnaBfGen.generate();
    EncryptedAmount balanceCt = jnaEnc.encrypt(BALANCE, senderKeyPair.publicKey(), balanceEncBf);
    PedersenCommitment amtCommit = jnaCommitGen.generateCommitment(AMOUNT, amountBf);
    PedersenProofParams amountParams = PedersenProofParams.builder()
      .pedersenCommitment(amtCommit.value()).amount(AMOUNT)
      .encryptedAmount(senderCt).blindingFactor(amountBf).build();
    PedersenCommitment balCommit = jnaCommitGen.generateCommitment(BALANCE, balanceBf);
    PedersenProofParams balanceParams = PedersenProofParams.builder()
      .pedersenCommitment(balCommit.value()).amount(BALANCE)
      .encryptedAmount(balanceCt).blindingFactor(balanceBf).build();
    ConfidentialMptSendProof proof = jnaGen.generateProof(
      senderKeyPair, AMOUNT, recipients, sendBf, sendContext, amountParams, balanceParams
    );

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      long start = System.nanoTime();
      bcVerifier.verifyProof(
        proof, recipients, balanceCt, sendContext,
        PedersenCommitment.of(amountParams.pedersenCommitment()),
        PedersenCommitment.of(balanceParams.pedersenCommitment())
      );
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaVerifier.verifyProof(
        proof, recipients, balanceCt, sendContext,
        PedersenCommitment.of(amountParams.pedersenCommitment()),
        PedersenCommitment.of(balanceParams.pedersenCommitment())
      );
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkConvertBackProofVerify() {
    BcConfidentialMptConvertBackProofVerifier bcVerifier = new BcConfidentialMptConvertBackProofVerifier();
    JnaConfidentialMptConvertBackProofVerifier jnaVerifier = new JnaConfidentialMptConvertBackProofVerifier();
    JnaConfidentialMptConvertBackProofGenerator jnaGen = new JnaConfidentialMptConvertBackProofGenerator();
    JnaPedersenCommitmentGenerator jnaCommitGen = new JnaPedersenCommitmentGenerator();
    JnaMptAmountEncryptor jnaEnc = new JnaMptAmountEncryptor();
    JnaBlindingFactorGenerator jnaBfGen = new JnaBlindingFactorGenerator();

    // Pre-generate a proof to verify repeatedly
    BlindingFactor encBf = jnaBfGen.generate();
    EncryptedAmount balanceCt = jnaEnc.encrypt(BALANCE, senderKeyPair.publicKey(), encBf);
    BlindingFactor commitBf = jnaBfGen.generate();
    PedersenCommitment commitment = jnaCommitGen.generateCommitment(BALANCE, commitBf);
    PedersenProofParams balanceParams = PedersenProofParams.builder()
      .pedersenCommitment(commitment.value()).amount(BALANCE)
      .encryptedAmount(balanceCt).blindingFactor(commitBf).build();
    ConfidentialMptConvertBackProof proof = jnaGen.generateProof(
      senderKeyPair, AMOUNT, convertBackContext, balanceParams
    );

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      long start = System.nanoTime();
      bcVerifier.verifyProof(proof, senderKeyPair.publicKey(), balanceCt, commitment, AMOUNT, convertBackContext);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaVerifier.verifyProof(proof, senderKeyPair.publicKey(), balanceCt, commitment, AMOUNT, convertBackContext);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkConvertBackProofGen() {
    BcConfidentialMptConvertBackProofGenerator bcGen = new BcConfidentialMptConvertBackProofGenerator();
    JnaConfidentialMptConvertBackProofGenerator jnaGen = new JnaConfidentialMptConvertBackProofGenerator();
    JnaPedersenCommitmentGenerator jnaCommitGen = new JnaPedersenCommitmentGenerator();
    JnaMptAmountEncryptor jnaEnc = new JnaMptAmountEncryptor();
    JnaBlindingFactorGenerator jnaBfGen = new JnaBlindingFactorGenerator();

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      BlindingFactor encBf = jnaBfGen.generate();
      EncryptedAmount balanceCt = jnaEnc.encrypt(BALANCE, senderKeyPair.publicKey(), encBf);
      BlindingFactor commitBf = jnaBfGen.generate();
      PedersenCommitment commitment = jnaCommitGen.generateCommitment(BALANCE, commitBf);
      PedersenProofParams balanceParams = PedersenProofParams.builder()
        .pedersenCommitment(commitment.value()).amount(BALANCE)
        .encryptedAmount(balanceCt).blindingFactor(commitBf).build();

      long start = System.nanoTime();
      bcGen.generateProof(senderKeyPair, AMOUNT, convertBackContext, balanceParams);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaGen.generateProof(senderKeyPair, AMOUNT, convertBackContext, balanceParams);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkClawbackProofGen() {
    BcConfidentialMptClawbackProofGenerator bcGen = new BcConfidentialMptClawbackProofGenerator();
    JnaConfidentialMptClawbackProofGenerator jnaGen = new JnaConfidentialMptClawbackProofGenerator();
    JnaMptAmountEncryptor jnaEnc = new JnaMptAmountEncryptor();
    JnaBlindingFactorGenerator jnaBfGen = new JnaBlindingFactorGenerator();

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      BlindingFactor bf = jnaBfGen.generate();
      EncryptedAmount ct = jnaEnc.encrypt(AMOUNT, issuerKeyPair.publicKey(), bf);

      long start = System.nanoTime();
      bcGen.generateProof(ct, issuerKeyPair.publicKey(), AMOUNT, issuerKeyPair.privateKey(), clawbackContext);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaGen.generateProof(ct, issuerKeyPair.publicKey(), AMOUNT, issuerKeyPair.privateKey(), clawbackContext);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  private long[] benchmarkClawbackProofVerify() {
    JnaConfidentialMptClawbackProofGenerator jnaGen = new JnaConfidentialMptClawbackProofGenerator();
    BcConfidentialMptClawbackProofVerifier bcVerifier = new BcConfidentialMptClawbackProofVerifier();
    JnaConfidentialMptClawbackProofVerifier jnaVerifier = new JnaConfidentialMptClawbackProofVerifier();
    JnaMptAmountEncryptor jnaEnc = new JnaMptAmountEncryptor();

    EncryptedAmount ct = jnaEnc.encrypt(AMOUNT, issuerKeyPair.publicKey(), blindingFactor);
    ConfidentialMptClawbackProof proof = jnaGen.generateProof(
      ct, issuerKeyPair.publicKey(), AMOUNT, issuerKeyPair.privateKey(), clawbackContext
    );

    long bcTotal = 0;
    long jnaTotal = 0;
    for (int i = 0; i < ITERATIONS; i++) {
      long start = System.nanoTime();
      bcVerifier.verifyProof(proof, ct, issuerKeyPair.publicKey(), AMOUNT, clawbackContext);
      bcTotal += System.nanoTime() - start;

      start = System.nanoTime();
      jnaVerifier.verifyProof(proof, ct, issuerKeyPair.publicKey(), AMOUNT, clawbackContext);
      jnaTotal += System.nanoTime() - start;
    }
    return new long[]{bcTotal / ITERATIONS, jnaTotal / ITERATIONS};
  }

  // ========================= Output =========================

  private void printSummary(Map<String, long[]> results) {
    System.out.println();
    System.out.println(new String(new char[85]).replace('\0', '='));
    System.out.println("  Confidential MPT Cryptographic Operations: BC vs JNA Performance Comparison");
    System.out.println("  Iterations per operation: " + ITERATIONS);
    System.out.println(new String(new char[85]).replace('\0', '='));
    System.out.printf("%-25s | %15s | %15s | %10s | %s%n",
      "Operation", "BC Avg (ms)", "JNA Avg (ms)", "Speedup", "Winner");
    System.out.println(new String(new char[85]).replace('\0', '-'));

    for (Map.Entry<String, long[]> entry : results.entrySet()) {
      String op = entry.getKey();
      double bcMs = entry.getValue()[0] / 1_000_000.0;
      double jnaMs = entry.getValue()[1] / 1_000_000.0;
      double speedup = bcMs / jnaMs;
      String winner = jnaMs < bcMs ? "JNA" : "BC";

      System.out.printf("%-25s | %12.3f ms | %12.3f ms | %9.1fx | %s%n",
        op, bcMs, jnaMs, speedup, winner);
    }

    System.out.println(new String(new char[85]).replace('\0', '-'));
    System.out.println();
    System.out.println("| Operation | BC Avg (ms) | JNA Avg (ms) | Speedup | Winner |");
    System.out.println("|---|---|---|---|---|");
    for (Map.Entry<String, long[]> entry : results.entrySet()) {
      String op = entry.getKey();
      double bcMs = entry.getValue()[0] / 1_000_000.0;
      double jnaMs = entry.getValue()[1] / 1_000_000.0;
      double speedup = bcMs / jnaMs;
      String winner = jnaMs < bcMs ? "JNA" : "BC";
      System.out.printf("| %s | %.3f | %.3f | %.1fx | %s |%n", op, bcMs, jnaMs, speedup, winner);
    }
    System.out.println();
  }
}
