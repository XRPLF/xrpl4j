package org.xrpl.xrpl4j.client.faucet.hooks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

@Value.Immutable
@JsonSerialize(as = ImmutableHooksFaucetAccountResponse.class)
@JsonDeserialize(as = ImmutableHooksFaucetAccountResponse.class)
public interface HooksFaucetAccountResponse {

  static ImmutableHooksFaucetAccountResponse.Builder builder() {
    return ImmutableHooksFaucetAccountResponse.builder();
  }

  Address address();

  String secret();

  @JsonProperty("xrp")
  UnsignedInteger balance();

  Hash256 hash();

  String code();

}
