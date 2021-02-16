package org.xrpl.xrpl4j.keypairs;

import org.immutables.value.Value;

/**
 * Represents an XRPL public/private key pair.
 *
 * @deprecated This class will go away in a future version. Prefer xrpl4j-crypto variants instead.
 */
@Value.Immutable
@Deprecated
public interface KeyPair {

  static ImmutableKeyPair.Builder builder() {
    return ImmutableKeyPair.builder();
  }

  /**
   * The private key of this {@link KeyPair}.
   *
   * @return A {@link String} containing the private key.
   */
  String privateKey();

  /**
   * The public key of this {@link KeyPair}.
   *
   * @return A {@link String} containing the public key.
   */
  String publicKey();

}
