package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

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

import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * In-memory implementation of ElGamal decryption over the secp256k1 elliptic curve.
 *
 * <p>This implementation uses BouncyCastle for cryptographic operations and works
 * with in-memory {@link ElGamalPrivateKey} instances.</p>
 *
 * <p><strong>WARNING:</strong> This implementation uses in-memory private key material.
 * For server-side applications, consider using an implementation that works with
 * {@link org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyReference} instead.</p>
 *
 * @see ElGamalBalanceDecryptor
 * @see ElGamalPrivateKey
 */
public class JavaElGamalBalanceDecryptor implements ElGamalBalanceDecryptor<ElGamalPrivateKey> {

  /**
   * Maximum amount that can be decrypted via brute-force search.
   */
  public static final long MAX_DECRYPTABLE_AMOUNT = 1_000_000L;

  /**
   * Constructs a new {@link JavaElGamalBalanceDecryptor} instance.
   */
  public JavaElGamalBalanceDecryptor() {
  }

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * <p>This uses brute-force search over the discrete logarithm and is only practical
   * for small amounts (up to {@link #MAX_DECRYPTABLE_AMOUNT}).</p>
   *
   * <p>The decryption algorithm:</p>
   * <ol>
   *   <li>Compute shared secret: S = privateKey * C1</li>
   *   <li>Recover masked amount: M = C2 - S = amount * G</li>
   *   <li>Brute-force search for amount such that amount * G = M</li>
   * </ol>
   *
   * @param ciphertext The {@link ElGamalCiphertext} to decrypt.
   * @param privateKey The {@link ElGamalPrivateKey} to use for decryption.
   *
   * @return The decrypted amount as a long.
   *
   * @throws IllegalArgumentException if the amount cannot be found within the search range.
   * @throws NullPointerException     if ciphertext or privateKey is null.
   */
  @Override
  public long decrypt(final ElGamalCiphertext ciphertext, final ElGamalPrivateKey privateKey) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(privateKey, "privateKey must not be null");

    byte[] privateKeyBytes = privateKey.naturalBytes().toByteArray();

    BigInteger privKeyScalar = new BigInteger(1, privateKeyBytes);

    // S = privateKey * C1
    ECPoint sharedSecret = Secp256k1Operations.multiply(ciphertext.c1(), privKeyScalar);

    // Check for amount = 0: C2 == S
    if (Secp256k1Operations.pointsEqual(ciphertext.c2(), sharedSecret)) {
      return 0;
    }

    // M = C2 - S
    ECPoint negS = Secp256k1Operations.negate(sharedSecret);
    ECPoint m = Secp256k1Operations.add(ciphertext.c2(), negS);

    // Brute-force search: find i such that i * G == M
    ECPoint gPoint = Secp256k1Operations.getG();
    ECPoint currentM = gPoint;

    for (long i = 1; i <= MAX_DECRYPTABLE_AMOUNT; i++) {
      if (Secp256k1Operations.pointsEqual(m, currentM)) {
        return i;
      }
      currentM = Secp256k1Operations.add(currentM, gPoint);
    }

    throw new IllegalArgumentException(
      "Amount not found within search range (0 to " + MAX_DECRYPTABLE_AMOUNT + ")"
    );

  }
}
