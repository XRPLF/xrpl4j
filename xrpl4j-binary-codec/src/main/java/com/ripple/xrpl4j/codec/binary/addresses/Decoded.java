package com.ripple.xrpl4j.codec.binary.addresses;

import com.ripple.xrpl4j.codec.binary.UnsignedByte;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface Decoded {

  static ImmutableDecoded.Builder builder() {
    return ImmutableDecoded.builder();
  }

  Version version();

  UnsignedByteArray bytes();

  Optional<VersionType> type();

}
