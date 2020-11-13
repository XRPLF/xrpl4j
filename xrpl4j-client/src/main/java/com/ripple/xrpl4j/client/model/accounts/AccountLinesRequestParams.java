package com.ripple.xrpl4j.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.client.rippled.JsonRpcRequestParams;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountLinesRequestParams.class)
@JsonDeserialize(as = ImmutableAccountLinesRequestParams.class)
public interface AccountLinesRequestParams extends JsonRpcRequestParams {

  static ImmutableAccountLinesRequestParams.Builder builder() {
    return ImmutableAccountLinesRequestParams.builder();
  }

  Address account();

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  @JsonProperty("ledger_index")
  @JsonSerialize(using = LedgerIndexSerializer.class)
  @Value.Default
  default String ledgerIndex() {
    return "current";
  }

  Optional<Address> peer();

  Optional<UnsignedInteger> limit();

  Optional<String> marker();

}
