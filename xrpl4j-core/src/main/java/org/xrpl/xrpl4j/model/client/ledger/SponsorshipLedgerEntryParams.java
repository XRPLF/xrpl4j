package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.SponsorshipObject} on ledger that can be
 * used in a {@link LedgerEntryRequestParams} to request a {@link org.xrpl.xrpl4j.model.ledger.SponsorshipObject}.
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Immutable
@JsonSerialize(as = ImmutableSponsorshipLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableSponsorshipLedgerEntryParams.class)
@Beta
public interface SponsorshipLedgerEntryParams {

  /**
   * Construct a {@code SponsorshipLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableSponsorshipLedgerEntryParams.Builder}.
   */
  static ImmutableSponsorshipLedgerEntryParams.Builder builder() {
    return ImmutableSponsorshipLedgerEntryParams.builder();
  }

  /**
   * The {@link Address} of the account that owns the sponsorship (the sponsor).
   *
   * @return An {@link Address}.
   */
  Address owner();

  /**
   * The {@link Address} of the account being sponsored (the sponsee).
   *
   * @return An {@link Address}.
   */
  Address sponsee();
}
