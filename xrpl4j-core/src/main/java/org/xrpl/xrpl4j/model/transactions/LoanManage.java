package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
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
}
