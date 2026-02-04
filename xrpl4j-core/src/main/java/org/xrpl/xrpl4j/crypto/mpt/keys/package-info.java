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
 * Provides key management classes for Confidential Multi-Purpose Token (MPT) operations.
 *
 * <p>This package contains the ElGamal key hierarchy used for confidential balance
 * encryption and decryption. ElGamal encryption in Confidential MPT uses secp256k1
 * keys exclusively.</p>
 *
 * <p>The key hierarchy follows the same pattern as the main xrpl4j keys package:</p>
 * <ul>
 *   <li>{@link org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyable} - Marker interface for all ElGamal private keys</li>
 *   <li>{@link org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey} - In-memory private key implementation</li>
 *   <li>{@link org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyReference} - Reference to external key (HSM/KMS)</li>
 * </ul>
 *
 * @see org.xrpl.xrpl4j.crypto.mpt.elgamal
 */
package org.xrpl.xrpl4j.crypto.mpt.keys;

