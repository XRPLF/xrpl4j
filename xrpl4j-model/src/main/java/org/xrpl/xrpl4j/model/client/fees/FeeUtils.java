package org.xrpl.xrpl4j.model.client.fees;

import static org.xrpl.xrpl4j.model.transactions.CurrencyAmount.MAX_XRP_IN_DROPS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.ledger.SignerListObject;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utils relating to XRPL fees.
 */
public class FeeUtils {

  private static final BigInteger MAX_UNSIGNED_LONG = UnsignedLong.MAX_VALUE.bigIntegerValue();

  private static final BigDecimal ONE_POINT_ONE = new BigDecimal("1.1");

  private static final BigDecimal ZERO_POINT_ONE = new BigDecimal("0.1");

  private static final BigInteger FIVE_HUNDRED = BigInteger.valueOf(500);

  private static final BigDecimal TWO = new BigDecimal(2);

  private static final BigDecimal THREE = new BigDecimal(3);

  private static final BigInteger FIFTEEN = BigInteger.valueOf(15);

  private static final BigInteger TEN_THOUSAND = BigInteger.valueOf(10000);

  private static final BigInteger ONE_THOUSAND = BigInteger.valueOf(1000);


  /**
   * Computes the fee necessary for a multisigned transaction.
   *
   * <p>The transaction cost of a multisigned transaction must be at least {@code (N + 1) * (the normal
   * transaction cost)}, where {@code N} is the number of signatures provided.
   *
   * @param currentLedgerFeeDrops The current ledger fee, represented as an {@link XrpCurrencyAmount}.
   * @param signerList            The {@link SignerListObject} containing the signers of the transaction.
   *
   * @return An {@link XrpCurrencyAmount} representing the multisig fee.
   */
  public static XrpCurrencyAmount computeMultiSigFee(final XrpCurrencyAmount currentLedgerFeeDrops,
    final SignerListObject signerList) {
    Objects.requireNonNull(currentLedgerFeeDrops);
    Objects.requireNonNull(signerList);

    return currentLedgerFeeDrops.times(
      XrpCurrencyAmount.of(UnsignedLong.valueOf(signerList.signerEntries().size() + 1)));
  }

  /**
   * Calculate a suggested fee to be used for submitting a transaction to the XRPL. The calculated value depends on the
   * current size of the job queue as compared to its total capacity.
   *
   * @param feeResult {@link FeeResult} object obtained by querying the ledger (e.g., via an `XrplClient#fee()` call).
   *
   * @return {@link XrpCurrencyAmount} value of the fee that should be used for the transaction.
   *
   * @see "https://xrpl.org/fee.html"
   * @see "https://github.com/XRPL-Labs/XUMM-App/blob/master/src/services/LedgerService.ts#L244"
   */
  public static XrpCurrencyAmount calculateFeeDynamically(final FeeResult feeResult) {
    Objects.requireNonNull(feeResult);

    final DecomposedFees decomposedFees = DecomposedFees.builder(feeResult);
    final BigDecimal queuePercentage = decomposedFees.queuePercentage();

    final XrpCurrencyAmount fee;
    if (queueIsEmpty(queuePercentage)) {
      // queue is empty
      final UnsignedLong feeLow = computeFeeLow(decomposedFees); // <- empty tx queue
      fee = XrpCurrencyAmount.ofDrops(feeLow);
    } else if (queueIsNotEmptyAndNotFull(queuePercentage)) {
      final UnsignedLong feeLow = computeFeeLow(decomposedFees); // <- empty tx queue
      final UnsignedLong feeMedium = computeFeeMedium(decomposedFees, feeLow); // <- in-between tx queue size
      fee = XrpCurrencyAmount.ofDrops(feeMedium);
    } else { // queue is full
      final UnsignedLong feeHigh = computeFeeHigh(decomposedFees); // <- full tx queue
      fee = XrpCurrencyAmount.ofDrops(feeHigh);
    }

    return fee;
  }

  /**
   * Calculate the lowest fee the user is able to pay if the queue is empty.
   *
   * @param decomposedFees A {@link DecomposedFees} that contains information about current XRPL fees.
   *
   * @return An {@link UnsignedLong} representing the `low` fee.
   */
  private static UnsignedLong computeFeeLow(final DecomposedFees decomposedFees) {
    Objects.requireNonNull(decomposedFees);

    final BigInteger adjustedMinimumFeeDrops = decomposedFees.adjustedMinimumFeeDrops();
    final BigInteger medianFee = decomposedFees.medianFeeDrops();
    final BigInteger openLedgerFee = decomposedFees.openLedgerFeeDrops();

    // Cap `feeLow` to the size of an UnsignedLong.
    return toUnsignedLongSafe(
      min(
        max(
          adjustedMinimumFeeDrops, // min fee * 1.50
          divideToBigInteger(max(medianFee, openLedgerFee), FIVE_HUNDRED)
        ),
        ONE_THOUSAND
      )
    );
  }

