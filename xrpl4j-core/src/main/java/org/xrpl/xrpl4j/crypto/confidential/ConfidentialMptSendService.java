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
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcConfidentialMptSendProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcPedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.List;
import java.util.Objects;

/**
 * Service for generating and verifying ConfidentialMptSend proofs.
 *
 * <p>This service provides high-level methods for:</p>
 * <ul>
 *   <li>Generating context hashes for send transactions</li>
 *   <li>Generating Pedersen proof parameters</li>
 *   <li>Generating ConfidentialMptSend proofs</li>
 *   <li>Verifying ConfidentialMptSend proofs</li>
 * </ul>
 */
public class ConfidentialMptSendService {

  private final ConfidentialMptSendProofGenerator proofGenerator;
  private final ConfidentialMptSendProofVerifier proofVerifier;
  private final PedersenCommitmentGenerator commitmentGenerator;

  /**
   * Creates a new instance with default BouncyCastle implementations.
   */
  public ConfidentialMptSendService() {
    this(
      new BcConfidentialMptSendProofGenerator(),
      new BcConfidentialMptSendProofVerifier(),
      new BcPedersenCommitmentGenerator()
    );
  }

  /**
   * Creates a new instance with custom implementations.
   *
   * @param proofGenerator      The proof generator to use.
   * @param proofVerifier       The proof verifier to use.
   * @param commitmentGenerator The Pedersen commitment generator to use.
   */
  public ConfidentialMptSendService(
    final ConfidentialMptSendProofGenerator proofGenerator,
    final ConfidentialMptSendProofVerifier proofVerifier,
    final PedersenCommitmentGenerator commitmentGenerator
  ) {
    this.proofGenerator = Objects.requireNonNull(proofGenerator, "proofGenerator must not be null");
    this.proofVerifier = Objects.requireNonNull(proofVerifier, "proofVerifier must not be null");
    this.commitmentGenerator = Objects.requireNonNull(commitmentGenerator, "commitmentGenerator must not be null");
  }

  /**
   * Generates a context hash for a ConfidentialMptSend transaction.
   *
   * @param account     The sender's XRPL account address.
   * @param sequence    The sender's account sequence number.
   * @param issuanceId  The MPT issuance ID.
   * @param destination The destination XRPL account address.
   * @param version     The confidential balance version.
   *
   * @return A {@link ConfidentialMptSendContext} containing the 32-byte context hash.
   */
  public ConfidentialMptSendContext generateContext(
    final Address account,
    final UnsignedInteger sequence,
    final MpTokenIssuanceId issuanceId,
    final Address destination,
    final UnsignedInteger version
  ) {
    return ConfidentialMptContextUtil.generateSendContext(account, sequence, issuanceId, destination, version);
  }

  /**
   * Generates Pedersen proof parameters for a given amount.
   *
   * <p>This method creates all the parameters needed for a Pedersen linkage proof:</p>
   * <ul>
   *   <li>Generates a Pedersen commitment for the amount</li>
   *   <li>Bundles everything with the encrypted amount and blinding factor</li>
   * </ul>
   *
   * @param amount                  The amount to commit to.
   * @param encryptedAmount         The ElGamal ciphertext of the encrypted amount.
   * @param pedersenBlindingFactor  The blinding factor for the Pedersen commitment.
   *
   * @return A {@link PedersenProofParams} containing all parameters.
   */
  public PedersenProofParams generatePedersenProofParams(
    final UnsignedLong amount,
    final EncryptedAmount encryptedAmount,
    final BlindingFactor pedersenBlindingFactor
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(encryptedAmount, "encryptedAmount must not be null");
    Objects.requireNonNull(pedersenBlindingFactor, "pedersenBlindingFactor must not be null");

    // Generate Pedersen commitment
    PedersenCommitment commitment = commitmentGenerator.generateCommitment(amount, pedersenBlindingFactor);

    return PedersenProofParams.builder()
      .pedersenCommitment(commitment.value())
      .amount(amount)
      .encryptedAmount(encryptedAmount)
      .blindingFactor(pedersenBlindingFactor)
      .build();
  }

  /**
   * Generates a ConfidentialMptSend proof.
   *
   * <p>The proof consists of:</p>
   * <ul>
   *   <li>Same plaintext multi proof - proves all ciphertexts encrypt the same amount</li>
   *   <li>Amount linkage proof - links ElGamal ciphertext to Pedersen commitment for amount</li>
   *   <li>Balance linkage proof - links ElGamal ciphertext to Pedersen commitment for balance</li>
   *   <li>Aggregated bulletproof range proof - proves amount and remaining balance are in valid range</li>
   * </ul>
   *
   * @param senderKeyPair     The sender's key pair (must be secp256k1).
   * @param amount            The amount being sent.
   * @param recipients        The list of recipients (sender, destination, issuer, and optionally auditor).
   * @param txBlindingFactor  The single blinding factor used to encrypt the amount for all recipients.
   * @param context           The context hash binding the proof to a specific transaction.
   * @param amountParams      The Pedersen proof parameters for the amount.
   * @param balanceParams     The Pedersen proof parameters for the sender's balance.
   *
   * @return A {@link ConfidentialMptSendProof} containing the complete proof.
   */
  public ConfidentialMptSendProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final List<MptConfidentialParty> recipients,
    final BlindingFactor txBlindingFactor,
    final ConfidentialMptSendContext context,
    final PedersenProofParams amountParams,
    final PedersenProofParams balanceParams
  ) {
    return proofGenerator.generateProof(
      senderKeyPair,
      amount,
      recipients,
      txBlindingFactor,
      context,
      amountParams,
      balanceParams
    );
  }

  /**
   * Verifies a ConfidentialMptSend proof.
   *
   * @param proof             The proof to verify.
   * @param recipients        The list of recipients (sender, destination, issuer, and optionally auditor).
   * @param context           The context hash binding the proof to a specific transaction.
   * @param amountCommitment  The Pedersen commitment for the amount.
   * @param balanceCommitment The Pedersen commitment for the sender's balance.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  public boolean verifyProof(
    final ConfidentialMptSendProof proof,
    final List<MptConfidentialParty> recipients,
    final ConfidentialMptSendContext context,
    final PedersenCommitment amountCommitment,
    final PedersenCommitment balanceCommitment
  ) {
    return proofVerifier.verifyProof(proof, recipients, context, amountCommitment, balanceCommitment);
  }
}

