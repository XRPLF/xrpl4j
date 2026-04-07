package org.xrpl.xrpl4j.crypto.confidential.util.jna;

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
 * Pure Java bridge interface for the mpt-crypto native library.
 *
 * <p>This interface defines the contract for native cryptographic operations without depending on JNA.
 * The JNA-based implementation ({@code JnaNativeMptCrypto}) lives in the {@code xrpl4j-mpt-crypto} module
 * and is loaded via reflection when {@code xrpl4j-mpt-crypto} is on the classpath.</p>
 *
 * <p>This design allows {@code xrpl4j-core} to contain the encryptor/decryptor logic without
 * a compile-time dependency on JNA or the native library.</p>
 */
public interface NativeMptCrypto {

  /**
   * Encrypts a {@code uint64} amount using ElGamal encryption with a secp256k1 public key.
   *
   * @param amount          The integer value to encrypt.
   * @param pubkey          The 33-byte compressed secp256k1 public key.
   * @param blindingFactor  The 32-byte random blinding factor (scalar r).
   * @param outCiphertext   A 66-byte buffer to receive the resulting ciphertext (C1 || C2).
   *
   * @return 0 on success, -1 on failure.
   */
  int encryptAmount(long amount, byte[] pubkey, byte[] blindingFactor, byte[] outCiphertext);

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * @param ciphertext The 66-byte ciphertext buffer (C1 || C2).
   * @param privkey    The 32-byte private key.
   * @param outAmount  A 1-element {@code long[]} to receive the decrypted amount.
   *
   * @return 0 on success, -1 on failure.
   */
  int decryptAmount(byte[] ciphertext, byte[] privkey, long[] outAmount);

  /**
   * Generates a 32-byte random blinding factor suitable for ElGamal encryption.
   *
   * @param outFactor A 32-byte buffer to receive the blinding factor.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateBlindingFactor(byte[] outFactor);
}
