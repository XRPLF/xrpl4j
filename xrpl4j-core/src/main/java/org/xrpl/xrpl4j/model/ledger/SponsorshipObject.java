package org.xrpl.xrpl4j.model.ledger;

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
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * Represents a pre-funded sponsorship relationship in the XRP Ledger. A {@link SponsorshipObject} is created
 * by a {@link SponsorshipSet} transaction and tracks the allocation of fees and reserves that a sponsor
 * has committed to cover for a sponsee account.
 *
 * <p>Unlike co-signed sponsorship (via {@link org.xrpl.xrpl4j.model.transactions.SponsorshipTransfer}),
 * this object enables pre-funded sponsorship where the sponsee can use the allocated funds without requiring
 * the sponsor's signature on each transaction.</p>
 *
 * <p>The sponsorship object tracks:</p>
 * <ul>
 *   <li>The total amount of XRP allocated for transaction fees ({@link #feeAmount()})</li>
 *   <li>The maximum fee per transaction ({@link #maxFee()})</li>
 *   <li>The number of reserve units sponsored ({@link #reserveCount()})</li>
 * </ul>
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableSponsorshipObject.class)
@JsonDeserialize(as = ImmutableSponsorshipObject.class)
public interface SponsorshipObject extends LedgerObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSponsorshipObject.Builder}.
   */
  static ImmutableSponsorshipObject.Builder builder() {
    return ImmutableSponsorshipObject.builder();
  }

  /**
   * The type of ledger object, which is always "Sponsorship".
   *
   * @return Always {@link LedgerEntryType#SPONSORSHIP}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.SPONSORSHIP;
  }

  /**
   * A bit-map of boolean flags. No flags are defined for {@link SponsorshipObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The {@link Address} of the account that owns this sponsorship (the sponsor). This account is responsible
   * for providing the funds allocated in this sponsorship.
   *
   * @return The {@link Address} of the sponsor account.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * The {@link Address} of the account being sponsored (the sponsee). This account can use the pre-funded
   * fees and reserves allocated by this sponsorship.
   *
   * @return The {@link Address} of the sponsee account.
   */
  @JsonProperty("Sponsee")
  Address sponsee();

  /**
   * The total amount of XRP (in drops) allocated for transaction fees. The sponsee can draw from this amount
   * to pay transaction fees, up to the {@link #maxFee()} limit per transaction.
   *
   * @return An {@link Optional} {@link XrpCurrencyAmount} representing the remaining fee allocation.
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
   * The number of reserve units sponsored for the sponsee. Each unit represents the reserve requirement
   * for one ledger object owned by the sponsee. Defaults to 0 if not specified.
   *
   * @return An {@link UnsignedInteger} representing the number of reserve units.
   */
  @JsonProperty("ReserveCount")
  @Value.Default
  default UnsignedInteger reserveCount() {
    return UnsignedInteger.ZERO;
  }

  /**
   * A hint indicating which page of the owner directory link list this object is linked into.
   * This field is used for efficient directory traversal.
   *
   * @return An {@link UnsignedLong} representing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  UnsignedLong ownerNode();

  /**
   * A hint indicating which page of the sponsee directory link list this object is linked into.
   * This field is used for efficient directory traversal.
   *
   * @return An {@link UnsignedLong} representing the sponsee node hint.
   */
  @JsonProperty("SponseeNode")
  UnsignedLong sponseeNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The unique ID of this {@link SponsorshipObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();

}

