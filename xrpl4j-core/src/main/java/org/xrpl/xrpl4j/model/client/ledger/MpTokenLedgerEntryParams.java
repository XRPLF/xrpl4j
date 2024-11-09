package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

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

  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mpTokenIssuanceId();

  Address account();

}
