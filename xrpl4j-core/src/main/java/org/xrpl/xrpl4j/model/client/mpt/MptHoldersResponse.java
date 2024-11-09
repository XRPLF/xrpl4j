package org.xrpl.xrpl4j.model.client.mpt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.List;
import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableMptHoldersResponse.class)
@JsonDeserialize(as = ImmutableMptHoldersResponse.class)
public interface MptHoldersResponse {

  /**
   * Construct a {@code MptHoldersResponse} builder.
   *
   * @return An {@link ImmutableMptHoldersResponse.Builder}.
   */
  static ImmutableMptHoldersResponse.Builder builder() {
    return ImmutableMptHoldersResponse.builder();
  }

  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mpTokenIssuanceId();

  @JsonProperty("mptokens")
  List<MptHoldersMpToken> mpTokens();

  Optional<Marker> marker();

  Optional<UnsignedInteger> limit();

  // FIXME: Is this always the field, or does clio also return `ledger_hash` and potentially `ledger_current_index`?
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();
}
