package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public interface AffectedNode {

  @JsonProperty("LedgerEntryType")
  MetaLedgerEntryType ledgerEntryType();

  @JsonProperty("LedgerIndex")
  Hash256 ledgerIndex();

}
