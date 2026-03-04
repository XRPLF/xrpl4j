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
 * Port of {@code secp256k1_bulletproof_verify_agg} from bulletproof_aggregated.c.
 *
 * <p>Verifies an aggregated Bulletproof range proof proving that m committed values
 * are each in the range [0, 2^64 - 1].</p>
 */
public interface RangeProofVerifier {

  /**
   * Verifies an aggregated bulletproof range proof.
   *
   * @param gVec          Generator vector G of length n = 64 * m (each 33 bytes compressed).
   * @param hVec          Generator vector H of length n = 64 * m (each 33 bytes compressed).
   * @param proof         The proof bytes (688 bytes for m=1, 754 bytes for m=2).
   * @param commitmentVec Array of m Pedersen commitments (each 33 bytes compressed).
   * @param pkBase        The generator H used for commitments (C = vG + rH), 33 bytes compressed.
   * @param contextId     The 32-byte context identifier for domain separation. Can be null.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verifyProof(
    UnsignedByteArray[] gVec,
    UnsignedByteArray[] hVec,
    UnsignedByteArray proof,
    UnsignedByteArray[] commitmentVec,
    UnsignedByteArray pkBase,
    UnsignedByteArray contextId
  );
}

