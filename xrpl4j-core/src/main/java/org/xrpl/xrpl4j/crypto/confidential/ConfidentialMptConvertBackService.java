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
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertBackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertBackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptConvertBackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialMptConvertBackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaPedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Objects;

/**
 * Service for generating and verifying ConfidentialMptConvertBack proofs.
 *
 * <p>This service provides high-level methods for:</p>
 * <ul>
 *   <li>Generating context hashes for convert back transactions</li>
 *   <li>Generating Pedersen proof parameters</li>
 *   <li>Generating ConfidentialMptConvertBack proofs</li>
 *   <li>Verifying ConfidentialMptConvertBack proofs</li>
 * </ul>
 */
public class ConfidentialMptConvertBackService {

  private final ConfidentialMptConvertBackProofGenerator proofGenerator;
  private final ConfidentialMptConvertBackProofVerifier proofVerifier;
  private final PedersenCommitmentGenerator commitmentGenerator;

  /**
   * Creates a new instance with default BouncyCastle implementations.
   */
  public ConfidentialMptConvertBackService() {
    this(
      new JnaConfidentialMptConvertBackProofGenerator(),
      new BcConfidentialMptConvertBackProofVerifier(),
      new JnaPedersenCommitmentGenerator()
    );
  }

  /**
   * Creates a new instance with custom implementations.
   *
   * @param proofGenerator      The proof generator to use.
   * @param proofVerifier       The proof verifier to use.
   * @param commitmentGenerator The Pedersen commitment generator to use.
   */
  public ConfidentialMptConvertBackService(
    final ConfidentialMptConvertBackProofGenerator proofGenerator,
    final ConfidentialMptConvertBackProofVerifier proofVerifier,
    final PedersenCommitmentGenerator commitmentGenerator
  ) {
    this.proofGenerator = Objects.requireNonNull(proofGenerator, "proofGenerator must not be null");
    this.proofVerifier = Objects.requireNonNull(proofVerifier, "proofVerifier must not be null");
    this.commitmentGenerator = Objects.requireNonNull(commitmentGenerator, "commitmentGenerator must not be null");
  }

  /**
   * Generates a context hash for a ConfidentialMptConvertBack transaction.
   *
   * @param account    The sender's XRPL account address.
   * @param sequence   The sender's account sequence number.
   * @param issuanceId The MPT issuance ID.
   * @param version    The confidential balance version.
   *
   * @return A {@link ConfidentialMptConvertBackContext} containing the 32-byte context hash.
   */
  public ConfidentialMptConvertBackContext generateContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final UnsignedInteger version
  ) {
    return ConfidentialMptContextUtil.generateConvertBackContext(account, sequence, issuanceId, version);
  }

  /**
   * Generates Pedersen proof parameters for the current balance.
   *
   * <p>This method creates all the parameters needed for a Pedersen linkage proof:</p>
   * <ul>
   *   <li>Generates a Pedersen commitment for the balance</li>
   *   <li>Bundles everything with the encrypted balance and blinding factor</li>
   * </ul>
   *
   * @param balance                 The current balance amount.
   * @param encryptedBalance        The ElGamal ciphertext of the encrypted balance.
   * @param pedersenBlindingFactor  The blinding factor for the Pedersen commitment.
   *
   * @return A {@link PedersenProofParams} containing all parameters.
   */
  public PedersenProofParams generatePedersenProofParams(
    final UnsignedLong balance,
    final EncryptedAmount encryptedBalance,
    final BlindingFactor pedersenBlindingFactor
  ) {
    Objects.requireNonNull(balance, "balance must not be null");
    Objects.requireNonNull(encryptedBalance, "encryptedBalance must not be null");
    Objects.requireNonNull(pedersenBlindingFactor, "pedersenBlindingFactor must not be null");

    // Generate Pedersen commitment
    PedersenCommitment commitment = commitmentGenerator.generateCommitment(balance, pedersenBlindingFactor);

    return PedersenProofParams.builder()
      .pedersenCommitment(commitment.value())
      .amount(balance)
      .encryptedAmount(encryptedBalance)
      .blindingFactor(pedersenBlindingFactor)
      .build();
  }

  /**
   * Generates a ConfidentialMptConvertBack proof.
   *
   * <p>The proof consists of:</p>
   * <ul>
   *   <li>Balance linkage proof - links ElGamal ciphertext to Pedersen commitment for balance</li>
   *   <li>Single bulletproof range proof - proves remaining balance is in valid range [0, 2^64)</li>
   * </ul>
   *
   * @param senderKeyPair  The sender's key pair (must be secp256k1).
   * @param amount         The amount being converted back to public balance.
   * @param context        The context hash binding the proof to a specific transaction.
   * @param balanceParams  The Pedersen proof parameters for the sender's current balance.
   *
   * @return A {@link ConfidentialMptConvertBackProof} containing the complete proof.
   */
  public ConfidentialMptConvertBackProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final ConfidentialMptConvertBackContext context,
    final PedersenProofParams balanceParams
  ) {
    return proofGenerator.generateProof(senderKeyPair, amount, context, balanceParams);
  }

  /**
   * Verifies a ConfidentialMptConvertBack proof.
   *
   * @param proof             The proof to verify.
   * @param senderPublicKey   The sender's ElGamal public key.
   * @param encryptedBalance  The sender's encrypted balance from the ledger.
   * @param balanceCommitment The Pedersen commitment for the sender's balance.
   * @param amount            The amount being converted back.
   * @param context           The context hash binding the proof to a specific transaction.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  public boolean verifyProof(
    final ConfidentialMptConvertBackProof proof,
    final PublicKey senderPublicKey,
    final EncryptedAmount encryptedBalance,
    final PedersenCommitment balanceCommitment,
    final UnsignedLong amount,
    final ConfidentialMptConvertBackContext context
  ) {
    return proofVerifier.verifyProof(proof, senderPublicKey, encryptedBalance, balanceCommitment, amount, context);
  }
}

