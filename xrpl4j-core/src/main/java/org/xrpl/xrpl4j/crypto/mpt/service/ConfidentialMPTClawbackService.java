package org.xrpl.xrpl4j.crypto.mpt.service;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
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

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTContextUtil;
import org.xrpl.xrpl4j.crypto.mpt.models.ConfidentialMPTClawbackProof;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ConfidentialMPTClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ConfidentialMPTClawbackProofVerifier;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.bc.BcConfidentialMPTClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.bc.BcConfidentialMPTClawbackProofVerifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Objects;

/**
 * Service for generating and verifying ConfidentialMPTClawback proofs.
 *
 * <p>This service provides high-level methods for:</p>
 * <ul>
 *   <li>Generating context hashes for clawback transactions</li>
 *   <li>Generating ConfidentialMPTClawback proofs</li>
 *   <li>Verifying ConfidentialMPTClawback proofs</li>
 * </ul>
 */
public class ConfidentialMPTClawbackService {

  private final ConfidentialMPTClawbackProofGenerator proofGenerator;
  private final ConfidentialMPTClawbackProofVerifier proofVerifier;

  /**
   * Creates a new instance with default BouncyCastle implementations.
   */
  public ConfidentialMPTClawbackService() {
    this(
      new BcConfidentialMPTClawbackProofGenerator(),
      new BcConfidentialMPTClawbackProofVerifier()
    );
  }

  /**
   * Creates a new instance with custom implementations.
   *
   * @param proofGenerator The proof generator to use.
   * @param proofVerifier  The proof verifier to use.
   */
  public ConfidentialMPTClawbackService(
    final ConfidentialMPTClawbackProofGenerator proofGenerator,
    final ConfidentialMPTClawbackProofVerifier proofVerifier
  ) {
    this.proofGenerator = Objects.requireNonNull(proofGenerator, "proofGenerator must not be null");
    this.proofVerifier = Objects.requireNonNull(proofVerifier, "proofVerifier must not be null");
  }

  /**
   * Generates a context hash for a ConfidentialMPTClawback transaction.
   *
   * @param account    The issuer's XRPL account address.
   * @param sequence   The issuer's account sequence number.
   * @param issuanceId The MPT issuance ID.
   * @param amount     The amount being clawed back.
   * @param holder     The holder account from which tokens are being clawed back.
   *
   * @return A {@link ConfidentialMPTClawbackContext} containing the 32-byte context hash.
   */
  public ConfidentialMPTClawbackContext generateContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedLong amount,
    final Address holder
  ) {
    return ConfidentialMPTContextUtil.generateClawbackContext(account, sequence, issuanceId, amount, holder);
  }

  /**
   * Generates a ConfidentialMPTClawback proof.
   *
   * <p>The proof is a plaintext equality proof that proves the issuer knows
   * the plaintext amount being clawed back from the holder's IssuerEncryptedBalance.</p>
   *
   * @param issuerEncryptedBalance The IssuerEncryptedBalance ciphertext from the MPToken.
   * @param issuerPublicKey        The issuer's ElGamal public key.
   * @param amount                 The amount being clawed back.
   * @param issuerPrivateKey       The issuer's ElGamal private key.
   * @param context                The context hash binding the proof to a specific transaction.
   *
   * @return A {@link ConfidentialMPTClawbackProof} containing the 98-byte proof.
   */
  public ConfidentialMPTClawbackProof generateProof(
    final org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final PrivateKey issuerPrivateKey,
    final ConfidentialMPTClawbackContext context
  ) {
    return proofGenerator.generateProof(issuerEncryptedBalance, issuerPublicKey, amount, issuerPrivateKey, context);
  }

  /**
   * Verifies a ConfidentialMPTClawback proof.
   *
   * @param proof                  The proof to verify.
   * @param issuerEncryptedBalance The IssuerEncryptedBalance ciphertext from the MPToken.
   * @param issuerPublicKey        The issuer's ElGamal public key.
   * @param amount                 The amount being clawed back.
   * @param context                The context hash binding the proof to a specific transaction.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  public boolean verifyProof(
    final ConfidentialMPTClawbackProof proof,
    final ElGamalCiphertext issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final ConfidentialMPTClawbackContext context
  ) {
    return proofVerifier.verifyProof(proof, issuerEncryptedBalance, issuerPublicKey, amount, context);
  }
}

