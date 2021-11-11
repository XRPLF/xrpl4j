package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;

/**
 * Request params for "nft_buy_offers" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNftBuyOffersRequestParams.class)
@JsonDeserialize(as = ImmutableNftBuyOffersRequestParams.class)
public interface NftBuyOffersRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNftBuyOffersRequestParams.Builder}.
   */
  static ImmutableNftBuyOffersRequestParams.Builder builder() {
    return ImmutableNftBuyOffersRequestParams.builder();
  }

  /**
   *  the TokenID of the NFToken object
   */
  @JsonProperty("TokenID")
  String tokenId();
}
