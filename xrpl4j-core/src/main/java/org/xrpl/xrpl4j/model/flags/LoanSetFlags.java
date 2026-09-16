package org.xrpl.xrpl4j.model.flags;

import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.model.transactions.LoanSet;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link LoanSet} transactions.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
public class LoanSetFlags extends TransactionFlags {

  /**
   * Constant {@link LoanSetFlags} for the {@code tfLoanOverpayment} flag.
   */
  public static final LoanSetFlags LOAN_OVERPAYMENT = new LoanSetFlags(0x00010000L);

  private LoanSetFlags(long value) {
    super(value);
  }

  private LoanSetFlags() {
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
   * Construct {@link LoanSetFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link LoanSetFlags}.
   *
   * @return New {@link LoanSetFlags}.
   */
  public static LoanSetFlags of(long value) {
    return new LoanSetFlags(value);
  }

  private static LoanSetFlags of(boolean tfFullyCanonicalSig, boolean tfLoanOverpayment) {
    long value = Flags.of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfLoanOverpayment ? LOAN_OVERPAYMENT : UNSET
    ).getValue();
    return new LoanSetFlags(value);
  }

  /**
   * Construct an empty instance of {@link LoanSetFlags}. Transactions with empty flags will not be serialized with
   * a {@code Flags} field.
   *
   * @return An empty {@link LoanSetFlags}.
   */
  public static LoanSetFlags empty() {
    return new LoanSetFlags();
  }

  /**
   * Indicates that the loan supports overpayments.
   *
   * @return {@code true} if {@code tfLoanOverpayment} is set, otherwise {@code false}.
   */
  public boolean tfLoanOverpayment() {
    return this.isSet(LoanSetFlags.LOAN_OVERPAYMENT);
  }

  /**
   * A builder class for {@link LoanSetFlags} flags.
   */
  public static class Builder {

    private boolean tfLoanOverpayment = false;

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
     * Build a new {@link LoanSetFlags} from the current boolean values.
     *
     * @return A new {@link LoanSetFlags}.
     */
    public LoanSetFlags build() {
      return LoanSetFlags.of(true, tfLoanOverpayment);
    }
  }
}
