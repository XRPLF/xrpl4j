package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

import java.util.List;

/**
 * The result of an "nft_sell_offers" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNftSellOffersResult.class)
@JsonDeserialize(as = ImmutableNftSellOffersResult.class)
public interface NftSellOffersResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNftSellOffersResult.Builder}.
   */
  static ImmutableNftSellOffersResult.Builder builder() {
    return ImmutableNftSellOffersResult.builder();
  }

  /**
   *  The TokenID of the NFToken object.
   *
   *  @return the TokenID of the {@link org.xrpl.xrpl4j.model.client.accounts.NfTokenObject} object.
   */
  @JsonProperty("NFTokenID")
  NfTokenId tokenId();

  /**
   * List of sell offers for a particular NFToken.
   *
   * @return {@link List} of all {@link NfTokenOfferObject}s owned by an account.
   */
  List<NfTokenOfferObject> offers();
}
