package org.xrpl.xrpl4j.crypto.signing.bc;

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

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;

/**
 * Static constants for Secp256k1 operations.
 *
 * @deprecated This interface is deprecated in-favor of {@link BcKeyUtils#PARAMS}.
 */
@Deprecated
public interface Secp256k1 {

  /**
   * Elliptic Curve parameters for the curve named `secp256k1`.
   *
   * @deprecated This interface is deprecated in-favor of {@link BcKeyUtils#PARAMS}.
   */
  @Deprecated
  X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");

  /**
   * Elliptic Curve domain parameters for the curve named `secp256k1`.
   *
   * @deprecated This interface is deprecated in-favor of {@link BcKeyUtils#PARAMS}.
   */
  @Deprecated
  ECDomainParameters EC_DOMAIN_PARAMETERS = new ECDomainParameters(
    EC_PARAMETERS.getCurve(),
    EC_PARAMETERS.getG(),
    EC_PARAMETERS.getN(),
    EC_PARAMETERS.getH()
  );

}
