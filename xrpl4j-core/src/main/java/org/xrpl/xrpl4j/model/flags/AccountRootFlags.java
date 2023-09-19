package org.xrpl.xrpl4j.model.flags;

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

import org.xrpl.xrpl4j.model.transactions.AccountSet;

/**
 * A set of static {@link Flags} which can be set on {@link AccountSet} transactions.
 */
public class AccountRootFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final AccountRootFlags UNSET = new AccountRootFlags(0);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDefaultRipple} account flag.
   */
  public static final AccountRootFlags DEFAULT_RIPPLE = new AccountRootFlags(0x00800000L);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDepositAuth} account flag.
   */
  public static final AccountRootFlags DEPOSIT_AUTH = new AccountRootFlags(0x01000000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDisableMaster} account flag.
   */
  public static final AccountRootFlags DISABLE_MASTER = new AccountRootFlags(0x00100000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDisallowXRP} account flag.
   */
  public static final AccountRootFlags DISALLOW_XRP = new AccountRootFlags(0x00080000L);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfGlobalFreeze} account flag.
   */
  public static final AccountRootFlags GLOBAL_FREEZE = new AccountRootFlags(0x00400000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfNoFreeze} account flag.
   */
  public static final AccountRootFlags NO_FREEZE = new AccountRootFlags(0x00200000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfPasswordSpent} account flag.
   */
  public static final AccountRootFlags PASSWORD_SPENT = new AccountRootFlags(0x00010000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfRequireAuth} account flag.
   */
  public static final AccountRootFlags REQUIRE_AUTH = new AccountRootFlags(0x00040000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfRequireDestTag} account flag.
   */
  public static final AccountRootFlags REQUIRE_DEST_TAG = new AccountRootFlags(0x00020000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDisallowIncomingNFTokenOffer} account flag.
   */
  public static final AccountRootFlags DISALLOW_INCOMING_NFT_OFFER = new AccountRootFlags(0x04000000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDisallowIncomingCheck} account flag.
   */
  public static final AccountRootFlags DISALLOW_INCOMING_CHECK = new AccountRootFlags(0x08000000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDisallowIncomingPayChan} account flag.
   */
  public static final AccountRootFlags DISALLOW_INCOMING_PAY_CHAN = new AccountRootFlags(0x10000000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfDisallowIncomingTrustline} account flag.
   */
  public static final AccountRootFlags DISALLOW_INCOMING_TRUSTLINE = new AccountRootFlags(0x20000000);

  /**
   * Constant {@link AccountRootFlags} for the {@code lsfAllowTrustLineClawback} account flag.
   */
  public static final AccountRootFlags ALLOW_TRUSTLINE_CLAWBACK = new AccountRootFlags(0x80000000L);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link AccountRootFlags}.
   */
  private AccountRootFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link AccountRootFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link AccountRootFlags}.
   * @return New {@link AccountRootFlags}.
   */
  public static AccountRootFlags of(long value) {
    return new AccountRootFlags(value);
  }

  /**
   * Enable rippling on this addresses's trust lines by default. Required for issuing addresses; discouraged for
   * others.
   *
   * @return {@code true} if {@code lsfDefaultRipple} is set, otherwise {@code false}.
   */
  public boolean lsfDefaultRipple() {
    return this.isSet(AccountRootFlags.DEFAULT_RIPPLE);
  }

  /**
   * This account can only receive funds from transactions it sends, and from preauthorized accounts. (It has
   * DepositAuth enabled.)
   *
   * @return {@code true} if {@code lsfDepositAuth} is set, otherwise {@code false}.
   */
  public boolean lsfDepositAuth() {
    return this.isSet(AccountRootFlags.DEPOSIT_AUTH);
  }

  /**
   * Disallows use of the master key to sign transactions for this account.
   *
   * @return {@code true} if {@code lsfDisableMaster} is set, otherwise {@code false}.
   */
  public boolean lsfDisableMaster() {
    return this.isSet(AccountRootFlags.DISABLE_MASTER);
  }

  /**
   * Client applications should not send XRP to this account. Not enforced by rippled.
   *
   * @return {@code true} if {@code lsfDisallowXrp} is set, otherwise {@code false}.
   */
  public boolean lsfDisallowXrp() {
    return this.isSet(AccountRootFlags.DISALLOW_XRP);
  }

  /**
   * All assets issued by this address are frozen.
   *
   * @return {@code true} if {@code lsfGlobalFreeze} is set, otherwise {@code false}.
   */
  public boolean lsfGlobalFreeze() {
    return this.isSet(AccountRootFlags.GLOBAL_FREEZE);
  }

  /**
   * This address cannot freeze trust lines connected to it. Once enabled, cannot be disabled.
   *
   * @return {@code true} if {@code lsfNoFreeze} is set, otherwise {@code false}.
   */
  public boolean lsfNoFreeze() {
    return this.isSet(AccountRootFlags.NO_FREEZE);
  }

  /**
   * The account has used its free SetRegularKey transaction.
   *
   * @return {@code true} if {@code lsfPasswordSpent} is set, otherwise {@code false}.
   */
  public boolean lsfPasswordSpent() {
    return this.isSet(AccountRootFlags.PASSWORD_SPENT);
  }

  /**
   * This account must individually approve other users for those users to hold this account's issued currencies.
   *
   * @return {@code true} if {@code lsfRequireAuth} is set, otherwise {@code false}.
   */
  public boolean lsfRequireAuth() {
    return this.isSet(AccountRootFlags.REQUIRE_AUTH);
  }

  /**
   * Requires incoming payments to specify a Destination Tag.
   *
   * @return {@code true} if {@code lsfRequireDestTag} is set, otherwise {@code false}.
   */
  public boolean lsfRequireDestTag() {
    return this.isSet(AccountRootFlags.REQUIRE_DEST_TAG);
  }

  /**
   * Blocks incoming NFToken Offers.
   *
   * @return {@code true} if {@code lsfDisallowIncomingNFTokenOffer} is set, otherwise {@code false}.
   */
  @SuppressWarnings("AbbreviationAsWordInName")
  public boolean lsfDisallowIncomingNFTokenOffer() {
    return this.isSet(AccountRootFlags.DISALLOW_INCOMING_NFT_OFFER);
  }

  /**
   * Blocks incoming Checks.
   *
   * @return {@code true} if {@code lsfDisallowIncomingCheck} is set, otherwise {@code false}.
   */
  public boolean lsfDisallowIncomingCheck() {
    return this.isSet(AccountRootFlags.DISALLOW_INCOMING_CHECK);
  }

  /**
   * Blocks incoming Payment Channels.
   *
   * @return {@code true} if {@code lsfDisallowIncomingPayChan} is set, otherwise {@code false}.
   */
  public boolean lsfDisallowIncomingPayChan() {
    return this.isSet(AccountRootFlags.DISALLOW_INCOMING_PAY_CHAN);
  }

  /**
   * Blocks incoming Trustlines.
   *
   * @return {@code true} if {@code lsfDisallowIncomingTrustline} is set, otherwise {@code false}.
   */
  public boolean lsfDisallowIncomingTrustline() {
    return this.isSet(AccountRootFlags.DISALLOW_INCOMING_TRUSTLINE);
  }

  /**
   * Allows trustline clawback on this account.
   *
   * @return {@code true} if {@code lsfAllowTrustLineClawback} is set, otherwise {@code false}.
   */
  public boolean lsfAllowTrustLineClawback() {
    return this.isSet(AccountRootFlags.ALLOW_TRUSTLINE_CLAWBACK);
  }
}
