package org.xrpl.xrpl4j.crypto.confidential.util;

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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;

/**
 * High-level interface for generating Pedersen Commitments.
 *
 * <p>A Pedersen Commitment is computed as: C = amount * G + rho * H, where G is the standard
 * secp256k1 generator and H is a NUMS (Nothing-Up-My-Sleeve) generator.</p>
 *
 * <p>This interface uses Java-friendly types for all parameters and return values.</p>
 *
 * @see org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment
 */
public interface PedersenCommitmentGenerator {

  /**
   * Generates a Pedersen Commitment for the given amount and blinding factor.
   *
   * @param amount         The value to commit to.
   * @param blindingFactor The blinding factor (rho).
   *
   * @return A {@link org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment} containing the 33-byte
   *   compressed commitment.
   *
   * @throws NullPointerException  if any parameter is null.
   * @throws IllegalStateException if commitment generation fails.
   */
  org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment generateCommitment(
    UnsignedLong amount, BlindingFactor blindingFactor
  );
}

