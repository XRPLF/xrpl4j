package org.xrpl.xrpl4j.model.client.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

/**
 * Result from a "advance_ledger" rippled admin API call.
 */
@Immutable
@JsonSerialize(as = ImmutableAcceptLedgerResult.class)
@JsonDeserialize(as = ImmutableAcceptLedgerResult.class)
public interface AcceptLedgerResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAcceptLedgerResult.Builder}.
   */
  static ImmutableAcceptLedgerResult.Builder builder() {
    return ImmutableAcceptLedgerResult.builder();
  }

  /**
   * The Ledger Index of the current open ledger these stats describe.
   *
   * @return A {@link LedgerIndex} denoting the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  LedgerIndex ledgerCurrentIndex();

}
