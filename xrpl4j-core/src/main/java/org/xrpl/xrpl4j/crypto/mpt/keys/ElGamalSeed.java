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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
  private static final Secp256k1Operations SECP256K1 = new Secp256k1Operations();

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
    } while (!SECP256K1.isValidScalar(entropy));
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
      SECP256K1.isValidScalar(entropy),
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
   * Creates an ElGamal seed from a passphrase using SHA-256 hashing.
   *
   * <p>If the initial hash is not a valid secp256k1 scalar, the method will
   * append a counter and rehash until a valid scalar is produced.</p>
   *
   * <p><strong>WARNING:</strong> This method is provided for convenience and testing.
   * For production use, prefer {@link #generate()} or {@link #fromEntropy(byte[])} with cryptographically secure random
   * entropy.</p>
   *
   * @param passphrase The passphrase to hash.
   *
   * @return An {@link ElGamalSeed}.
   *
   * @throws NullPointerException  if passphrase is null.
   * @throws IllegalStateException if a valid scalar cannot be derived after 256 attempts.
   */
  public static ElGamalSeed fromPassphrase(final String passphrase) {
    Objects.requireNonNull(passphrase, "passphrase must not be null");
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(passphrase.getBytes(StandardCharsets.UTF_8));

      // If hash is not a valid scalar, append counter and rehash
      int counter = 0;
      while (!SECP256K1.isValidScalar(hash)) {
        digest.reset();
        digest.update(passphrase.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) counter++);
        hash = digest.digest();
        if (counter > 255) {
          throw new IllegalStateException("Failed to derive valid scalar from passphrase after 256 attempts");
        }
      }
      return new ElGamalSeed(UnsignedByteArray.of(hash));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
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
    ECPoint publicKeyPoint = SECP256K1.multiplyG(privateKeyScalar);

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

