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
import org.xrpl.xrpl4j.crypto.SecureRandomUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * A 32-byte random value used as a blinding factor in ElGamal encryption and zero-knowledge proofs.
 *
 * <p>A blinding factor must be a valid secp256k1 scalar (0 &lt; value &lt; curve order) because it is
 * used as a scalar multiplier in elliptic curve operations.</p>
 *
 * <p>Use {@link #hexValue()} to get the hex string representation for use in transaction objects.</p>
 */
public final class BlindingFactor {

  public static final int LENGTH = 32;

  private final byte[] value;

  private BlindingFactor(final byte[] value) {
    this.value = Arrays.copyOf(value, value.length);
  }

  /**
   * Generates a random blinding factor using secure random entropy.
   *
   * <p>The generated value is guaranteed to be a valid secp256k1 scalar
   * (0 &lt; value &lt; curve order) using rejection sampling.</p>
   *
   * @return A randomly generated {@link BlindingFactor}.
   */
  public static BlindingFactor generate() {
    byte[] bytes = new byte[LENGTH];
    do {
      SecureRandomUtils.secureRandom().nextBytes(bytes);
    } while (!Secp256k1Operations.isValidScalar(bytes));
    return new BlindingFactor(bytes);
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
  public static BlindingFactor fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == LENGTH,
      "Blinding factor must be %s bytes, but was %s bytes",
      LENGTH, bytes.length
    );
    Preconditions.checkArgument(
      Secp256k1Operations.isValidScalar(bytes),
      "Blinding factor must be a valid scalar (0 < value < curve order)"
    );
    return new BlindingFactor(bytes);
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
  public static BlindingFactor fromHex(final String hex) {
    Objects.requireNonNull(hex, "hex must not be null");
    byte[] bytes = BaseEncoding.base16().decode(hex.toUpperCase());
    return fromBytes(bytes);
  }

  /**
   * Returns the blinding factor as a byte array.
   *
   * @return A copy of the 32-byte value.
   */
  public byte[] toBytes() {
    return Arrays.copyOf(value, value.length);
  }

  /**
   * Returns the blinding factor as an uppercase hex string.
   *
   * <p>This value can be used directly in transaction objects that require a blinding factor.</p>
   *
   * @return A 64-character uppercase hex string.
   */
  public String hexValue() {
    return BaseEncoding.base16().encode(value);
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
    return Arrays.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }

  @Override
  public String toString() {
    return "BlindingFactor{" + hexValue() + "}";
  }
}

