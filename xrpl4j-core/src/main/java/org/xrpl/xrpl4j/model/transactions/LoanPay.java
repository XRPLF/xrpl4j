package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.LoanPayFlags;

/**
 * Make a payment on a Loan.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanPay.class)
@JsonDeserialize(as = ImmutableLoanPay.class)
@Beta
public interface LoanPay extends Transaction {

  /**
   * Construct a {@code LoanPay} builder.
   *
   * @return An {@link ImmutableLoanPay.Builder}.
   */
  static ImmutableLoanPay.Builder builder() {
    return ImmutableLoanPay.builder();
  }

  /**
   * Set of {@link LoanPayFlags}s for this {@link LoanPay}.
   *
   * @return A {@link LoanPayFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default LoanPayFlags flags() {
    return LoanPayFlags.empty();
  }

  /**
   * The ID of the Loan object to be paid to.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanID")
  Hash256 loanId();

  /**
   * The amount of funds to pay.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * Validates LoanPay data verification preconditions per the Lending Protocol spec section 3.11.4.1.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      !loanId().equals(Hash256.ZERO),
      "LoanID must not be zero."
    );

    Preconditions.checkArgument(
      !amount().isNegative() && !amount().isZero(),
      "Amount must be greater than zero."
    );

    // tfLoanLatePayment, tfLoanFullPayment, tfLoanOverpayment are mutually exclusive.
    int flagCount = (flags().tfLoanLatePayment() ? 1 : 0) +
      (flags().tfLoanFullPayment() ? 1 : 0) +
      (flags().tfLoanOverpayment() ? 1 : 0);
    Preconditions.checkArgument(
      flagCount <= 1,
      "Only one of tfLoanLatePayment, tfLoanFullPayment, or tfLoanOverpayment may be set."
    );
  }
}
