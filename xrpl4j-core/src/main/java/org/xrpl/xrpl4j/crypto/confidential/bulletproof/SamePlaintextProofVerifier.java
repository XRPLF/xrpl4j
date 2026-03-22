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

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.List;

/**
 * Port of {@code secp256k1_mpt_verify_equality_shared_r} from proof_same_plaintext_multi_shared_r.c.
 *
 * <p>Verifies a Zero-Knowledge Proof that N ElGamal ciphertexts all encrypt the same plaintext amount,
 * where all ciphertexts share a single randomness value {@code r} (and thus a single C1 point).</p>
 */
@SuppressWarnings("checkstyle")
public interface SamePlaintextProofVerifier {

  /**
   * Verifies a Same Plaintext proof with shared randomness.
   *
   * <p>All ciphertexts share the same C1 point.</p>
   *
   * @param proof     The proof bytes.
   * @param c1        The shared C1 point (r * G), 33 bytes compressed.
   * @param c2List    List of C2 points (one per recipient), each 33 bytes compressed.
   * @param pkList    List of public keys (one per recipient), each 33 bytes compressed.
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return true if the proof is valid, false otherwise.
   *
   * @throws IllegalArgumentException if list sizes don't match or are less than 2.
   */
  boolean verifyProof(
    UnsignedByteArray proof,
    UnsignedByteArray c1,
    List<UnsignedByteArray> c2List,
    List<UnsignedByteArray> pkList,
    UnsignedByteArray contextId
  );
}
