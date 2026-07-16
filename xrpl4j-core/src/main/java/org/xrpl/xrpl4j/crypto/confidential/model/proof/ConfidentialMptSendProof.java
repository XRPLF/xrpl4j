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
import org.xrpl.xrpl4j.model.jackson.modules.ConfidentialMptSendProofDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.ConfidentialMptSendProofSerializer;

/**
 * The zero-knowledge proof for a {@code ConfidentialMptSend} transaction: a compact AND-composed sigma proof
 * (192 bytes) proving ciphertext equality, Pedersen commitment linkage, and balance ownership, followed by an
 * aggregated Bulletproof range proof (754 bytes). Total fixed at 946 bytes
 * (SECP256K1_COMPACT_STANDARD_PROOF_SIZE + kMPT_DOUBLE_BULLETPROOF_SIZE). Held as raw bytes; on the wire it is
 * serialized as an uppercase hex string.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptSendProof.class, using = ConfidentialMptSendProofSerializer.class)
@JsonDeserialize(as = ImmutableConfidentialMptSendProof.class, using = ConfidentialMptSendProofDeserializer.class)
public interface ConfidentialMptSendProof {

  /**
   * Size of the compact AND-composed sigma proof (SECP256K1_COMPACT_STANDARD_PROOF_SIZE).
   */
  int COMPACT_SIGMA_SIZE = 192;

  /**
   * Size of the aggregated Bulletproof for two values (amount + remaining balance) (kMPT_DOUBLE_BULLETPROOF_SIZE).
   */
  int DOUBLE_BULLETPROOF_SIZE = 754;

  /**
   * The exact size of this proof in bytes: compact sigma (192) + double bulletproof (754) = 946.
   */
  int EXPECTED_SIZE = COMPACT_SIGMA_SIZE + DOUBLE_BULLETPROOF_SIZE;

  /**
   * Creates a proof from an {@link UnsignedByteArray}.
   *
   * @param value The 946-byte proof.
   *
   * @return A {@link ConfidentialMptSendProof}.
   */
  static ConfidentialMptSendProof of(final UnsignedByteArray value) {
    return ImmutableConfidentialMptSendProof.builder().value(value).build();
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The 1892-character hex string representing the proof.
   *
   * @return A {@link ConfidentialMptSendProof}.
   */
  static ConfidentialMptSendProof fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The raw proof bytes.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Validates that the proof is exactly {@link #EXPECTED_SIZE} bytes.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      value().length() == EXPECTED_SIZE,
      "ConfidentialMptSendProof must be %s bytes, but was %s bytes",
      EXPECTED_SIZE, value().length()
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
