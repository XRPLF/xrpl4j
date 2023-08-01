package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

/**
 * Request parameters for the {@code nft_info} RPC request. This request is only supported on Clio servers.
 */
@Immutable
@JsonSerialize(as = ImmutableNftInfoRequestParams.class)
@JsonDeserialize(as = ImmutableNftInfoRequestParams.class)
public interface NftInfoRequestParams extends XrplRequestParams {

  /**
   * Construct a {@code NftInfoRequestParams} builder.
   *
   * @return An {@link ImmutableNftInfoRequestParams.Builder}.
   */
  static ImmutableNftInfoRequestParams.Builder builder() {
    return ImmutableNftInfoRequestParams.builder();
  }

  @JsonProperty("nft_id")
  NfTokenId nfTokenId();

  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

}
