package org.xrpl.xrpl4j.crypto.mpt.tmp;

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

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a Bulletproof range proof.
 *
 * <p>This proof demonstrates that a committed value is within the range [0, 2^64 - 1]
 * without revealing the actual value. Used in ConfidentialMPTConvertBack to prove
 * that the remaining balance after conversion is non-negative.</p>
 *
 * <p>Proof sizes are determined by the formula: 292 + 66 * rounds, where rounds = log2(64 * m)
 * and m is the number of values being proven.</p>
 *
 * <ul>
 *   <li>Single value (m=1): 688 bytes (rounds=6)</li>
 *   <li>Double value (m=2): 754 bytes (rounds=7)</li>
 * </ul>
 */
public final class BulletproofRangeProof {

  /**
   * The length of a single-value bulletproof in bytes (292 + 66*6 = 688).
   */
  public static final int SINGLE_PROOF_LENGTH = 688;

  /**
   * The length of a double-value bulletproof in bytes (292 + 66*7 = 754).
   */
  public static final int DOUBLE_PROOF_LENGTH = 754;

  private final byte[] proof;

  private BulletproofRangeProof(final byte[] proof) {
    this.proof = Arrays.copyOf(proof, proof.length);
  }

  /**
   * Creates a proof from raw bytes.
   *
   * @param bytes The proof bytes (688 for single, 754 for double).
   *
   * @return A {@link BulletproofRangeProof}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes length is not valid (688 or 754).
   */
  public static BulletproofRangeProof fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == SINGLE_PROOF_LENGTH || bytes.length == DOUBLE_PROOF_LENGTH,
      "Proof must be %s bytes (single) or %s bytes (double), but was %s bytes",
      SINGLE_PROOF_LENGTH, DOUBLE_PROOF_LENGTH, bytes.length
    );
    return new BulletproofRangeProof(bytes);
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The hex string representing the proof.
   *
   * @return A {@link BulletproofRangeProof}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid proof hex string.
   */
  public static BulletproofRangeProof fromHex(final String hex) {
    Objects.requireNonNull(hex, "hex must not be null");
    byte[] bytes = BaseEncoding.base16().decode(hex.toUpperCase());
    return fromBytes(bytes);
  }

  /**
   * Returns the proof as a byte array.
   *
   * @return A copy of the proof bytes.
   */
  public byte[] toBytes() {
    return Arrays.copyOf(proof, proof.length);
  }

  /**
   * Returns the proof as an uppercase hex string.
   *
   * @return An uppercase hex string.
   */
  public String hexValue() {
    return BaseEncoding.base16().encode(proof);
  }

  /**
   * Returns whether this is a single-value proof.
   *
   * @return {@code true} if this is a single-value proof (688 bytes).
   */
  public boolean isSingleProof() {
    return proof.length == SINGLE_PROOF_LENGTH;
  }

  /**
   * Returns whether this is a double-value proof.
   *
   * @return {@code true} if this is a double-value proof (754 bytes).
   */
  public boolean isDoubleProof() {
    return proof.length == DOUBLE_PROOF_LENGTH;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    BulletproofRangeProof that = (BulletproofRangeProof) obj;
    return Arrays.equals(proof, that.proof);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(proof);
  }

  @Override
  public String toString() {
    return "BulletproofRangeProof{length=" + proof.length + ", proof=" + hexValue() + "}";
  }
}

