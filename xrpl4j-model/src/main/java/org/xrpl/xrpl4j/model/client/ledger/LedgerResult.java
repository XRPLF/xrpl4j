package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;
import org.xrpl.xrpl4j.model.transactions.Hash256;

@Value.Immutable
@JsonSerialize(as = ImmutableLedgerResult.class)
@JsonDeserialize(as = ImmutableLedgerResult.class)
public interface LedgerResult extends XrplResult {

  static ImmutableLedgerResult.Builder builder() {
    return ImmutableLedgerResult.builder();
  }

  LedgerHeader ledger();

  @JsonProperty("ledger_hash")
  Hash256 ledgerHash();

  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  @Value.Default
  default boolean validated() {
    return false;
  }

  // TODO: Add queue data if people need it.

}
