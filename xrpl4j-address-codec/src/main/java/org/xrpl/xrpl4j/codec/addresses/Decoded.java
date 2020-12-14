package org.xrpl.xrpl4j.codec.addresses;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * Represents a decoded Base58 {@link String}.
 */
@Value.Immutable
public interface Decoded {

  /**
   * Get a new {@link ImmutableDecoded.Builder} instance.
   *
   * @return A {@link ImmutableDecoded.Builder}.
   */
  static ImmutableDecoded.Builder builder() {
    return ImmutableDecoded.builder();
  }

  /**
   * The {@link Version} of the decoded Base58 {@link String}.
   *
   * @return A {@link Version}.
   */
  Version version();

  /**
   * The bytes of the decoded Base58 {@link String}.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray bytes();

  /**
   * The {@link VersionType} of the decoded Base58 {@link String}.
   *
   * @return An optionally present {@link VersionType}.
   */
  Optional<VersionType> type();

}
