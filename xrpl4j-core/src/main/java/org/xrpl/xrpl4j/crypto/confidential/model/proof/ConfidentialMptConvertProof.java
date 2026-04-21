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
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * A * Represents a compact Schnorr proof of knowledge of a secret key for Confidential MPT conversions.
 *
 * <p>Total size: 64 bytes (kMPT_SCHNORR_PROOF_SIZE).</p>
 */
@Value.Immutable
public interface ConfidentialMptConvertProof {

  /**
   * The length of the compact Schnorr proof in bytes (kMPT_SCHNORR_PROOF_SIZE).
   */
  int PROOF_LENGTH = 64;

  /**
   * Creates a proof from an {@link UnsignedByteArray}.
   *
   * @param value The 64-byte compact Schnorr proof.
   *
   * @return A {@link ConfidentialMptConvertProof}.
   *
   * @throws NullPointerException     if value is null.
   * @throws IllegalArgumentException if value is not exactly 64 bytes.
   */
  static ConfidentialMptConvertProof of(final UnsignedByteArray value) {
    return ImmutableConfidentialMptConvertProof.builder()
      .value(value)
      .build();
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The 128-character hex string representing the proof.
   *
   * @return A {@link ConfidentialMptConvertProof}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid 64-byte hex string.
   */
  static ConfidentialMptConvertProof fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The 64-byte compact Schnorr proof value.
   *
   * @return An {@link UnsignedByteArray} containing the proof bytes.
   */
  UnsignedByteArray value();

  /**
   * Validates that the proof is exactly 64 bytes.
   */
  @Value.Check
  default void check() {
    if (value().length() != PROOF_LENGTH) {
      throw new IllegalArgumentException(
        String.format("Proof must be %d bytes, but was %d bytes", PROOF_LENGTH, value().length())
      );
    }
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

