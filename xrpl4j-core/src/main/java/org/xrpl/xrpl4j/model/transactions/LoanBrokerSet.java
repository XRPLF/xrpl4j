package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Create or update a LoanBroker.
 *
 * <p>This class will be marked {@link Beta} until the LendingProtocol amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLoanBrokerSet.class)
@JsonDeserialize(as = ImmutableLoanBrokerSet.class)
@Beta
public interface LoanBrokerSet extends Transaction {

  /**
   * Construct a {@code LoanBrokerSet} builder.
   *
   * @return An {@link ImmutableLoanBrokerSet.Builder}.
   */
  static ImmutableLoanBrokerSet.Builder builder() {
    return ImmutableLoanBrokerSet.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link LoanBrokerSet}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The Vault ID that the Lending Protocol will use to access liquidity.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("VaultID")
  Hash256 vaultId();

  /**
   * The Loan Broker ID that the transaction is modifying.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("LoanBrokerID")
  Optional<Hash256> loanBrokerId();

  /**
   * Arbitrary metadata in hex format. The field is limited to 256 bytes.
   *
   * @return An optionally-present {@link LoanBrokerData}.
   */
  @JsonProperty("Data")
  Optional<LoanBrokerData> data();

  /**
   * The 1/10th basis point fee charged by the Lending Protocol Owner. Valid values are between 0 and 10000
   * inclusive (0% - 10%).
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("ManagementFeeRate")
  Optional<UnsignedInteger> managementFeeRate();

  /**
   * The maximum amount the protocol can owe the Vault. The default value of 0 means there is no limit to the
   * debt. Must not be negative.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("DebtMaximum")
  Optional<AssetAmount> debtMaximum();

  /**
   * The 1/10th basis point {@code DebtTotal} that the first-loss capital must cover. Valid values are between 0
   * and 100000 inclusive.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("CoverRateMinimum")
  Optional<UnsignedInteger> coverRateMinimum();

  /**
   * The 1/10th basis point of minimum required first-loss capital liquidated to cover a Loan default. Valid
   * values are between 0 and 100000 inclusive.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("CoverRateLiquidation")
  Optional<UnsignedInteger> coverRateLiquidation();

  /**
   * Validates LoanBrokerSet data verification preconditions.
   */
  @Value.Check
  default void check() {
    // VaultID must not be all zeros
    Preconditions.checkArgument(
      !vaultId().equals(Hash256.ZERO),
      "VaultID must not be zero."
    );

    // LoanBrokerID, if present, must not be all zeros
    loanBrokerId().ifPresent(id -> Preconditions.checkArgument(
      !id.equals(Hash256.ZERO),
      "LoanBrokerID must not be zero."
    ));

    // When updating (LoanBrokerID present), cannot modify fixed fields
    loanBrokerId().ifPresent(id -> {
      Preconditions.checkArgument(
        !managementFeeRate().isPresent(),
        "ManagementFeeRate cannot be modified when updating an existing LoanBroker."
      );
      Preconditions.checkArgument(
        !coverRateMinimum().isPresent(),
        "CoverRateMinimum cannot be modified when updating an existing LoanBroker."
      );
      Preconditions.checkArgument(
        !coverRateLiquidation().isPresent(),
        "CoverRateLiquidation cannot be modified when updating an existing LoanBroker."
      );
    });

    // ManagementFeeRate must be between 0 and 10000 inclusive (0% to 10% in 1/10 bps)
    managementFeeRate().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(10000)) <= 0,
      "ManagementFeeRate must be between 0 and 10000 inclusive."
    ));

    // DebtMaximum must not be negative
    debtMaximum().ifPresent(debt -> Preconditions.checkArgument(
      !debt.isNegative(),
      "DebtMaximum must not be negative."
    ));

    // CoverRateMinimum must be between 0 and 100000 inclusive (0% to 100% in 1/10 bps)
    coverRateMinimum().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(100000)) <= 0,
      "CoverRateMinimum must be between 0 and 100000 inclusive."
    ));

    // CoverRateLiquidation must be between 0 and 100000 inclusive (0% to 100% in 1/10 bps)
    coverRateLiquidation().ifPresent(rate -> Preconditions.checkArgument(
      rate.compareTo(UnsignedInteger.valueOf(100000)) <= 0,
      "CoverRateLiquidation must be between 0 and 100000 inclusive."
    ));

    // CoverRateMinimum and CoverRateLiquidation must both be zero or both be non-zero
    boolean minIsZero = !coverRateMinimum().isPresent() ||
      coverRateMinimum().get().equals(UnsignedInteger.ZERO);
    boolean liqIsZero = !coverRateLiquidation().isPresent() ||
      coverRateLiquidation().get().equals(UnsignedInteger.ZERO);
    Preconditions.checkArgument(
      minIsZero == liqIsZero,
      "CoverRateMinimum and CoverRateLiquidation must both be zero or both be non-zero."
    );
  }
}
