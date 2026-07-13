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
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.SponsorshipSetFlags;

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
   * Set of {@link SponsorshipSetFlags} for this {@link SponsorshipSet}.
   *
   * @return The {@link SponsorshipSetFlags} for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default SponsorshipSetFlags flags() {
    return SponsorshipSetFlags.empty();
  }

  /**
   * The sponsee's account, present when {@link Transaction#account()} is acting as the sponsor creating or
   * modifying this sponsorship. Mutually exclusive with {@link #counterpartySponsor()} — exactly one of the
   * two must be specified, to identify the counterparty of the sponsorship relationship.
   *
   * @return An {@link Optional} {@link Address} of the sponsee account.
   */
  @JsonProperty("Sponsee")
  Optional<Address> sponsee();

  /**
   * The sponsor's account, present when {@link Transaction#account()} is acting as the sponsee. Only the
   * sponsor may create or update a {@code Sponsorship} object, so a transaction with this field set must also
   * set {@link SponsorshipSetFlags#tfDeleteObject()} to end the sponsorship. Mutually exclusive with
   * {@link #sponsee()}.
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

  /**
   * Validates this transaction's field combinations per XLS-0068 Section 9.4, mirroring the field-level checks
   * from rippled's {@code SponsorshipSet::preflight} (i.e. the checks that don't require ledger state):
   * <ul>
   *   <li>Exactly one of {@link #sponsee()} or {@link #counterpartySponsor()} must be specified.</li>
   *   <li>Self-sponsorship (naming the transaction's own {@link Transaction#account()} as the counterparty) is
   *       not allowed.</li>
   *   <li>Only the sponsor (i.e. when {@link #sponsee()} is set) may create or update the object; when
   *       {@link #counterpartySponsor()} is set instead, {@link SponsorshipSetFlags#tfDeleteObject()} must be
   *       set.</li>
   *   <li>When {@link SponsorshipSetFlags#tfDeleteObject()} is set, none of {@link #feeAmount()},
   *       {@link #maxFee()}, or {@link #reserveCount()} may be present, and none of the RequireSignFor* /
   *       ClearRequireSignFor* flags may be set.</li>
   *   <li>The RequireSignForFee/ClearRequireSignForFee flags are mutually exclusive, as are the
   *       RequireSignForReserve/ClearRequireSignForReserve flags.</li>
   *   <li>{@link #feeAmount()} and {@link #maxFee()}, if present, must not be negative.</li>
   * </ul>
   *
   * @throws IllegalStateException if validation fails.
   */
  @Value.Check
  default void check() {
    SponsorshipSetFlags txFlags = flags();

    Preconditions.checkState(
      !(txFlags.tfRequireSignForFee() && txFlags.tfClearRequireSignForFee()),
      "tfSponsorshipSetRequireSignForFee and tfSponsorshipClearRequireSignForFee must not both be set"
    );
    Preconditions.checkState(
      !(txFlags.tfRequireSignForReserve() && txFlags.tfClearRequireSignForReserve()),
      "tfSponsorshipSetRequireSignForReserve and tfSponsorshipClearRequireSignForReserve must not both be set"
    );

    boolean hasSponsor = counterpartySponsor().isPresent();
    boolean hasSponsee = sponsee().isPresent();
    Preconditions.checkState(
      hasSponsor != hasSponsee,
      "Exactly one of CounterpartySponsor or Sponsee must be specified"
    );

    Address sponsorId = counterpartySponsor().orElse(account());
    Address sponseeId = sponsee().orElse(account());
    Preconditions.checkState(
      !sponsorId.equals(sponseeId),
      "A SponsorshipSet transaction must not name its own account as the counterparty (self-sponsorship)"
    );

    if (txFlags.tfDeleteObject()) {
      Preconditions.checkState(
        !txFlags.tfRequireSignForFee() && !txFlags.tfRequireSignForReserve() &&
          !txFlags.tfClearRequireSignForFee() && !txFlags.tfClearRequireSignForReserve(),
        "SponsorshipSet must not set any RequireSignFor*/ClearRequireSignFor* flags when tfDeleteObject is set"
      );
      Preconditions.checkState(
        !feeAmount().isPresent() && !maxFee().isPresent() && !reserveCount().isPresent(),
        "SponsorshipSet must not include FeeAmount, MaxFee, or ReserveCount when tfDeleteObject is set"
      );
    } else {
      Preconditions.checkState(
        account().equals(sponsorId),
        "Only the sponsor can create or update a Sponsorship object; the sponsee may only delete it " +
          "(set tfDeleteObject)"
      );

      feeAmount().ifPresent(amount -> Preconditions.checkState(!amount.isNegative(), "FeeAmount must not be negative"));
      maxFee().ifPresent(amount -> Preconditions.checkState(!amount.isNegative(), "MaxFee must not be negative"));
    }
  }

}

