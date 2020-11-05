package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrplj4.client.rippled.JsonRpcRequestParams;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountObjectsRequestParams.class)
@JsonDeserialize(as = ImmutableAccountObjectsRequestParams.class)
public interface AccountObjectsRequestParams extends JsonRpcRequestParams {

  static ImmutableAccountObjectsRequestParams.Builder builder() {
    return ImmutableAccountObjectsRequestParams.builder();
  }

  Address account();

  Optional<String> type();

  @JsonProperty("deletion_blockers_only")
  @Value.Default
  default boolean deletionBlockersOnly() {
    return false;
  }

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  @JsonSerialize(using = LedgerIndexSerializer.class)
  @JsonProperty("ledger_index")
  @Value.Default
  default String ledgerIndex() {
    return "current";
  }

  Optional<UnsignedInteger> limit();

  Optional<String> marker();

}
