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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.modules.CommitmentDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.CommitmentSerializer;

/**
 * A Pedersen commitment ({@code C = amount * G + rho * H}) used in Confidential MPT transactions: a single compressed
 * secp256k1 point, always exactly 33 bytes. Held as raw bytes; on the wire it is serialized as an uppercase hex string.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCommitment.class, using = CommitmentSerializer.class)
@JsonDeserialize(as = ImmutableCommitment.class, using = CommitmentDeserializer.class)
public interface Commitment {

  /**
   * The exact size of a commitment in bytes (a compressed EC point).
   */
  int LENGTH = 33;

  /**
   * Creates a commitment from an {@link UnsignedByteArray}.
   *
   * @param value The 33-byte compressed point.
   *
   * @return A {@link Commitment}.
   */
  static Commitment of(final UnsignedByteArray value) {
    return ImmutableCommitment.builder().value(value).build();
  }

  /**
   * Creates a commitment from a hex string.
   *
   * @param hex The 66-character hex string representing the compressed point.
   *
   * @return A {@link Commitment}.
   */
  static Commitment of(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * Creates a commitment from a 33-byte array.
   *
   * @param bytes The 33-byte compressed point.
   *
   * @return A {@link Commitment}.
   */
  static Commitment fromBytes(final byte[] bytes) {
    return of(UnsignedByteArray.of(bytes));
  }

  /**
   * The raw 33-byte compressed commitment point.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Validates that the commitment is exactly {@link #LENGTH} bytes.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      value().length() == LENGTH,
      "Commitment must be %s bytes, but was %s bytes",
      LENGTH, value().length()
    );
  }

  /**
   * The commitment as an uppercase hex string, as it appears on the XRP Ledger wire format.
   *
   * @return A 66-character hex {@link String}.
   */
  @JsonIgnore
  @Value.Lazy
  default String hexValue() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}
