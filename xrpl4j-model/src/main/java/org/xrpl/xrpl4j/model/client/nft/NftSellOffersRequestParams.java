package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

import java.util.Optional;

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
  NfTokenId tokenId();

  /**
   * Limit the number of sell offers for the {@link NfTokenId}. The server is not required to honor
   * this value. Must be within the inclusive range 10 to 400.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the response limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link String} containing the marker.
   */
  Optional<Marker> marker();
}
