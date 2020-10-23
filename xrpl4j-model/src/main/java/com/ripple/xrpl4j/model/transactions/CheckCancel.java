package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCheckCancel.class)
@JsonDeserialize(as = ImmutableCheckCancel.class)
public interface CheckCancel extends Transaction {

  static ImmutableCheckCancel.Builder builder() {
    return ImmutableCheckCancel.builder();
  }

  @Override
  @JsonProperty("TransactionType")
  @Value.Derived
  default TransactionType transactionType() {
    return TransactionType.CHECK_CANCEL;
  }

  @JsonProperty("CheckID")
  Hash256 checkId();
}
