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
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Represents the proof for a ConfidentialMptConvertBack transaction.
 *
 * <p>The proof consists of a compact AND-composed sigma proof (128 bytes) over the balance
 * witness, followed by a single Bulletproof range proof (688 bytes) over the remainder
 * commitment.</p>
 *
 * <p>Total size: 128 + 688 = 816 bytes
 * (SECP256K1_COMPACT_CONVERTBACK_PROOF_SIZE + kMPT_SINGLE_BULLETPROOF_SIZE)</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptConvertBackProof.class)
@JsonDeserialize(as = ImmutableConfidentialMptConvertBackProof.class)
public interface ConfidentialMptConvertBackProof {

  /**
   * Size of the compact AND-composed sigma proof (SECP256K1_COMPACT_CONVERTBACK_PROOF_SIZE).
   */
  int COMPACT_SIGMA_SIZE = 128;

  /**
   * Size of a single bulletproof range proof (for 1 value).
   */
  int SINGLE_BULLETPROOF_SIZE = 688;

  /**
   * Expected total proof size: compact sigma (128) + single bulletproof (688) = 816 bytes.
   */
  int EXPECTED_SIZE = COMPACT_SIGMA_SIZE + SINGLE_BULLETPROOF_SIZE;

  /**
   * Creates a new builder for {@link ConfidentialMptConvertBackProof}.
   *
   * @return A new builder.
   */
  static ImmutableConfidentialMptConvertBackProof.Builder builder() {
    return ImmutableConfidentialMptConvertBackProof.builder();
  }

  /**
   * Creates a proof from an {@link UnsignedByteArray}.
   *
   * @param value The proof bytes.
   *
   * @return A {@link ConfidentialMptConvertBackProof}.
   *
   * @throws NullPointerException if value is null.
   */
  static ConfidentialMptConvertBackProof of(final UnsignedByteArray value) {
    return builder().value(value).build();
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The hex string representing the proof.
   *
   * @return A {@link ConfidentialMptConvertBackProof}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid hex string.
   */
  static ConfidentialMptConvertBackProof fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The proof bytes.
   *
   * @return An {@link UnsignedByteArray} containing the proof bytes.
   */
  UnsignedByteArray value();

  /**
   * Validates that the proof is exactly the expected size.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      value().length() == EXPECTED_SIZE,
      "ConvertBack proof must be %s bytes, but was %s bytes",
      EXPECTED_SIZE, value().length()
    );
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

