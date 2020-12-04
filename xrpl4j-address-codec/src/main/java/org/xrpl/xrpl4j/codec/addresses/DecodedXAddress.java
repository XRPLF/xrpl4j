package org.xrpl.xrpl4j.codec.addresses;

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

/**
 * An X-Address, decoded into an AccountID, destination tag, and a boolean for testnet or mainnet.
 * Note that the AccountID in this decoded X-Address is not Base58 encoded.
 */
@Value.Immutable
public interface DecodedXAddress {

  static ImmutableDecodedXAddress.Builder builder() {
    return ImmutableDecodedXAddress.builder();
  }

  UnsignedByteArray accountId();

  UnsignedInteger tag();

  boolean test();

}
