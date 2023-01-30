package org.xrpl.xrpl4j.model;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

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
