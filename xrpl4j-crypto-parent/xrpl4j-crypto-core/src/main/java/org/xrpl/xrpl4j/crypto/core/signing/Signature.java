package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Represents a digital signature for a transaction that can be submitted to the XRP Ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignature.class)
@JsonDeserialize(as = ImmutableSignature.class)
public interface Signature {

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableSignature.Builder}.
   */
  static ImmutableSignature.Builder builder() {
    return ImmutableSignature.builder();
  }

  /**
   * The bytes of this signature.
   *
   * @return A {@link UnsignedByteArray}.
   */
  @JsonSerialize(using = UnsignedByteArraySerializer.class)
  @JsonDeserialize  (using = UnsignedByteArrayDeserializer.class)
  UnsignedByteArray value();

  /**
   * Accessor for this signature as a base16-encoded (HEX) string.
   *
   * @return A {@link String}.
   */
  @Lazy
  @JsonIgnore
  default String base16Value() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }

  /**
   * Accessor for this signature as a base16-encoded (HEX) string.
   *
   * @return A {@link String}.
   */
  @Lazy
  @JsonIgnore
  default String hexValue() {
    return base16Value();
  }
}
