package com.ripple.xrpl4j.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.client.model.JsonRpcResult;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountLinesResult.class)
@JsonDeserialize(as = ImmutableAccountLinesResult.class)
public interface AccountLinesResult extends JsonRpcResult {

  Address account();

  List<TrustLine> lines();

  @JsonProperty("ledger_current_index")
  Optional<UnsignedInteger> ledgerCurrentIndex();

  @JsonProperty("ledger_index")
  Optional<UnsignedInteger> ledgerIndex();

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  Optional<String> marker();

}
