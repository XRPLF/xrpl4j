package org.xrpl.xrpl4j.crypto.keys;

import org.immutables.value.Value;

/**
 * Represents an XRPL public/private key pair.
 */
@Value.Immutable
public interface KeyPair {

  /**
   * Convenience builder.
   *
   * @return A {@link ImmutableKeyPair.Builder}.
   */
  static ImmutableKeyPair.Builder builder() {
    return ImmutableKeyPair.builder();
  }

  /**
   * The private key of this {@link KeyPair}.
   *
   * @return A {@link PrivateKey} containing the private key.
   */
  PrivateKey privateKey();

  /**
   * The public key of this {@link KeyPair}.
   *
   * @return A {@link PublicKey} containing the public key.
   */
  PublicKey publicKey();

}
