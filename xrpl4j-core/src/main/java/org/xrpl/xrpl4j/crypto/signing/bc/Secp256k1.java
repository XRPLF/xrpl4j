package org.xrpl.xrpl4j.crypto.signing.bc;

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
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Static constants for Secp256k1 operations.
 */
public interface Secp256k1 {

  /**
   * Elliptic Curve parameters for the curve named `secp256k1`.
   *
   * @deprecated This interface is deprecated in-favor of {@link BcKeyUtils#PARAMS}.
   */
  @Deprecated
  X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");

  /**
   * Elliptic Curve domain parameters for the curve named `secp256k1`.
   *
   * @deprecated This interface is deprecated in-favor of {@link BcKeyUtils#PARAMS}.
   */
  @Deprecated
  ECDomainParameters EC_DOMAIN_PARAMETERS = new ECDomainParameters(
    EC_PARAMETERS.getCurve(),
    EC_PARAMETERS.getG(),
    EC_PARAMETERS.getN(),
    EC_PARAMETERS.getH()
  );


  /**
   * Creates an {@link UnsignedByteArray} from the bytes of a supplied {@link BigInteger}. If the length of the
   * resulting array is not at least {@code minFinalByteLength}, then the result is prefix padded with `0x00` bytes
   * until the final array length is {@code minFinalByteLength}.
   *
   * <p>This function exists to ensure that transformation of secp256k1 private keys from a {@link BigInteger} to a
   * byte array are done in a consistent manner, always yielding the desired number of bytes. For example, secp256k1
   * private keys are 32-bytes long naturally. However, when transformed to a byte array via
   * {@link BigInteger#toByteArray()}, the result will not always have the same number of leading zero bytes that one
   * might expect. Sometimes the returned array will have 33 bytes, one of which is a zero-byte prefix pad that is meant
   * to ensure the underlying number is not represented as a negative number. Other times, the array will have fewer
   * than 32 bytes, for example 31 or even 30, if the byte array has redundant leading zero bytes.
   *
   * <p>Note that this function assumes the supplied {@code amount} is always positive, which roughly correlates with
   * the secp256k1 requirement that private key scalar `D` values be in the range [1, N-1].
   *
   * <p>Thus, this function can be used to normalize a byte array containing a secp256k1 private key with a desired
   * number of 0-byte padding to ensure that it is always the desired {@code minFinalByteLength} (e.g., in this library,
   * secp256k1 private keys should always be comprised of a 32-byte natural private key with a one-byte `0x00` prefix
   * pad).
   *
   * @param amount             A {@link BigInteger} to convert into an {@link UnsignedByteArray}.
   * @param minFinalByteLength The minimum length, in bytes, that the final result must be. If the final byte length is
   *                           less than this number, the resulting array will be prefix padded to increase its length
   *                           to this number.
   *
   * @return An {@link UnsignedByteArray} with a length of at least {@code minFinalByteLength}.
   *
   * @see "https://github.com/XRPLF/xrpl4j/issues/486"
   */
  static UnsignedByteArray toUnsignedByteArray(final BigInteger amount, int minFinalByteLength) {
    Objects.requireNonNull(amount);
    Preconditions.checkArgument(amount.signum() >= 0, "amount must not be negative");
    Preconditions.checkArgument(minFinalByteLength >= 0, "minFinalByteLength must not be negative");

    // Return the `amount` as an UnsignedByteArray, but with the proper zero-byte prefix padding.
    return withZeroPrefixPadding(amount.toByteArray(), minFinalByteLength);
  }

  /**
   * Construct a new {@link UnsignedByteArray} that contains the bytes from {@code bytes}, but with enough `0x00` prefix
   * padding bytes such that the final length of the returned value is {@code minFinalByteLength}.
   *
   * @param bytes              An {@link UnsignedByteArray} to zero-pad.
   * @param minFinalByteLength The minimum length, in bytes, that the final result must be zero-byte prefix-padded to.
   *                           If this number is greater-than {@code #length}, then this value will be reduced to
   *                           {@code #length}.
   *
   * @return A copy of this {@link UnsignedByteArray} that has been zero-byte prefix-padded such that its final length
   *   is at least {@code minFinalByteLength}.
   */
  static UnsignedByteArray withZeroPrefixPadding(final UnsignedByteArray bytes, int minFinalByteLength) {
    Preconditions.checkArgument(minFinalByteLength >= 0, "minFinalByteLength must not be negative");

    return withZeroPrefixPadding(bytes.toByteArray(), minFinalByteLength);
  }

  /**
   * Construct a new {@link UnsignedByteArray} that contains the bytes from {@code bytes}, but with enough `0x00` prefix
   * padding bytes such that the final length of the returned value is {@code minFinalByteLength}.
   *
   * @param bytes              A byte array to zero-pad.
   * @param minFinalByteLength The minimum length, in bytes, that the final result must be zero-byte prefix-padded to.
   *                           If this number is greater-than {@code #length}, then this value will be reduced to
   *                           {@code #length}.
   *
   * @return A copy of this {@link UnsignedByteArray} that has been zero-byte prefix-padded such that its final length
   *   is at least {@code minFinalByteLength}.
   */
  static UnsignedByteArray withZeroPrefixPadding(final byte[] bytes, int minFinalByteLength) {
    Preconditions.checkArgument(minFinalByteLength >= 0, "minFinalByteLength must not be negative");

    if (bytes.length > minFinalByteLength) { // <-- Increase `minFinalByteLength` to be at least bytes.length
      minFinalByteLength = bytes.length;
    }

    final int numPadBytes = minFinalByteLength - bytes.length; // <-- numPadBytes will never be negative
    byte[] resultBytes = new byte[minFinalByteLength];
    System.arraycopy(bytes, 0, resultBytes, numPadBytes, bytes.length);
    return UnsignedByteArray.of(resultBytes);
  }

}
