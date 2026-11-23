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
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.SponsorshipFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * Represents a pre-funded sponsorship relationship, as represented in transaction metadata.
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableMetaSponsorshipObject.class)
@JsonDeserialize(as = ImmutableMetaSponsorshipObject.class)
public interface MetaSponsorshipObject extends MetaLedgerObject {

  /**
   * A bit-map of boolean flags enabled for this {@link MetaSponsorshipObject}.
   *
   * @return An {@link Optional} {@link SponsorshipFlags}.
   */
  @JsonProperty("Flags")
  Optional<SponsorshipFlags> flags();

  /**
   * The {@link Address} of the account that owns this sponsorship (the sponsor).
   *
   * @return An {@link Optional} {@link Address} of the sponsor account.
   */
  @JsonProperty("Owner")
  Optional<Address> owner();

  /**
   * The {@link Address} of the account being sponsored (the sponsee).
   *
   * @return An {@link Optional} {@link Address} of the sponsee account.
   */
  @JsonProperty("Sponsee")
  Optional<Address> sponsee();

  /**
   * The total amount of XRP (in drops) allocated for transaction fees.
   *
   * @return An {@link Optional} {@link XrpCurrencyAmount}.
   */
  @JsonProperty("FeeAmount")
  Optional<XrpCurrencyAmount> feeAmount();

  /**
   * The maximum fee (in drops) that can be charged for a single transaction using this sponsorship.
   *
   * @return An {@link Optional} {@link XrpCurrencyAmount}.
   */
  @JsonProperty("MaxFee")
  Optional<XrpCurrencyAmount> maxFee();

  /**
   * The number of reserve units sponsored for the sponsee.
   *
   * @return An {@link Optional} {@link UnsignedInteger}.
   */
  @JsonProperty("RemainingOwnerCount")
  Optional<UnsignedInteger> remainingOwnerCount();

  /**
   * A hint indicating which page of the owner directory link list this object is linked into.
   *
   * @return An {@link Optional} of type {@link String}.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * A hint indicating which page of the sponsee directory link list this object is linked into.
   *
   * @return An {@link Optional} of type {@link String}.
   */
  @JsonProperty("SponseeNode")
  Optional<String> sponseeNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return An {@link Optional} {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTxnId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link Optional} {@link LedgerIndex}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionLedgerSequence();

}
