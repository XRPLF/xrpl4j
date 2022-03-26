package org.xrpl.xrpl4j.keypairs;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: keypairs
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

/**
 * Static constants for Secp256k1 cryptography.
 */
public interface Secp256k1 {

  X9ECParameters x9ECParameters = SECNamedCurves.getByName("secp256k1");
  ECDomainParameters ecDomainParameters = new ECDomainParameters(
    x9ECParameters.getCurve(),
    x9ECParameters.getG(),
    x9ECParameters.getN(),
    x9ECParameters.getH()
  );

}
