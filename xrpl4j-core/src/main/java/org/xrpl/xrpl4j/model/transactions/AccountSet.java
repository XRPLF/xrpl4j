package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.util.Optional;

/**
 * An {@link AccountSet} transaction modifies the properties of an account in the XRP Ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountSet.class)
@JsonDeserialize(as = ImmutableAccountSet.class)
public interface AccountSet extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountSet.Builder}.
   */
  static ImmutableAccountSet.Builder builder() {
    return ImmutableAccountSet.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link AccountDelete}, which only allows the
   * {@code tfFullyCanonicalSig} flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link Flags.TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Default
  default Flags.AccountSetTransactionFlags flags() {
    return new Flags.AccountSetTransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * Unique identifier of a flag to disable for this account.
   *
   * <p>Because the preferred way of setting account flags is with {@link AccountSetFlag}s, this field should
   * not be set in conjunction with the {@link AccountSet#flags()} field.
   *
   * @return An {@link Optional} of type {@link AccountSetFlag} representing the flag to disable on this account.
   */
  @JsonProperty("ClearFlag")
  Optional<AccountSetFlag> clearFlag();

  /**
   * Unique identifier of a flag to enable for this account.
   *
   * <p>Because the preferred way of setting account flags is with {@link AccountSetFlag}s, this field should not be set
   * in conjunction with the {@link AccountSet#flags()} field.
   *
   * @return An {@link Optional} of type {@link AccountSetFlag} representing the flag to enable on this account.
   */
  @JsonProperty("SetFlag")
  Optional<AccountSetFlag> setFlag();

  /**
   * The hex string of the lowercase ASCII of the domain for the account. For example, the domain example.com would be
   * represented as "6578616D706C652E636F6D".
   *
   * <p>To remove the Domain field from an account, send an {@link AccountSet} with the {@link AccountSet#domain()}
   * set to an empty string.
   *
   * @return An {@link Optional} of type {@link String} containing the domain.
   */
  @JsonProperty("Domain")
  Optional<String> domain();

  /**
   * Hash of an email address to be used for generating an avatar image. Conventionally, clients use <a
   * href="http://en.gravatar.com/site/implement/hash/">Gravatar</a> to display this image.
   *
   * @return An {@link Optional} of type {@link String} containing the hash of the email.
   */
  @JsonProperty("EmailHash")
  Optional<String> emailHash();

  /**
   * Hexadecimal encoded public key for sending encrypted messages to this account.
   *
   * @return An {@link Optional} of type {@link String} containing the messaging public key.
   */
  @JsonProperty("MessageKey")
  Optional<String> messageKey();

  /**
   * The fee to charge when users transfer this account's issued currencies, represented as billionths of a unit. Cannot
   * be more than 2000000000 or less than 1000000000, except for the special case 0 meaning no fee.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the transfer rate.
   */
  @JsonProperty("TransferRate")
  Optional<UnsignedInteger> transferRate();

  /**
   * Tick size to use for offers involving a currency issued by this address. The exchange rates of those offers is
   * rounded to this many significant digits. Valid values are 3 to 15 inclusive, or 0 to disable.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the tick size.
   */
  @JsonProperty("TickSize")
  Optional<UnsignedInteger> tickSize();

  /**
   * Sets an alternate account that is allowed to mint NFTokens on this
   * account's behalf using NFTokenMint's `Issuer` field.
   *
   * @return An {@link Optional} field MintAccount of type {@link Address}.
   */
  @JsonProperty("MintAccount")
  Optional<Address> mintAccount();

  /**
   * Check email hash length.
   */
  @Value.Check
  default void checkEmailHashLength() {
    emailHash()
      .ifPresent(hash ->
        Preconditions.checkArgument(
          hash.length() == 32,
          String.format("emailHash must be 32 characters (128 bits), but was %s characters long.", hash.length())
        )
      );
  }

  /**
   * Check transfer rate.
   */
  @Value.Check
  default void checkTransferRate() {
    transferRate()
      .ifPresent(rate ->
        Preconditions.checkArgument(rate.equals(UnsignedInteger.ZERO) ||
            (rate.compareTo(UnsignedInteger.valueOf(1000000000L)) >= 0 &&
              rate.compareTo(UnsignedInteger.valueOf(2000000000L)) <= 0),
          "transferRate must be between 1,000,000,000 and 2,000,000,000 or equal to 0."
        )
      );
  }

  /**
   * Check tick size.
   */
  @Value.Check
  default void checkTickSize() {
    tickSize()
      .ifPresent(tickSize ->
        Preconditions.checkArgument(tickSize.equals(UnsignedInteger.ZERO) ||
            (tickSize.compareTo(UnsignedInteger.valueOf(3)) >= 0 &&
              tickSize.compareTo(UnsignedInteger.valueOf(15)) <= 0),
          "tickSize must be between 3 and 15 inclusive or be equal to 0."
        )
      );

  }

  /**
   * There are several options which can be either enabled or disabled for an account. Account options are represented
   * by different types of flags depending on the situation.
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
  enum AccountSetFlag {

    /**
     * Require a destination tag to send transactions to this account.
     */
    REQUIRE_DEST(1),
    /**
     * Require authorization for users to hold balances issued by this address. Can only be enabled if the address has
     * no trust lines connected to it.
     */
    REQUIRE_AUTH(2),
    /**
     * XRP should not be sent to this account. (Enforced by client applications, not by rippled).
     */
    DISALLOW_XRP(3),
    /**
     * Disallow use of the master key pair. Can only be enabled if the account has configured another way to sign
     * transactions, such as a Regular Key or a Signer List.
     */
    DISABLE_MASTER(4),
    /**
     * Track the ID of this account's most recent transaction. Required for {@link Transaction#accountTransactionId()}.
     */
    ACCOUNT_TXN_ID(5),
    /**
     * Permanently give up the ability to freeze individual trust lines or disable Global Freeze. This flag can never be
     * disabled after being enabled.
     *
     * @see "https://xrpl.org/freezes.html"
     */
    NO_FREEZE(6),
    /**
     * Freeze all assets issued by this account.
     *
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
    DEPOSIT_AUTH(9),
    /**
     * Allow another account to mint and burn tokens on behalf of this account.
     */
    AUTHORIZED_MINTER(10);

    int value;

    AccountSetFlag(int value) {
      this.value = value;
    }

    /**
     * To deserialize enums with integer values, you need to specify this factory method with the {@link JsonCreator}
     * annotation, otherwise Jackson treats the JSON integer value as an ordinal.
     *
     * @param value The int value of the flag.
     *
     * @return The {@link AccountSetFlag} for the given integer value.
     * @see "https://github.com/FasterXML/jackson-databind/issues/1850"
     */
    @JsonCreator
    public static AccountSetFlag forValue(int value) {
      for (AccountSetFlag flag : AccountSetFlag.values()) {
        if (flag.value == value) {
          return flag;
        }
      }

      throw new IllegalArgumentException("No matching AccountSetFlag enum value for int value " + value);
    }

    /**
     * Get the underlying value of this {@link AccountSetFlag}.
     *
     * @return The underlying {@code int} value of this {@link AccountSetFlag}.
     */
    @JsonValue
    public int getValue() {
      return value;
    }
  }
}
