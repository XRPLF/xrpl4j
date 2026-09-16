package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.LoanBrokerObject} on ledger that can be
 * used in a {@link LedgerEntryRequestParams} to request a {@link org.xrpl.xrpl4j.model.ledger.LoanBrokerObject}.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableLoanBrokerLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableLoanBrokerLedgerEntryParams.class)
public interface LoanBrokerLedgerEntryParams {

  /**
   * Construct a {@code LoanBrokerLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableLoanBrokerLedgerEntryParams.Builder}.
   */
  static ImmutableLoanBrokerLedgerEntryParams.Builder builder() {
    return ImmutableLoanBrokerLedgerEntryParams.builder();
  }

  /**
   * The owner of the LoanBroker.
   *
   * @return The {@link Address} of the LoanBroker owner.
   */
  Address owner();

  /**
   * The Sequence Number of the transaction that created the LoanBroker. If the transaction used a Ticket,
   * this should be the TicketSequence value.
   *
   * @return An {@link UnsignedInteger}.
   */
  UnsignedInteger seq();

}
