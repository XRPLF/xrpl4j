package org.xrpl.xrpl4j.model.client.mpt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Optional;

/**
 * Request parameters for the {@code mpt_holders} RPC.
 */
@Immutable
@JsonSerialize(as = ImmutableMptHoldersRequestParams.class)
@JsonDeserialize(as = ImmutableMptHoldersRequestParams.class)
public interface MptHoldersRequestParams extends XrplRequestParams {

  /**
   * Construct a {@code MptHoldersRequestParams} builder.
   *
   * @return An {@link ImmutableMptHoldersRequestParams.Builder}.
   */
  static ImmutableMptHoldersRequestParams.Builder builder() {
    return ImmutableMptHoldersRequestParams.builder();
  }

  /**
   * The {@link MpTokenIssuanceId} of the issuance.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * A {@link LedgerSpecifier}.
   *
   * @return A {@link LedgerSpecifier}.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

  Optional<Marker> marker();

  Optional<UnsignedInteger> limit();

}
