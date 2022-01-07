package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;

/**
 * A TicketCreate transaction sets aside one or more sequence numbers as Tickets.
 *
 * @see "https://xrpl.org/ticketcreate.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTicketCreate.class)
@JsonDeserialize(as = ImmutableTicketCreate.class)
public interface TicketCreate extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableTicketCreate.Builder}.
   */
  static ImmutableTicketCreate.Builder builder() {
    return ImmutableTicketCreate.builder();
  }

  /**
   * How many Tickets to create. This number of tickets cannot cause the account to own more than 250 Tickets after
   * executing this transaction.
   *
   * @return An {@link UnsignedInteger} denoting the number of Tickets to create.
   */
  @JsonProperty("TicketCount")
  UnsignedInteger ticketCount();

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link PaymentChannelFund}, which only allows
   * {@code tfFullyCanonicalSig} flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link Flags.TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }
}