  /**
   * Compute the `medium` fee, which is the fee to use when the transaction queue is neither empty nor full.
   *
   * @param decomposedFees A {@link DecomposedFees} with precomputed values to use.
   * @param feeLow         The computed `low` fee as found in {@link #computeFeeLow(DecomposedFees)}.
   *
   * @return An {@link UnsignedLong} representing the `medium` fee.
   */
  private static UnsignedLong computeFeeMedium(final DecomposedFees decomposedFees, final UnsignedLong feeLow) {
    Objects.requireNonNull(decomposedFees);
    Objects.requireNonNull(feeLow);

    final BigInteger minimumFee = decomposedFees.adjustedMinimumFeeDrops();
    final BigDecimal minimumFeeBd = decomposedFees.adjustedMinimumFeeDropsAsBigDecimal();
    final BigDecimal medianFeeBd = decomposedFees.medianFeeDropsAsBigDecimal();
    final BigDecimal queuePercentage = decomposedFees.queuePercentage();

    final BigInteger possibleFeeMedium;
    if (FluentCompareTo.is(queuePercentage).greaterThan(ZERO_POINT_ONE)) {
      possibleFeeMedium = minimumFeeBd
        .add(medianFeeBd)
        .add(decomposedFees.openLedgerFeeDropsAsBigDecimal())
        .divide(THREE, 0, RoundingMode.HALF_UP)
        .toBigIntegerExact();
    } else { // 0 > `queuePercentage` < 0.1
      // Note: `computeFeeMedium` is not called if `queuePercentage` is 0, so we omit that check even though it's in
      // the original xumm code.
      possibleFeeMedium = max(minimumFee.multiply(BigInteger.TEN),
        minimumFeeBd.add(medianFeeBd).divide(TWO, 0, RoundingMode.HALF_UP).toBigIntegerExact());
    }

    // calculate the lowest fee the user is able to pay if there are txns in the queue
    final BigInteger feeMedium = min(possibleFeeMedium, feeLow.bigIntegerValue().multiply(FIFTEEN), TEN_THOUSAND);

    return toUnsignedLongSafe(feeMedium);
  }

  /**
   * Compute the `high` fee, which is the fee to use when the transaction queue is full.
   *
   * @param decomposedFees A {@link DecomposedFees} with precomputed values to use.
   *
   * @return An {@link UnsignedLong} representing the `high` fee.
   */
  private static UnsignedLong computeFeeHigh(final DecomposedFees decomposedFees) {
    Objects.requireNonNull(decomposedFees);

    final BigInteger minimumFee = decomposedFees.adjustedMinimumFeeDrops();
    final BigInteger medianFee = decomposedFees.medianFeeDrops();
    final BigInteger openLedgerFee = decomposedFees.openLedgerFeeDrops();

    final BigInteger highFee = min(
      max(minimumFee.multiply(BigInteger.TEN), multiplyToBigInteger(max(medianFee, openLedgerFee), ONE_POINT_ONE)),
      TEN_THOUSAND);
    return toUnsignedLongSafe(highFee);
  }

  /**
   * Helper method to determine if a transaction queue is empty by inspecting a `percent-full` measurement.
   *
   * @param queuePercentage A {@link BigDecimal} representing the percent-full value for a tx queue.
   *
   * @return {@code true} if the queue is empty; {@code false} otherwise.
   */
  @VisibleForTesting
  protected static boolean queueIsEmpty(final BigDecimal queuePercentage) {
    Objects.requireNonNull(queuePercentage);
    return FluentCompareTo.is(queuePercentage).lessThanOrEqualTo(BigDecimal.ZERO);
  }

  /**
   * Helper method to determine if a transaction queue is both non-empty, but not completely full, by inspecting a
   * `percent-full` measurement.
   *
   * @param queuePercentage A {@link BigDecimal} representing the percent-full value for a tx queue.
   *
   * @return {@code true} if the queue is empty; {@code false} otherwise.
   */
  @VisibleForTesting
  protected static boolean queueIsNotEmptyAndNotFull(final BigDecimal queuePercentage) {
    Objects.requireNonNull(queuePercentage);
    return FluentCompareTo.is(queuePercentage).betweenExclusive(BigDecimal.ZERO, BigDecimal.ONE);
  }

