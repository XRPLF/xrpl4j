package org.xrpl.xrpl4j.crypto.confidential;

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

import org.xrpl.xrpl4j.crypto.SecureRandomUtils;

/**
 * A {@link BlindingFactorGenerator} that uses secure random entropy.
 *
 * <p>The generated values are guaranteed to be valid secp256k1 scalars
 * (0 &lt; value &lt; curve order) using rejection sampling, matching the
 * behavior of the C reference implementation.</p>
 *
 * <p>This is the default implementation used in production. For testing,
 * a mock implementation can be injected to produce deterministic values.</p>
 */
public class SecureRandomBlindingFactorGenerator implements BlindingFactorGenerator {

  /**
   * Generates a random blinding factor using secure random entropy.
   *
   * <p>Uses rejection sampling to ensure the generated value is a valid
   * secp256k1 scalar (0 &lt; value &lt; curve order).</p>
   *
   * @return A newly generated {@link BlindingFactor}.
   */
  @Override
  public BlindingFactor generate() {
    byte[] bytes = new byte[Secp256k1Operations.BLINDING_FACTOR_SIZE];
    do {
      SecureRandomUtils.secureRandom().nextBytes(bytes);
    } while (!Secp256k1Operations.isValidScalar(bytes));
    return BlindingFactor.fromBytes(bytes);
  }
}

