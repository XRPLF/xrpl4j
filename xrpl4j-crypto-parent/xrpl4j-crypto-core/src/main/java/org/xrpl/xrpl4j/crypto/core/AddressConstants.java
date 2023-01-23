package org.xrpl.xrpl4j.crypto.core;

import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Constants defining well-known XRPL addresses.
 */
public interface AddressConstants {

  /**
   * An address that is the XRP Ledger's base58 encoding of the value 0. In peer-to-peer communications, rippled uses
   * this address as the issuer for XRP. This is a Black hole account.
   *
   * @see "https://xrpl.org/accounts.html#special-addresses"
   */
  Address ACCOUNT_ZERO = Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp");

  /**
   * An address that is the XRP Ledger's base58 encoding of the value 1. In the ledger, RippleState entries use this
   * address as a placeholder for the issuer of a trust line balance. This is a Black hole account.
   *
   * @see "https://xrpl.org/accounts.html#special-addresses"
   */
  Address ACCOUNT_ONE = Address.of("rrrrrrrrrrrrrrrrrrrrBZbvji");

  /**
   * When rippled starts a new genesis ledger from scratch (for example, in stand-alone mode), this account holds all
   * the XRP. This address is generated from the seed value masterpassphrase which is hard-coded.
   *
   * @see "https://xrpl.org/accounts.html#special-addresses"
   */
  Address GENESIS_ACCOUNT = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

  /**
   * In the past, Ripple asked users to send XRP to this account to reserve Ripple Names. This is a Black hole account.
   *
   * @see "https://xrpl.org/accounts.html#special-addresses"
   */
  Address NAME_RESERVATION_BLACKHOLE = Address.of("rrrrrrrrrrrrrrrrrNAMEtxvNvQ");

  /**
   * Previous versions of ripple-lib  generated this address when encoding the value NaN using the XRP Ledger's base58
   * string encoding format. This is a Black hole account.
   *
   * @see "https://xrpl.org/accounts.html#special-addresses"
   */
  Address NAN_ADDRESS = Address.of("rrrrrrrrrrrrrrrrrrrn5RM1rHd");
}