  /**
   * Convert a {@link BigInteger} into an {@link UnsignedLong} without overflowing. If the input overflows, return
   * {@link UnsignedLong#MAX_VALUE} instead.
   *
   * @param value A {@link BigInteger} to convert.
   *
   * @return An equivalent {@code value} as an {@link UnsignedLong}, or {@link UnsignedLong#MAX_VALUE} if the input
   *   would overflow during conversion.
   */
  // TODO: Move to MathUtils once all v3 modules are condensed and MathUtils is accessible.
  @VisibleForTesting
  static UnsignedLong toUnsignedLongSafe(final BigInteger value) {
    Objects.requireNonNull(value);
    return UnsignedLong.valueOf(min(value, MAX_UNSIGNED_LONG));
  }

  /**
   * Pick the smaller of the two supplied inputs.
   *
   * @param input1      A {@link BigInteger} to potentially choose as the min (i.e., smallest) value.
   * @param otherInputs A potentially empty array of {@link BigInteger}'s to compare and potentially choose from.
   *
   * @return The smallest value of any supplied inputs, or {@code input1} if that is the only supplied input.
   */
  // TODO: Move to MathUtils once all v3 modules are condensed and MathUtils is accessible.
  @VisibleForTesting
  static BigInteger min(final BigInteger input1, final BigInteger... otherInputs) {
    Objects.requireNonNull(input1);
    Objects.requireNonNull(otherInputs);

    return Arrays.stream(otherInputs).min(BigInteger::compareTo).orElse(input1).min(input1);
  }

  /**
   * Pick the larger of the two supplied inputs.
   *
   * @param input1      A {@link BigInteger} to potentially choose as the max (i.e., largest) value.
   * @param otherInputs A potentially empty array of {@link BigInteger}'s to compare and potentially choose from.
   *
   * @return The largest value of any supplied inputs, or {@code input1} if that is the only supplied input.
   */
  // TODO: Move to MathUtils once all v3 modules are condensed and MathUtils is accessible.
  @VisibleForTesting
  static BigInteger max(final BigInteger input1, final BigInteger... otherInputs) {
    Objects.requireNonNull(input1);
    Objects.requireNonNull(otherInputs);

    return Arrays.stream(otherInputs).max(BigInteger::compareTo).orElse(input1).max(input1);
  }

  /**
   * Divides two {@link BigDecimal} numbers and then converts the result into a rounded {@link BigInteger}.
   *
   * @param numerator   A {@link BigInteger} numerator for purposes of division.
   * @param denominator A {@link BigInteger} denominator for purposes of division.
   *
   * @return A {@link BigInteger} result.
   */
  @VisibleForTesting
  static BigInteger divideToBigInteger(final BigDecimal numerator, final BigDecimal denominator) {
    Objects.requireNonNull(numerator);
    Objects.requireNonNull(denominator);
    Preconditions.checkArgument(FluentCompareTo.is(denominator).greaterThan(BigDecimal.ZERO));
    return numerator.divide(denominator, 0, RoundingMode.HALF_UP).toBigIntegerExact();
  }

  /**
   * Divides two {@link BigInteger} numbers and then converts the result into a rounded {@link BigInteger}.
   *
   * @param numerator   A {@link BigInteger} numerator for purposes of division.
   * @param denominator A {@link BigInteger} denominator for purposes of division.
   *
   * @return A {@link BigInteger} result.
   */
  @VisibleForTesting
  static BigInteger divideToBigInteger(final BigInteger numerator, final BigInteger denominator) {
    return divideToBigInteger(new BigDecimal(numerator), new BigDecimal(denominator));
  }

  /**
   * Multiply input1 {@link BigInteger} by input1 {@link BigDecimal} and then return the result as input1 rounded
   * {@link BigInteger}.
   *
   * @param input1 The first {@link BigInteger}.
   * @param input2 The second {@link BigInteger}.
   *
   * @return The multiplied amount.
   */
  @VisibleForTesting
  static BigInteger multiplyToBigInteger(final BigInteger input1, final BigDecimal input2) {
    Objects.requireNonNull(input1);
    Objects.requireNonNull(input2);
    return new BigDecimal(input1).multiply(input2).setScale(0, RoundingMode.HALF_UP).toBigIntegerExact();
  }

  /**
   * Helper object that exists solely to aid fee calculation so that BigInteger/BigDecimal objects don't have to be
   * created more than once per call, and to put data into a state that simplifies fee calculation logic.
   */
  @Immutable
  interface DecomposedFees {

    BigDecimal ONE_POINT_FIVE = new BigDecimal("1.5");
    BigInteger MAX_XRP_IN_DROPS_BIG_INT = BigInteger.valueOf(MAX_XRP_IN_DROPS);

