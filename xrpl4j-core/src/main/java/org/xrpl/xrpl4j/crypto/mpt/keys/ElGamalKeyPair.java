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

import java.util.Objects;

/**
 * An ElGamal key pair for secp256k1 used in Confidential MPT operations.
 *
 * <p>This class holds both the private and public keys for ElGamal encryption.
 * Unlike XRPL signing key pairs, ElGamal key pairs are used exclusively for
 * encryption/decryption and zero-knowledge proofs in confidential transactions.</p>
 *
 * @see ElGamalPrivateKey
 * @see ElGamalPublicKey
 * @see ElGamalSeed
 */
public final class ElGamalKeyPair {

  private final ElGamalPrivateKey privateKey;
  private final ElGamalPublicKey publicKey;

  private ElGamalKeyPair(final ElGamalPrivateKey privateKey, final ElGamalPublicKey publicKey) {
    this.privateKey = Objects.requireNonNull(privateKey, "privateKey must not be null");
    this.publicKey = Objects.requireNonNull(publicKey, "publicKey must not be null");
  }

  /**
   * Creates an ElGamal key pair from a private key and public key.
   *
   * @param privateKey The ElGamal private key.
   * @param publicKey  The ElGamal public key.
   * @return An {@link ElGamalKeyPair}.
   * @throws NullPointerException if privateKey or publicKey is null.
   */
  public static ElGamalKeyPair of(final ElGamalPrivateKey privateKey, final ElGamalPublicKey publicKey) {
    return new ElGamalKeyPair(privateKey, publicKey);
  }

  /**
   * Generates a random ElGamal key pair using secure random entropy.
   *
   * <p>This is a convenience method equivalent to {@code ElGamalSeed.generate().deriveKeyPair()}.</p>
   *
   * @return A randomly generated {@link ElGamalKeyPair}.
   */
  public static ElGamalKeyPair generate() {
    return ElGamalSeed.generate().deriveKeyPair();
  }

  /**
   * Creates an ElGamal key pair from 32 bytes of entropy.
   *
   * <p>This is a convenience method equivalent to {@code ElGamalSeed.fromEntropy(entropy).deriveKeyPair()}.</p>
   *
   * @param entropy The 32-byte entropy value.
   * @return An {@link ElGamalKeyPair}.
   * @throws NullPointerException     if entropy is null.
   * @throws IllegalArgumentException if entropy is not exactly 32 bytes.
   */
  public static ElGamalKeyPair fromEntropy(final byte[] entropy) {
    return ElGamalSeed.fromEntropy(entropy).deriveKeyPair();
  }

  /**
   * Creates an ElGamal key pair from a passphrase.
   *
   * <p>This is a convenience method equivalent to {@code ElGamalSeed.fromPassphrase(passphrase).deriveKeyPair()}.</p>
   *
   * <p><strong>WARNING:</strong> This method is provided for convenience and testing.
   * For production use, prefer {@link #generate()} or {@link #fromEntropy(byte[])}.</p>
   *
   * @param passphrase The passphrase to use for key derivation.
   * @return An {@link ElGamalKeyPair}.
   * @throws NullPointerException if passphrase is null.
   */
  public static ElGamalKeyPair fromPassphrase(final String passphrase) {
    return ElGamalSeed.fromPassphrase(passphrase).deriveKeyPair();
  }

  /**
   * Returns the private key.
   *
   * @return The {@link ElGamalPrivateKey}.
   */
  public ElGamalPrivateKey privateKey() {
    return privateKey;
  }

  /**
   * Returns the public key.
   *
   * @return The {@link ElGamalPublicKey}.
   */
  public ElGamalPublicKey publicKey() {
    return publicKey;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ElGamalKeyPair that = (ElGamalKeyPair) obj;
    return Objects.equals(privateKey, that.privateKey) && Objects.equals(publicKey, that.publicKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(privateKey, publicKey);
  }

  @Override
  public String toString() {
    return "ElGamalKeyPair{publicKey=" + publicKey + "}";
  }
}

