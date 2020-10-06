package com.ripple.xrpl4j.keypairs;

import org.immutables.value.Value;

@Value.Immutable
public interface KeyPair {

  static ImmutableKeyPair.Builder builder() {
    return ImmutableKeyPair.builder();
  }

  String privateKey();

  String publicKey();

}
