package org.xrpl.xrpl4j.model.transactions.metadata;

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
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.Optional;

/**
 * The Offer object type describes an offer to exchange currencies, more traditionally known as an order.
 *
 * @see "https://xrpl.org/offer.html"
 */
@Immutable
@JsonSerialize(as = ImmutableMetaOfferObject.class)
@JsonDeserialize(as = ImmutableMetaOfferObject.class)
public interface MetaOfferObject extends MetaLedgerObject {

  /**
   * The sender of the {@link MetaOfferObject}. Cashing the {@link MetaOfferObject} debits this address's balance.
   *
   * @return The {@link Address} of the offer sender.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * A bit-map of boolean flags.
   *
   * @return A {@link OfferFlags}.
   */
  @JsonProperty("Flags")
  Optional<OfferFlags> flags();

  /**
   * The sequence number of the {@link org.xrpl.xrpl4j.model.transactions.OfferCreate} transaction that created this
   * offer.
   *
   * @return An {@link UnsignedInteger} representing the sequence number.
   */
  @JsonProperty("Sequence")
  Optional<UnsignedInteger> sequence();

  /**
   * The remaining amount and type of currency requested by the offer creator.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("TakerPays")
  Optional<CurrencyAmount> takerPays();


  /**
   * The remaining amount and type of currency being provided by the offer creator.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("TakerGets")
  Optional<CurrencyAmount> takerGets();


  /**
   * The ID of the Offer Directory that links to this offer.
   *
   * @return A {@link Hash256} containing the ID.
   */
  @JsonProperty("BookDirectory")
  Optional<Hash256> bookDirectory();

  /**
   * A hint indicating which page of the offer directory links to this object, in case the directory consists of
   * multiple pages.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("BookNode")
  Optional<String> bookNode();

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory consists
   * of multiple pages. Note: The object does not contain a direct link to the owner directory containing it, since that
   * value can be derived from the Account.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link LedgerIndex} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionLedgerSequence();

  /**
   * Indicates the time after which this offer is considered expired, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the expiration of this offer.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

  /**
   * The domain that the offer must be a part of.
   *
   * @return A {@link Hash256} representing DomainID.
   */
  @JsonProperty("DomainID")
  Optional<Hash256> domainId();

  /**
   * An additional list of order book directories that this offer belongs to. Currently, this field only applicable to
   * hybrid offers.
   *
   * @return A list of {@link MetaAdditionalBook} representing order book directories.
   */
  @JsonProperty("AdditionalBooks")
  List<MetaAdditionalBook> additionalBooks();
}
