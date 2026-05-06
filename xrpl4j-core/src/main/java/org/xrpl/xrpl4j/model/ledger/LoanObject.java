package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.LoanFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanData;

import java.util.Optional;

/**
 * Represents a Loan ledger object, which describes a single loan instance.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanObject.class)
@JsonDeserialize(as = ImmutableLoanObject.class)
@Beta
public interface LoanObject extends LedgerObject {

  /**
   * Construct a {@code LoanObject} builder.
   *
   * @return An {@link ImmutableLoanObject.Builder}.
   */
  static ImmutableLoanObject.Builder builder() {
    return ImmutableLoanObject.builder();
  }

  /**
   * The type of ledger object, which will always be "Loan" in this case.
   *
   * @return Always returns {@link LedgerEntryType#LOAN}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.LOAN;
  }

  /**
   * Ledger object flags.
   *
   * @return A {@link LoanFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default LoanFlags flags() {
    return LoanFlags.UNSET;
  }

  /**
   * The ID of the transaction that last modified this object.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The ledger sequence containing the transaction that last modified this object.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The sequence number of the Loan.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("LoanSequence")
  UnsignedInteger loanSequence();

  /**
   * Identifies the page where this item is referenced in the {@code Borrower} owner's directory.
   *
   * @return A {@link String}.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * Identifies the page where this item is referenced in the {@code LoanBroker}'s owner directory.
   *
   * @return A {@link String}.
   */
  @JsonProperty("LoanBrokerNode")
  String loanBrokerNode();

  /**
   * The ID of the {@code LoanBroker} associated with this Loan Instance.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Hash256 loanBrokerId();

  /**
   * The address of the account that is the borrower.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Borrower")
  Address borrower();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when the Loan is created.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("LoanOriginationFee")
  @Value.Default
  default Amount loanOriginationFee() {
    return Amount.ZERO;
  }

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} with every Loan payment.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("LoanServiceFee")
  @Value.Default
  default Amount loanServiceFee() {
    return Amount.ZERO;
  }

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when a payment is late.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("LatePaymentFee")
  @Value.Default
  default Amount latePaymentFee() {
    return Amount.ZERO;
  }

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when a full payment is made.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("ClosePaymentFee")
  @Value.Default
  default Amount closePaymentFee() {
    return Amount.ZERO;
  }

  /**
   * A fee charged on overpayments in 1/10th basis points. Valid values are between 0 and 100000 inclusive
   * (0 - 100%).
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("OverpaymentFee")
  @Value.Default
  default UnsignedInteger overpaymentFee() {
    return UnsignedInteger.ZERO;
  }

  /**
   * Annualized interest rate of the Loan in 1/10th basis points.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("InterestRate")
  @Value.Default
  default UnsignedInteger interestRate() {
    return UnsignedInteger.ZERO;
  }

  /**
   * A premium is added to the interest rate for late payments in 1/10th basis points. Valid values are
   * between 0 and 100000 inclusive (0 - 100%).
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("LateInterestRate")
  @Value.Default
  default UnsignedInteger lateInterestRate() {
    return UnsignedInteger.ZERO;
  }

  /**
   * An interest rate charged for repaying the Loan early in 1/10th basis points. Valid values are between 0
   * and 100000 inclusive (0 - 100%).
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("CloseInterestRate")
  @Value.Default
  default UnsignedInteger closeInterestRate() {
    return UnsignedInteger.ZERO;
  }

  /**
   * An interest rate charged on overpayments in 1/10th basis points. Valid values are between 0 and 100000
   * inclusive (0 - 100%).
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("OverpaymentInterestRate")
  @Value.Default
  default UnsignedInteger overpaymentInterestRate() {
    return UnsignedInteger.ZERO;
  }

  /**
   * The timestamp of when the Loan started in Ripple Epoch.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("StartDate")
  UnsignedInteger startDate();

  /**
   * Number of seconds between Loan payments.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("PaymentInterval")
  UnsignedInteger paymentInterval();

  /**
   * The number of seconds after the Loan's Payment Due Date that the Loan can be Defaulted.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("GracePeriod")
  @Value.Default
  default UnsignedInteger gracePeriod() {
    return UnsignedInteger.ZERO;
  }

  /**
   * The timestamp of when the previous payment was made in Ripple Epoch.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("PreviousPaymentDueDate")
  Optional<UnsignedInteger> previousPaymentDueDate();

  /**
   * The timestamp of when the next payment is due in Ripple Epoch.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("NextPaymentDueDate")
  Optional<UnsignedInteger> nextPaymentDueDate();

  /**
   * The number of payments remaining on the Loan.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("PaymentRemaining")
  @Value.Default
  default UnsignedInteger paymentRemaining() {
    return UnsignedInteger.ZERO;
  }

  /**
   * The total outstanding value of the Loan, including all fees and interest.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("TotalValueOutstanding")
  @Value.Default
  default Amount totalValueOutstanding() {
    return Amount.ZERO;
  }

  /**
   * The principal amount that the Borrower still owes.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("PrincipalOutstanding")
  @Value.Default
  default Amount principalOutstanding() {
    return Amount.ZERO;
  }

  /**
   * The remaining Management Fee owed to the LoanBroker.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("ManagementFeeOutstanding")
  @Value.Default
  default Amount managementFeeOutstanding() {
    return Amount.ZERO;
  }

  /**
   * The calculated periodic payment amount for each payment interval.
   *
   * @return An {@link Amount}.
   */
  @JsonProperty("PeriodicPayment")
  Amount periodicPayment();

  /**
   * The scale factor that ensures all computed amounts are rounded to the same number of decimal places. It
   * is determined based on the total loan value at creation time.
   *
   * @return An {@link Integer}.
   */
  @JsonProperty("LoanScale")
  @Value.Default
  default Integer loanScale() {
    return 0;
  }

  /**
   * Arbitrary metadata in hex format. The field is limited to 256 bytes.
   *
   * @return An optionally-present {@link LoanData}.
   */
  @JsonProperty("Data")
  Optional<LoanData> data();

  /**
   * The unique ID of this {@link LoanObject}.
   *
   * @return A {@link Hash256}.
   */
  Hash256 index();

}
