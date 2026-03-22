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
 * Port of {@code secp256k1_equality_plaintext_prove} from proof_equality_plaintext.c.
 *
 * <p>Generates a Sigma protocol proof (Chaum-Pedersen style) that proves knowledge of the
 * secret key r such that C1 = r*G and C2 - m*G = r*Pk, where m is the known plaintext.</p>
 *
 * <p>The proof format is: T1 (33 bytes) || T2 (33 bytes) || s (32 bytes) = 98 bytes total.</p>
 *
 * <p>C function signature:
 * <pre>
 * int secp256k1_equality_plaintext_prove(
 *     secp256k1_context const* ctx,
 *     uint8_t proof[kMPT_EQUALITY_PLAINTEXT_PROOF_SIZE],
 *     secp256k1_pubkey const* c1,
 *     secp256k1_pubkey const* c2,
 *     secp256k1_pubkey const* pk_recipient,
 *     uint64_t const m,
 *     uint8_t const r[kMPT_PRIVKEY_SIZE],
 *     uint8_t const context_id[kMPT_HALF_SHA_SIZE])
 * </pre>
 */
@SuppressWarnings("checkstyle")
public interface PlaintextEqualityProofGenerator {

  /**
   * The length of the proof in bytes: T1 (33) + T2 (33) + s (32) = 98.
   */
  int PROOF_LENGTH = 98;

  /**
   * Generates a plaintext equality proof.
   *
   * @param c1        The first ciphertext component (33 bytes compressed point).
   * @param c2        The second ciphertext component (33 bytes compressed point).
   * @param pk        The recipient's public key (33 bytes compressed point).
   * @param amount    The plaintext amount being proven.
   * @param r         The secret key/randomness (32 bytes scalar).
   * @param contextId The 32-byte context identifier for domain separation.
   *
   * @return A 98-byte proof (T1 || T2 || s).
   *
   * @throws IllegalArgumentException if any point is invalid or r is not a valid scalar.
   * @throws IllegalStateException    if proof generation fails.
   */
  UnsignedByteArray generateProof(
    UnsignedByteArray c1,
    UnsignedByteArray c2,
    UnsignedByteArray pk,
    UnsignedLong amount,
    UnsignedByteArray r,
    UnsignedByteArray contextId
  );
}

