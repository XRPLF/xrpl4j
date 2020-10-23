package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCheckCash.class)
@JsonDeserialize(as = ImmutableCheckCash.class)
public interface CheckCash extends Transaction {

  static ImmutableCheckCash.Builder builder() {
    return ImmutableCheckCash.builder();
  }
}
