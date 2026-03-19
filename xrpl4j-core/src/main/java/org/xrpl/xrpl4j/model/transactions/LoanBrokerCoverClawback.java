package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Clawback first-loss capital from a LoanBroker. Must be submitted by the asset issuer.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanBrokerCoverClawback.class)
@JsonDeserialize(as = ImmutableLoanBrokerCoverClawback.class)
@Beta
public interface LoanBrokerCoverClawback extends Transaction {

  /**
   * Construct a {@code LoanBrokerCoverClawback} builder.
   *
   * @return An {@link ImmutableLoanBrokerCoverClawback.Builder}.
   */
  static ImmutableLoanBrokerCoverClawback.Builder builder() {
    return ImmutableLoanBrokerCoverClawback.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link LoanBrokerCoverClawback}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The Loan Broker ID from which to clawback First-Loss Capital.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Optional<Hash256> loanBrokerId();

  /**
   * The First-Loss Capital amount to clawback. If the amount is 0 or not provided, clawback funds up to
   * {@code LoanBroker.DebtTotal * LoanBroker.CoverRateMinimum}.
   *
   * @return An optionally-present {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

}
