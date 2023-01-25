package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * An Offer returned in an {@link SellOffer} list.
 * This offer is related to NfTokens.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSellOffer.class)
@JsonDeserialize(as = ImmutableSellOffer.class)
public interface SellOffer {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSellOffer.Builder}.
   */
  static ImmutableSellOffer.Builder builder() {
    return ImmutableSellOffer.builder();
  }

  /**
   * The amount offered to buy the NFT for {@link XrpCurrencyAmount}.
   *
   * @return The {@link XrpCurrencyAmount}.
   */
  @JsonProperty("Amount")
  XrpCurrencyAmount amount();

  /**
   * A set of boolean {@link Flags.NfTokenOfferFlags} containing options
   * enabled for this object.
   *
   * @return The {@link Flags.NfTokenOfferFlags} for this object.
   */
  @JsonProperty("Flags")
  Flags.NfTokenOfferFlags flags();

  /**
   * The ledger object ID of this offer.
   *
   * @return The {@link String} index.
   */
  @JsonProperty("nft_offer_index")
  Hash256 nftOfferIndex();

  /**
   * The account that placed this {@link BuyOffer}.
   *
   * @return The {@link Address} of owner of the NfToken.
   */
  Address owner();
}
