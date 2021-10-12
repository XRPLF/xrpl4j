package org.xrpl.xrpl4j.crypto.signing;

import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Represents a digital signature for a transaction that can be submitted to the XRP Ledger.
 *
 * @deprecated Prefer the variant found in {@link org.xrpl.xrpl4j.crypto.core} instead.
 */
@Deprecated
@Value.Immutable
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
  UnsignedByteArray value();

  /**
   * Accessor for this signature as a base16-encoded (HEX) string.
   *
   * @return A {@link String}.
   */
  @Derived
  default String base16Value() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}
