package org.xrpl.xrpl4j.model.client.fees;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.xrpl.xrpl4j.model.transactions.CurrencyAmount.MAX_XRP_IN_DROPS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.ledger.SignerListObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
   * The extra base fees rippled charges a confidential MPT transaction, on top of the base fee every transaction
   * pays. This is rippled's {@code kConfidentialFeeMultiplier}.
   */
  private static final long CONFIDENTIAL_FEE_MULTIPLIER = 9L;

  /**
   * The {@link TransactionType}s that rippled charges {@link #CONFIDENTIAL_FEE_MULTIPLIER} extra base fees for.
   */
  private static final Set<TransactionType> CONFIDENTIAL_MPT_TRANSACTION_TYPES = ImmutableSet.of(
    TransactionType.CONFIDENTIAL_MPT_CONVERT,
    TransactionType.CONFIDENTIAL_MPT_CONVERT_BACK,
    TransactionType.CONFIDENTIAL_MPT_SEND,
    TransactionType.CONFIDENTIAL_MPT_CLAWBACK,
    TransactionType.CONFIDENTIAL_MPT_MERGE_INBOX
  );

  /**
   * Computes the fee necessary for a multisigned transaction.
   *
   * <p>The transaction cost of a multisigned transaction must be at least {@code (N + 1) * (the normal
   * transaction cost)}, where {@code N} is the number of signatures provided.
   *
   * @param feeResult  {@link FeeResult} object obtained by querying the ledger (e.g., via an `XrplClient#fee()` call).
   * @param signerList The {@link SignerListObject} containing the signers of the transaction.
   *
   * @return An {@link XrpCurrencyAmount} representing the multisig fee.
   *
   * @deprecated This counts entries in {@code signerList} rather than the signatures a transaction will actually
   *   carry, so it over-charges whenever a quorum is met by fewer signers than the list holds. It also has no term
   *   for a sponsor's signatures. Use {@link #computeFee(FeeParams)}, supplying
   *   {@link FeeParams#signersCount()} and {@link FeeParams#sponsorSignersCount()}.
   */
  @Deprecated
  public static ComputedNetworkFees computeMultisigNetworkFees(
    final FeeResult feeResult,
    final SignerListObject signerList
  ) {
    Objects.requireNonNull(feeResult);
    Objects.requireNonNull(signerList);

    ComputedNetworkFees computedNetworkFees = computeNetworkFees(feeResult);
    XrpCurrencyAmount numberOfSignersAsAmount = XrpCurrencyAmount.of(
      UnsignedLong.valueOf(signerList.signerEntries().size() + 1)
    );
    return ComputedNetworkFees.builder()
      .feeLow(computedNetworkFees.feeLow().times(numberOfSignersAsAmount))
      .feeMedium(computedNetworkFees.feeMedium().times(numberOfSignersAsAmount))
      .feeHigh(computedNetworkFees.feeHigh().times(numberOfSignersAsAmount))
      .queuePercentage(computedNetworkFees.queuePercentage())
      .build();
  }

  /**
   * Calculate a suggested fee to be used for submitting a transaction to the XRPL. The calculated value depends on the
   * current size of the job queue as compared to its total capacity.
   *
   * @param feeResult {@link FeeResult} object obtained by querying the ledger (e.g., via an `XrplClient#fee()` call).
   *
   * @return {@link ComputedNetworkFees} with low, medium and high fee levels to choose from for the transaction.
   *
   * @see "https://xrpl.org/fee.html"
   * @see "https://github.com/XRPL-Labs/XUMM-App/blob/master/src/services/LedgerService.ts#L244"
   */
  public static ComputedNetworkFees computeNetworkFees(final FeeResult feeResult) {
    Objects.requireNonNull(feeResult);

    final DecomposedFees decomposedFees = DecomposedFees.builder(feeResult);
    final XrpCurrencyAmount feeLow = computeFeeLow(decomposedFees);

    return ComputedNetworkFees.builder()
      .feeLow(feeLow)
      .feeMedium(computeFeeMedium(decomposedFees, feeLow))
      .feeHigh(computeFeeHigh(decomposedFees))
      .queuePercentage(decomposedFees.queuePercentage())
      .build();
  }






  /**
   * Computes the fee for any transaction, applying whichever of rippled's fee rules its type calls for.
   *
   * <p>The rules, all of which derive from rippled's {@code calculateBaseFee} overrides:
   * <ul>
   *   <li>Every transaction pays {@code base × (1 + signersCount + sponsorSignersCount)}.</li>
   *   <li>A confidential MPT transaction adds {@code 9 × base}.</li>
   *   <li>An {@code EscrowFinish} carrying a fulfillment adds {@code base × (32 + fulfillmentBytes / 16)}.</li>
   *   <li>A {@code LoanSet} adds {@code base × counterpartySignatureCount}.</li>
   *   <li>A {@code LoanPay} multiplies the whole amount by its number of fee increments.</li>
   *   <li>An {@code AccountDelete} or {@code AMMCreate} costs one owner reserve increment instead, flat.</li>
   *   <li>A {@link Batch} costs {@code base × (2 + batchSignatures + signersCount + sponsorSignersCount)} plus the
   *       fee of each inner transaction, computed by these same rules but without signature terms, since rippled
   *       does not permit an inner transaction to carry signatures or fee sponsorship.</li>
   * </ul>
   *
   * @param feeParams The {@link FeeParams} describing the transaction and how it will be signed.
   *
   * @return A {@link ComputedNetworkFees} whose low, medium and high levels are each priced for this transaction.
   *
   * @see "https://github.com/XRPLF/rippled/blob/develop/src/libxrpl/tx/Transactor.cpp"
   */
  public static ComputedNetworkFees computeFee(final FeeParams feeParams) {
    Objects.requireNonNull(feeParams);

    final ComputedNetworkFees baseFees = computeNetworkFees(feeParams.feeResult());
    final Transaction transaction = feeParams.transaction();
    final TransactionType transactionType = transaction.transactionType();

    // An owner reserve replaces the base-fee formula rather than adding to it, so signature counts do not apply.
    if (FeeParams.OWNER_RESERVE_TRANSACTION_TYPES.contains(transactionType)) {
      return flatFee(feeParams.ownerReserve().get(), baseFees.queuePercentage());
    }

    if (transactionType == TransactionType.BATCH) {
      return computeFeeForBatch(feeParams, (Batch) transaction, baseFees);
    }

    final long signatureUnits = signatureUnits(feeParams);
    final long feeUnits = transactionType == TransactionType.LOAN_PAY ?
      signatureUnits * feeParams.loanPaymentFeeIncrements().longValue() :
      signatureUnits + surchargeUnits(feeParams, transaction);

    return scaleByBaseFees(baseFees, feeUnits);
  }

  /**
   * Computes the fee for a {@link Batch}, being its own cost plus one base fee per batch signature plus the fee of
   * each inner transaction.
   *
   * @param feeParams The {@link FeeParams} describing the Batch and how it will be signed.
   * @param batch     The {@link Batch} being priced.
   * @param baseFees  The unscaled {@link ComputedNetworkFees} for the current ledger.
   *
   * @return A {@link ComputedNetworkFees} priced for this Batch.
   */
  private static ComputedNetworkFees computeFeeForBatch(
    final FeeParams feeParams,
    final Batch batch,
    final ComputedNetworkFees baseFees
  ) {
    // batchBase is one base fee for the Batch being a transaction like any other, plus one flat base fee for batch
    // processing. The outer account's own signatures are carried by the first of those two.
    long feeUnits = 1L + signatureUnits(feeParams) + batchSignatureCount(feeParams, batch);
    long ownerReserveInners = 0L;

    for (final RawTransactionWrapper wrapper : batch.rawTransactions()) {
      final Transaction inner = wrapper.rawTransaction();
      if (FeeParams.OWNER_RESERVE_TRANSACTION_TYPES.contains(inner.transactionType())) {
        ownerReserveInners++;
      } else {
        // An inner transaction never carries signatures or fee sponsorship, so it costs one base fee plus whatever
        // surcharge its type attracts.
        feeUnits += 1L + surchargeUnits(feeParams, inner);
      }
    }

    final ComputedNetworkFees scaled = scaleByBaseFees(baseFees, feeUnits);
    return ownerReserveInners == 0 ? scaled : plusFlatFee(
      scaled, feeParams.ownerReserve().get().times(XrpCurrencyAmount.of(UnsignedLong.valueOf(ownerReserveInners)))
    );
  }

  /**
   * Counts the signatures that will appear across a {@link Batch}'s {@code BatchSigners} array.
   *
   * <p>Once signatures have been collected the count is a fact, read from {@link Batch#batchSigners()}: one for an
   * entry signing with a single key, or the size of its nested {@code Signers} array otherwise. Before then it is
   * derived from {@link Batch#requiredSigners()}, counting one signature per required signer except where
   * {@link FeeParams#signaturesPerBatchSigner()} says a participant will multi-sign.
   *
   * @param feeParams The {@link FeeParams} being applied.
   * @param batch     The {@link Batch} being priced.
   *
   * @return The number of batch signatures, each of which costs one base fee.
   */
  private static long batchSignatureCount(final FeeParams feeParams, final Batch batch) {
    if (!batch.batchSigners().isEmpty()) {
      return batch.batchSigners().stream()
        .map(BatchSignerWrapper::batchSigner)
        .mapToLong(batchSigner -> batchSigner.transactionSignature().isPresent() ? 1L : batchSigner.signers().size())
        .sum();
    }

    final Map<Address, UnsignedInteger> signaturesPerBatchSigner = feeParams.signaturesPerBatchSigner();
    return batch.requiredSigners().stream()
      .mapToLong(address -> signaturesPerBatchSigner.getOrDefault(address, UnsignedInteger.ONE).longValue())
      .sum();
  }

  /**
   * The number of base fees a transaction owes for its signatures, being one for the transaction itself plus one for
   * each additional signature of a multi-signature, on the transaction or on its sponsor.
   *
   * @param feeParams The {@link FeeParams} being applied.
   *
   * @return A number of base fees, at least one.
   */
  private static long signatureUnits(final FeeParams feeParams) {
    return 1L + feeParams.signersCount().longValue() + feeParams.sponsorSignersCount().longValue();
  }

  /**
   * The number of extra base fees a transaction owes because of its type, over and above what every transaction pays.
   *
   * @param feeParams   The {@link FeeParams} being applied.
   * @param transaction The {@link Transaction} being priced, which may be an inner transaction of a Batch rather than
   *                    {@link FeeParams#transaction()} itself.
   *
   * @return A number of extra base fees, which is zero for most transaction types.
   */
  private static long surchargeUnits(final FeeParams feeParams, final Transaction transaction) {
    final TransactionType transactionType = transaction.transactionType();

    if (CONFIDENTIAL_MPT_TRANSACTION_TYPES.contains(transactionType)) {
      return CONFIDENTIAL_FEE_MULTIPLIER;
    }
    if (transaction instanceof EscrowFinish) {
      return fulfillmentUnits((EscrowFinish) transaction);
    }
    if (transactionType == TransactionType.LOAN_SET) {
      return feeParams.counterpartySignatureCount().longValue();
    }
    return 0L;
  }

  /**
   * The number of extra base fees an {@code EscrowFinish} owes for the fulfillment it carries, being
   * {@code 32 + fulfillmentBytes / 16}, or zero when it carries none.
   *
   * @param escrowFinish The {@link EscrowFinish} being priced.
   *
   * @return A number of extra base fees.
   */
  private static long fulfillmentUnits(final EscrowFinish escrowFinish) {
    return escrowFinish.fulfillment()
      .map(fulfillment -> {
        Preconditions.checkArgument(
          PreimageSha256Fulfillment.class.isAssignableFrom(fulfillment.getClass()),
          "Only PreimageSha256Fulfillment is supported, but the fulfillment was a %s.",
          fulfillment.getClass().getSimpleName()
        );
        final long fulfillmentByteSize = Base64.getUrlDecoder()
          .decode(((PreimageSha256Fulfillment) fulfillment).getEncodedPreimage()).length;
        // See rippled's EscrowFinish::calculateBaseFee: extraFee = base * (32 + (fulfillment size / 16)).
        return 32L + (fulfillmentByteSize / 16L);
      })
      .orElse(0L);
  }

  /**
   * Scales each of the supplied fee levels by a number of base fees.
   *
   * @param computedNetworkFees The {@link ComputedNetworkFees} to scale, each level of which is one base fee.
   * @param feeUnits            The number of base fees the transaction costs.
   *
   * @return A {@link ComputedNetworkFees} with every level scaled by {@code feeUnits}.
   */
  private static ComputedNetworkFees scaleByBaseFees(
    final ComputedNetworkFees computedNetworkFees,
    final long feeUnits
  ) {
    final XrpCurrencyAmount feeUnitsAsAmount = XrpCurrencyAmount.of(UnsignedLong.valueOf(feeUnits));
    return ComputedNetworkFees.builder()
      .feeLow(computedNetworkFees.feeLow().times(feeUnitsAsAmount))
      .feeMedium(computedNetworkFees.feeMedium().times(feeUnitsAsAmount))
      .feeHigh(computedNetworkFees.feeHigh().times(feeUnitsAsAmount))
      .queuePercentage(computedNetworkFees.queuePercentage())
      .build();
  }

  /**
   * Adds a flat amount to each of the supplied fee levels, for a cost that is not a multiple of the base fee.
   *
   * @param computedNetworkFees The {@link ComputedNetworkFees} to add to.
   * @param flatAmount          The {@link XrpCurrencyAmount} to add to every level.
   *
   * @return A {@link ComputedNetworkFees} with {@code flatAmount} added to every level.
   */
  private static ComputedNetworkFees plusFlatFee(
    final ComputedNetworkFees computedNetworkFees,
    final XrpCurrencyAmount flatAmount
  ) {
    return ComputedNetworkFees.builder()
      .feeLow(computedNetworkFees.feeLow().plus(flatAmount))
      .feeMedium(computedNetworkFees.feeMedium().plus(flatAmount))
      .feeHigh(computedNetworkFees.feeHigh().plus(flatAmount))
      .queuePercentage(computedNetworkFees.queuePercentage())
      .build();
  }

  /**
   * A {@link ComputedNetworkFees} whose every level is the same flat amount, for a cost that does not vary with the
   * base fee.
   *
   * @param flatAmount      The {@link XrpCurrencyAmount} every level should carry.
   * @param queuePercentage How full the transaction queue is.
   *
   * @return A {@link ComputedNetworkFees}.
   */
  private static ComputedNetworkFees flatFee(
    final XrpCurrencyAmount flatAmount,
    final BigDecimal queuePercentage
  ) {
    return ComputedNetworkFees.builder()
      .feeLow(flatAmount)
      .feeMedium(flatAmount)
      .feeHigh(flatAmount)
      .queuePercentage(queuePercentage)
      .build();
  }

  /**
   * Calculate the lowest fee the user is able to pay if the queue is empty.
   *
   * @param decomposedFees A {@link DecomposedFees} that contains information about current XRPL fees.
   *
   * @return An {@link XrpCurrencyAmount} representing the `low` fee.
   */
  private static XrpCurrencyAmount computeFeeLow(final DecomposedFees decomposedFees) {
    Objects.requireNonNull(decomposedFees);

    final BigInteger adjustedMinimumFeeDrops = decomposedFees.adjustedMinimumFeeDrops();
    final BigInteger medianFee = decomposedFees.medianFeeDrops();
    final BigInteger openLedgerFee = decomposedFees.openLedgerFeeDrops();

    // Cap `feeLow` to the size of an UnsignedLong.
    return XrpCurrencyAmount.ofDrops(
      toUnsignedLongSafe(
        min(
          max(
            adjustedMinimumFeeDrops, // min fee * 1.50
            divideToBigInteger(max(medianFee, openLedgerFee), FIVE_HUNDRED)
          ),
          ONE_THOUSAND
        )
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
  private static XrpCurrencyAmount computeFeeMedium(final DecomposedFees decomposedFees,
    final XrpCurrencyAmount feeLow) {
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
    final BigInteger feeMedium = min(
      possibleFeeMedium,
      feeLow.value().bigIntegerValue().multiply(FIFTEEN), TEN_THOUSAND
    );

    return XrpCurrencyAmount.ofDrops(
      toUnsignedLongSafe(feeMedium)
    );
  }

  /**
   * Compute the `high` fee, which is the fee to use when the transaction queue is full.
   *
   * @param decomposedFees A {@link DecomposedFees} with precomputed values to use.
   *
   * @return An {@link UnsignedLong} representing the `high` fee.
   */
  private static XrpCurrencyAmount computeFeeHigh(final DecomposedFees decomposedFees) {
    Objects.requireNonNull(decomposedFees);

    final BigInteger minimumFee = decomposedFees.adjustedMinimumFeeDrops();
    final BigInteger medianFee = decomposedFees.medianFeeDrops();
    final BigInteger openLedgerFee = decomposedFees.openLedgerFeeDrops();

    final BigInteger feeHigh = min(
      max(minimumFee.multiply(BigInteger.TEN), multiplyToBigInteger(max(medianFee, openLedgerFee), ONE_POINT_ONE)),
      TEN_THOUSAND);
    return XrpCurrencyAmount.ofDrops(
      toUnsignedLongSafe(feeHigh)
    );
  }

  /**
   * Helper method to determine if a transaction queue is empty by inspecting a `percent-full` measurement.
   *
   * @param queuePercentage A {@link BigDecimal} representing the percent-full value for a tx queue.
   *
   * @return {@code true} if the queue is empty; {@code false} otherwise.
   */
  @VisibleForTesting
  public static boolean queueIsEmpty(final BigDecimal queuePercentage) {
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
  public static boolean queueIsNotEmptyAndNotFull(final BigDecimal queuePercentage) {
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
  public interface DecomposedFees {

    /**
     * The number 1.5, as a {@link BigDecimal}.
     */
    BigDecimal ONE_POINT_FIVE = new BigDecimal("1.5");

    /**
     * The maximum number of XRP drops, as a {@link BigInteger}.
     */
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
