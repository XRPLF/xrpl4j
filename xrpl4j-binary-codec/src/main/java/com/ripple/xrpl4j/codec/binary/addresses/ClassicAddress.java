package com.ripple.xrpl4j.codec.binary.addresses;

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

@Value.Immutable
public interface ClassicAddress {

  static ImmutableClassicAddress.Builder builder() {
    return ImmutableClassicAddress.builder();
  }

  String classicAddress();

  UnsignedInteger tag();

  boolean test();
}
