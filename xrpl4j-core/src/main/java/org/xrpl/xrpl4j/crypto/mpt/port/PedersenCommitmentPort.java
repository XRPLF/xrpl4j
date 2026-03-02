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

/**
 * Port of {@code secp256k1_mpt_pedersen_commit} from commitments.c.
 *
 * <p>Creates a Pedersen Commitment: C = amount * G + rho * H, where G is the standard
 * secp256k1 generator and H is a NUMS (Nothing-Up-My-Sleeve) generator derived using
 * hash-to-curve.</p>
 *
 * <p>The commitment is returned as a 33-byte compressed point.</p>
 */
public interface PedersenCommitmentPort {

  /**
   * Generates a Pedersen Commitment for the given amount and blinding factor.
   *
   * <p>Handles the edge case where amount = 0, in which case C = rho * H.</p>
   *
   * @param amount The value to commit to (64-bit unsigned).
   * @param rho    The blinding factor (32 bytes, must be a valid scalar).
   *
   * @return A 33-byte compressed point representing the commitment.
   *
   * @throws IllegalArgumentException if rho is not a valid scalar.
   * @throws IllegalStateException    if commitment generation fails.
   */
  UnsignedByteArray generateCommitment(UnsignedLong amount, UnsignedByteArray rho);
}

