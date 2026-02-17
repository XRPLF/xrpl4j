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

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a Same Plaintext Multi Proof (Zero-Knowledge Proof of Plaintext Equality).
 *
 * <p>This proof demonstrates that N distinct ElGamal ciphertexts all encrypt the same
 * underlying plaintext amount m, using distinct randomness r_i for each.</p>
 *
 * <p><b>Proof format (serialized):</b>
 * <ul>
 *   <li>Tm (33 bytes) - Shared commitment to amount nonce</li>
 *   <li>TrG[0..N-1] (N * 33 bytes) - Commitments k_{r,i} * G</li>
 *   <li>TrP[0..N-1] (N * 33 bytes) - Commitments k_{r,i} * P_i</li>
 *   <li>sm (32 bytes) - Shared response for amount</li>
 *   <li>sr[0..N-1] (N * 32 bytes) - Responses for randomness</li>
 * </ul>
 * Total size: (1 + 2N) * 33 + (1 + N) * 32 bytes</p>
 */
public final class SamePlaintextMultiProof {

  private final byte[] proof;
  private final int participantCount;

  private SamePlaintextMultiProof(final byte[] proof, final int participantCount) {
    this.proof = Arrays.copyOf(proof, proof.length);
    this.participantCount = participantCount;
  }

  /**
   * Creates a proof from raw bytes.
   *
   * @param bytes            The serialized proof bytes.
   * @param participantCount The number of participants (N) in the proof.
   *
   * @return A new {@link SamePlaintextMultiProof}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes length doesn't match expected size for N participants.
   */
  public static SamePlaintextMultiProof fromBytes(final byte[] bytes, final int participantCount) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(participantCount >= 2, "participantCount must be at least 2");

    int expectedSize = proofSize(participantCount);
    Preconditions.checkArgument(
      bytes.length == expectedSize,
      "Proof must be %s bytes for %s participants, but was %s bytes",
      expectedSize, participantCount, bytes.length
    );

    return new SamePlaintextMultiProof(bytes, participantCount);
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex              The hex-encoded proof.
   * @param participantCount The number of participants (N) in the proof.
   *
   * @return A new {@link SamePlaintextMultiProof}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is invalid or length doesn't match expected size.
   */
  public static SamePlaintextMultiProof fromHex(final String hex, final int participantCount) {
    Objects.requireNonNull(hex, "hex must not be null");
    return fromBytes(BaseEncoding.base16().decode(hex.toUpperCase()), participantCount);
  }

  /**
   * Computes the proof size for N participants.
   *
   * <p>Format: (1 Tm + 2N Tr) * 33 + (1 sm + N sr) * 32</p>
   *
   * @param n The number of participants.
   *
   * @return The proof size in bytes.
   */
  public static int proofSize(final int n) {
    return ((1 + 2 * n) * 33) + ((1 + n) * 32);
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
   * @return The hex-encoded proof.
   */
  public String hexValue() {
    return BaseEncoding.base16().encode(proof);
  }

  /**
   * Returns the number of participants in this proof.
   *
   * @return The participant count (N).
   */
  public int participantCount() {
    return participantCount;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SamePlaintextMultiProof that = (SamePlaintextMultiProof) obj;
    return participantCount == that.participantCount && Arrays.equals(proof, that.proof);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(participantCount);
    result = 31 * result + Arrays.hashCode(proof);
    return result;
  }

  @Override
  public String toString() {
    return "SamePlaintextMultiProof{size=" + proof.length + ", participants=" + participantCount + "}";
  }
}

