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
 * Port of {@code secp256k1_mpt_pok_sk_prove} from proof_pok_sk.c.
 *
 * <p>Generates a Schnorr Proof of Knowledge (PoK) that proves possession of the secret key
 * corresponding to a public key, without revealing the secret key.</p>
 *
 * <p>The proof format is: T (33 bytes compressed point) || s (32 bytes scalar) = 65 bytes total.</p>
 */
public interface SecretKeyProofGenerator {

  /**
   * Generates a Schnorr Proof of Knowledge for the given secret key.
   *
   * @param pk        The public key (33 bytes compressed).
   * @param sk        The secret key (32 bytes scalar).
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return A 65-byte proof (T || s).
   *
   * @throws IllegalStateException if proof generation fails (matches C returning 0).
   */
  UnsignedByteArray generateProof(
    UnsignedByteArray pk,
    UnsignedByteArray sk,
    UnsignedByteArray contextId
  );
}

