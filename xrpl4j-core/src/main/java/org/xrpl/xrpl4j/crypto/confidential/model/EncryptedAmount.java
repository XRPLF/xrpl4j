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
import org.xrpl.xrpl4j.model.jackson.modules.EncryptedAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.EncryptedAmountSerializer;

/**
 * An ElGamal ciphertext ("encrypted amount") used in Confidential MPT transactions and ledger objects: a fixed-size
 * 66-byte value made of two compressed secp256k1 points (C1 || C2). Held as raw bytes; on the wire it is serialized as
 * an uppercase hex string.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEncryptedAmount.class, using = EncryptedAmountSerializer.class)
@JsonDeserialize(as = ImmutableEncryptedAmount.class, using = EncryptedAmountDeserializer.class)
public interface EncryptedAmount {

  /**
   * Creates an encrypted amount from an {@link UnsignedByteArray}.
   *
   * @param value The 66-byte ciphertext.
   *
   * @return An {@link EncryptedAmount}.
   */
  static EncryptedAmount of(final UnsignedByteArray value) {
    return ImmutableEncryptedAmount.builder().value(value).build();
  }

  /**
   * Creates an encrypted amount from a hex string.
   *
   * @param hex The 132-character hex string representing the ciphertext.
   *
   * @return An {@link EncryptedAmount}.
   */
  static EncryptedAmount of(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * Creates an encrypted amount from a 66-byte array.
   *
   * @param bytes The 66-byte ciphertext.
   *
   * @return An {@link EncryptedAmount}.
   */
  static EncryptedAmount fromBytes(final byte[] bytes) {
    return of(UnsignedByteArray.of(bytes));
  }

  /**
   * The raw 66-byte ElGamal ciphertext.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Validates that the ciphertext is exactly 66 bytes.
   */
  @Value.Check
  default void check() {
    final int expectedLength = 66;
    Preconditions.checkArgument(
      value().length() == expectedLength,
      "EncryptedAmount must be %s bytes, but was %s bytes",
      expectedLength, value().length()
    );
  }

  /**
   * The ciphertext as an uppercase hex string, as it appears on the XRP Ledger wire format.
   *
   * @return A 132-character hex {@link String}.
   */
  @JsonIgnore
  @Value.Lazy
  default String hexValue() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}
