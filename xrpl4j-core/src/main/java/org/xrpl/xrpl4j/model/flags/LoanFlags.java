package org.xrpl.xrpl4j.model.flags;

import com.google.common.annotations.Beta;

/**
 * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.LoanObject}s.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
public class LoanFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final LoanFlags UNSET = new LoanFlags(0);

  /**
   * Constant {@link LoanFlags} for the {@code lsfLoanDefault} flag.
   */
  public static final LoanFlags LOAN_DEFAULT = new LoanFlags(0x00010000);

  /**
   * Constant {@link LoanFlags} for the {@code lsfLoanImpaired} flag.
   */
  public static final LoanFlags LOAN_IMPAIRED = new LoanFlags(0x00020000);

  /**
   * Constant {@link LoanFlags} for the {@code lsfLoanOverpayment} flag.
   */
  public static final LoanFlags LOAN_OVERPAYMENT = new LoanFlags(0x00040000);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link LoanFlags}.
   */
  private LoanFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link LoanFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link LoanFlags}.
   *
   * @return New {@link LoanFlags}.
   */
  public static LoanFlags of(long value) {
    return new LoanFlags(value);
  }

  /**
   * If set, indicates the loan has been defaulted.
   *
   * @return {@code true} if {@code lsfLoanDefault} is set, otherwise {@code false}.
   */
  public boolean lsfLoanDefault() {
    return this.isSet(LoanFlags.LOAN_DEFAULT);
  }

  /**
   * If set, indicates the loan has been impaired.
   *
   * @return {@code true} if {@code lsfLoanImpaired} is set, otherwise {@code false}.
   */
  public boolean lsfLoanImpaired() {
    return this.isSet(LoanFlags.LOAN_IMPAIRED);
  }

  /**
   * If set, indicates the loan supports overpayments.
   *
   * @return {@code true} if {@code lsfLoanOverpayment} is set, otherwise {@code false}.
   */
  public boolean lsfLoanOverpayment() {
    return this.isSet(LoanFlags.LOAN_OVERPAYMENT);
  }

}
