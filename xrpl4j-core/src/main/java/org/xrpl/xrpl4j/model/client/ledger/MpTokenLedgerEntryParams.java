package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Parameters that uniquely identify an {@link org.xrpl.xrpl4j.model.ledger.MpTokenObject} on ledger that can be used in
 * a {@link LedgerEntryRequestParams} to request an {@link org.xrpl.xrpl4j.model.ledger.MpTokenObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableMpTokenLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableMpTokenLedgerEntryParams.class)
public interface MpTokenLedgerEntryParams {

  /**
   * Construct a {@code MpTokenLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableMpTokenLedgerEntryParams.Builder}.
   */
  static ImmutableMpTokenLedgerEntryParams.Builder builder() {
    return ImmutableMpTokenLedgerEntryParams.builder();
  }

  /**
   * The {@link MpTokenIssuanceId} of the issuance.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * The account that owns the {@link org.xrpl.xrpl4j.model.ledger.MpTokenObject}.
   *
   * @return An {@link Address}.
   */
  Address account();

}
