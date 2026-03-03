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
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;

/**
 * Represents a recipient in a Confidential MPT Send transaction.
 *
 * <p>This immutable mirrors the C struct {@code mpt_confidential_recipient} from mpt_utility.h,
 * which contains only a public key and an encrypted amount:</p>
 * <ul>
 *   <li>{@code publicKey} - The ElGamal public key of this recipient (33 bytes)</li>
 *   <li>{@code encryptedAmount} - The ElGamal ciphertext encrypting the amount for this recipient (66 bytes)</li>
 * </ul>
 *
 * <p>Note: The blinding factor used for encryption is passed separately to the proof generation
 * function, as all recipients share the same transaction blinding factor.</p>
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
   * Creates a new recipient with the given public key and encrypted amount.
   *
   * @param publicKey       The ElGamal public key of this recipient.
   * @param encryptedAmount The ElGamal ciphertext encrypting the amount for this recipient.
   *
   * @return A new {@link MPTConfidentialParty}.
   */
  static MPTConfidentialParty of(
    final PublicKey publicKey,
    final ElGamalCiphertext encryptedAmount
  ) {
    return builder()
      .publicKey(publicKey)
      .encryptedAmount(encryptedAmount)
      .build();
  }

  /**
   * The ElGamal public key of this recipient.
   *
   * @return The {@link PublicKey}.
   */
  PublicKey publicKey();

  /**
   * The ElGamal ciphertext encrypting the amount for this recipient.
   *
   * @return The {@link ElGamalCiphertext}.
   */
  ElGamalCiphertext encryptedAmount();
}

