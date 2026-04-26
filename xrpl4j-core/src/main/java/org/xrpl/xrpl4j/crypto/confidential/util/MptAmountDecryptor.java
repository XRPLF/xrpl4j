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

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;

/**
 * High-level interface for ElGamal decryption of MPT amounts.
 *
 * <p>This interface mirrors the C utility function {@code mpt_decrypt_amount} from mpt_utility.h,
 * but uses Java-friendly types for all parameters and return values.</p>
 *
 * <p>Decryption involves:</p>
 * <ol>
 *   <li>Parsing the ciphertext into c1 and c2 components</li>
 *   <li>Computing the shared secret: S = privateKey * C1</li>
 *   <li>Recovering the masked amount: M = C2 - S = amount * G</li>
 *   <li>Brute-force search for amount such that amount * G = M</li>
 * </ol>
 *
 */
public interface MptAmountDecryptor {

  /**
   * Decrypts an ElGamal ciphertext to recover the original MPT amount.
   *
   * <p>Uses brute-force search over the discrete logarithm within the range [minAmount, maxAmount].
   * For typical MPT transactions, amounts are expected to be small enough for efficient decryption.</p>
   *
   * @param ciphertext The {@link EncryptedAmount} to decrypt.
   * @param privateKey The secp256k1 private key for decryption.
   * @param minAmount  The minimum amount to search (inclusive). Must be in range [0, 2^63-1].
   * @param maxAmount  The maximum amount to search (inclusive). Must be in range [0, 2^63-1].
   *
   * @return The decrypted amount.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if privateKey is not a secp256k1 key.
   * @throws IllegalStateException    if decryption fails (e.g., amount not found in range).
   */
  UnsignedLong decrypt(
    EncryptedAmount ciphertext, PrivateKey privateKey, UnsignedLong minAmount, UnsignedLong maxAmount
  );
}

