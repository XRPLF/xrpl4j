package org.xrpl.xrpl4j.crypto.core.keys;

import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Objects;

/**
 * A typed instance of an XRPL Base58 Encoded Secret, which can be used to generate {@link Seed}.
 */
@Value.Immutable
public interface Base58EncodedSecret {

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableBase58EncodedSecret.Builder}.
   */
  static ImmutableBase58EncodedSecret.Builder builder() {
    return ImmutableBase58EncodedSecret.builder();
  }

  /**
   * Construct a {@link Base58EncodedSecret} from a base58-encoded {@link String}.
   *
   * @param base58EncodedSecret A base58-encoded {@link String}.
   *
   * @return A {@link Base58EncodedSecret}.
   */
  static Base58EncodedSecret of(final String base58EncodedSecret) {
    Objects.requireNonNull(base58EncodedSecret);
    return Base58EncodedSecret.builder()
      .value(base58EncodedSecret)
      .build();
  }

  /**
   * The value of Base58 Encoded Secret.
   *
   * @return An instance of {@link String}.
   */
  String value();

  /**
   * The decoded value of Base58 Encoded Secret.
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  @Value.Derived
  default UnsignedByteArray decodedValueBytes() {
    return UnsignedByteArray.of(Base58.decode(value()));
  }

}
