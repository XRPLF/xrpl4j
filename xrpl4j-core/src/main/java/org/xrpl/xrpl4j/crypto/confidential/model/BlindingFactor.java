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
import org.xrpl.xrpl4j.model.jackson.modules.BlindingFactorDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.BlindingFactorSerializer;

/**
 * The 32-byte scalar blinding factor (ElGamal randomness {@code r}) used to encrypt an amount in Confidential MPT
 * transactions. Held as raw bytes; on the wire it is serialized as an uppercase hex string.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBlindingFactor.class, using = BlindingFactorSerializer.class)
@JsonDeserialize(as = ImmutableBlindingFactor.class, using = BlindingFactorDeserializer.class)
public interface BlindingFactor {

  /**
   * Creates a blinding factor from an {@link UnsignedByteArray}.
   *
   * @param value The 32-byte scalar.
   *
   * @return A {@link BlindingFactor}.
   */
  static BlindingFactor of(final UnsignedByteArray value) {
    return ImmutableBlindingFactor.builder().value(value).build();
  }

  /**
   * Creates a blinding factor from a hex string.
   *
   * @param hex The 64-character hex string representing the scalar.
   *
   * @return A {@link BlindingFactor}.
   */
  static BlindingFactor of(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * Creates a blinding factor from a 32-byte array.
   *
   * @param bytes The 32-byte scalar.
   *
   * @return A {@link BlindingFactor}.
   */
  static BlindingFactor fromBytes(final byte[] bytes) {
    return of(UnsignedByteArray.of(bytes));
  }

  /**
   * The raw 32-byte scalar value.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Validates that the blinding factor is exactly 32 bytes.
   */
  @Value.Check
  default void check() {
    final int expectedLength = 32;
    Preconditions.checkArgument(
      value().length() == expectedLength,
      "BlindingFactor must be %s bytes, but was %s bytes",
      expectedLength, value().length()
    );
  }

  /**
   * The blinding factor as an uppercase hex string, as it appears on the XRP Ledger wire format.
   *
   * @return A 64-character hex {@link String}.
   */
  @JsonIgnore
  @Value.Lazy
  default String hexValue() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}
