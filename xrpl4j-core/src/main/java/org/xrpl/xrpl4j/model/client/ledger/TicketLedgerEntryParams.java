package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.TicketObject} on ledger that can be used in a
 * {@link LedgerEntryRequestParams} to request a {@link org.xrpl.xrpl4j.model.ledger.TicketObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableTicketLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableTicketLedgerEntryParams.class)
public interface TicketLedgerEntryParams {

  /**
   * Construct a {@code TicketLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableTicketLedgerEntryParams.Builder}.
   */
  static ImmutableTicketLedgerEntryParams.Builder builder() {
    return ImmutableTicketLedgerEntryParams.builder();
  }

  /**
   * The owner of the Ticket.
   *
   * @return The {@link Address} of the owner.
   */
  Address account();

  /**
   * The Ticket Sequence number of the Ticket to retrieve.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("ticket_seq")
  UnsignedInteger ticketSeq();

}
