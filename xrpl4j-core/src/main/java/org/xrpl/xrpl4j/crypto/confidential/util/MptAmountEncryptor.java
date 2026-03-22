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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.elgamal.ElGamalEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * High-level interface for ElGamal encryption of MPT amounts.
 *
 * <p>This interface mirrors the C utility function {@code mpt_encrypt_amount} from mpt_utility.h,
 * but uses Java-friendly types for all parameters and return values.</p>
 *
 * @see ElGamalEncryptor
 */
public interface MptAmountEncryptor {

  /**
   * Encrypts an MPT amount using ElGamal encryption.
   *
   * <p>The encryption produces a 66-byte ciphertext consisting of two compressed secp256k1 points:</p>
   * <ul>
   *   <li>C1 = r * G (33 bytes)</li>
   *   <li>C2 = m * G + r * Q (33 bytes)</li>
   * </ul>
   *
   * <p>where r is the blinding factor, m is the amount, G is the generator point,
   * and Q is the recipient's public key.</p>
   *
   * @param amount         The amount to encrypt (0 to 2^63-1 for MPT protocol).
   * @param publicKey      The recipient's secp256k1 public key.
   * @param blindingFactor The 32-byte random blinding factor.
   *
   * @return The encrypted amount as an {@link EncryptedAmount}.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if publicKey is not a secp256k1 key.
   */
  EncryptedAmount encrypt(UnsignedLong amount, PublicKey publicKey, BlindingFactor blindingFactor);
}

