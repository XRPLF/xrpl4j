package org.xrpl.xrpl4j.crypto.mpt.keys;

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

/**
 * Marker interface for ElGamal private keys used in Confidential MPT operations.
 *
 * <p>ElGamal encryption in Confidential MPT uses secp256k1 keys exclusively.
 * This interface serves as the parent type for both in-memory keys
 * ({@link ElGamalPrivateKey}) and references to external keys
 * ({@link ElGamalPrivateKeyReference}).</p>
 *
 * <p>This design follows the same pattern as {@link org.xrpl.xrpl4j.crypto.keys.PrivateKeyable}
 * but is specific to ElGamal operations and does not require a {@code keyType()} method
 * since ElGamal always uses secp256k1.</p>
 *
 * @see ElGamalPrivateKey
 * @see ElGamalPrivateKeyReference
 */
public interface ElGamalPrivateKeyable {
  // Marker interface - no methods required.
  // ElGamal always uses secp256k1, so no keyType() is needed.
}

