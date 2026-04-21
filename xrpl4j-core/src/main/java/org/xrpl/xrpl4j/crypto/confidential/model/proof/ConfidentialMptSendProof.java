package org.xrpl.xrpl4j.crypto.confidential.model.proof;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Represents the proof for a ConfidentialMptSend transaction.
 *
 * <p>The proof consists of a compact AND-composed sigma proof (192 bytes) that simultaneously
 * proves ciphertext equality, Pedersen commitment linkage, and balance ownership,
 * followed by an aggregated Bulletproof range proof (754 bytes).</p>
 *
 * <p>Total size: fixed at 946 bytes (SECP256K1_COMPACT_STANDARD_PROOF_SIZE + kMPT_DOUBLE_BULLETPROOF_SIZE).</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptSendProof.class)
@JsonDeserialize(as = ImmutableConfidentialMptSendProof.class)
public interface ConfidentialMptSendProof {

  /**
   * Size of the compact AND-composed sigma proof (SECP256K1_COMPACT_STANDARD_PROOF_SIZE).
   */
  int COMPACT_SIGMA_SIZE = 192;

  /**
   * Size of the aggregated bulletproof for 2 values (amount + remaining balance).
   */
  int DOUBLE_BULLETPROOF_SIZE = 754;

  /**
   * Fixed total proof size: 192 (compact sigma) + 754 (double bulletproof) = 946 bytes.
   */
  int EXPECTED_SIZE = COMPACT_SIGMA_SIZE + DOUBLE_BULLETPROOF_SIZE;

  /**
   * Creates a new builder for {@link ConfidentialMptSendProof}.
   *
   * @return A new builder.
   */
  static ImmutableConfidentialMptSendProof.Builder builder() {
    return ImmutableConfidentialMptSendProof.builder();
  }

  /**
   * Creates a proof from an {@link UnsignedByteArray}.
   *
   * @param value The proof bytes.
   *
   * @return A {@link ConfidentialMptSendProof}.
   *
   * @throws NullPointerException if value is null.
   */
  static ConfidentialMptSendProof of(final UnsignedByteArray value) {
    return builder().value(value).build();
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The hex string representing the proof.
   *
   * @return A {@link ConfidentialMptSendProof}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid hex string.
   */
  static ConfidentialMptSendProof fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The proof bytes.
   *
   * @return An {@link UnsignedByteArray} containing the proof bytes.
   */
  UnsignedByteArray value();

  /**
   * Returns the fixed expected proof size (946 bytes).
   *
   * @return The expected proof size in bytes.
   */
  static int expectedSize() {
    return EXPECTED_SIZE;
  }

  /**
   * Returns the proof as an uppercase hex string.
   *
   * @return A {@link String}.
   */
  @Lazy
  @JsonIgnore
  default String hexValue() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}

