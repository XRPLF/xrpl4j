package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.rippled.XrplResult;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;

@Value.Immutable
@JsonSerialize(as = ImmutableLedgerResult.class)
@JsonDeserialize(as = ImmutableLedgerResult.class)
public interface LedgerResult extends XrplResult {

  LedgerHeader ledger();

  @JsonProperty("ledger_hash")
  String ledgerHash();

  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  // TODO: Add queue data if people need it.

}
