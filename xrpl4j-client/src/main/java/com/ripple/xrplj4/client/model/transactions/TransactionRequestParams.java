package com.ripple.xrplj4.client.model.transactions;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrplj4.client.rippled.JsonRpcRequestParams;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableTransactionRequestParams.class)
@JsonDeserialize(as = ImmutableTransactionRequestParams.class)
public interface TransactionRequestParams extends JsonRpcRequestParams {

  static ImmutableTransactionRequestParams.Builder builder() {
    return ImmutableTransactionRequestParams.builder();
  }

  static TransactionRequestParams of(String transactionHash) {
    return builder().transaction(transactionHash).build();
  }

  String transaction();

  @Value.Default
  default boolean binary() {
    return false;
  }

  Optional<UnsignedInteger> minLedger();

  Optional<UnsignedInteger> maxLedger();

}
