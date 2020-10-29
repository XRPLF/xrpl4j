package com.ripple.xrplj4.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.Payment;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableSubmitAccountSetResponse.class)
@JsonDeserialize(as = ImmutableSubmitAccountSetResponse.class)
public interface SubmitAccountSetResponse {

  static ImmutableSubmitAccountSetResponse.Builder builder() {
    return ImmutableSubmitAccountSetResponse.builder();
  }

  boolean accepted();

  boolean applied();

  @JsonProperty("engine_result")
  String engineResult();

  @JsonProperty("tx_blob")
  String txBlob();

  // TODO: This isn't always a payment. E.g., AccountSet.
  @JsonProperty("tx_json")
  AccountSet txJson();


}
