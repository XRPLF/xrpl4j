package org.xrpl.xrpl4j.crypto.mpt.elgamal;

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

import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;

/**
 * Interface for decrypting ElGamal-encrypted confidential balances.
 *
 * <p>This interface works with {@link PrivateKey} instances for decryption.</p>
 *
 * @see PrivateKey
 */
public interface ElGamalDecryptor<P extends PrivateKeyable> {

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount within a specified range.
   *
   * <p>This uses brute-force search over the discrete logarithm within the specified range
   * [minAmount, maxAmount]. This is useful when the expected amount is known to be within
   * a specific range, allowing for faster decryption.</p>
   *
   * @param ciphertext The {@link ElGamalCiphertext} to decrypt.
   * @param privateKeyable The private key to use for decryption.
   * @param minAmount  The minimum amount to search (inclusive).
   * @param maxAmount  The maximum amount to search (inclusive).
   *
   * @return The decrypted amount as a long.
   *
   * @throws IllegalArgumentException if the amount cannot be found within the search range,
   *                                  or if minAmount is negative, or if minAmount > maxAmount.
   * @throws NullPointerException     if ciphertext or privateKey is null.
   */
  long decrypt(ElGamalCiphertext ciphertext, P privateKeyable, long minAmount, long maxAmount);
}
