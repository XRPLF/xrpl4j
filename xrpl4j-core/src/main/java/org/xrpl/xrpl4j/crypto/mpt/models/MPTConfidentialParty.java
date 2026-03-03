package org.xrpl.xrpl4j.crypto.mpt.models;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;

/**
 * Represents a party (recipient) in a Confidential MPT transaction.
 *
 * <p>This immutable encapsulates the data needed for one party (sender, destination, issuer, or auditor)
 * in the proof generation and verification process:</p>
 * <ul>
 *   <li>{@code ciphertext} - The ElGamal ciphertext encrypting the amount for this party</li>
 *   <li>{@code publicKey} - The ElGamal public key of this party</li>
 *   <li>{@code blindingFactor} - The blinding factor used to encrypt the ciphertext for this party</li>
 * </ul>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMPTConfidentialParty.class)
@JsonDeserialize(as = ImmutableMPTConfidentialParty.class)
public interface MPTConfidentialParty {

  /**
   * Creates a new builder for {@link MPTConfidentialParty}.
   *
   * @return A new builder.
   */
  static ImmutableMPTConfidentialParty.Builder builder() {
    return ImmutableMPTConfidentialParty.builder();
  }

  /**
   * Creates a new party with the given ciphertext, public key, and blinding factor.
   *
   * @param ciphertext      The ElGamal ciphertext encrypting the amount for this party.
   * @param publicKey       The ElGamal public key of this party.
   * @param blindingFactor  The blinding factor used to encrypt the ciphertext.
   *
   * @return A new {@link MPTConfidentialParty}.
   */
  static MPTConfidentialParty of(
    final ElGamalCiphertext ciphertext,
    final PublicKey publicKey,
    final BlindingFactor blindingFactor
  ) {
    return builder()
      .ciphertext(ciphertext)
      .publicKey(publicKey)
      .blindingFactor(blindingFactor)
      .build();
  }

  /**
   * The ElGamal ciphertext encrypting the amount for this party.
   *
   * @return The {@link ElGamalCiphertext}.
   */
  ElGamalCiphertext ciphertext();

  /**
   * The ElGamal public key of this party.
   *
   * @return The {@link PublicKey}.
   */
  PublicKey publicKey();

  /**
   * The blinding factor used to encrypt the ciphertext for this party.
   *
   * @return The {@link BlindingFactor}.
   */
  BlindingFactor blindingFactor();
}

