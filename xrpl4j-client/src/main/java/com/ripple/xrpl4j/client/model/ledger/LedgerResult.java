package com.ripple.xrpl4j.client.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.client.model.JsonRpcResult;
import com.ripple.xrpl4j.client.model.ledger.objects.LedgerHeader;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLedgerResult.class)
@JsonDeserialize(as = ImmutableLedgerResult.class)
public interface LedgerResult extends JsonRpcResult {

  LedgerHeader ledger();

  @JsonProperty("ledger_hash")
  String ledgerHash();

  @JsonProperty("ledger_index")
  UnsignedInteger ledgerIndex();

  // TODO: Add queue data if people need it.

}
