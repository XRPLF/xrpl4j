package org.xrpl.xrpl4j.codec.addresses;

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

/**
 * An X-Address, decoded into an AccountID, destination tag, and a boolean for testnet or mainnet.
 * Note that the AccountID in this decoded X-Address is not Base58 encoded.
 */
@Value.Immutable
public interface DecodedXAddress {

  /**
   * Get a new {@link ImmutableDecodedXAddress.Builder} instance.
   *
   * @return A {@link ImmutableDecodedXAddress.Builder}.
   */
  static ImmutableDecodedXAddress.Builder builder() {
    return ImmutableDecodedXAddress.builder();
  }

  /**
   * The Account ID of the X-Address.
   *
   * @return An {@link UnsignedByteArray} containing the Account ID.
   */
  UnsignedByteArray accountId();

  /**
   * The tag of the X-Address.
   *
   * @return An {@link UnsignedInteger} representing the tag.
   */
  UnsignedInteger tag();

  /**
   * Whether or not this address exists on mainnet or testnet.
   *
   * @return {@code true} if it is a tesnet address, {@code false} if it is mainnet.
   */
  boolean test();

}
