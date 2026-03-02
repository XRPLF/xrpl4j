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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Represents a Schnorr proof of knowledge of a secret key for Confidential MPT conversions.
 *
 * <p>The proof consists of:
 * <ul>
 *   <li>T (33 bytes) - commitment point (compressed EC point)</li>
 *   <li>s (32 bytes) - response scalar</li>
 * </ul>
 *
 * <p>Total size: 65 bytes.</p>
 */
@Value.Immutable
public interface ConfidentialMPTConvertProof {

  /**
   * The length of the proof in bytes (33 + 32 = 65).
   */
  int PROOF_LENGTH = 65;

  /**
   * Creates a proof from an {@link UnsignedByteArray}.
   *
   * @param value The 65-byte proof (T || s).
   *
   * @return A {@link ConfidentialMPTConvertProof}.
   *
   * @throws NullPointerException     if value is null.
   * @throws IllegalArgumentException if value is not exactly 65 bytes.
   */
  static ConfidentialMPTConvertProof of(final UnsignedByteArray value) {
    return ImmutableConfidentialMPTConvertProof.builder()
      .value(value)
      .build();
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The 130-character hex string representing the proof.
   *
   * @return A {@link ConfidentialMPTConvertProof}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid 65-byte hex string.
   */
  static ConfidentialMPTConvertProof fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The 65-byte proof value (T || s).
   *
   * @return An {@link UnsignedByteArray} containing the proof bytes.
   */
  UnsignedByteArray value();

  /**
   * Validates that the proof is exactly 65 bytes.
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

