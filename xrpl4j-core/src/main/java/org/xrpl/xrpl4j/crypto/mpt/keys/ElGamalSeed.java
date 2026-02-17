package org.xrpl.xrpl4j.crypto.mpt.keys;

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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.SecureRandomUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A seed for generating ElGamal key pairs used in Confidential MPT operations.
 *
 * <p>Unlike XRPL account seeds (16 bytes), ElGamal seeds use 32 bytes of entropy
 * which is directly used as the private key scalar.</p>
 *
 * @see ElGamalKeyPair
 * @see ElGamalPrivateKey
 * @see ElGamalPublicKey
 */
public final class ElGamalSeed {

  public static final int ENTROPY_LENGTH = 32;

  private final UnsignedByteArray entropy;

  private ElGamalSeed(final UnsignedByteArray entropy) {
    this.entropy = UnsignedByteArray.of(entropy.toByteArray());
  }

  /**
   * Generates a random ElGamal seed using 32 bytes of secure random entropy.
   *
   * <p>The generated entropy is guaranteed to be a valid secp256k1 scalar
   * (0 &lt; entropy &lt; curve order).</p>
   *
   * @return A randomly generated {@link ElGamalSeed}.
   */
  public static ElGamalSeed generate() {
    byte[] entropy = new byte[ENTROPY_LENGTH];
    do {
      SecureRandomUtils.secureRandom().nextBytes(entropy);
    } while (!Secp256k1Operations.isValidScalar(entropy));
    return new ElGamalSeed(UnsignedByteArray.of(entropy));
  }

  /**
   * Creates an ElGamal seed from 32 bytes of entropy.
   *
   * <p>The entropy must be a valid secp256k1 scalar (0 &lt; entropy &lt; curve order).</p>
   *
   * @param entropy The 32-byte entropy value.
   *
   * @return An {@link ElGamalSeed}.
   *
   * @throws NullPointerException     if entropy is null.
   * @throws IllegalArgumentException if entropy is not exactly 32 bytes or is not a valid scalar.
   */
  public static ElGamalSeed fromEntropy(final byte[] entropy) {
    Objects.requireNonNull(entropy, "entropy must not be null");
    Preconditions.checkArgument(
      entropy.length == ENTROPY_LENGTH,
      "ElGamal seed entropy must be %s bytes, but was %s bytes",
      ENTROPY_LENGTH, entropy.length
    );
    Preconditions.checkArgument(
      Secp256k1Operations.isValidScalar(entropy),
      "ElGamal seed entropy must be a valid scalar (0 < entropy < curve order)"
    );
    return new ElGamalSeed(UnsignedByteArray.of(entropy));
  }

  /**
   * Creates an ElGamal seed from 32 bytes of entropy.
   *
   * <p>The entropy must be a valid secp256k1 scalar (0 &lt; entropy &lt; curve order).</p>
   *
   * @param entropy The 32-byte entropy value as an {@link UnsignedByteArray}.
   *
   * @return An {@link ElGamalSeed}.
   *
   * @throws NullPointerException     if entropy is null.
   * @throws IllegalArgumentException if entropy is not exactly 32 bytes or is not a valid scalar.
   */
  public static ElGamalSeed fromEntropy(final UnsignedByteArray entropy) {
    Objects.requireNonNull(entropy, "entropy must not be null");
    return fromEntropy(entropy.toByteArray());
  }

  /**
   * Derives an ElGamal key pair from this seed.
   *
   * <p>Since the seed entropy is guaranteed to be a valid scalar, this method
   * uses it directly as the private key without modification.</p>
   *
   * @return An {@link ElGamalKeyPair}.
   */
  public ElGamalKeyPair deriveKeyPair() {
    byte[] privateKeyBytes = entropy.toByteArray();

    // The entropy is already validated as a valid scalar, so use it directly
    BigInteger privateKeyScalar = new BigInteger(1, privateKeyBytes);

    // Derive public key: publicKey = privateKey * G
    ECPoint publicKeyPoint = Secp256k1Operations.multiplyG(privateKeyScalar);

    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(privateKeyBytes);
    ElGamalPublicKey publicKey = ElGamalPublicKey.fromEcPoint(publicKeyPoint);

    return ElGamalKeyPair.of(privateKey, publicKey);
  }

  /**
   * Returns the 32-byte entropy value.
   *
   * @return A copy of the entropy bytes as an {@link UnsignedByteArray}.
   */
  public UnsignedByteArray entropy() {
    return UnsignedByteArray.of(entropy.toByteArray());
  }
}

