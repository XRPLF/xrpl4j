package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Hash256;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.LoanObject} on ledger that can be used in a
 * {@link LedgerEntryRequestParams} to request a {@link org.xrpl.xrpl4j.model.ledger.LoanObject}.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableLoanLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableLoanLedgerEntryParams.class)
public interface LoanLedgerEntryParams {

  /**
   * Construct a {@code LoanLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableLoanLedgerEntryParams.Builder}.
   */
  static ImmutableLoanLedgerEntryParams.Builder builder() {
    return ImmutableLoanLedgerEntryParams.builder();
  }

  /**
   * The LoanBrokerID of the LoanBroker associated with the Loan.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("loan_broker_id")
  Hash256 loanBrokerId();

  /**
   * The LoanSequence of the Loan.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("loan_seq")
  UnsignedInteger loanSeq();

}
