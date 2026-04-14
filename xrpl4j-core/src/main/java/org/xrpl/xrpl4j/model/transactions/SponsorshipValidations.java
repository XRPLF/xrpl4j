package org.xrpl.xrpl4j.model.transactions;

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

import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for validating sponsorship fields on transactions according to XLS-0068.
 *
 * <p>Per the spec:</p>
 * <ul>
 *   <li>If {@code Sponsor} is present, {@code SponsorFlags} MUST also be present</li>
 *   <li>If {@code SponsorFlags} is present, at least one of {@code spfSponsorFee} or
 *       {@code spfSponsorReserve} MUST be set</li>
 *   <li>{@code SponsorFlags} MUST NOT be present if {@code Sponsor} is not present</li>
 * </ul>
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
public final class SponsorshipValidations {

  private SponsorshipValidations() {
    // Utility class
  }

  /**
   * Validates the sponsorship fields on a transaction according to XLS-0068.
   *
   * <p>Note: {@link SponsorshipSet} and {@link SponsorshipTransfer} transactions are exempt from this validation
   * because they use the {@code Sponsor} field differently - to specify the new sponsor in a sponsorship
   * management operation, not to indicate who is sponsoring this transaction's fee/reserve.</p>
   *
   * @param transaction The transaction to validate.
   *
   * @throws IllegalStateException if the sponsorship fields are invalid.
   */
  public static void validateSponsorFields(Transaction transaction) {
    Objects.requireNonNull(transaction, "transaction must not be null");

    // SponsorshipSet and SponsorshipTransfer transactions use the Sponsor field differently
    // (to specify the new sponsor), so they are exempt from this validation
    if (transaction instanceof SponsorshipSet || transaction instanceof SponsorshipTransfer) {
      return;
    }

    Optional<Address> sponsor = transaction.sponsor();
    Optional<UnsignedInteger> sponsorFlags = transaction.sponsorFlags();

    // If SponsorFlags is present, Sponsor must also be present
    if (sponsorFlags.isPresent() && sponsor.isEmpty()) {
      throw new IllegalStateException(
        "SponsorFlags must not be present without Sponsor field. " +
          "Per XLS-0068, SponsorFlags requires Sponsor to be set."
      );
    }

    // If Sponsor is present, SponsorFlags must also be present
    if (sponsor.isPresent() && sponsorFlags.isEmpty()) {
      throw new IllegalStateException(
        "Sponsor field requires SponsorFlags to be set. " +
          "Per XLS-0068, at least one of spfSponsorFee or spfSponsorReserve must be specified."
      );
    }

    // If SponsorFlags is present, at least one flag must be set
    if (sponsorFlags.isPresent()) {
      SponsorFlags flags = SponsorFlags.of(sponsorFlags.get().longValue());
      if (!flags.isValid()) {
        throw new IllegalStateException(
          "SponsorFlags must have at least one flag set (spfSponsorFee=0x01 or spfSponsorReserve=0x02). " +
            "Per XLS-0068, at least one sponsorship type must be specified. Current value: " +
            sponsorFlags.get()
        );
      }
    }
  }

  /**
   * Checks if the sponsorship fields on a transaction are valid according to XLS-0068.
   *
   * @param transaction The transaction to check.
   *
   * @return {@code true} if the sponsorship fields are valid or not present, {@code false} otherwise.
   */
  public static boolean isValidSponsorFields(Transaction transaction) {
    try {
      validateSponsorFields(transaction);
      return true;
    } catch (IllegalStateException e) {
      return false;
    }
  }
}
