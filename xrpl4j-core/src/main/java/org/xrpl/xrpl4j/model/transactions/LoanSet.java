package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.LoanSetFlags;

import java.util.Optional;

/**
 * Create a new Loan.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanSet.class)
@JsonDeserialize(as = ImmutableLoanSet.class)
@Beta
public interface LoanSet extends Transaction {

  /**
   * Construct a {@code LoanSet} builder.
   *
   * @return An {@link ImmutableLoanSet.Builder}.
   */
  static ImmutableLoanSet.Builder builder() {
    return ImmutableLoanSet.builder();
  }

  /**
   * Set of {@link LoanSetFlags}s for this {@link LoanSet}.
   *
   * @return A {@link LoanSetFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default LoanSetFlags flags() {
    return LoanSetFlags.empty();
  }

  /**
   * The Loan Broker ID associated with the loan.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Hash256 loanBrokerId();

  /**
   * The address of the counterparty of the Loan.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Counterparty")
  Optional<Address> counterparty();

  /**
   * The signature of the counterparty over the transaction.
   *
   * @return An optionally-present {@link CounterpartySignature}.
   */
  @JsonProperty("CounterpartySignature")
  Optional<CounterpartySignature> counterpartySignature();

  /**
   * Arbitrary metadata in hex format. The field is limited to 256 bytes.
   *
   * @return An optionally-present {@link LoanData}.
   */
  @JsonProperty("Data")
  Optional<LoanData> data();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when the Loan is created.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("LoanOriginationFee")
  Optional<AssetAmount> loanOriginationFee();

  /**
   * A nominal amount paid to the {@code LoanBroker.Owner} with every Loan payment.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("LoanServiceFee")
  Optional<AssetAmount> loanServiceFee();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when a payment is late.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("LatePaymentFee")
  Optional<AssetAmount> latePaymentFee();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when an early full repayment is made.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("ClosePaymentFee")
  Optional<AssetAmount> closePaymentFee();

  /**
   * A fee charged on overpayments in 1/10th basis points. Valid values are between 0 and 100000 inclusive
   * (0 - 100%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("OverpaymentFee")
  Optional<UnsignedInteger> overpaymentFee();

  /**
   * Annualized interest rate of the Loan in 1/10th basis points. Valid values are between 0 and 100000
   * inclusive (0 - 100%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("InterestRate")
  Optional<UnsignedInteger> interestRate();

  /**
   * A premium added to the interest rate for late payments in 1/10th basis points. Valid values are between
   * 0 and 100000 inclusive (0 - 100%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("LateInterestRate")
  Optional<UnsignedInteger> lateInterestRate();

  /**
   * A Fee Rate charged for repaying the Loan early in 1/10th basis points. Valid values are between 0 and
   * 100000 inclusive (0 - 100%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("CloseInterestRate")
  Optional<UnsignedInteger> closeInterestRate();

  /**
   * An interest rate charged on overpayments in 1/10th basis points. Valid values are between 0 and 100000
   * inclusive (0 - 100%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("OverpaymentInterestRate")
  Optional<UnsignedInteger> overpaymentInterestRate();

  /**
   * The principal amount requested by the Borrower.
   *
   * @return An {@link AssetAmount}.
   */
  @JsonProperty("PrincipalRequested")
  AssetAmount principalRequested();

  /**
   * The total number of payments to be made against the Loan.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("PaymentTotal")
  Optional<UnsignedInteger> paymentTotal();

  /**
   * Number of seconds between Loan payments.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("PaymentInterval")
  Optional<UnsignedInteger> paymentInterval();

  /**
   * The number of seconds after the Loan's Payment Due Date can be Defaulted.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("GracePeriod")
  Optional<UnsignedInteger> gracePeriod();
}
