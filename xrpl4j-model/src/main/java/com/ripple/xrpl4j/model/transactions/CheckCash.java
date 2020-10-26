package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableCheckCash.class)
@JsonDeserialize(as = ImmutableCheckCash.class)
public interface CheckCash extends Transaction {

  static ImmutableCheckCash.Builder builder() {
    return ImmutableCheckCash.builder();
  }

  @JsonProperty("CheckID")
  Hash256 checkId();

  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

  @JsonProperty("DeliverMin")
  Optional<CurrencyAmount> deliverMin();

  @Value.Check
  default void validateOnlyOneAmountSet() {
    Preconditions.checkArgument((amount().isPresent() || deliverMin().isPresent())
      && !(amount().isPresent() && deliverMin().isPresent()),
      "The CheckCash transaction must include either amount or deliverMin, but not both.");
  }
}
