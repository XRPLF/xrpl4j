package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Delete a LoanBroker ledger object.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanBrokerDelete.class)
@JsonDeserialize(as = ImmutableLoanBrokerDelete.class)
@Beta
public interface LoanBrokerDelete extends Transaction {

  /**
   * Construct a {@code LoanBrokerDelete} builder.
   *
   * @return An {@link ImmutableLoanBrokerDelete.Builder}.
   */
  static ImmutableLoanBrokerDelete.Builder builder() {
    return ImmutableLoanBrokerDelete.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link LoanBrokerDelete}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The Loan Broker ID that the transaction is deleting.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Hash256 loanBrokerId();
}
