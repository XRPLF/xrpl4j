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

/**
 * Port of {@code secp256k1_equality_plaintext_verify} from proof_equality_plaintext.c.
 *
 * <p>Verifies a Sigma protocol proof (Chaum-Pedersen style) that proves knowledge of the
 * secret key r such that C1 = r*G and C2 - m*G = r*Pk, where m is the known plaintext.</p>
 *
 * <p>C function signature:
 * <pre>
 * int secp256k1_equality_plaintext_verify(
 *     secp256k1_context const* ctx,
 *     uint8_t const proof[kMPT_EQUALITY_PLAINTEXT_PROOF_SIZE],
 *     secp256k1_pubkey const* c1,
 *     secp256k1_pubkey const* c2,
 *     secp256k1_pubkey const* pk_recipient,
 *     uint64_t const m,
 *     uint8_t const context_id[kMPT_HALF_SHA_SIZE])
 * </pre>
 */
@SuppressWarnings("checkstyle")
public interface PlaintextEqualityProofVerifier {

  /**
   * Verifies a plaintext equality proof.
   *
   * @param proof     The 98-byte proof (T1 || T2 || s).
   * @param c1        The first ciphertext component (33 bytes compressed point).
   * @param c2        The second ciphertext component (33 bytes compressed point).
   * @param pk        The recipient's public key (33 bytes compressed point).
   * @param amount    The plaintext amount being verified.
   * @param contextId The 32-byte context identifier for domain separation.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verifyProof(
    UnsignedByteArray proof,
    UnsignedByteArray c1,
    UnsignedByteArray c2,
    UnsignedByteArray pk,
    UnsignedLong amount,
    UnsignedByteArray contextId
  );
}