    /**
     * Build a new instance of {@link DecomposedFees} from the supplied input.
     *
     * @param feeResult A {@link FeeResult} to use as input.
     *
     * @return A {@link DecomposedFees}.
     */
    static DecomposedFees builder(final FeeResult feeResult) {
      Objects.requireNonNull(feeResult);

      final BigDecimal currentQueueSize = new BigDecimal(feeResult.currentQueueSize().bigIntegerValue());
      final BigDecimal maxQueueSize = feeResult.maxQueueSize().map(UnsignedInteger::bigIntegerValue)
        .map(BigDecimal::new).orElse(new BigDecimal(5000)); // Arbitrary value, but should generally be present.
      // Don't divide by 0
      final BigDecimal queuePercentage = FluentCompareTo.is(currentQueueSize).equalTo(BigDecimal.ZERO) ? BigDecimal.ZERO
        : currentQueueSize.divide(maxQueueSize, MathContext.DECIMAL128);

      return builder(feeResult.drops(), queuePercentage);
    }

    /**
     * Build a new instance of {@link DecomposedFees} from the supplied input.
     *
     * @param feeDrops        A {@link FeeDrops} to use as input.
     * @param queuePercentage A {@link BigDecimal} representing how full the transaction queue is.
     *
     * @return A {@link DecomposedFees}.
     */
    static DecomposedFees builder(final FeeDrops feeDrops, final BigDecimal queuePercentage) {
      Objects.requireNonNull(feeDrops);
      Objects.requireNonNull(queuePercentage);
      Preconditions.checkArgument(FluentCompareTo.is(queuePercentage).greaterThanEqualTo(BigDecimal.ZERO));
      Preconditions.checkArgument(FluentCompareTo.is(queuePercentage).lessThanOrEqualTo(BigDecimal.ONE));

      // Min fee should be slightly larger than the indicated min.
      final BigInteger adjustedMinimumFeeDrops = min(MAX_XRP_IN_DROPS_BIG_INT,
        new BigDecimal(feeDrops.minimumFee().value().bigIntegerValue()).multiply(ONE_POINT_FIVE)
          .setScale(0, RoundingMode.HALF_DOWN).toBigIntegerExact());

      return ImmutableDecomposedFees.builder().adjustedMinimumFeeDrops(adjustedMinimumFeeDrops)
        .medianFeeDrops(feeDrops.medianFee().value().bigIntegerValue())
        .openLedgerFeeDrops(feeDrops.openLedgerFee().value().bigIntegerValue()).queuePercentage(queuePercentage)
        .build();
    }

    /**
     * The minimum ledger fee as found in the supplied {@link FeeDrops} that was used to construct this instance.,
     * adjusted to be at least 50% larger than what was supplied in order to provide a buffer for fee calculations.
     *
     * @return A {@link BigInteger} representing the adjusted minimum fee (in drops).
     */
    BigInteger adjustedMinimumFeeDrops();

    /**
     * An equivalent of {@link #adjustedMinimumFeeDrops()}, but as a {@link BigDecimal}.
     *
     * @return A {@link BigDecimal} representing the adjusted minimum transaction fee on ledger (in drops).
     */
    @Derived
    default BigDecimal adjustedMinimumFeeDropsAsBigDecimal() {
      return new BigDecimal(adjustedMinimumFeeDrops());
    }

    /**
     * The median ledger fee as found in the supplied {@link FeeDrops} that was used to construct this instance.
     *
     * @return A {@link BigInteger} representing the median transaction fee on ledger (in drops).
     */
    BigInteger medianFeeDrops();

    /**
     * An equivalent of {@link #medianFeeDrops()}, but as a {@link BigDecimal}.
     *
     * @return A {@link BigDecimal} representing the median transaction fee on ledger (in drops).
     */
    @Derived
    default BigDecimal medianFeeDropsAsBigDecimal() {
      return new BigDecimal(medianFeeDrops());
    }

    /**
     * The open ledger fee as found in the supplied {@link FeeDrops} that was used to construct this instance.
     *
     * @return A {@link BigInteger} representing open ledger fee on ledger (in drops).
     */
    BigInteger openLedgerFeeDrops();

    /**
     * An equivalent of {@link #openLedgerFeeDrops()}, but as a {@link BigDecimal}.
     *
     * @return A {@link BigInteger} representing open ledger fee on ledger (in drops).
     */
    @Derived
    default BigDecimal openLedgerFeeDropsAsBigDecimal() {
      return new BigDecimal(openLedgerFeeDrops());
    }

    /**
     * Measures the fullness of the transaction queue by representing the percent full that the queue is. For example,
     * if the transaction queue can hold two transactions, and one is in the queue, then this value would be 50%, or
     * 0.5.
     *
     * @return A {@link BigDecimal}.
     */
    BigDecimal queuePercentage();
  }
}
