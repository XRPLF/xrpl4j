package org.xrpl.xrpl4j.crypto.mpt.port;

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

/**
 * Port of {@code secp256k1_elgamal_encrypt} from elgamal.c.
 */
public interface ElGamalEncryptorPort {

  /**
   * Encrypts an amount using ElGamal encryption.
   *
   * @param amount         The plaintext amount to encrypt.
   * @param pubkeyQ        The recipient's public key (33 bytes, compressed).
   * @param blindingFactor The random blinding factor (32 bytes).
   *
   * @return The ciphertext containing c1 and c2 components.
   *
   * @throws IllegalStateException if encryption fails (e.g., invalid inputs produce point at infinity).
   */
  ElGamalCiphertext encrypt(
    UnsignedLong amount,
    UnsignedByteArray pubkeyQ,
    UnsignedByteArray blindingFactor
  );
}

