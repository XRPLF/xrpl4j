package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

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
 * Specifies the type of ElGamal-Pedersen linkage proof to generate.
 *
 * <p>The linkage proof proves that an ElGamal ciphertext and a Pedersen commitment
 * encode the same plaintext amount. The proof parameters are ordered differently
 * depending on whether we're proving linkage for an amount commitment or a balance commitment.</p>
 *
 * @see ElGamalPedersenLinkProofGenerator
 */
public enum LinkageProofType {

  /**
   * Amount commitment linkage proof.
   *
   * <p>Used when proving that a newly encrypted amount matches a Pedersen commitment.
   * The parameters are ordered as:
   * <ul>
   *   <li>c1 = ciphertext.c1 (r * G)</li>
   *   <li>c2 = ciphertext.c2 (m * G + r * Pk)</li>
   *   <li>pk = recipient's ElGamal public key</li>
   *   <li>r = ElGamal blinding factor (random scalar)</li>
   * </ul>
   */
  AMOUNT_COMMITMENT,

  /**
   * Balance commitment linkage proof.
   *
   * <p>Used when proving that an existing encrypted balance matches a Pedersen commitment.
   * The parameters are swapped to prove knowledge of the private key:
   * <ul>
   *   <li>c1 = sender's ElGamal public key (sk * G)</li>
   *   <li>c2 = ciphertext.c2</li>
   *   <li>pk = ciphertext.c1</li>
   *   <li>r = sender's ElGamal private key</li>
   * </ul>
   */
  BALANCE_COMMITMENT
}

