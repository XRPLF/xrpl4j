package org.xrpl.xrpl4j.codec.addresses;

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

/**
 * An address on the XRP Ledger represented in Classic Address form.  This form includes a Base58Check encoded
 * address, as well as a destination tag and an indicator of if the address is on XRPL-testnet or XRPL-mainnet.
 *
 * @see "https://xrpl.org/accounts.html#addresses"
 */
@Value.Immutable
public interface ClassicAddress {

  static ImmutableClassicAddress.Builder builder() {
    return ImmutableClassicAddress.builder();
  }

  String classicAddress();

  UnsignedInteger tag();

  boolean test();
}
