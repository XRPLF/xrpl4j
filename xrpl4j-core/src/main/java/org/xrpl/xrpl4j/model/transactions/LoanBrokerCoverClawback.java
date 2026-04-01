package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.AddressConstants;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Clawback first-loss capital from a LoanBroker. Must be submitted by the asset issuer.
 * The transaction can only clawback funds up to the minimum cover required for the current loans.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanBrokerCoverClawback.class)
@JsonDeserialize(as = ImmutableLoanBrokerCoverClawback.class)
@Beta
public interface LoanBrokerCoverClawback extends Transaction {

  /**
   * Construct a {@code LoanBrokerCoverClawback} builder.
   *
   * @return An {@link ImmutableLoanBrokerCoverClawback.Builder}.
   */
  static ImmutableLoanBrokerCoverClawback.Builder builder() {
    return ImmutableLoanBrokerCoverClawback.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link LoanBrokerCoverClawback}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The Loan Broker ID from which to clawback First-Loss Capital.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Optional<Hash256> loanBrokerId();

  /**
   * The First-Loss Capital amount to clawback. If the amount is 0 or not provided, clawback funds up to
   * {@code LoanBroker.DebtTotal * LoanBroker.CoverRateMinimum}.
   *
   * @return An optionally-present {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

  /**
   * Validates LoanBrokerCoverClawback data verification preconditions.
   */
  @Value.Check
  default void check() {
    // 1. At least one of LoanBrokerID or Amount must be specified.
    Preconditions.checkArgument(
      loanBrokerId().isPresent() || amount().isPresent(),
      "At least one of LoanBrokerID or Amount must be specified."
    );

    // 2. LoanBrokerID, if present, must not be zero.
    loanBrokerId().ifPresent(id -> Preconditions.checkArgument(
      !id.value().equals("0000000000000000000000000000000000000000000000000000000000000000"),
      "LoanBrokerID must not be zero."
    ));

    amount().ifPresent(amt -> {
      // 3. Amount must not be negative (zero is valid — means clawback up to minimum cover).
      Preconditions.checkArgument(
        !amt.isNegative(),
        "Amount must not be negative."
      );

      // 4. Amount must not be XRP (cannot clawback native asset).
      Preconditions.checkArgument(
        !(amt instanceof XrpCurrencyAmount),
        "Amount must not be XRP."
      );

      // 6. LoanBrokerID absent + MPT amount is invalid.
      if (!loanBrokerId().isPresent() && amt instanceof MptCurrencyAmount) {
        throw new IllegalArgumentException(
          "LoanBrokerID must be specified when Amount is an MPT."
        );
      }

      // 7. LoanBrokerID absent + IOU with issuer == submitter or issuer == ACCOUNT_ZERO is invalid.
      if (!loanBrokerId().isPresent() && amt instanceof IssuedCurrencyAmount) {
        IssuedCurrencyAmount iou = (IssuedCurrencyAmount) amt;
        Preconditions.checkArgument(
          !iou.issuer().equals(account()),
          "LoanBrokerID must be specified when Amount issuer is the submitter."
        );
        Preconditions.checkArgument(
          !iou.issuer().equals(AddressConstants.ACCOUNT_ZERO),
          "LoanBrokerID must be specified when Amount issuer is the zero account."
        );
      }
    });
  }
}
