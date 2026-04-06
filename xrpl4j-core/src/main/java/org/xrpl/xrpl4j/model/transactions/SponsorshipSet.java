package org.xrpl.xrpl4j.model.transactions;

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
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Create or modify a pre-funded sponsorship relationship. This transaction creates a
 * {@link org.xrpl.xrpl4j.model.ledger.SponsorshipObject} in the ledger that allows the sponsor to pre-fund fees
 * and reserves for a sponsee account.
 *
 * <p>Unlike {@link SponsorshipTransfer} which requires co-signing for each transaction, this transaction
 * establishes a pre-funded sponsorship where the sponsee can use the pre-allocated funds without requiring
 * the sponsor's signature on each transaction.</p>
 *
 * <p>The sponsor can specify:</p>
 * <ul>
 *   <li>{@link #feeAmount()} - Total amount of XRP allocated for transaction fees</li>
 *   <li>{@link #maxFee()} - Maximum fee per transaction that can be drawn from the allocation</li>
 *   <li>{@link #reserveCount()} - Number of reserve units to sponsor for the sponsee</li>
 * </ul>
 *
 * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureSponsorship
 * amendment is enabled on mainnet. Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableSponsorshipSet.class)
@JsonDeserialize(as = ImmutableSponsorshipSet.class)
public interface SponsorshipSet extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSponsorshipSet.Builder}.
   */
  static ImmutableSponsorshipSet.Builder builder() {
    return ImmutableSponsorshipSet.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link SponsorshipSet}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The account that will be sponsored. This account will be able to use the pre-funded fees and reserves
   * allocated by this sponsorship.
   *
   * @return An {@link Optional} {@link Address} of the sponsee account.
   */
  @JsonProperty("Sponsee")
  Optional<Address> sponsee();

  /**
   * An alternative sponsor account that can also modify or cancel this sponsorship. This allows for
   * more flexible sponsorship management where multiple parties can be involved.
   *
   * @return An {@link Optional} {@link Address} of the counterparty sponsor.
   */
  @JsonProperty("CounterpartySponsor")
  Optional<Address> counterpartySponsor();

  /**
   * The total amount of XRP (in drops) to allocate for transaction fees. The sponsee can draw from this
   * amount to pay transaction fees, up to the {@link #maxFee()} limit per transaction.
   *
   * @return An {@link Optional} {@link XrpCurrencyAmount} representing the fee allocation.
   */
  @JsonProperty("FeeAmount")
  Optional<XrpCurrencyAmount> feeAmount();

  /**
   * The maximum fee (in drops) that can be charged for a single transaction using this sponsorship.
   * This prevents the sponsee from consuming the entire {@link #feeAmount()} in a single transaction.
   *
   * @return An {@link Optional} {@link XrpCurrencyAmount} representing the maximum fee per transaction.
   */
  @JsonProperty("MaxFee")
  Optional<XrpCurrencyAmount> maxFee();

  /**
   * The number of reserve units to sponsor for the sponsee. Each unit represents the reserve requirement
   * for one ledger object owned by the sponsee. This allows the sponsee to create ledger objects without
   * having to maintain the reserve themselves.
   *
   * @return An {@link Optional} {@link UnsignedInteger} representing the number of reserve units.
   */
  @JsonProperty("ReserveCount")
  Optional<UnsignedInteger> reserveCount();

}

