package org.xrpl.xrpl4j.crypto.core.keys;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Interface of a service that can perform the crypto operations necessary derive XRPL seeds, key-pairs, and addresses.
 */
public interface KeyPairService {

  /**
   * Generate a random 16-byte seed to be used to derive a private key.
   *
   * @return A {@link String} containing a randomly generated Base58Check encoded seed value.
   * @deprecated This method will be removed in a future release. Prefer {@link Seed#ed25519Seed()} or
   *  {@link Seed#secp256k1Seed()}.
   */
  @Deprecated
  default Seed generateSeed() {
    return generateSeed(Entropy.newInstance());
  }

  /**
   * Generate a 16 byte seed, which can be used to derive a private key, from a non-encoded value.
   *
   * @param entropy An {@link UnsignedByteArray} containing the bytes of entropy to encode into a seed.
   * @return A {@link String} containing the Base58Check encoded seed value.
   * @deprecated This method will be removed in a future release. Prefer {@link Seed#ed25519SeedFromEntropy(Entropy)}
   *    or {@link Seed#secp256k1SeedFromEntropy(Entropy)}.
   */
  @Deprecated
  Seed generateSeed(Entropy entropy);

  /**
   * Derive a public/private keypair from a Base58Check encoded 16 byte seed.
   *
   * @param seed A Base58Check encoded {@link String} containing the seed.
   * @return The {@link KeyPair} derived from the seed.
   */
  KeyPair deriveKeyPair(Seed seed);
}
