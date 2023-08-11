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

  /**
   * A unique identifier for the non-fungible token (NFT).
   *
   * @return An {@link NfTokenId}.
   */
  @JsonProperty("nft_id")
  NfTokenId nfTokenId();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash, numerical ledger index,
   * or a shortcut value.
   *
   * <p>Because {@code nft_info} is only supported on Clio nodes, and because Clio does not have access to non-validated
   * ledgers, specifying a ledger that has not yet been validated, or specifying a ledger index shortcut other than
   * {@link LedgerSpecifier#VALIDATED} will result in Clio returning an error.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

}
