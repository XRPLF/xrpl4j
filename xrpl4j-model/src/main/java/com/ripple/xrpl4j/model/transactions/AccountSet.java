package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountSet.class)
@JsonDeserialize(as = ImmutableAccountSet.class)
public interface AccountSet extends Transaction {

  static ImmutableAccountSet.Builder builder() {
    return ImmutableAccountSet.builder();
  }

  @Override
  @JsonProperty("TransactionType")
  @Value.Default
  default TransactionType type() {
    return TransactionType.ACCOUNT_SET;
  };

  @JsonProperty("ClearFlag")
  Optional<AccountSetFlag> clearFlag();

  @JsonProperty("Domain")
  Optional<String> domain();

  @JsonProperty("EmailHash")
  Optional<String> emailHash();

  @JsonProperty("MessageKey")
  Optional<String> messageKey();

  @JsonProperty("SetFlag")
  Optional<AccountSetFlag> setFlag();

  @JsonProperty("TransferRate")
  Optional<UnsignedInteger> transferRate();

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
