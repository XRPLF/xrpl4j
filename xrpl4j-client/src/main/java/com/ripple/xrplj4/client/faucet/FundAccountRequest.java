package com.ripple.xrplj4.client.faucet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableFundAccountRequest.class)
@JsonDeserialize(as = ImmutableFundAccountRequest.class)
public interface FundAccountRequest {

  static ImmutableFundAccountRequest.Builder builder() {
    return ImmutableFundAccountRequest.builder();
  }

  static FundAccountRequest of(String classicAddress) {
    return builder().destination(classicAddress).build();
  }

  String destination();

}
