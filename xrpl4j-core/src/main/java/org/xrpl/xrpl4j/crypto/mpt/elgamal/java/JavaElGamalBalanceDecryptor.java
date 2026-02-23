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

import com.google.common.base.Preconditions;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;

import java.math.BigInteger;
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
   * Constructs a new {@link JavaElGamalBalanceDecryptor} instance.
   */
  public JavaElGamalBalanceDecryptor() {
  }

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount within a specified range.
   *
   * <p>This uses brute-force search over the discrete logarithm within the specified range
   * [minAmount, maxAmount]. This is useful when the expected amount is known to be within
   * a specific range, allowing for faster decryption.</p>
   *
   * <p>The decryption algorithm:</p>
   * <ol>
   *   <li>Compute shared secret: S = privateKey * C1</li>
   *   <li>Recover masked amount: M = C2 - S = amount * G</li>
   *   <li>Brute-force search for amount such that amount * G = M within [minAmount, maxAmount]</li>
   * </ol>
   *
   * @param ciphertext The {@link ElGamalCiphertext} to decrypt.
   * @param privateKey The {@link ElGamalPrivateKey} to use for decryption.
   * @param minAmount  The minimum amount to search (inclusive).
   * @param maxAmount  The maximum amount to search (inclusive).
   *
   * @return The decrypted amount as a long.
   *
   * @throws IllegalArgumentException if the amount cannot be found within the search range,
   *                                  or if minAmount is negative, or if minAmount > maxAmount.
   * @throws NullPointerException     if ciphertext or privateKey is null.
   */
  @Override
  public long decrypt(
    final ElGamalCiphertext ciphertext,
    final ElGamalPrivateKey privateKey,
    final long minAmount,
    final long maxAmount
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Preconditions.checkArgument(minAmount >= 0, "minAmount must be non-negative, but was %s", minAmount);
    Preconditions.checkArgument(
      minAmount <= maxAmount,
      "minAmount (%s) must be less than or equal to maxAmount (%s)",
      minAmount, maxAmount
    );

    byte[] privateKeyBytes = privateKey.naturalBytes().toByteArray();
    BigInteger privKeyScalar = new BigInteger(1, privateKeyBytes);

    // S = privateKey * C1
    ECPoint sharedSecret = Secp256k1Operations.multiply(ciphertext.c1(), privKeyScalar);

    // M = C2 - S
    ECPoint negS = Secp256k1Operations.negate(sharedSecret);
    ECPoint m = Secp256k1Operations.add(ciphertext.c2(), negS);

    // Brute-force search: find i such that i * G == M within [minAmount, maxAmount]
    ECPoint gPoint = Secp256k1Operations.getG();
    ECPoint currentM = Secp256k1Operations.multiplyG(BigInteger.valueOf(minAmount));

    for (long i = minAmount; i <= maxAmount; i++) {
      if (Secp256k1Operations.pointsEqual(m, currentM)) {
        return i;
      }
      currentM = Secp256k1Operations.add(currentM, gPoint);
    }

    throw new IllegalArgumentException(
      "Amount not found within search range (" + minAmount + " to " + maxAmount + ")"
    );
  }
}
