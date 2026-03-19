package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Withdraw first-loss capital from a LoanBroker.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanBrokerCoverWithdraw.class)
@JsonDeserialize(as = ImmutableLoanBrokerCoverWithdraw.class)
@Beta
public interface LoanBrokerCoverWithdraw extends Transaction {

  /**
   * Construct a {@code LoanBrokerCoverWithdraw} builder.
   *
   * @return An {@link ImmutableLoanBrokerCoverWithdraw.Builder}.
   */
  static ImmutableLoanBrokerCoverWithdraw.Builder builder() {
    return ImmutableLoanBrokerCoverWithdraw.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link LoanBrokerCoverWithdraw}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The Loan Broker ID from which to withdraw First-Loss Capital.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Hash256 loanBrokerId();

  /**
   * The First-Loss Capital amount to withdraw.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * An account to receive the assets. It must be able to receive the asset.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Destination")
  Optional<Address> destination();

  /**
   * Arbitrary tag identifying the reason for the transaction to the destination.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

}
