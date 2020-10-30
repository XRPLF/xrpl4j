package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableQueueTransaction.class)
@JsonDeserialize(as = ImmutableQueueTransaction.class)
public interface QueueTransaction {

  static ImmutableQueueTransaction.Builder builder() {
    return ImmutableQueueTransaction.builder();
  }

  @JsonProperty("auth_change")
  boolean authChange();

  String fee();

  @JsonProperty("fee_level")
  String feeLevel();

  @JsonProperty("max_spend_drops")
  String maxSpendDrops();

  UnsignedInteger seq();

}
