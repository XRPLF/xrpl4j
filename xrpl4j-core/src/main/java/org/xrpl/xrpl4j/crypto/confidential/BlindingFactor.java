package org.xrpl.xrpl4j.crypto.confidential;

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

/**
 * An immutable 32-byte value used as a blinding factor in ElGamal encryption and zero-knowledge proofs.
 *
 * <p>A blinding factor must be a valid secp256k1 scalar (0 &lt; value &lt; curve order) because it is
 * used as a scalar multiplier in elliptic curve operations.</p>
 *
 * <p>For production code that needs dependency injection (e.g., for testing with deterministic values),
 * use {@link BlindingFactorGenerator} interface with {@link SecureRandomBlindingFactorGenerator} implementation.
 * For convenience, a {@link SecureRandomBlindingFactorGenerator} can be used to generate random blinding
 * factors.</p>
 *
 * @see BlindingFactorGenerator
 * @see SecureRandomBlindingFactorGenerator
 */
@Value.Immutable
public interface BlindingFactor {

  /**
   * Instantiates a new builder.
   *
   * @return An {@link ImmutableBlindingFactor.Builder}.
   */
  static ImmutableBlindingFactor.Builder builder() {
    return ImmutableBlindingFactor.builder();
  }

  /**
   * Creates a blinding factor from an {@link UnsignedByteArray}.
   *
   * @param value The 32-byte value as an {@link UnsignedByteArray}.
   *
   * @return A {@link BlindingFactor}.
   */
  static BlindingFactor of(final UnsignedByteArray value) {
    return builder().value(value).build();
  }

  /**
   * Creates a blinding factor from 32 bytes.
   *
   * @param bytes The 32-byte value.
   *
   * @return A {@link BlindingFactor}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 32 bytes or is not a valid scalar.
   */
  static BlindingFactor fromBytes(final byte[] bytes) {
    return of(UnsignedByteArray.of(bytes));
  }

  /**
   * Creates a blinding factor from a hex string.
   *
   * @param hex The 64-character hex string.
   *
   * @return A {@link BlindingFactor}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid 64-character hex string.
   */
  static BlindingFactor fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The blinding factor value as an {@link UnsignedByteArray}.
   *
   * @return The 32-byte value.
   */
  UnsignedByteArray value();

  /**
   * Validates that the blinding factor is exactly 32 bytes and is a valid secp256k1 scalar.
   */
  @Value.Check
  default void validate() {
    Preconditions.checkArgument(
      value().length() == Secp256k1Operations.BLINDING_FACTOR_SIZE,
      "Blinding factor must be %s bytes, but was %s bytes",
      Secp256k1Operations.BLINDING_FACTOR_SIZE, value().length()
    );
    Preconditions.checkArgument(
      Secp256k1Operations.isValidScalar(value().toByteArray()),
      "Blinding factor must be a valid scalar (0 < value < curve order)"
    );
  }

  /**
   * Returns the blinding factor as a byte array.
   *
   * @return A copy of the 32-byte value.
   */
  @JsonIgnore
  @Value.Lazy
  default byte[] toBytes() {
    return value().toByteArray();
  }

  /**
   * Returns the blinding factor as an uppercase hex string.
   *
   * <p>This value can be used directly in transaction objects that require a blinding factor.</p>
   *
   * @return A 64-character uppercase hex string.
   */
  @JsonIgnore
  @Value.Lazy
  default String hexValue() {
    return BaseEncoding.base16().encode(toBytes());
  }
}

