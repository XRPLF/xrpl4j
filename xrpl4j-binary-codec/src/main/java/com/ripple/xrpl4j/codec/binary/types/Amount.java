package com.ripple.xrpl4j.codec.binary.types;

import org.immutables.value.Value.Immutable;

@Immutable
interface Amount {

  static ImmutableAmount.Builder builder() {
    return ImmutableAmount.builder();
  }

  String value();

  String currency();

  String issuer();

}
