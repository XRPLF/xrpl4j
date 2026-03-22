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
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;

/**
 * Port of {@code secp256k1_elgamal_pedersen_link_prove} from proof_link.c.
 *
 * <p>Generates a Zero-Knowledge Proof linking an ElGamal ciphertext and a Pedersen commitment,
 * proving they encode the same underlying plaintext value without revealing it.</p>
 *
 * <p><b>Statement:</b> The prover demonstrates knowledge of scalars (m, r, rho) such that:
 * <ul>
 *   <li>C1 = r * G (ElGamal Ephemeral Key)</li>
 *   <li>C2 = m * G + r * Pk (ElGamal Masked Value)</li>
 *   <li>PCm = m * G + rho * H (Pedersen Commitment)</li>
 * </ul>
 *
 * <p><b>Proof format (195 bytes):</b>
 * <ul>
 *   <li>T1 (33 bytes) - Commitment kr * G</li>
 *   <li>T2 (33 bytes) - Commitment km * G + kr * Pk</li>
 *   <li>T3 (33 bytes) - Commitment km * G + krho * H</li>
 *   <li>sm (32 bytes) - Response for amount</li>
 *   <li>sr (32 bytes) - Response for ElGamal randomness</li>
 *   <li>srho (32 bytes) - Response for Pedersen blinding factor</li>
 * </ul>
 */
@SuppressWarnings("checkstyle")
public interface PedersenLinkProofGenerator {

  /**
   * Size of the proof in bytes.
   */
  int PROOF_SIZE = Secp256k1Operations.PEDERSEN_LINK_SIZE;

  /**
   * Generates a proof linking an ElGamal ciphertext and a Pedersen commitment.
   *
   * @param c1        The ElGamal ephemeral key (R = r * G), 33 bytes compressed.
   * @param c2        The ElGamal masked value (S = m * G + r * Pk), 33 bytes compressed.
   * @param pk        The recipient's public key, 33 bytes compressed.
   * @param pcm       The Pedersen commitment (m * G + rho * H), 33 bytes compressed.
   * @param amount    The plaintext amount (m).
   * @param r         The 32-byte ElGamal blinding factor.
   * @param rho       The 32-byte Pedersen blinding factor.
   * @param contextId The 32-byte context identifier. Can be null.
   *
   * @return A 195-byte proof.
   *
   * @throws IllegalArgumentException if r or rho is not a valid scalar.
   * @throws IllegalStateException    if proof generation fails.
   */
  UnsignedByteArray generateProof(
    UnsignedByteArray c1,
    UnsignedByteArray c2,
    UnsignedByteArray pk,
    UnsignedByteArray pcm,
    UnsignedLong amount,
    UnsignedByteArray r,
    UnsignedByteArray rho,
    UnsignedByteArray contextId
  );
}

