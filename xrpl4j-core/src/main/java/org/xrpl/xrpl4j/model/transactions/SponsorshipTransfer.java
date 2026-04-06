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
   * Validates that exactly one mode flag is set (tfSponsorshipEnd, tfSponsorshipCreate, or tfSponsorshipReassign).
   * These flags are mutually exclusive and define the operation mode of the transaction.
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
  }

}

