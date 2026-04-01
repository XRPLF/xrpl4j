package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Deposit first-loss capital into a LoanBroker.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanBrokerCoverDeposit.class)
@JsonDeserialize(as = ImmutableLoanBrokerCoverDeposit.class)
@Beta
public interface LoanBrokerCoverDeposit extends Transaction {

  /**
   * Construct a {@code LoanBrokerCoverDeposit} builder.
   *
   * @return An {@link ImmutableLoanBrokerCoverDeposit.Builder}.
   */
  static ImmutableLoanBrokerCoverDeposit.Builder builder() {
    return ImmutableLoanBrokerCoverDeposit.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link LoanBrokerCoverDeposit}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The Loan Broker ID to which to deposit First-Loss Capital.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Hash256 loanBrokerId();

  /**
   * The First-Loss Capital amount to deposit.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * Validates LoanBrokerCoverDeposit data verification preconditions.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      !loanBrokerId().value().equals(
        "0000000000000000000000000000000000000000000000000000000000000000"
      ),
      "LoanBrokerID must not be zero."
    );

    Preconditions.checkArgument(
      !amount().isNegative() && !amount().isZero(),
      "Amount must be greater than zero."
    );
  }
}
