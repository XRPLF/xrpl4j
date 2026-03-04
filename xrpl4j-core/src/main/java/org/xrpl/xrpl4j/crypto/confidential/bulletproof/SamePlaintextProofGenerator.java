package org.xrpl.xrpl4j.crypto.confidential.bulletproof;

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

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.List;

/**
 * Port of {@code secp256k1_mpt_prove_same_plaintext_multi} from proof_same_plaintext_multi.c.
 *
 * <p>Generates a Zero-Knowledge Proof that N ElGamal ciphertexts all encrypt the same plaintext amount.</p>
 *
 * <p>The proof format is: Tm (33 bytes) || TrG[0..N-1] (N*33 bytes) || TrP[0..N-1] (N*33 bytes)
 * || sm (32 bytes) || sr[0..N-1] (N*32 bytes).</p>
 *
 * <p>Total size: (1 + 2N) * 33 + (1 + N) * 32 bytes.</p>
 */
public interface SamePlaintextProofGenerator {

  /**
   * Generates a Same Plaintext Multi proof.
   *
   * <p>R and S arrays are linked by index - R[i] and S[i] form the ciphertext for participant i.</p>
   *
   * @param amount    The plaintext amount (witness).
   * @param R         List of R points (c1 from ciphertexts), each 33 bytes compressed.
   * @param S         List of S points (c2 from ciphertexts), each 33 bytes compressed.
   * @param Pk        List of public keys, each 33 bytes compressed.
   * @param rArray    List of blinding factors (randomness), each 32 bytes.
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return The proof bytes.
   *
   * @throws IllegalStateException    if proof generation fails.
   * @throws IllegalArgumentException if array sizes don't match or are less than 2.
   */
  UnsignedByteArray generateProof(
    UnsignedLong amount,
    List<UnsignedByteArray> R,
    List<UnsignedByteArray> S,
    List<UnsignedByteArray> Pk,
    List<UnsignedByteArray> rArray,
    UnsignedByteArray contextId
  );

  /**
   * Computes the proof size for N participants.
   *
   * <p>Format: (1 Tm + 2N Tr) * 33 + (1 sm + N sr) * 32</p>
   *
   * @param n The number of participants.
   *
   * @return The proof size in bytes.
   */
  static int proofSize(int n) {
    return ((1 + 2 * n) * 33) + ((1 + n) * 32);
  }
}

