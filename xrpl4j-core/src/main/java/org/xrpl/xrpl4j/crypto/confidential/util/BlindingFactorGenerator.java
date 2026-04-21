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

import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;

/**
 * Interface for generating blinding factors used in ElGamal encryption and zero-knowledge proofs.
 *
 * @see BlindingFactor
 */
public interface BlindingFactorGenerator {

  /**
   * Generates a new blinding factor.
   *
   * <p>The generated value must be a valid secp256k1 scalar (0 &lt; value &lt; curve order).</p>
   *
   * @return A newly generated {@link BlindingFactor}.
   */
  BlindingFactor generate();
}
