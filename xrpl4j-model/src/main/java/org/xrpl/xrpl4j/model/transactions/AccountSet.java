package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.flags.Flags.TransactionFlags;

import java.util.Optional;

/**
 * An {@link AccountSet} transaction modifies the properties of an account in the XRP Ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountSet.class)
@JsonDeserialize(as = ImmutableAccountSet.class)
public interface AccountSet extends Transaction {

  static ImmutableAccountSet.Builder builder() {
    return ImmutableAccountSet.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link AccountDelete}, which only allows tfFullyCanonicalSig flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   */
  @JsonProperty("Flags")
  @Derived
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * Unique identifier of a flag to disable for this account.
   *
   * <p>Because the preferred way of setting account flags is with {@link AccountSetFlag}s, this field should
   * not be set in conjunction with the {@link AccountSet#flags()} field.
   */
  @JsonProperty("ClearFlag")
  Optional<AccountSetFlag> clearFlag();

  /**
   * Unique identifier of a flag to enable for this account.
   *
   * <p>Because the preferred way of setting account flags is with {@link AccountSetFlag}s, this field should not be set
   * in conjunction with the {@link AccountSet#flags()} field.
   */
  @JsonProperty("SetFlag")
  Optional<AccountSetFlag> setFlag();

  /**
   * The hex string of the lowercase ASCII of the domain for the account. For example, the domain example.com would be
   * represented as "6578616D706C652E636F6D".
   *
   * <p>To remove the Domain field from an account, send an {@link AccountSet} with the {@link AccountSet#domain()}
   * set to an empty string.
   */
  @JsonProperty("Domain")
  Optional<String> domain();

  /**
   * Hash of an email address to be used for generating an avatar image. Conventionally, clients use <a
   * href="http://en.gravatar.com/site/implement/hash/">Gravatar</a> to display this image.
   */
  @JsonProperty("EmailHash")
  Optional<String> emailHash();

  /**
   * Hexadecimal encoded public key for sending encrypted messages to this account.
   */
  @JsonProperty("MessageKey")
  Optional<String> messageKey();

  /**
   * The fee to charge when users transfer this account's issued currencies, represented as billionths of a unit. Cannot
   * be more than 2000000000 or less than 1000000000, except for the special case 0 meaning no fee.
   */
  @JsonProperty("TransferRate")
  Optional<UnsignedInteger> transferRate();

  /**
   * Tick size to use for offers involving a currency issued by this address. The exchange rates of those offers is
   * rounded to this many significant digits. Valid values are 3 to 15 inclusive, or 0 to disable.
   */
  @JsonProperty("TickSize")
  Optional<UnsignedInteger> tickSize();

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

  @Value.Check
  default void checkTransferRate() {
    transferRate()
        .ifPresent(rate ->
            Preconditions.checkArgument(rate.equals(UnsignedInteger.ZERO) ||
                    (rate.compareTo(UnsignedInteger.valueOf(1000000000L)) >= 0 &&
                        rate.compareTo(UnsignedInteger.valueOf(2000000000L)) <= 0),
                "transferRate must be between 1,000,000,000 and 2,000,000,000 or equal to 0.")
        );
  }

  @Value.Check
  default void checkTickSize() {
    tickSize()
        .ifPresent(tickSize ->
            Preconditions.checkArgument(tickSize.equals(UnsignedInteger.ZERO) ||
                    (tickSize.compareTo(UnsignedInteger.valueOf(3)) >= 0 &&
                        tickSize.compareTo(UnsignedInteger.valueOf(15)) <= 0),
                "tickSize must be between 3 and 15 inclusive or be equal to 0.")
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
      for (AccountSetFlag flag : AccountSetFlag.values()) {
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
}
