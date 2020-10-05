package com.ripple.xrpl4j.codec.binary.addresses;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;
import org.immutables.value.Value;

@Value.Immutable
public interface DecodedXAddress {

  static ImmutableDecodedXAddress.Builder builder() {
    return ImmutableDecodedXAddress.builder();
  }

  UnsignedByteArray accountId();

  UnsignedInteger tag();

  boolean test();

}
