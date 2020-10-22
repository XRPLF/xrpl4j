package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * There are several options which can be either enabled or disabled for an account.
 * Account options are represented by different types of flags depending on the situation.
 * <ul>
 *   <li>
 *     The AccountSet transaction type has several "AccountSet Flags" (prefixed asf) that can enable an option when
 *     passed as the SetFlag parameter, or disable an option when passed as the ClearFlag parameter.
 *   </li>
 *   <li>
 *     The AccountSet transaction type has several transaction flags (prefixed tf) that can be used to enable or
 *     disable specific account options when passed in the Flags parameter. This style is discouraged.
 *     New account options do not have corresponding transaction (tf) flags.
 *   </li>
 *   <li>
 *     The AccountRoot ledger object type has several ledger-state-flags (prefixed lsf) which represent the state of
 *     particular account options within a particular ledger. These settings apply until a transaction changes them.
 *   </li>
 * </ul>
 */
public enum AccountSetFlag {

  /**
   * Require a destination tag to send transactions to this account.
   */
  REQUIRE_DEST(1),
  /**
   * Require authorization for users to hold balances issued by this address.
   * Can only be enabled if the address has no trust lines connected to it.
   */
  REQUIRE_AUTH(2),
  /**
   * XRP should not be sent to this account. (Enforced by client applications, not by rippled).
   */
  DISALLOW_XRP(3),
  /**
   * Disallow use of the master key pair. Can only be enabled if the account has configured another way to
   * sign transactions, such as a Regular Key or a Signer List.
   */
  DISABLE_MASTER(4),
  /**
   * Track the ID of this account's most recent transaction. Required for {@link Transaction#accountTransactionId()}.
   */
  ACCOUNT_TXN_ID(5),
  /**
   * Permanently give up the ability to freeze individual trust lines or disable Global Freeze.
   * This flag can never be disabled after being enabled.
   *
   * @see "https://xrpl.org/freezes.html"
   */
  NO_FREEZE(6),
  /**
   * Freeze all assets issued by this account.
   * @see "https://xrpl.org/freezes.html"
   */
  GLOBAL_FREEZE(7),
  /**
   * Enable rippling on this account's trust lines by default.
   *
   * @see "https://xrpl.org/rippling.html"
   */
  DEFAULT_RIPPLE(8),
  /**
   * Enable Deposit Authorization on this account.
   *
   * @see "https://xrpl.org/depositauth.html"
   */
  DEPOSIT_AUTH(9);

  int value;

  AccountSetFlag(int value) {
    this.value = value;
  }

  /**
   * To deserialize enums with integer values, you need to specify this factory method with the {@link JsonCreator}
   * annotation, otherwise Jackson treats the JSON integer value as an ordinal.
   *
   * @see "https://github.com/FasterXML/jackson-databind/issues/1850"
   */
  @JsonCreator
  public static AccountSetFlag forValue(int value) {
    for (AccountSetFlag flag : values()) {
      if (flag.value == value) {
        return flag;
      }
    }

    throw new IllegalArgumentException("No matching AccountSetFlag enum value for int value " + value);
  }

  @JsonValue
  public int getValue() {
    return value;
  }
}
