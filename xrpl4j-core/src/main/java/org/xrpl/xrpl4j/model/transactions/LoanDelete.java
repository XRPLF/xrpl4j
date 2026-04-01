package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Delete a fully-paid or defaulted Loan.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanDelete.class)
@JsonDeserialize(as = ImmutableLoanDelete.class)
@Beta
public interface LoanDelete extends Transaction {

  /**
   * Construct a {@code LoanDelete} builder.
   *
   * @return An {@link ImmutableLoanDelete.Builder}.
   */
  static ImmutableLoanDelete.Builder builder() {
    return ImmutableLoanDelete.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link LoanDelete}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The ID of the Loan object to be deleted.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("LoanID")
  Hash256 loanId();

  /**
   * Validates LoanDelete data verification preconditions.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      !loanId().value().equals(
        "0000000000000000000000000000000000000000000000000000000000000000"
      ),
      "LoanID must not be zero."
    );
  }
}
