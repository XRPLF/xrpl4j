package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.LoanSetFlags;

import java.math.BigDecimal;
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

  /**
   * Validates LoanSet data verification preconditions per the Lending Protocol spec section 3.8.5.1.
   */
  @Value.Check
  default void check() {
    // 1. LoanBrokerID must not be zero.
    Preconditions.checkArgument(
      !loanBrokerId().value().equals(
        "0000000000000000000000000000000000000000000000000000000000000000"
      ),
      "LoanBrokerID must not be zero."
    );

    // 6. LoanServiceFee, LatePaymentFee, or ClosePaymentFee must not be negative.
    loanServiceFee().ifPresent(fee -> Preconditions.checkArgument(
      !fee.isNegative(),
      "LoanServiceFee must not be negative."
    ));
    latePaymentFee().ifPresent(fee -> Preconditions.checkArgument(
      !fee.isNegative(),
      "LatePaymentFee must not be negative."
    ));
    closePaymentFee().ifPresent(fee -> Preconditions.checkArgument(
      !fee.isNegative(),
      "ClosePaymentFee must not be negative."
    ));

    // 7. PrincipalRequested must be greater than zero.
    Preconditions.checkArgument(
      !principalRequested().isNegative() && !principalRequested().isZero(),
      "PrincipalRequested must be greater than zero."
    );

    // 8. LoanOriginationFee must not be negative and must not exceed PrincipalRequested.
    loanOriginationFee().ifPresent(fee -> {
      Preconditions.checkArgument(
        !fee.isNegative(),
        "LoanOriginationFee must not be negative."
      );
      Preconditions.checkArgument(
        new BigDecimal(fee.value()).compareTo(new BigDecimal(principalRequested().value())) <= 0,
        "LoanOriginationFee must not exceed PrincipalRequested."
      );
    });

    // 9-13. Rate fields must not exceed 100000.
    interestRate().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(100000)) <= 0,
      "InterestRate must be between 0 and 100000 inclusive."
    ));
    overpaymentFee().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(100000)) <= 0,
      "OverpaymentFee must be between 0 and 100000 inclusive."
    ));
    lateInterestRate().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(100000)) <= 0,
      "LateInterestRate must be between 0 and 100000 inclusive."
    ));
    closeInterestRate().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(100000)) <= 0,
      "CloseInterestRate must be between 0 and 100000 inclusive."
    ));
    overpaymentInterestRate().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(100000)) <= 0,
      "OverpaymentInterestRate must be between 0 and 100000 inclusive."
    ));

    // 14. PaymentTotal must be greater than zero (if specified).
    paymentTotal().ifPresent(total -> Preconditions.checkArgument(
      total.compareTo(UnsignedInteger.ZERO) > 0,
      "PaymentTotal must be greater than zero."
    ));

    // 15. PaymentInterval must be at least 60 seconds (if specified).
    paymentInterval().ifPresent(interval -> Preconditions.checkArgument(
      interval.compareTo(UnsignedInteger.valueOf(60)) >= 0,
      "PaymentInterval must be at least 60 seconds."
    ));

    // 16. GracePeriod must be at least 60 seconds and not exceed PaymentInterval (if specified).
    gracePeriod().ifPresent(gp -> {
      Preconditions.checkArgument(
        gp.compareTo(UnsignedInteger.valueOf(60)) >= 0,
        "GracePeriod must be at least 60 seconds."
      );
      paymentInterval().ifPresent(interval -> Preconditions.checkArgument(
        gp.compareTo(interval) <= 0,
        "GracePeriod must not exceed PaymentInterval."
      ));
    });
  }
}
