package org.xrpl.xrpl4j.model.client.nft;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.NfTokenOfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * An Offer returned in an {@link BuyOffer} list.
 * This offer is related to NfTokens.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBuyOffer.class)
@JsonDeserialize(as = ImmutableBuyOffer.class)
public interface BuyOffer {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableBuyOffer.Builder}.
   */
  static ImmutableBuyOffer.Builder builder() {
    return ImmutableBuyOffer.builder();
  }

  /**
   * The amount offered to buy the NFT.
   *
   * @return The {@link CurrencyAmount}.
   */
  CurrencyAmount amount();

  /**
   * A set of boolean {@link NfTokenOfferFlags} containing options
   * enabled for this object.
   *
   * @return The {@link NfTokenOfferFlags} for this object.
   */
  NfTokenOfferFlags flags();

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
