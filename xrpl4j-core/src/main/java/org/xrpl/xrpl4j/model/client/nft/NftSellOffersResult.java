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
import org.xrpl.xrpl4j.model.client.XrplResult;
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
  @JsonProperty("nft_id")
  NfTokenId nfTokenId();

  /**
   * List of sell offers for a particular NFToken.
   *
   * @return {@link List} of all {@link SellOffer}s owned by an account.
   */
  List<SellOffer> offers();
}
