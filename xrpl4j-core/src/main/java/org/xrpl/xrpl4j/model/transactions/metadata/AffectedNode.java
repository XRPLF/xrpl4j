package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.xrpl.xrpl4j.model.transactions.Hash256;

/**
 * Top level interface for all types of transaction metadata
 * <a href="https://xrpl.org/transaction-metadata.html#affectednodes">AffectedNodes</a>.
 */
public interface AffectedNode {

  /**
   * The type of ledger entry this {@link AffectedNode} affected.
   *
   * @return A {@link MetaLedgerEntryType}.
   */
  @JsonProperty("LedgerEntryType")
  MetaLedgerEntryType ledgerEntryType();

  /**
   * The unique ID of the ledger object that we affected.
   *
   * @return A {@link Hash256} containing the ledger entry's ID.
   */
  @JsonProperty("LedgerIndex")
  Hash256 ledgerIndex();

}
