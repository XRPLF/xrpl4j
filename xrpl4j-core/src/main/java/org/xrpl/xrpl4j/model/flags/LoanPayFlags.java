package org.xrpl.xrpl4j.model.flags;

import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.model.transactions.LoanPay;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link LoanPay} transactions.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
public class LoanPayFlags extends TransactionFlags {

  /**
   * Constant {@link LoanPayFlags} for the {@code tfLoanOverpayment} flag.
   */
  public static final LoanPayFlags LOAN_OVERPAYMENT = new LoanPayFlags(0x00010000L);

  /**
   * Constant {@link LoanPayFlags} for the {@code tfLoanFullPayment} flag.
   */
  public static final LoanPayFlags LOAN_FULL_PAYMENT = new LoanPayFlags(0x00020000L);

  /**
   * Constant {@link LoanPayFlags} for the {@code tfLoanLatePayment} flag.
   */
  public static final LoanPayFlags LOAN_LATE_PAYMENT = new LoanPayFlags(0x00040000L);

  private LoanPayFlags(long value) {
    super(value);
  }

  private LoanPayFlags() {
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Construct {@link LoanPayFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link LoanPayFlags}.
   *
   * @return New {@link LoanPayFlags}.
   */
  public static LoanPayFlags of(long value) {
    return new LoanPayFlags(value);
  }

  private static LoanPayFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfLoanOverpayment,
    boolean tfLoanFullPayment,
    boolean tfLoanLatePayment
  ) {
    long value = Flags.of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfLoanOverpayment ? LOAN_OVERPAYMENT : UNSET,
      tfLoanFullPayment ? LOAN_FULL_PAYMENT : UNSET,
      tfLoanLatePayment ? LOAN_LATE_PAYMENT : UNSET
    ).getValue();
    return new LoanPayFlags(value);
  }

  /**
   * Construct an empty instance of {@link LoanPayFlags}. Transactions with empty flags will not be serialized with
   * a {@code Flags} field.
   *
   * @return An empty {@link LoanPayFlags}.
   */
  public static LoanPayFlags empty() {
    return new LoanPayFlags();
  }

  /**
   * Indicates that remaining payment amount should be treated as an overpayment.
   *
   * @return {@code true} if {@code tfLoanOverpayment} is set, otherwise {@code false}.
   */
  public boolean tfLoanOverpayment() {
    return this.isSet(LoanPayFlags.LOAN_OVERPAYMENT);
  }

  /**
   * Indicates that the borrower is making a full early repayment.
   *
   * @return {@code true} if {@code tfLoanFullPayment} is set, otherwise {@code false}.
   */
  public boolean tfLoanFullPayment() {
    return this.isSet(LoanPayFlags.LOAN_FULL_PAYMENT);
  }

  /**
   * Indicates that the borrower is making a late loan payment.
   *
   * @return {@code true} if {@code tfLoanLatePayment} is set, otherwise {@code false}.
   */
  public boolean tfLoanLatePayment() {
    return this.isSet(LoanPayFlags.LOAN_LATE_PAYMENT);
  }

  /**
   * A builder class for {@link LoanPayFlags} flags.
   */
  public static class Builder {

    private boolean tfLoanOverpayment = false;
    private boolean tfLoanFullPayment = false;
    private boolean tfLoanLatePayment = false;

    /**
     * Set {@code tfLoanOverpayment} to the given value.
     *
     * @param tfLoanOverpayment A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfLoanOverpayment(boolean tfLoanOverpayment) {
      this.tfLoanOverpayment = tfLoanOverpayment;
      return this;
    }

    /**
     * Set {@code tfLoanFullPayment} to the given value.
     *
     * @param tfLoanFullPayment A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfLoanFullPayment(boolean tfLoanFullPayment) {
      this.tfLoanFullPayment = tfLoanFullPayment;
      return this;
    }

    /**
     * Set {@code tfLoanLatePayment} to the given value.
     *
     * @param tfLoanLatePayment A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfLoanLatePayment(boolean tfLoanLatePayment) {
      this.tfLoanLatePayment = tfLoanLatePayment;
      return this;
    }

    /**
     * Build a new {@link LoanPayFlags} from the current boolean values.
     *
     * @return A new {@link LoanPayFlags}.
     */
    public LoanPayFlags build() {
      return LoanPayFlags.of(
        true,
        tfLoanOverpayment,
        tfLoanFullPayment,
        tfLoanLatePayment
      );
    }
  }
}
