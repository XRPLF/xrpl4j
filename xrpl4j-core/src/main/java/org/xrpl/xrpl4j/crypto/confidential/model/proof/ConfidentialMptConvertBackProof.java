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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.modules.ConfidentialMptConvertBackProofDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.ConfidentialMptConvertBackProofSerializer;

/**
 * The zero-knowledge proof for a {@code ConfidentialMptConvertBack} transaction: a compact AND-composed sigma proof
 * (128 bytes) over the balance witness, followed by a single Bulletproof range proof (688 bytes) over the remainder
 * commitment. Total 816 bytes (SECP256K1_COMPACT_CONVERTBACK_PROOF_SIZE + kMPT_SINGLE_BULLETPROOF_SIZE). Held as raw
 * bytes; on the wire it is serialized as an uppercase hex string.
 */
@Value.Immutable
@JsonSerialize(
  as = ImmutableConfidentialMptConvertBackProof.class, using = ConfidentialMptConvertBackProofSerializer.class
)
@JsonDeserialize(
  as = ImmutableConfidentialMptConvertBackProof.class, using = ConfidentialMptConvertBackProofDeserializer.class
)
public interface ConfidentialMptConvertBackProof {

  /**
   * Creates a proof from an {@link UnsignedByteArray}.
   *
   * @param value The 816-byte proof.
   *
   * @return A {@link ConfidentialMptConvertBackProof}.
   */
  static ConfidentialMptConvertBackProof of(final UnsignedByteArray value) {
    return ImmutableConfidentialMptConvertBackProof.builder().value(value).build();
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The 1632-character hex string representing the proof.
   *
   * @return A {@link ConfidentialMptConvertBackProof}.
   */
  static ConfidentialMptConvertBackProof fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The raw proof bytes.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Validates that the proof is exactly 816 bytes: compact sigma (128) + single bulletproof (688).
   */
  @Value.Check
  default void check() {
    final int compactSigmaSize = 128;
    final int singleBulletproofSize = 688;
    final int expectedSize = compactSigmaSize + singleBulletproofSize;
    Preconditions.checkArgument(
      value().length() == expectedSize,
      "ConfidentialMptConvertBackProof must be %s bytes, but was %s bytes",
      expectedSize, value().length()
    );
  }

  /**
   * The proof as an uppercase hex string, as it appears on the XRP Ledger wire format.
   *
   * @return A hex-encoded {@link String}.
   */
  @JsonIgnore
  @Value.Lazy
  default String hexValue() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}
