package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.LoanManageFlags;

/**
 * Default, impair, or unimpair a Loan.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanManage.class)
@JsonDeserialize(as = ImmutableLoanManage.class)
@Beta
public interface LoanManage extends Transaction {

  /**
   * Construct a {@code LoanManage} builder.
   *
   * @return An {@link ImmutableLoanManage.Builder}.
   */
  static ImmutableLoanManage.Builder builder() {
    return ImmutableLoanManage.builder();
  }

  /**
   * Set of {@link LoanManageFlags}s for this {@link LoanManage}.
   *
   * @return A {@link LoanManageFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default LoanManageFlags flags() {
    return LoanManageFlags.empty();
  }

  /**
   * The ID of the Loan object to be updated.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanID")
  Hash256 loanId();

  /**
   * Validates LoanManage data verification preconditions per the Lending Protocol spec section 3.10.4.1.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      !loanId().equals(Hash256.ZERO),
      "LoanID must not be zero."
    );

    // tfLoanDefault, tfLoanImpair, tfLoanUnimpair are mutually exclusive.
    int flagCount = (flags().tfLoanDefault() ? 1 : 0) +
      (flags().tfLoanImpair() ? 1 : 0) +
      (flags().tfLoanUnimpair() ? 1 : 0);
    Preconditions.checkArgument(
      flagCount <= 1,
      "Only one of tfLoanDefault, tfLoanImpair, or tfLoanUnimpair may be set."
    );
  }
}
