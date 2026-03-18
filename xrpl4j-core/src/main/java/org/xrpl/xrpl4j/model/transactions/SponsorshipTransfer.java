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
 *   <li>tfSponsorshipReassign - Transfer sponsorship from one sponsor to another</li>
 * </ul>
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
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

}

