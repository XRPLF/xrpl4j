package org.xrpl.xrpl4j.crypto.mpt.context;

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
 * Common interface for context hashes used in ElGamal-Pedersen linkage proofs.
 *
 * <p>Context hashes bind proofs to specific transactions, preventing replay attacks.
 * This interface is implemented by:
 * <ul>
 *   <li>{@link ConfidentialMPTSendContext} - for ConfidentialMPTSend transactions</li>
 *   <li>{@link ConfidentialMPTConvertBackContext} - for ConfidentialMPTConvertBack transactions</li>
 * </ul>
 *
 * @see org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ElGamalPedersenLinkProofGenerator
 */
public interface LinkProofContext {

  /**
   * The length of the context hash in bytes.
   */
  int CONTEXT_LENGTH = 32;

  /**
   * Returns the context hash as a byte array.
   *
   * @return A copy of the 32-byte context hash.
   */
  byte[] toBytes();

  /**
   * Returns the context hash as an uppercase hex string.
   *
   * @return A 64-character uppercase hex string.
   */
  String hexValue();
}

