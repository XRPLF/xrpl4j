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
 * Port of {@code secp256k1_mpt_verify_same_plaintext_multi} from proof_same_plaintext_multi.c.
 *
 * <p>Verifies a Zero-Knowledge Proof that N ElGamal ciphertexts all encrypt the same plaintext amount.</p>
 */
public interface SamePlaintextProofVerifier {

  /**
   * Verifies a Same Plaintext Multi proof.
   *
   * <p>R and S arrays are linked by index - R[i] and S[i] form the ciphertext for participant i.</p>
   *
   * @param proof     The proof bytes.
   * @param R         List of R points (c1 from ciphertexts), each 33 bytes compressed.
   * @param S         List of S points (c2 from ciphertexts), each 33 bytes compressed.
   * @param Pk        List of public keys, each 33 bytes compressed.
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return true if the proof is valid, false otherwise.
   *
   * @throws IllegalArgumentException if array sizes don't match or are less than 2.
   */
  boolean verifyProof(
    UnsignedByteArray proof,
    List<UnsignedByteArray> R,
    List<UnsignedByteArray> S,
    List<UnsignedByteArray> Pk,
    UnsignedByteArray contextId
  );
}

