package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.LoanFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanData;

import java.util.Optional;

/**
 * Represents a Loan ledger object as it appears in transaction metadata.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableMetaLoanObject.class)
@JsonDeserialize(as = ImmutableMetaLoanObject.class)
@Beta
public interface MetaLoanObject extends MetaLedgerObject {

  /**
   * Ledger object flags.
   *
   * @return An optionally-present {@link LoanFlags}.
   */
  @JsonProperty("Flags")
  Optional<LoanFlags> flags();

  /**
   * The ID of the transaction that last modified this object.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The ledger sequence containing the transaction that last modified this object.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<UnsignedInteger> previousTransactionLedgerSequence();

  /**
   * The sequence number of the Loan.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("LoanSequence")
  Optional<UnsignedInteger> loanSequence();

  /**
   * Identifies the page where this item is referenced in the {@code Borrower} owner's directory.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * Identifies the page where this item is referenced in the {@code LoanBroker}'s owner directory.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("LoanBrokerNode")
  Optional<String> loanBrokerNode();

  /**
   * The ID of the {@code LoanBroker} associated with this Loan Instance.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Optional<Hash256> loanBrokerId();

  /**
   * The address of the account that is the borrower.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Borrower")
  Optional<Address> borrower();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when the Loan is created.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("LoanOriginationFee")
  Optional<Amount> loanOriginationFee();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} with every Loan payment.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("LoanServiceFee")
  Optional<Amount> loanServiceFee();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when a payment is late.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("LatePaymentFee")
  Optional<Amount> latePaymentFee();

  /**
   * A nominal funds amount paid to the {@code LoanBroker.Owner} when a full payment is made.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("ClosePaymentFee")
  Optional<Amount> closePaymentFee();

  /**
   * A fee charged on overpayments in 1/10th basis points. Valid values are between 0 and 100000 inclusive
   * (0 - 100%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("OverpaymentFee")
  Optional<UnsignedInteger> overpaymentFee();

  /**
   * Annualized interest rate of the Loan in 1/10th basis points.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("InterestRate")
  Optional<UnsignedInteger> interestRate();

  /**
   * A premium is added to the interest rate for late payments in 1/10th basis points. Valid values are
   * between 0 and 100000 inclusive (0 - 100%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("LateInterestRate")
  Optional<UnsignedInteger> lateInterestRate();

  /**
   * An interest rate charged for repaying the Loan early in 1/10th basis points. Valid values are between 0
   * and 100000 inclusive (0 - 100%).
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
   * The timestamp of when the Loan started in Ripple Epoch.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("StartDate")
  Optional<UnsignedInteger> startDate();

  /**
   * Number of seconds between Loan payments.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("PaymentInterval")
  Optional<UnsignedInteger> paymentInterval();

  /**
   * The number of seconds after the Loan's Payment Due Date that the Loan can be Defaulted.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("GracePeriod")
  Optional<UnsignedInteger> gracePeriod();

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
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("PaymentRemaining")
  Optional<UnsignedInteger> paymentRemaining();

  /**
   * The total outstanding value of the Loan, including all fees and interest.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("TotalValueOutstanding")
  Optional<Amount> totalValueOutstanding();

  /**
   * The principal amount that the Borrower still owes.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("PrincipalOutstanding")
  Optional<Amount> principalOutstanding();

  /**
   * The remaining Management Fee owed to the LoanBroker.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("ManagementFeeOutstanding")
  Optional<Amount> managementFeeOutstanding();

  /**
   * The calculated periodic payment amount for each payment interval.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("PeriodicPayment")
  Optional<Amount> periodicPayment();

  /**
   * The scale factor that ensures all computed amounts are rounded to the same number of decimal places. It
   * is determined based on the total loan value at creation time.
   *
   * @return An optionally-present {@link Integer}.
   */
  @JsonProperty("LoanScale")
  Optional<Integer> loanScale();

  /**
   * Arbitrary metadata in hex format. The field is limited to 256 bytes.
   *
   * @return An optionally-present {@link LoanData}.
   */
  @JsonProperty("Data")
  Optional<LoanData> data();

}
