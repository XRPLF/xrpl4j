package com.ripple.xrpl4j.keypairs;

import org.immutables.value.Value;

@Value.Immutable
public interface Wallet {

  static ImmutableWallet.Builder builder() {
    return ImmutableWallet.builder();
  }

  String privateKey();

  String publicKey();

  boolean test();

}
