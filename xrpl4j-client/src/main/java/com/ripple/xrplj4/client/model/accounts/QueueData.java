package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableQueueData.class)
@JsonDeserialize(as = ImmutableQueueData.class)
public interface QueueData {

  static ImmutableQueueData.Builder builder() {
    return ImmutableQueueData.builder();
  }

  @JsonProperty("txn_count")
  UnsignedInteger transactionCount();

  @JsonProperty("auth_change_queued")
  @Value.Default
  default boolean authChangeQueued() {
    return false;
  }

  @JsonProperty("lowest_sequence")
  Optional<UnsignedInteger> lowestSequence();

  @JsonProperty("highest_sequence")
  Optional<UnsignedInteger> highestSequence();

  @JsonProperty("max_spend_drops_total")
  Optional<String> maxSpendDropsTotal();

  List<QueueTransaction> transactions();
}
