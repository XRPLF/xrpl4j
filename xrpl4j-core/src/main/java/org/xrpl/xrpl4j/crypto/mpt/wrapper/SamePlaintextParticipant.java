package org.xrpl.xrpl4j.crypto.mpt.wrapper;

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

import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;

import java.util.Objects;

/**
 * Represents a participant in a SamePlaintextMultiProof.
 *
 * <p>This class encapsulates all the data needed for one participant (sender, destination, issuer, or auditor)
 * in the proof generation and verification process:</p>
 * <ul>
 *   <li>{@code ciphertext} - The ElGamal ciphertext encrypting the amount for this participant</li>
 *   <li>{@code publicKey} - The ElGamal public key of this participant</li>
 *   <li>{@code blindingFactor} - The blinding factor (r) used to encrypt the ciphertext</li>
 *   <li>{@code nonceKr} - The nonce (k_r) used for the randomness commitment in the proof</li>
 * </ul>
 *
 * <p>For verification, only {@code ciphertext} and {@code publicKey} are required.</p>
 */
public final class SamePlaintextParticipant {

  private final ElGamalCiphertext ciphertext;
  private final ElGamalPublicKey publicKey;
  private final BlindingFactor blindingFactor;
  private final BlindingFactor nonceKr;

  private SamePlaintextParticipant(
    final ElGamalCiphertext ciphertext,
    final ElGamalPublicKey publicKey,
    final BlindingFactor blindingFactor,
    final BlindingFactor nonceKr
  ) {
    this.ciphertext = ciphertext;
    this.publicKey = publicKey;
    this.blindingFactor = blindingFactor;
    this.nonceKr = nonceKr;
  }

  /**
   * Creates a participant for proof generation (requires all fields).
   *
   * @param ciphertext     The ElGamal ciphertext for this participant.
   * @param publicKey      The ElGamal public key of this participant.
   * @param blindingFactor The blinding factor used to create the ciphertext.
   * @param nonceKr        The nonce for the randomness commitment.
   *
   * @return A new {@link SamePlaintextParticipant}.
   */
  public static SamePlaintextParticipant forProofGeneration(
    final ElGamalCiphertext ciphertext,
    final ElGamalPublicKey publicKey,
    final BlindingFactor blindingFactor,
    final BlindingFactor nonceKr
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");
    Objects.requireNonNull(nonceKr, "nonceKr must not be null");
    return new SamePlaintextParticipant(ciphertext, publicKey, blindingFactor, nonceKr);
  }

  /**
   * Creates a participant for proof verification (only ciphertext and publicKey required).
   *
   * @param ciphertext The ElGamal ciphertext for this participant.
   * @param publicKey  The ElGamal public key of this participant.
   *
   * @return A new {@link SamePlaintextParticipant}.
   */
  public static SamePlaintextParticipant forVerification(
    final ElGamalCiphertext ciphertext,
    final ElGamalPublicKey publicKey
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    return new SamePlaintextParticipant(ciphertext, publicKey, null, null);
  }

  /**
   * Returns the ElGamal ciphertext for this participant.
   *
   * @return The ciphertext.
   */
  public ElGamalCiphertext ciphertext() {
    return ciphertext;
  }

  /**
   * Returns the ElGamal public key of this participant.
   *
   * @return The public key.
   */
  public ElGamalPublicKey publicKey() {
    return publicKey;
  }

  /**
   * Returns the blinding factor used to create the ciphertext.
   *
   * @return The blinding factor, or null if this participant is for verification only.
   */
  public BlindingFactor blindingFactor() {
    return blindingFactor;
  }

  /**
   * Returns the nonce for the randomness commitment.
   *
   * @return The nonce, or null if this participant is for verification only.
   */
  public BlindingFactor nonceKr() {
    return nonceKr;
  }

  @Override
  public String toString() {
    return "SamePlaintextParticipant{publicKey=" + publicKey + "}";
  }
}

