package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify an {@link org.xrpl.xrpl4j.model.ledger.OfferObject} on ledger that can be used in a
 * {@link LedgerEntryRequestParams} to request an {@link org.xrpl.xrpl4j.model.ledger.OfferObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableOfferLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableOfferLedgerEntryParams.class)
public interface OfferLedgerEntryParams {

  /**
   * Construct a {@code OfferLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableOfferLedgerEntryParams.Builder}.
   */
  static ImmutableOfferLedgerEntryParams.Builder builder() {
    return ImmutableOfferLedgerEntryParams.builder();
  }

  /**
   * The account that placed the offer.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * The Sequence Number of the transaction that created the Offer entry.
   *
   * @return An {@link UnsignedInteger}.
   */
  UnsignedInteger seq();

}
