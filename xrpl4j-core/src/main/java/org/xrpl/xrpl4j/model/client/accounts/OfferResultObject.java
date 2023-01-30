package org.xrpl.xrpl4j.model.client.accounts;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;

import java.util.Optional;

/**
 * An Offer returned in an {@link AccountOffersResult#offers()} list. Note that this object is NOT the same as the Offer
 * ledger object.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableOfferResultObject.class)
@JsonDeserialize(as = ImmutableOfferResultObject.class)
public interface OfferResultObject extends XrplResult {

  /**
   * Construct a builder.
   *
   * @return {@link ImmutableOfferResultObject.Builder}
   */
  static ImmutableOfferResultObject.Builder builder() {
    return ImmutableOfferResultObject.builder();
  }

  /**
   * Options set for this offer entry.
   *
   * @return The {@link OfferCreateFlags} for this offer.
   */
  OfferCreateFlags flags();

  /**
   * Sequence number of the transaction that created this entry. (Transaction sequence numbers are relative to
   * accounts.)
   *
   * @return The sequence number of the transaction that created this offer.
   */
  UnsignedInteger seq();

  /**
   * The amount the account accepting the offer receives, as a String representing an amount in XRP, or a currency
   * specification object.
   *
   * @return A {@link CurrencyAmount} representing the amount being offered.
   */
  @JsonProperty("taker_gets")
  CurrencyAmount takerGets();


  /**
   * The amount and type of currency being requested by the offer creator.
   *
   * @return A {@link CurrencyAmount} representing the amount being requested.
   */
  @JsonProperty("taker_pays")
  CurrencyAmount takerPays();

  /**
   * The exchange rate of the offer, as the ratio of the original taker_pays divided by the original taker_gets. When
   * executing offers, the offer with the most favorable (lowest) quality is consumed first; offers with the same
   * quality are executed from oldest to newest.
   *
   * @return The offer's quality.
   */
  String quality();

  /**
   * A time after which this offer is considered unfunded, as the number of seconds since the Ripple Epoch.
   *
   * @return The offer's expiration.
   */
  Optional<UnsignedInteger> expiration();
}
