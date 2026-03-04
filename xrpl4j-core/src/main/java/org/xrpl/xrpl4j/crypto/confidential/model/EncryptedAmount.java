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
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.model.ImmutableEncryptedAmount.Builder;

/**
 * An immutable ElGamal ciphertext containing two compressed secp256k1 points (C1 and C2).
 *
 * <p>Mirrors the C function output where c1 and c2 are separate secp256k1_pubkey structures:</p>
 * <ul>
 *   <li>C1 = r * G (ephemeral public key, 33 bytes)</li>
 *   <li>C2 = m * G + r * Q (masked amount, 33 bytes)</li>
 * </ul>
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
   * Creates a new ciphertext from c1 and c2 components.
   *
   * @param c1 The first ciphertext component (r * G), 33 bytes compressed.
   * @param c2 The second ciphertext component (m * G + r * Q), 33 bytes compressed.
   *
   * @return A new {@link EncryptedAmount}.
   *
   * @throws IllegalArgumentException if c1 or c2 is not exactly 33 bytes.
   */
  static EncryptedAmount of(UnsignedByteArray c1, UnsignedByteArray c2) {
    return builder().c1(c1).c2(c2).build();
  }

  /**
   * Creates a ciphertext from a 66-byte concatenated buffer (c1 || c2).
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
    Preconditions.checkArgument(
      bytes.length == Secp256k1Operations.ELGAMAL_TOTAL_SIZE,
      "bytes must be %s bytes, but was %s bytes",
      Secp256k1Operations.ELGAMAL_TOTAL_SIZE, bytes.length
    );
    byte[] c1Bytes = new byte[Secp256k1Operations.ELGAMAL_CIPHER_SIZE];
    byte[] c2Bytes = new byte[Secp256k1Operations.ELGAMAL_CIPHER_SIZE];
    System.arraycopy(bytes, 0, c1Bytes, 0, Secp256k1Operations.ELGAMAL_CIPHER_SIZE);
    System.arraycopy(bytes, Secp256k1Operations.ELGAMAL_CIPHER_SIZE, c2Bytes, 0, Secp256k1Operations.ELGAMAL_CIPHER_SIZE);
    return of(UnsignedByteArray.of(c1Bytes), UnsignedByteArray.of(c2Bytes));
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
    return fromBytes(value.toByteArray());
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
   * The first ciphertext component C1 = r * G.
   *
   * @return The 33-byte compressed point.
   */
  UnsignedByteArray c1();

  /**
   * The second ciphertext component C2 = m * G + r * Q.
   *
   * @return The 33-byte compressed point.
   */
  UnsignedByteArray c2();

  /**
   * Validates that c1 and c2 are each exactly 33 bytes.
   */
  @Value.Check
  default void validate() {
    Preconditions.checkArgument(
      c1().length() == Secp256k1Operations.ELGAMAL_CIPHER_SIZE,
      "c1 must be %s bytes, but was %s bytes",
      Secp256k1Operations.ELGAMAL_CIPHER_SIZE, c1().length()
    );
    Preconditions.checkArgument(
      c2().length() == Secp256k1Operations.ELGAMAL_CIPHER_SIZE,
      "c2 must be %s bytes, but was %s bytes",
      Secp256k1Operations.ELGAMAL_CIPHER_SIZE, c2().length()
    );
  }

  /**
   * Returns the concatenated ciphertext (c1 || c2).
   *
   * @return A 66-byte {@link UnsignedByteArray}.
   */
  @JsonIgnore
  @Value.Lazy
  default UnsignedByteArray toBytes() {
    byte[] result = new byte[Secp256k1Operations.ELGAMAL_TOTAL_SIZE];
    System.arraycopy(c1().toByteArray(), 0, result, 0, Secp256k1Operations.ELGAMAL_CIPHER_SIZE);
    System.arraycopy(c2().toByteArray(), 0, result, Secp256k1Operations.ELGAMAL_CIPHER_SIZE, Secp256k1Operations.ELGAMAL_CIPHER_SIZE);
    return UnsignedByteArray.of(result);
  }

  /**
   * Returns the ciphertext as an uppercase hex string.
   *
   * @return A 132-character uppercase hex string.
   */
  @JsonIgnore
  @Value.Lazy
  default String toHex() {
    return BaseEncoding.base16().encode(toBytes().toByteArray());
  }
}

