package org.xrpl.xrpl4j.crypto.mpt.port;

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
 * Port of {@code secp256k1_mpt_pok_sk_verify} from proof_pok_sk.c.
 *
 * <p>Verifies a Schnorr Proof of Knowledge (PoK) that proves possession of the secret key
 * corresponding to a public key.</p>
 */
public interface SecretKeyProofVerifierPort {

  /**
   * Verifies a Schnorr Proof of Knowledge.
   *
   * <p>Verification checks: s * G == T + e * P</p>
   *
   * @param proof     The 65-byte proof (T || s).
   * @param pk        The public key (33 bytes compressed).
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verifyProof(
    UnsignedByteArray proof,
    UnsignedByteArray pk,
    UnsignedByteArray contextId
  );
}

