package org.xrpl.xrpl4j.crypto.mpt;

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

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.SecureRandomUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * A cryptographic blinding factor used in ElGamal encryption and zero-knowledge proofs.
 *
 * <p>A blinding factor is a 32-byte scalar value that must be valid for the secp256k1 curve
 * (0 &lt; value &lt; curve order). This class provides validation and convenient factory methods.</p>
 *
 * <p>Use {@link #hexValue()} to get the hex string representation for use in transaction objects.</p>
 */
public final class BlindingFactor {

  public static final int SCALAR_LENGTH = 32;

  private final UnsignedByteArray value;

  private BlindingFactor(final UnsignedByteArray value) {
    this.value = UnsignedByteArray.of(value.toByteArray());
  }

  /**
   * Generates a random blinding factor using secure random entropy.
   *
   * <p>The generated value is guaranteed to be a valid secp256k1 scalar
   * (0 &lt; value &lt; curve order).</p>
   *
   * @return A randomly generated {@link BlindingFactor}.
   */
  public static BlindingFactor generate() {
    byte[] scalar = new byte[SCALAR_LENGTH];
    do {
      SecureRandomUtils.secureRandom().nextBytes(scalar);
    } while (!Secp256k1Operations.isValidScalar(scalar));
    return new BlindingFactor(UnsignedByteArray.of(scalar));
  }

  /**
   * Creates a blinding factor from 32 bytes.
   *
   * <p>The bytes must represent a valid secp256k1 scalar (0 &lt; value &lt; curve order).</p>
   *
   * @param bytes The 32-byte scalar value.
   *
   * @return A {@link BlindingFactor}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 32 bytes or is not a valid scalar.
   */
  public static BlindingFactor fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == SCALAR_LENGTH,
      "Blinding factor must be %s bytes, but was %s bytes",
      SCALAR_LENGTH, bytes.length
    );
    Preconditions.checkArgument(
      Secp256k1Operations.isValidScalar(bytes),
      "Blinding factor must be a valid scalar (0 < value < curve order)"
    );
    return new BlindingFactor(UnsignedByteArray.of(bytes));
  }

  /**
   * Creates a blinding factor from a hex string.
   *
   * <p>The hex string must represent a valid 32-byte secp256k1 scalar.</p>
   *
   * @param hex The 64-character hex string.
   *
   * @return A {@link BlindingFactor}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not valid or does not represent a valid scalar.
   */
  public static BlindingFactor fromHex(final String hex) {
    Objects.requireNonNull(hex, "hex must not be null");
    byte[] bytes = BaseEncoding.base16().decode(hex.toUpperCase());
    return fromBytes(bytes);
  }

  /**
   * Returns the blinding factor as a byte array.
   *
   * @return A copy of the 32-byte scalar value.
   */
  public byte[] toBytes() {
    return value.toByteArray();
  }

  /**
   * Returns the blinding factor as an uppercase hex string.
   *
   * <p>This value can be used directly in transaction objects that require a blinding factor.</p>
   *
   * @return A 64-character uppercase hex string.
   */
  public String hexValue() {
    return BaseEncoding.base16().encode(value.toByteArray());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlindingFactor that = (BlindingFactor) o;
    return Arrays.equals(value.toByteArray(), that.value.toByteArray());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value.toByteArray());
  }

  @Override
  public String toString() {
    return "BlindingFactor{" + hexValue() + "}";
  }
}

