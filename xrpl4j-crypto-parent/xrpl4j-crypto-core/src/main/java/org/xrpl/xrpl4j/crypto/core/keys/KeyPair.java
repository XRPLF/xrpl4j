package org.xrpl.xrpl4j.crypto.core.keys;

import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.core.keys.ImmutableKeyPair.Builder;

/**
 * Represents an XRPL public/private key pair.
 */
@Value.Immutable
public interface KeyPair {

  static Builder builder() {
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
