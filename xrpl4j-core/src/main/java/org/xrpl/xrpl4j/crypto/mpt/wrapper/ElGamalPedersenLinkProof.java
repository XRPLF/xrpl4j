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
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ElGamalPedersenLinkProofGenerator;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an ElGamal-Pedersen linkage proof.
 *
 * <p>This proof demonstrates that an ElGamal ciphertext and a Pedersen commitment
 * encode the same plaintext amount without revealing the amount.</p>
 *
 * <p>The proof consists of:
 * <ul>
 *   <li>T1 (33 bytes) - commitment point 1 (compressed EC point)</li>
 *   <li>T2 (33 bytes) - commitment point 2 (compressed EC point)</li>
 *   <li>T3 (33 bytes) - commitment point 3 (compressed EC point)</li>
 *   <li>sm (32 bytes) - response scalar for message</li>
 *   <li>sr (32 bytes) - response scalar for ElGamal randomness</li>
 *   <li>srho (32 bytes) - response scalar for Pedersen randomness</li>
 * </ul>
 *
 * <p>Total size: 195 bytes.</p>
 *
 * @see ElGamalPedersenLinkProofGenerator
 */
public final class ElGamalPedersenLinkProof {

  /**
   * The length of the proof in bytes (33 + 33 + 33 + 32 + 32 + 32 = 195).
   */
  public static final int PROOF_LENGTH = 195;

  private final byte[] proof;

  private ElGamalPedersenLinkProof(final byte[] proof) {
    this.proof = Arrays.copyOf(proof, proof.length);
  }

  /**
   * Creates a proof from raw bytes.
   *
   * @param bytes The 195-byte proof.
   *
   * @return An {@link ElGamalPedersenLinkProof}.
   *
   * @throws NullPointerException     if bytes is null.
   * @throws IllegalArgumentException if bytes is not exactly 195 bytes.
   */
  public static ElGamalPedersenLinkProof fromBytes(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes must not be null");
    Preconditions.checkArgument(
      bytes.length == PROOF_LENGTH,
      "Proof must be %s bytes, but was %s bytes",
      PROOF_LENGTH, bytes.length
    );
    return new ElGamalPedersenLinkProof(bytes);
  }

  /**
   * Creates a proof from a hex string.
   *
   * @param hex The 390-character hex string representing the proof.
   *
   * @return An {@link ElGamalPedersenLinkProof}.
   *
   * @throws NullPointerException     if hex is null.
   * @throws IllegalArgumentException if hex is not a valid 195-byte hex string.
   */
  public static ElGamalPedersenLinkProof fromHex(final String hex) {
    Objects.requireNonNull(hex, "hex must not be null");
    byte[] bytes = BaseEncoding.base16().decode(hex.toUpperCase());
    return fromBytes(bytes);
  }

  /**
   * Returns the proof as a byte array.
   *
   * @return A copy of the 195-byte proof.
   */
  public byte[] toBytes() {
    return Arrays.copyOf(proof, proof.length);
  }

  /**
   * Returns the proof as an uppercase hex string.
   *
   * @return A 390-character uppercase hex string.
   */
  public String hexValue() {
    return BaseEncoding.base16().encode(proof);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ElGamalPedersenLinkProof that = (ElGamalPedersenLinkProof) obj;
    return Arrays.equals(proof, that.proof);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(proof);
  }

  @Override
  public String toString() {
    return "ElGamalPedersenLinkProof{proof=" + hexValue() + "}";
  }
}

