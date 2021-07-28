package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * The result of a "ledger" rippled API request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerResult.class)
@JsonDeserialize(as = ImmutableLedgerResult.class)
public interface LedgerResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableLedgerResult.Builder}.
   */
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
  Optional<Hash256> ledgerHash();

  /**
   * The {@link LedgerIndex} of this ledger.
   *
   * @return The {@link LedgerIndex} of this ledger.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * The {@link LedgerIndex} of this ledger, if the ledger is the current ledger. Only present on a current ledger
   * response.
   *
   * @return A {@link LedgerIndex} if this result is for the current ledger, otherwise {@link Optional#empty()}.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

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
