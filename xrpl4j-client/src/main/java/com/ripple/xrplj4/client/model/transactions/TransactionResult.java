package com.ripple.xrplj4.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Transaction;
import com.ripple.xrplj4.client.model.JsonRpcResult;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTransactionResult.class)
@JsonDeserialize(as = ImmutableTransactionResult.class)
public interface TransactionResult<TxnType extends Transaction<? extends Flags>> extends JsonRpcResult {

  static ImmutableTransactionResult.Builder builder() {
    return ImmutableTransactionResult.builder();
  }

  @JsonUnwrapped
  TxnType transaction();

  String hash();

  @JsonProperty("ledger_index")
  UnsignedInteger ledgerIndex();

  @Value.Default
  default boolean validated() {
    return false;
  }

  // TODO: Add tx metadata if people need it
}
