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

/**
 * Port of {@code secp256k1_elgamal_pedersen_link_verify} from proof_link.c.
 *
 * <p>Verifies a Zero-Knowledge Proof linking an ElGamal ciphertext and a Pedersen commitment.</p>
 *
 * <p><b>Verification equations:</b>
 * <ul>
 *   <li>sr * G == T1 + e * C1</li>
 *   <li>sm * G + sr * Pk == T2 + e * C2</li>
 *   <li>sm * G + srho * H == T3 + e * PCm</li>
 * </ul>
 */
public interface PedersenLinkProofVerifier {

  /**
   * Verifies a proof linking an ElGamal ciphertext and a Pedersen commitment.
   *
   * @param proof     The 195-byte proof to verify.
   * @param c1        The ElGamal ephemeral key (R = r * G), 33 bytes compressed.
   * @param c2        The ElGamal masked value (S = m * G + r * Pk), 33 bytes compressed.
   * @param pk        The recipient's public key, 33 bytes compressed.
   * @param pcm       The Pedersen commitment (m * G + rho * H), 33 bytes compressed.
   * @param contextId The 32-byte context identifier. Can be null.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verifyProof(
    UnsignedByteArray proof,
    UnsignedByteArray c1,
    UnsignedByteArray c2,
    UnsignedByteArray pk,
    UnsignedByteArray pcm,
    UnsignedByteArray contextId
  );
}

