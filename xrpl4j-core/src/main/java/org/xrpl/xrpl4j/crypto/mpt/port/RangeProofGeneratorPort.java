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

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

/**
 * Port of {@code secp256k1_bulletproof_prove_agg} from bulletproof_aggregated.c.
 *
 * <p>Generates an aggregated Bulletproof range proof proving that m values
 * are each in the range [0, 2^64 - 1].</p>
 *
 * <p>The proof is generated for Pedersen commitments of the form:
 * C = value * G + blindingFactor * H</p>
 *
 * <p>Proof sizes:
 * <ul>
 *   <li>m=1 (single value): 688 bytes (rounds=6)</li>
 *   <li>m=2 (two values): 754 bytes (rounds=7)</li>
 * </ul>
 */
public interface RangeProofGeneratorPort {

  /**
   * Size of a single-value proof in bytes (292 + 66*6 = 688).
   */
  int SINGLE_PROOF_SIZE = Secp256k1Operations.SINGLE_BULLETPROOF_SIZE;

  /**
   * Size of a double-value proof in bytes (292 + 66*7 = 754).
   */
  int DOUBLE_PROOF_SIZE = Secp256k1Operations.DOUBLE_BULLETPROOF_SIZE;

  /**
   * Number of bits per value (64).
   */
  int VALUE_BITS = 64;

  /**
   * Generates an aggregated bulletproof range proof.
   *
   * @param values        Array of m values to prove are in range [0, 2^64 - 1].
   *                      m must be 1 or 2.
   * @param blindingsFlat Flat array of m blinding factors (m * 32 bytes).
   *                      Each 32-byte segment is a blinding factor for the corresponding value.
   * @param pkBase        The generator H used for commitments (C = vG + rH), 33 bytes compressed.
   *                      This is typically {@code Secp256k1Operations.getH()}.
   * @param contextId     The 32-byte context identifier for domain separation. Can be null.
   *
   * @return The proof bytes (688 bytes for m=1, 754 bytes for m=2).
   *
   * @throws IllegalArgumentException if m is not 1 or 2, or if inputs are invalid.
   * @throws IllegalStateException    if proof generation fails.
   */
  UnsignedByteArray generateProof(
    UnsignedLong[] values,
    UnsignedByteArray blindingsFlat,
    UnsignedByteArray pkBase,
    UnsignedByteArray contextId
  );
}

