package org.xrpl.xrpl4j.model.flags;

import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.model.transactions.LoanManage;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link LoanManage} transactions.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
public class LoanManageFlags extends TransactionFlags {

  /**
   * Constant {@link LoanManageFlags} for the {@code tfLoanDefault} flag.
   */
  public static final LoanManageFlags LOAN_DEFAULT = new LoanManageFlags(0x00010000L);

  /**
   * Constant {@link LoanManageFlags} for the {@code tfLoanImpair} flag.
   */
  public static final LoanManageFlags LOAN_IMPAIR = new LoanManageFlags(0x00020000L);

  /**
   * Constant {@link LoanManageFlags} for the {@code tfLoanUnimpair} flag.
   */
  public static final LoanManageFlags LOAN_UNIMPAIR = new LoanManageFlags(0x00040000L);

  private LoanManageFlags(long value) {
    super(value);
  }

  private LoanManageFlags() {
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
   * Construct {@link LoanManageFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link LoanManageFlags}.
   *
   * @return New {@link LoanManageFlags}.
   */
  public static LoanManageFlags of(long value) {
    return new LoanManageFlags(value);
  }

  private static LoanManageFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfLoanDefault,
    boolean tfLoanImpair,
    boolean tfLoanUnimpair
  ) {
    long value = Flags.of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfLoanDefault ? LOAN_DEFAULT : UNSET,
      tfLoanImpair ? LOAN_IMPAIR : UNSET,
      tfLoanUnimpair ? LOAN_UNIMPAIR : UNSET
    ).getValue();
    return new LoanManageFlags(value);
  }

  /**
   * Construct an empty instance of {@link LoanManageFlags}. Transactions with empty flags will not be serialized
   * with a {@code Flags} field.
   *
   * @return An empty {@link LoanManageFlags}.
   */
  public static LoanManageFlags empty() {
    return new LoanManageFlags();
  }

  /**
   * Indicates that the Loan should be defaulted.
   *
   * @return {@code true} if {@code tfLoanDefault} is set, otherwise {@code false}.
   */
  public boolean tfLoanDefault() {
    return this.isSet(LoanManageFlags.LOAN_DEFAULT);
  }

  /**
   * Indicates that the Loan should be impaired.
   *
   * @return {@code true} if {@code tfLoanImpair} is set, otherwise {@code false}.
   */
  public boolean tfLoanImpair() {
    return this.isSet(LoanManageFlags.LOAN_IMPAIR);
  }

  /**
   * Indicates that the Loan should be un-impaired.
   *
   * @return {@code true} if {@code tfLoanUnimpair} is set, otherwise {@code false}.
   */
  public boolean tfLoanUnimpair() {
    return this.isSet(LoanManageFlags.LOAN_UNIMPAIR);
  }

  /**
   * A builder class for {@link LoanManageFlags} flags.
   */
  public static class Builder {

    private boolean tfLoanDefault = false;
    private boolean tfLoanImpair = false;
    private boolean tfLoanUnimpair = false;

    /**
     * Set {@code tfLoanDefault} to the given value.
     *
     * @param tfLoanDefault A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfLoanDefault(boolean tfLoanDefault) {
      this.tfLoanDefault = tfLoanDefault;
      return this;
    }

    /**
     * Set {@code tfLoanImpair} to the given value.
     *
     * @param tfLoanImpair A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfLoanImpair(boolean tfLoanImpair) {
      this.tfLoanImpair = tfLoanImpair;
      return this;
    }

    /**
     * Set {@code tfLoanUnimpair} to the given value.
     *
     * @param tfLoanUnimpair A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfLoanUnimpair(boolean tfLoanUnimpair) {
      this.tfLoanUnimpair = tfLoanUnimpair;
      return this;
    }

    /**
     * Build a new {@link LoanManageFlags} from the current boolean values.
     *
     * @return A new {@link LoanManageFlags}.
     */
    public LoanManageFlags build() {
      return LoanManageFlags.of(
        true,
        tfLoanDefault,
        tfLoanImpair,
        tfLoanUnimpair
      );
    }
  }
}
