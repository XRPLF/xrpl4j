package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

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
   * Unique identifier of a flag to disable for this account.
   *
   * Because the preferred way of setting account flags is with {@link AccountSetFlag}s, this field should not be
   * set in conjunction with the {@link AccountSet#flags()} field.
   */
  @JsonProperty("ClearFlag")
  Optional<AccountSetFlag> clearFlag();

  /**
   * The hex string of the lowercase ASCII of the domain for the account.
   * For example, the domain example.com would be represented as "6578616D706C652E636F6D".
   *
   * To remove the Domain field from an account, send an {@link AccountSet} with the {@link AccountSet#domain()} set
   * to an empty string.
   */
  @JsonProperty("Domain")
  Optional<String> domain();

  /**
   * Hash of an email address to be used for generating an avatar image. Conventionally,
   * clients use <a href="http://en.gravatar.com/site/implement/hash/">Gravatar</a>
   * to display this image.
   */
  @JsonProperty("EmailHash")
  Optional<String> emailHash();

  /**
   * Hexadecimal encoded public key for sending encrypted messages to this account.
   */
  @JsonProperty("MessageKey")
  Optional<String> messageKey();

  /**
   * Unique identifier of a flag to enable for this account.
   *
   * Because the preferred way of setting account flags is with {@link AccountSetFlag}s, this field should not be
   * set in conjunction with the {@link AccountSet#flags()} field.
   */
  @JsonProperty("SetFlag")
  Optional<AccountSetFlag> setFlag();

  /**
   * The fee to charge when users transfer this account's issued currencies, represented as billionths of a unit.
   * Cannot be more than 2000000000 or less than 1000000000, except for the special case 0 meaning no fee.
   */
  @JsonProperty("TransferRate")
  Optional<UnsignedInteger> transferRate();

  /**
   * Tick size to use for offers involving a currency issued by this address.
   * The exchange rates of those offers is rounded to this many significant digits.
   * Valid values are 3 to 15 inclusive, or 0 to disable.
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
          (tickSize.compareTo(UnsignedInteger.valueOf(3)) >= 0 && tickSize.compareTo(UnsignedInteger.valueOf(15)) <= 0),
          "tickSize must be between 3 and 15 inclusive or be equal to 0.")
      );
  }
}
