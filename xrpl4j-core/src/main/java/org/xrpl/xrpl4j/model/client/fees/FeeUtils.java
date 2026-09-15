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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
   * <p>This returns the base fee levels for a plain, single-signed transaction, and is correct on its own only for
   * transaction types that cost exactly the base fee. For anything with a different fee shape — a multi-signed or
   * sponsored transaction, or a type with its own rule ({@link Batch}, {@code EscrowFinish} with a fulfillment,
   * confidential MPT, {@code LoanSet}, {@code LoanPay}, {@code AccountDelete}, {@code AMMCreate}) — use
   * {@link #computeFee(FeeParams)}, which starts from these same levels and applies the type's rule.
   *
   * @param feeResult {@link FeeResult} object obtained by querying the ledger (e.g., via an `XrplClient#fee()` call).
   *
   * @return {@link ComputedNetworkFees} with low, medium and high fee levels to choose from for the transaction.
   *
   * @see "https://xrpl.org/fee.html"
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
   * <p>Because the fee is signed over, it must be computed before signing, and the intended flow is therefore: build
   * the transaction with a placeholder fee of zero (the {@code Fee} field is ignored while pricing), price it, attach
   * the fee, then sign:
   * <pre>{@code
   * Payment unpriced = Payment.builder()
   *   // ... every other field ...
   *   .fee(XrpCurrencyAmount.ofDrops(0)) // placeholder; ignored while pricing
   *   .build();
   * XrpCurrencyAmount fee = FeeUtils.computeFee(FeeParams.of(feeResult, unpriced).build()).recommendedFee();
   * Payment payment = ImmutablePayment.copyOf(unpriced).withFee(fee);
   * // sign and submit `payment`
   * }</pre>
   *
   * <p>A {@link Batch} is the one exception to "price before signing": {@code serializeBatch} excludes the outer
   * {@code Fee} from what the participants sign, so a Batch may be priced <em>after</em> the inner and batch
   * signatures are collected — at which point every signature count is read from the transaction and nothing need be
   * forecast. When a Batch must be priced up front instead (a wallet displaying the fee, say), the returned
   * {@link ComputedNetworkFees#feeBreakdown()} itemizes every assumption made; review its {@code [assumed]} lines to
   * find the inputs still worth supplying.
   *
   * @param feeParams The {@link FeeParams} describing the transaction and how it will be signed. Prefer the
   *                  type-scoped entry points ({@link FeeParams#of}, {@link FeeParams#forBatch},
   *                  {@link FeeParams#forLoanSet}, {@link FeeParams#forLoanPay},
   *                  {@link FeeParams#forOwnerReserve}), which expose exactly the inputs the transaction's type
   *                  prices by.
   *
   * @return A {@link ComputedNetworkFees} whose low, medium and high levels are each priced for this transaction, and
   *   whose {@link ComputedNetworkFees#feeBreakdown()} itemizes how.
   *
   * @see "https://github.com/XRPLF/rippled/blob/develop/src/libxrpl/tx/Transactor.cpp"
   */
  public static ComputedNetworkFees computeFee(final FeeParams feeParams) {
    Objects.requireNonNull(feeParams);

    final FeeBreakdown feeBreakdown = computeFeeBreakdown(feeParams);
    final ComputedNetworkFees baseFees = computeNetworkFees(feeParams.feeResult());

    final long feeUnits = feeBreakdown.totalFeeUnits();
    final Optional<XrpCurrencyAmount> flatAmount = feeBreakdown.totalFlatAmount();

    // An owner-reserve transaction has no base-fee term at all; everything else is a base-fee multiple plus any flat
    // owner reserves its Batch inners contribute.
    final ComputedNetworkFees pricedFees = feeUnits == 0L ?
      flatFee(flatAmount.get(), baseFees.queuePercentage()) :
      flatAmount
        .map(flat -> plusFlatFee(scaleByBaseFees(baseFees, feeUnits), flat))
        .orElseGet(() -> scaleByBaseFees(baseFees, feeUnits));

    return ComputedNetworkFees.builder().from(pricedFees).feeBreakdown(feeBreakdown).build();
  }

  /**
   * Itemizes the fee of {@link FeeParams#transaction()} as a {@link FeeBreakdown}: one {@link FeeTerm} per reason the
   * transaction owes something, each tagged {@code specified}, {@code assumed} or {@code derived}. This is the same
   * computation {@link #computeFee(FeeParams)} prices from — the breakdown's totals <em>are</em> the fee — exposed
   * separately so a caller can inspect or display the reasoning without the fee levels.
   *
   * @param feeParams The {@link FeeParams} describing the transaction and how it will be signed.
   *
   * @return A {@link FeeBreakdown}.
   */
  public static FeeBreakdown computeFeeBreakdown(final FeeParams feeParams) {
    Objects.requireNonNull(feeParams);

    final Transaction transaction = feeParams.transaction();
    final TransactionType transactionType = transaction.transactionType();

    if (FeeParams.OWNER_RESERVE_TRANSACTION_TYPES.contains(transactionType)) {
      // The owner reserve replaces the base-fee formula rather than adding to it, so signature counts do not apply.
      return FeeBreakdown.of(FeeTerm.flat(
        transactionType.value() + ": one owner reserve increment, flat (replaces the base-fee formula, so " +
          "signature counts are ignored)",
        feeParams.ownerReserve().get(),
        FeeTerm.Provenance.DERIVED
      ));
    }

    if (transactionType == TransactionType.BATCH) {
      return computeBatchFeeBreakdown(feeParams, (Batch) transaction);
    }

    if (transactionType == TransactionType.LOAN_PAY) {
      return computeLoanPayFeeBreakdown(feeParams);
    }

    final List<FeeTerm> terms = new ArrayList<>();
    terms.add(FeeTerm.of("base fee (" + transactionType.value() + ")", 1L, FeeTerm.Provenance.DERIVED));
    terms.addAll(signerCountTerms(feeParams, transaction));
    surchargeTerm(feeParams, transaction).ifPresent(terms::add);
    return FeeBreakdown.of(terms);
  }

  /**
   * Itemizes the fee of a {@link Batch}: two base fees for the outer transaction and batch processing, the outer
   * account's own signature terms, one base fee per batch signature, and each inner transaction's fee — computed
   * without signature terms, since rippled does not permit an inner transaction to carry signatures or fee
   * sponsorship.
   *
   * @param feeParams The {@link FeeParams} describing the Batch and how it will be signed.
   * @param batch     The {@link Batch} being priced.
   *
   * @return A {@link FeeBreakdown}.
   */
  private static FeeBreakdown computeBatchFeeBreakdown(final FeeParams feeParams, final Batch batch) {
    final List<FeeTerm> terms = new ArrayList<>();
    terms.add(FeeTerm.of("Batch outer: base fee + batch processing fee", 2L, FeeTerm.Provenance.DERIVED));
    terms.addAll(signerCountTerms(feeParams, batch));

    if (!batch.batchSigners().isEmpty()) {
      // Signatures have been collected, so every count is a fact read from the transaction.
      batch.batchSigners().stream()
        .map(BatchSignerWrapper::batchSigner)
        .forEach(batchSigner -> {
          final long count = batchSigner.transactionSignature().isPresent() ? 1L : batchSigner.signers().size();
          terms.add(FeeTerm.of(
            "batch signer " + batchSigner.account() + ": " + count + " collected signature(s)",
            count, FeeTerm.Provenance.DERIVED
          ));
        });
    } else {
      // Pricing before signing: one signature per required signer, except where the caller forecast a multi-sign.
      final Map<Address, UnsignedInteger> signaturesPerBatchSigner = feeParams.signaturesPerBatchSigner();
      batch.requiredSigners().forEach(requiredSigner -> {
        final UnsignedInteger specified = signaturesPerBatchSigner.get(requiredSigner);
        terms.add(specified != null ?
          FeeTerm.of(
            "batch signer " + requiredSigner + ": will supply " + specified + " signature(s)",
            specified.longValue(), FeeTerm.Provenance.SPECIFIED
          ) :
          FeeTerm.of(
            "batch signer " + requiredSigner + ": assumed to sign with a single key (declare signaturesFor(" +
              "address, count) if this participant will multi-sign)",
            1L, FeeTerm.Provenance.ASSUMED
          ));
      });
    }

    for (final RawTransactionWrapper wrapper : batch.rawTransactions()) {
      terms.add(innerTransactionTerm(feeParams, wrapper.rawTransaction()));
    }
    return FeeBreakdown.of(terms);
  }

  /**
   * Itemizes the fee of a {@code LoanPay} as a single term, because its increment count multiplies the whole fee
   * rather than adding to it: {@code increments × (1 + signersCount + sponsorSignersCount)} base fees.
   *
   * @param feeParams The {@link FeeParams} describing the LoanPay and how it will be signed.
   *
   * @return A {@link FeeBreakdown}.
   */
  private static FeeBreakdown computeLoanPayFeeBreakdown(final FeeParams feeParams) {
    final long perIncrementUnits = signatureUnits(feeParams);
    final Optional<UnsignedInteger> specifiedIncrements = feeParams.loanPaymentFeeIncrements();
    final long increments = specifiedIncrements.orElse(UnsignedInteger.ONE).longValue();

    final String formula =
      "LoanPay: " + increments + " fee increment(s) x " + perIncrementUnits + " base fee(s) for the transaction " +
        "and its signatures";
    return FeeBreakdown.of(specifiedIncrements.isPresent() ?
      FeeTerm.of(formula, increments * perIncrementUnits, FeeTerm.Provenance.SPECIFIED) :
      FeeTerm.of(
        formula + " (assumed a single payment; set loanPaymentFeeIncrements when the payment spans more)",
        increments * perIncrementUnits, FeeTerm.Provenance.ASSUMED
      ));
  }

  /**
   * The terms a transaction owes for its own and its sponsor's multi-signatures, plus zero-unit "assumed" terms that
   * surface the overridable single-signing assumptions — including a sponsor term only when the transaction actually
   * carries a {@code Sponsor}, so the hint appears exactly where it is relevant.
   *
   * @param feeParams   The {@link FeeParams} being applied.
   * @param transaction The {@link Transaction} being priced.
   *
   * @return A {@link List} of {@link FeeTerm}.
   */
  private static List<FeeTerm> signerCountTerms(final FeeParams feeParams, final Transaction transaction) {
    final List<FeeTerm> terms = new ArrayList<>();

    final long signersCount = feeParams.signersCount().longValue();
    terms.add(signersCount > 0L ?
      FeeTerm.of(
        "own multi-signature: " + signersCount + " Signers entries", signersCount, FeeTerm.Provenance.SPECIFIED
      ) :
      FeeTerm.of(
        "assumed single-signed: a lone TxnSignature is free (set signersCount if this account will multi-sign)",
        0L, FeeTerm.Provenance.ASSUMED
      ));

    final long sponsorSignersCount = feeParams.sponsorSignersCount().longValue();
    if (sponsorSignersCount > 0L) {
      terms.add(FeeTerm.of(
        "sponsor multi-signature: " + sponsorSignersCount + " SponsorSignature.Signers entries",
        sponsorSignersCount, FeeTerm.Provenance.SPECIFIED
      ));
    } else if (transaction.sponsor().isPresent()) {
      terms.add(FeeTerm.of(
        "assumed the sponsor signs with a single key: a lone SponsorSignature.TxnSignature is free (set " +
          "sponsorSignersCount if the sponsor will multi-sign)",
        0L, FeeTerm.Provenance.ASSUMED
      ));
    }
    return terms;
  }

  /**
   * The term a Batch inner transaction contributes: its flat owner reserve for an {@code AccountDelete} or
   * {@code AMMCreate}, or one base fee plus its type's surcharge otherwise. An inner never carries signatures or fee
   * sponsorship, so it has no signature terms, and a {@code LoanSet} inner's counterparty is counted among the batch
   * signers rather than here.
   *
   * @param feeParams The {@link FeeParams} being applied.
   * @param inner     The inner {@link Transaction}.
   *
   * @return A {@link FeeTerm}.
   */
  private static FeeTerm innerTransactionTerm(final FeeParams feeParams, final Transaction inner) {
    final TransactionType innerType = inner.transactionType();
    final String label = "inner " + innerType.value() + " (" + inner.account() + ")";

    if (FeeParams.OWNER_RESERVE_TRANSACTION_TYPES.contains(innerType)) {
      return FeeTerm.flat(
        label + ": one owner reserve increment, flat", feeParams.ownerReserve().get(), FeeTerm.Provenance.DERIVED
      );
    }
    if (CONFIDENTIAL_MPT_TRANSACTION_TYPES.contains(innerType)) {
      return FeeTerm.of(
        label + ": base fee + " + CONFIDENTIAL_FEE_MULTIPLIER + " confidential MPT surcharge",
        1L + CONFIDENTIAL_FEE_MULTIPLIER, FeeTerm.Provenance.DERIVED
      );
    }
    if (inner instanceof EscrowFinish) {
      final long fulfillmentUnits = fulfillmentUnits((EscrowFinish) inner);
      if (fulfillmentUnits > 0L) {
        return FeeTerm.of(
          label + ": base fee + " + fulfillmentUnits + " fulfillment surcharge (32 + fulfillmentBytes / 16)",
          1L + fulfillmentUnits, FeeTerm.Provenance.DERIVED
        );
      }
    }
    if (innerType == TransactionType.LOAN_SET) {
      return FeeTerm.of(
        label + ": base fee (its counterparty signature is counted among the batch signers, not here)",
        1L, FeeTerm.Provenance.DERIVED
      );
    }
    return FeeTerm.of(label + ": base fee", 1L, FeeTerm.Provenance.DERIVED);
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
   * The extra term a transaction owes because of its type, over and above what every transaction pays.
   *
   * @param feeParams   The {@link FeeParams} being applied.
   * @param transaction The {@link Transaction} being priced.
   *
   * @return An optionally-present {@link FeeTerm}, empty for the many transaction types with no surcharge.
   */
  private static Optional<FeeTerm> surchargeTerm(final FeeParams feeParams, final Transaction transaction) {
    final TransactionType transactionType = transaction.transactionType();

    if (CONFIDENTIAL_MPT_TRANSACTION_TYPES.contains(transactionType)) {
      return Optional.of(FeeTerm.of(
        "confidential MPT surcharge (rippled's kConfidentialFeeMultiplier)",
        CONFIDENTIAL_FEE_MULTIPLIER, FeeTerm.Provenance.DERIVED
      ));
    }
    if (transaction instanceof EscrowFinish) {
      final long fulfillmentUnits = fulfillmentUnits((EscrowFinish) transaction);
      return fulfillmentUnits == 0L ? Optional.empty() : Optional.of(FeeTerm.of(
        "EscrowFinish fulfillment surcharge: 32 + fulfillmentBytes / 16",
        fulfillmentUnits, FeeTerm.Provenance.DERIVED
      ));
    }
    if (transactionType == TransactionType.LOAN_SET) {
      final Optional<UnsignedInteger> specifiedCount = feeParams.counterpartySignatureCount();
      return Optional.of(specifiedCount.isPresent() ?
        FeeTerm.of(
          "LoanSet counterparty multi-signature: " + specifiedCount.get() + " signature(s)",
          specifiedCount.get().longValue(), FeeTerm.Provenance.SPECIFIED
        ) :
        FeeTerm.of(
          "LoanSet counterparty signature: assumed a single key, which is itself charged (set " +
            "counterpartySignatureCount if the counterparty will multi-sign)",
          1L, FeeTerm.Provenance.ASSUMED
        ));
    }
    return Optional.empty();
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
    // rippled charges by the on-wire size of the sfFulfillment blob:
    //   extraFee = base * (32 + fulfillmentBytes / 16)   (EscrowFinish::calculateBaseFee)
    // fulfillmentRawValue() is that exact blob, hex-encoded — the DER-encoded crypto-condition fulfillment that
    // EscrowFinish#normalizeFulfillment always populates. It is NOT the decoded preimage: for a PREIMAGE-SHA-256
    // fulfillment the DER wrapper adds a few bytes, which can change the /16 term near a boundary. Measuring the
    // blob (not the preimage) is what matches rippled.
    return escrowFinish.fulfillmentRawValue()
      .map(fulfillmentHex -> 32L + ((fulfillmentHex.length() / 2L) / 16L))
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
