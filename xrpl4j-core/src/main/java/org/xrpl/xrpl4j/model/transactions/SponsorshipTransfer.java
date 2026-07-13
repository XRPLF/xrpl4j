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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;
import org.xrpl.xrpl4j.model.flags.SponsorshipTransferFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Transfer sponsorship of a ledger object or account from one sponsor to another, create a new sponsorship,
 * or dissolve an existing sponsorship relationship.
 *
 * <p>This transaction supports three distinct scenarios indicated by transaction flags:</p>
 * <ul>
 *   <li>tfSponsorshipEnd - End a sponsorship, transferring reserve burden back to the sponsee</li>
 *   <li>tfSponsorshipCreate - Create a new sponsorship for an unsponsored object/account</li>
 *   <li>tfSponsorshipReassign - Transfer sponsorship from one sponsor to another. In this scenario,
 *       the {@link Transaction#sponsor()} field identifies the new sponsor who will take over the
 *       sponsorship responsibility. The transaction must be signed by both the current sponsor
 *       (via {@link Transaction#signingPublicKey()}) and the new sponsor
       (via {@link Transaction#sponsorSignature()}).</li>
 * </ul>
 *
 * <p><strong>Note on Reassign Semantics:</strong> When using tfSponsorshipReassign, the {@link Transaction#sponsor()}
 * field serves a dual purpose: it identifies both who is paying the transaction fee AND who will become the new
 * sponsor of the object. This design reuses the existing sponsor field rather than introducing a separate
 * "newSponsor" field. The current sponsor must sign the transaction normally, and the new sponsor must provide
 * a {@link Transaction#sponsorSignature()} to authorize taking over the sponsorship.</p>
 *
 * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureSponsorship
 * amendment is enabled on mainnet. Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableSponsorshipTransfer.class)
@JsonDeserialize(as = ImmutableSponsorshipTransfer.class)
public interface SponsorshipTransfer extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSponsorshipTransfer.Builder}.
   */
  static ImmutableSponsorshipTransfer.Builder builder() {
    return ImmutableSponsorshipTransfer.builder();
  }

  /**
   * Set of {@link SponsorshipTransferFlags}s for this {@link SponsorshipTransfer}.
   *
   * @return The {@link SponsorshipTransferFlags} for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default SponsorshipTransferFlags flags() {
    return SponsorshipTransferFlags.empty();
  }

  /**
   * The ID of the ledger object to transfer sponsorship for. If not included, the transaction refers to
   * the account sending the transaction (for account sponsorship).
   *
   * @return An {@link Optional} {@link Hash256} representing the object ID.
   */
  @JsonProperty("ObjectID")
  Optional<Hash256> objectId();

  /**
   * The {@link Address} of the account whose sponsorship is being ended. Only used with
   * {@code tfSponsorshipEnd}, to allow the current sponsor (or the sponsee) to end sponsorship on behalf of
   * an account other than the transaction sender. Must not be present for {@code tfSponsorshipCreate} or
   * {@code tfSponsorshipReassign}, and if omitted for {@code tfSponsorshipEnd}, defaults to
   * {@link Transaction#account()}.
   *
   * @return An {@link Optional} {@link Address} representing the sponsee whose sponsorship is being ended.
   */
  @JsonProperty("Sponsee")
  Optional<Address> sponsee();

  /**
   * Validates that exactly one mode flag is set (tfSponsorshipEnd, tfSponsorshipCreate, or tfSponsorshipReassign).
   * These flags are mutually exclusive and define the operation mode of the transaction. Also validates that
   * {@link #sponsee()} is only present when {@code tfSponsorshipEnd} is set, and that an account-level
   * {@code tfSponsorshipReassign} (i.e. one with no {@link #objectId()}) is accompanied by a
   * {@link Transaction#sponsorSignature()} from the new sponsor, consistent with rippled's requirement that
   * account-level reserve sponsorship cannot be reassigned without the new sponsor's consent. Object-level
   * reassignment drawing from a pre-funded {@code Sponsorship} object's budget does not require this signature.
   *
   * <p>Per XLS-0068 Section 10.3, also validates that {@link Transaction#sponsor()} and
   * {@link Transaction#sponsorFlags()} are excluded for {@code tfSponsorshipEnd} (which dissolves an existing
   * sponsorship), and that both are present (with {@code SponsorFlags.spfSponsorReserve} set) for
   * {@code tfSponsorshipCreate} and {@code tfSponsorshipReassign} (which establish a new sponsor).</p>
   *
   * @throws IllegalStateException if validation fails.
   */
  @Value.Check
  default void check() {
    SponsorshipTransferFlags txFlags = flags();

    int modeCount = 0;
    if (txFlags.tfSponsorshipEnd()) {
      modeCount++;
    }
    if (txFlags.tfSponsorshipCreate()) {
      modeCount++;
    }
    if (txFlags.tfSponsorshipReassign()) {
      modeCount++;
    }

    Preconditions.checkState(
      modeCount == 1,
      "SponsorshipTransfer must have exactly one mode flag set " +
        "(tfSponsorshipEnd, tfSponsorshipCreate, or tfSponsorshipReassign)"
    );

    Preconditions.checkState(
      !sponsee().isPresent() || txFlags.tfSponsorshipEnd(),
      "Sponsee must not be present unless tfSponsorshipEnd is set"
    );

    boolean isAccountLevelReassign = txFlags.tfSponsorshipReassign() && !objectId().isPresent();
    Preconditions.checkState(
      !isAccountLevelReassign || sponsorSignature().isPresent(),
      "An account-level SponsorshipTransfer (tfSponsorshipReassign with no ObjectID) requires a " +
        "SponsorSignature from the new sponsor"
    );

    if (txFlags.tfSponsorshipEnd()) {
      Preconditions.checkState(
        !sponsor().isPresent(),
        "Sponsor must not be present when tfSponsorshipEnd is set"
      );
      Preconditions.checkState(
        !sponsorFlags().isPresent() || !SponsorFlags.of(sponsorFlags().get().longValue()).spfSponsorReserve(),
        "SponsorFlags must not include spfSponsorReserve when tfSponsorshipEnd is set"
      );
    } else {
      Preconditions.checkState(
        sponsor().isPresent() && sponsorFlags().isPresent(),
        "Sponsor and SponsorFlags are both required when tfSponsorshipCreate or tfSponsorshipReassign is set"
      );
      Preconditions.checkState(
        SponsorFlags.of(sponsorFlags().get().longValue()).spfSponsorReserve(),
        "SponsorFlags must include spfSponsorReserve when tfSponsorshipCreate or tfSponsorshipReassign is set"
      );
    }
  }

}

