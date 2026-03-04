package org.xrpl.xrpl4j.crypto.confidential.elgamal;

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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;

/**
 * Port of {@code secp256k1_elgamal_decrypt} from elgamal.c.
 *
 * <p>Decryption involves two steps:</p>
 * <ol>
 *   <li>Remove the mask: M = C2 - sk * C1 = m * G</li>
 *   <li>Recover m from M by solving the Discrete Logarithm Problem (DLP) via brute-force search</li>
 * </ol>
 */
public interface ElGamalDecryptor {

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * <p>Uses brute-force search over the discrete logarithm within the range [minAmount, maxAmount].
   * The C implementation searches [0, 1,000,000] by default.</p>
   *
   * @param ciphertext The {@link EncryptedAmount} containing c1 and c2 components.
   * @param privkey    The private key for decryption (32 bytes).
   * @param minAmount  The minimum amount to search (inclusive). Must be in range [0, 2^63-1].
   * @param maxAmount  The maximum amount to search (inclusive). Must be in range [0, 2^63-1].
   *
   * @return The decrypted amount.
   *
   * @throws IllegalStateException if decryption fails (e.g., amount not found in range,
   *                               or invalid inputs produce point at infinity).
   */
  UnsignedLong decrypt(
    EncryptedAmount ciphertext,
    UnsignedByteArray privkey,
    UnsignedLong minAmount,
    UnsignedLong maxAmount
  );
}

