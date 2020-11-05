package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrplj4.client.model.ledger.LedgerObjectDeserializer;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrplj4.client.model.JsonRpcResult;
import com.ripple.xrplj4.client.model.ledger.LedgerObject;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountObjectsResult.class)
@JsonDeserialize(as = ImmutableAccountObjectsResult.class)
public interface AccountObjectsResult extends JsonRpcResult {

  static ImmutableAccountObjectsResult.Builder builder() {
    return ImmutableAccountObjectsResult.builder();
  }

  Address account();

  @JsonProperty("account_objects")
  List<LedgerObject> accountObjects();

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  @JsonSerialize(using = LedgerIndexSerializer.class)
  @JsonProperty("ledger_index")
  Optional<String> ledgerIndex();

  @JsonSerialize(using = LedgerIndexSerializer.class)
  @JsonProperty("ledger_current_index")
  Optional<String> ledgerCurrentIndex();

  Optional<UnsignedInteger> limit();

  Optional<String> marker();

  @Value.Default
  default boolean validated() {
    return false;
  }

}
