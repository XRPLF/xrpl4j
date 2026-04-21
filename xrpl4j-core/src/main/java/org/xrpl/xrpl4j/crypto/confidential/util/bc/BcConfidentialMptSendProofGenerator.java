package org.xrpl.xrpl4j.crypto.confidential.util.bc;

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

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.RangeProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcRangeProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcSamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMptSendProofGenerator}.
 *
 * <p>NOTE: The interface signature changed to match the updated mpt_utility.h compact proof format.
 * The amountParams parameter was replaced with amountCommitment (PedersenCommitment) because the
 * compact AND-composed sigma proof only requires the commitment point, not the full PedersenProofParams.
 * This BC implementation is not compatible with the new compact proof format and will throw
 * {@link UnsupportedOperationException}. Use the JNA implementation instead.</p>
 */
public class BcConfidentialMptSendProofGenerator implements ConfidentialMptSendProofGenerator {

  private final SamePlaintextProofGenerator samePlaintextProofGenerator;
  private final PedersenLinkProofGenerator pedersenLinkProofGenerator;
  private final RangeProofGenerator rangeProofGenerator;

  /**
   * Creates a new instance with default port implementations.
   */
  public BcConfidentialMptSendProofGenerator() {
    this(
      new BcSamePlaintextProofGenerator(),
      new BcPedersenLinkProofGenerator(),
      new BcRangeProofGenerator()
    );
  }

  /**
   * Creates a new instance with custom port implementations.
   *
   * @param samePlaintextProofGenerator The same-plaintext proof generator port.
   * @param pedersenLinkProofGenerator  The Pedersen link proof generator port.
   * @param rangeProofGenerator         The range proof generator port.
   */
  public BcConfidentialMptSendProofGenerator(
    final SamePlaintextProofGenerator samePlaintextProofGenerator,
    final PedersenLinkProofGenerator pedersenLinkProofGenerator,
    final RangeProofGenerator rangeProofGenerator
  ) {
    this.samePlaintextProofGenerator = Objects.requireNonNull(samePlaintextProofGenerator);
    this.pedersenLinkProofGenerator = Objects.requireNonNull(pedersenLinkProofGenerator);
    this.rangeProofGenerator = Objects.requireNonNull(rangeProofGenerator);
  }

  /**
   * NOTE: This BC implementation is not compatible with the new compact proof format from mpt_utility.h.
   * The interface signature changed: amountParams (PedersenProofParams) was replaced with
   * amountCommitment (PedersenCommitment) to match the updated C function which only needs the
   * commitment point for the compact AND-composed sigma proof.
   *
   * @throws UnsupportedOperationException always. Use the JNA implementation instead.
   */
  @Override
  public ConfidentialMptSendProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final List<MptConfidentialParty> recipients,
    final BlindingFactor txBlindingFactor,
    final ConfidentialMptSendContext context,
    final PedersenCommitment amountCommitment,
    final PedersenProofParams balanceParams
  ) {
    throw new UnsupportedOperationException(
      "BcConfidentialMptSendProofGenerator is not compatible with the new compact proof format. " +
      "Use JnaConfidentialMptSendProofGenerator instead."
    );
  }
}

