package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;

/**
 * Request params for "nft_sell_offers" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNftSellOffersRequestParams.class)
@JsonDeserialize(as = ImmutableNftSellOffersRequestParams.class)
public interface NftSellOffersRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNftSellOffersRequestParams.Builder}.
   */
  static ImmutableNftSellOffersRequestParams.Builder builder() {
    return ImmutableNftSellOffersRequestParams.builder();
  }

  /**
   * The TokenID of the NFToken object.
   *
   * @return the TokenID of the {@link org.xrpl.xrpl4j.model.client.accounts.NfTokenObject} object.
   */
  @JsonProperty("TokenID")
  String tokenId();
}
