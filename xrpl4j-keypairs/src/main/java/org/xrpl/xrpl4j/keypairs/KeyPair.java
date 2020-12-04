package org.xrpl.xrpl4j.keypairs;

import org.immutables.value.Value;

/**
 * Represents an XRPL public/private key pair.
 */
@Value.Immutable
public interface KeyPair {

  static ImmutableKeyPair.Builder builder() {
    return ImmutableKeyPair.builder();
  }

  String privateKey();

  String publicKey();

}
