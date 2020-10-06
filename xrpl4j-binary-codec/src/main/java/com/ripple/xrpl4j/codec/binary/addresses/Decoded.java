package com.ripple.xrpl4j.codec.binary.addresses;

import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Represents a decoded Base58 {@link String}.
 */
@Value.Immutable
public interface Decoded {

  static ImmutableDecoded.Builder builder() {
    return ImmutableDecoded.builder();
  }

  Version version();

  UnsignedByteArray bytes();

  Optional<VersionType> type();

}
