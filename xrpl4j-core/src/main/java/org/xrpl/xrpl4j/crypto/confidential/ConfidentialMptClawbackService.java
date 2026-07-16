package org.xrpl.xrpl4j.crypto.confidential;

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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptClawbackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.ContextHashGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptClawbackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaContextHashGenerator;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Objects;

/**
 * Service for generating and verifying ConfidentialMptClawback proofs.
 *
 * <p>This service provides high-level methods for:</p>
 * <ul>
 *   <li>Generating context hashes for clawback transactions</li>
 *   <li>Generating ConfidentialMptClawback proofs</li>
 *   <li>Verifying ConfidentialMptClawback proofs</li>
 * </ul>
 */
public class ConfidentialMptClawbackService {

  private final ContextHashGenerator contextHashGenerator;
  private final ConfidentialMptClawbackProofGenerator proofGenerator;
  private final ConfidentialMptClawbackProofVerifier proofVerifier;

  /**
   * Creates a new instance with default JNA implementations.
   */
  public ConfidentialMptClawbackService() {
    this(
      new JnaContextHashGenerator(),
      new JnaConfidentialMptClawbackProofGenerator(),
      new JnaConfidentialMptClawbackProofVerifier()
    );
  }

  /**
   * Creates a new instance with custom implementations.
   *
   * @param contextHashGenerator The context hash generator to use.
   * @param proofGenerator       The proof generator to use.
   * @param proofVerifier        The proof verifier to use.
   */
  public ConfidentialMptClawbackService(
    final ContextHashGenerator contextHashGenerator,
    final ConfidentialMptClawbackProofGenerator proofGenerator,
    final ConfidentialMptClawbackProofVerifier proofVerifier
  ) {
    this.contextHashGenerator = Objects.requireNonNull(contextHashGenerator, "contextHashGenerator must not be null");
    this.proofGenerator = Objects.requireNonNull(proofGenerator, "proofGenerator must not be null");
    this.proofVerifier = Objects.requireNonNull(proofVerifier, "proofVerifier must not be null");
  }

  /**
   * Generates a context hash for a ConfidentialMptClawback transaction.
   *
   * @param account    The issuer's XRPL account address.
   * @param sequence   The issuer's account sequence number.
   * @param issuanceId The MPT issuance ID.
   * @param holder     The holder account from which tokens are being clawed back.
   *
   * @return A {@link ConfidentialMptClawbackContext} containing the 32-byte context hash.
   */
  public ConfidentialMptClawbackContext generateContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final Address holder
  ) {
    return contextHashGenerator.generateClawbackContext(account, sequence, issuanceId, holder);
  }

  /**
   * Generates a ConfidentialMptClawback proof.
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
   * @return A {@link ConfidentialMptClawbackProof} containing the 64-byte compact sigma proof.
   */
  public ConfidentialMptClawbackProof generateProof(
    final EncryptedAmount issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final PrivateKey issuerPrivateKey,
    final ConfidentialMptClawbackContext context
  ) {
    return proofGenerator.generateProof(issuerEncryptedBalance, issuerPublicKey, amount, issuerPrivateKey, context);
  }

  /**
   * Verifies a ConfidentialMptClawback proof.
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
    final ConfidentialMptClawbackProof proof,
    final EncryptedAmount issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final ConfidentialMptClawbackContext context
  ) {
    return proofVerifier.verifyProof(proof, issuerEncryptedBalance, issuerPublicKey, amount, context);
  }
}

