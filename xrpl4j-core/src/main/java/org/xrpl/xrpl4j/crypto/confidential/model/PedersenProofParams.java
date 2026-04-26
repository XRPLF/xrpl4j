package org.xrpl.xrpl4j.crypto.confidential.model;

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
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;

/**
 * Parameters required to generate a Pedersen Linkage Proof.
 *
 * <p>Mirrors the C struct {@code mpt_pedersen_proof_params} from mpt_utility.h.</p>
 *
 * <p>Contains all the data needed to generate a proof linking an ElGamal ciphertext
 * and a Pedersen commitment:</p>
 * <ul>
 *   <li>{@code pedersenCommitment} - The 64-byte Pedersen commitment</li>
 *   <li>{@code amount} - The actual numeric value being committed</li>
 *   <li>{@code encryptedAmount} - The 66-byte ElGamal ciphertext</li>
 *   <li>{@code blindingFactor} - The 32-byte secret random value used to blind the Pedersen commitment</li>
 * </ul>
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePedersenProofParams.class)
@JsonDeserialize(as = ImmutablePedersenProofParams.class)
public interface PedersenProofParams {

  /**
   * Creates a new builder for {@link PedersenProofParams}.
   *
   * @return A new builder.
   */
  static ImmutablePedersenProofParams.Builder builder() {
    return ImmutablePedersenProofParams.builder();
  }

  /**
   * The 33-byte Pedersen commitment (compressed point).
   *
   * @return The Pedersen commitment as an {@link UnsignedByteArray}.
   */
  UnsignedByteArray pedersenCommitment();

  /**
   * The actual numeric value being committed.
   *
   * @return The amount as an {@link UnsignedLong}.
   */
  UnsignedLong amount();

  /**
   * The 66-byte ElGamal ciphertext (c1 || c2).
   *
   * @return The encrypted amount as an {@link EncryptedAmount}.
   */
  EncryptedAmount encryptedAmount();

  /**
   * The 32-byte secret random value used to blind the Pedersen commitment.
   *
   * @return The blinding factor.
   */
  BlindingFactor blindingFactor();
}
