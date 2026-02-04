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

import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyable;

/**
 * Interface for decrypting ElGamal-encrypted confidential balances.
 *
 * <p>This interface is generic over the type of private key it accepts,
 * allowing for both in-memory keys ({@link org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey})
 * and references to external keys ({@link org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyReference}).</p>
 *
 * @param <P> The type of private key this decryptor accepts, must extend {@link ElGamalPrivateKeyable}.
 *
 * @see org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyable
 * @see org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey
 * @see org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyReference
 */
public interface ElGamalBalanceDecryptor<P extends ElGamalPrivateKeyable> {

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * <p>This typically uses brute-force search over the discrete logarithm
   * and is only practical for small amounts (e.g., up to 1,000,000).</p>
   *
   * @param ciphertext The {@link ElGamalCiphertext} to decrypt.
   * @param privateKey The private key to use for decryption.
   *
   * @return The decrypted amount as a long.
   *
   * @throws IllegalArgumentException if the amount cannot be found within the search range.
   * @throws NullPointerException     if ciphertext or privateKey is null.
   */
  long decrypt(ElGamalCiphertext ciphertext, P privateKey);
}
