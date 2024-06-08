package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;

/**
 * Parameters specifying an {@link org.xrpl.xrpl4j.model.ledger.OracleObject} to retrieve via a {@code ledger_entry} RPC
 * call.
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableOracleLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableOracleLedgerEntryParams.class)
public interface OracleLedgerEntryParams {

  /**
   * Construct a {@code OracleLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableOracleLedgerEntryParams.Builder}.
   */
  static ImmutableOracleLedgerEntryParams.Builder builder() {
    return ImmutableOracleLedgerEntryParams.builder();
  }

  Address account();

  @JsonProperty("oracle_document_id")
  OracleDocumentId oracleDocumentId();

}
