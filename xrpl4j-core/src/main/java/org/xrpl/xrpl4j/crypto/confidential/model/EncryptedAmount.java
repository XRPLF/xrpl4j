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
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.ImmutableEncryptedAmount.Builder;

/**
 * An immutable ElGamal ciphertext represented as a 66-byte value (two compressed secp256k1 points).
 */
@Value.Immutable
public interface EncryptedAmount {

  /**
   * Instantiates a new builder.
   *
   * @return An {@link Builder}.
   */
  static Builder builder() {
    return ImmutableEncryptedAmount.builder();
  }

  /**
   * Creates a ciphertext from a 66-byte array.
   *
   * @param bytes The 66-byte ciphertext.
   *
   * @return A new {@link EncryptedAmount}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 66 bytes.
   */
  static EncryptedAmount fromBytes(byte[] bytes) {
    Preconditions.checkNotNull(bytes, "bytes must not be null");
    return builder().value(UnsignedByteArray.of(bytes)).build();
  }

  /**
   * Creates a ciphertext from a 66-byte {@link UnsignedByteArray}.
   *
   * @param value The 66-byte ciphertext.
   *
   * @return A new {@link EncryptedAmount}.
   *
   * @throws NullPointerException     if value is null.
   * @throws IllegalArgumentException if value is not exactly 66 bytes.
   */
  static EncryptedAmount fromBytes(UnsignedByteArray value) {
    Preconditions.checkNotNull(value, "value must not be null");
    return builder().value(value).build();
  }

  /**
   * Creates a ciphertext from a 132-character hex string.
   *
   * @param hex The hex string.
   *
   * @return A new {@link EncryptedAmount}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid 132-character hex string.
   */
  static EncryptedAmount fromHex(String hex) {
    Preconditions.checkNotNull(hex, "hex must not be null");
    return fromBytes(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The raw 66-byte ElGamal ciphertext.
   *
   * @return An {@link UnsignedByteArray} of 66 bytes.
   */
  UnsignedByteArray value();

  /**
   * Returns the ciphertext as a byte array.
   *
   * @return A 66-byte {@link UnsignedByteArray}.
   */
  @JsonIgnore
  @Value.Lazy
  default UnsignedByteArray toBytes() {
    return value();
  }

  /**
   * Returns the ciphertext as an uppercase hex string.
   *
   * @return A 132-character uppercase hex string.
   */
  @JsonIgnore
  @Value.Lazy
  default String toHex() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}

