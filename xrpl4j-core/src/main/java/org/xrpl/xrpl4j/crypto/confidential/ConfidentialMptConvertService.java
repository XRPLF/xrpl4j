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
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptConvertProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptConvertProofVerifier;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Objects;

/**
 * Service for generating and verifying Schnorr Proof of Knowledge for Confidential MPT conversions.
 *
 * <p>This service provides high-level methods for:</p>
 * <ul>
 *   <li>Generating context hashes for convert transactions</li>
 *   <li>Generating Schnorr proofs of knowledge</li>
 *   <li>Verifying Schnorr proofs of knowledge</li>
 * </ul>
 */
public class ConfidentialMptConvertService {

  private final ConfidentialMptConvertProofGenerator proofGenerator;
  private final ConfidentialMptConvertProofVerifier proofVerifier;

  /**
   * Creates a new instance with default BouncyCastle implementations.
   */
  public ConfidentialMptConvertService() {
    this(new JnaConfidentialMptConvertProofGenerator(), new JnaConfidentialMptConvertProofVerifier());
  }

  /**
   * Creates a new instance with custom implementations.
   *
   * @param proofGenerator The proof generator to use.
   * @param proofVerifier  The proof verifier to use.
   */
  public ConfidentialMptConvertService(
    final ConfidentialMptConvertProofGenerator proofGenerator,
    final ConfidentialMptConvertProofVerifier proofVerifier
  ) {
    this.proofGenerator = Objects.requireNonNull(proofGenerator, "proofGenerator must not be null");
    this.proofVerifier = Objects.requireNonNull(proofVerifier, "proofVerifier must not be null");
  }

  /**
   * Generates a context hash for a ConfidentialMptConvert transaction.
   *
   * @param account    The holder's XRPL account address.
   * @param sequence   The holder's account sequence number.
   * @param issuanceId The MPT issuance ID.
   *
   * @return A {@link ConfidentialMptConvertContext} containing the 32-byte context hash.
   */
  public ConfidentialMptConvertContext generateContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId
  ) {
    return ConfidentialMptContextUtil.generateConvertContext(account, sequence, issuanceId);
  }

  /**
   * Generates a Schnorr Proof of Knowledge for a ConfidentialMptConvert transaction.
   *
   * @param keyPair The secp256k1 key pair (ElGamal key pair).
   * @param context The context hash binding the proof to a specific transaction.
   *
   * @return A {@link ConfidentialMptConvertProof} containing the 65-byte proof.
   */
  public ConfidentialMptConvertProof generateProof(
    final KeyPair keyPair,
    final ConfidentialMptConvertContext context
  ) {
    return proofGenerator.generateProof(keyPair, context);
  }

  /**
   * Verifies a Schnorr Proof of Knowledge.
   *
   * @param proof     The proof to verify.
   * @param publicKey The secp256k1 public key.
   * @param context   The context hash binding the proof to a specific transaction.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  public boolean verifyProof(
    final ConfidentialMptConvertProof proof,
    final PublicKey publicKey,
    final ConfidentialMptConvertContext context
  ) {
    return proofVerifier.verifyProof(proof, publicKey, context);
  }
}

