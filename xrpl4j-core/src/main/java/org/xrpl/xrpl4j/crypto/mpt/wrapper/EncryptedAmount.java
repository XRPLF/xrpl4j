package org.xrpl.xrpl4j.crypto.mpt.wrapper;

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
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

/**
 * An immutable 66-byte ElGamal ciphertext representing an encrypted MPT amount.
 *
 * <p>The ciphertext consists of two compressed secp256k1 points (C1 and C2), each 33 bytes:</p>
 * <ul>
 *   <li>C1 = r * G (ephemeral public key)</li>
 *   <li>C2 = m * G + r * Q (masked amount)</li>
 * </ul>
 */
@Value.Immutable
public interface EncryptedAmount {

  /**
   * Instantiates a new builder.
   *
   * @return An {@link ImmutableEncryptedAmount.Builder}.
   */
  static ImmutableEncryptedAmount.Builder builder() {
    return ImmutableEncryptedAmount.builder();
  }

  /**
   * Creates an encrypted amount from an {@link UnsignedByteArray}.
   *
   * @param value The 66-byte ciphertext as an {@link UnsignedByteArray}.
   *
   * @return An {@link EncryptedAmount}.
   *
   * @throws IllegalArgumentException if value is not exactly 66 bytes.
   */
  static EncryptedAmount of(final UnsignedByteArray value) {
    return builder().value(value).build();
  }

  /**
   * Creates an encrypted amount from 66 bytes.
   *
   * @param bytes The 66-byte ciphertext.
   *
   * @return An {@link EncryptedAmount}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 66 bytes.
   */
  static EncryptedAmount fromBytes(final byte[] bytes) {
    return of(UnsignedByteArray.of(bytes));
  }

  /**
   * Creates an encrypted amount from a hex string.
   *
   * @param hex The 132-character hex string.
   *
   * @return An {@link EncryptedAmount}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid 132-character hex string.
   */
  static EncryptedAmount fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The encrypted amount value as an {@link UnsignedByteArray}.
   *
   * @return The 66-byte ciphertext.
   */
  UnsignedByteArray value();

  /**
   * Validates that the encrypted amount is exactly 66 bytes.
   */
  @Value.Check
  default void validate() {
    Preconditions.checkArgument(
      value().length() == Secp256k1Operations.ELGAMAL_TOTAL_SIZE,
      "Encrypted amount must be %s bytes, but was %s bytes",
      Secp256k1Operations.ELGAMAL_TOTAL_SIZE, value().length()
    );
  }

  /**
   * Returns the encrypted amount as a byte array.
   *
   * @return A copy of the 66-byte ciphertext.
   */
  @JsonIgnore
  @Value.Lazy
  default byte[] toBytes() {
    return value().toByteArray();
  }

  /**
   * Returns the encrypted amount as an uppercase hex string.
   *
   * @return A 132-character uppercase hex string.
   */
  @JsonIgnore
  @Value.Lazy
  default String toHex() {
    return BaseEncoding.base16().encode(toBytes());
  }
}

