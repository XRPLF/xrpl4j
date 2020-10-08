package com.ripple.xrplj4.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.transactions.Payment;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableSubmitTransactionResponse.class)
@JsonDeserialize(as = ImmutableSubmitTransactionResponse.class)
public interface SubmitTransactionResponse {

  static ImmutableSubmitTransactionResponse.Builder builder() {
    return ImmutableSubmitTransactionResponse.builder();
  }

  boolean accepted();

  boolean applied();

  @JsonProperty("engine_result")
  String engineResult();

  @JsonProperty("tx_blob")
  String txBlob();

  @JsonProperty("tx_json")
  Payment txJson();


}
