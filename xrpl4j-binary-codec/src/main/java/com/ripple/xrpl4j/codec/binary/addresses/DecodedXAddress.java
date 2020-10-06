package com.ripple.xrpl4j.codec.binary.addresses;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;
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
