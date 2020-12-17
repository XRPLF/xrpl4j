package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;
import org.xrpl.xrpl4j.model.transactions.Hash256;

/**
 * The result of a "ledger" rippled API request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerResult.class)
@JsonDeserialize(as = ImmutableLedgerResult.class)
public interface LedgerResult extends XrplResult {

  static ImmutableLedgerResult.Builder builder() {
    return ImmutableLedgerResult.builder();
  }

  /**
   * The complete header data of this ledger.
   *
   * @return A {@link LedgerHeader} containing the ledger data.
   */
  LedgerHeader ledger();

  /**
   * Unique identifying hash of the entire ledger.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Hash256 ledgerHash();

  /**
   * The {@link LedgerIndex} of this ledger.
   *
   * @return The {@link LedgerIndex} of this ledger.
   */
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  /**
   * True if this data is from a validated ledger version; if false, this data is not final.
   *
   * @return {@code true} if this data is from a validated ledger version, otherwise {@code false}.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  // TODO: Add queue data (https://github.com/XRPLF/xrpl4j/issues/17).

}
