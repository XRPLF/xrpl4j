package org.xrpl.xrpl4j.model.client.fees;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSigner;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LoanPay;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;

/**
 * Unit tests for the type-scoped {@link FeeParams} entry points ({@link FeeParams#of}, {@link FeeParams#forBatch},
 * {@link FeeParams#forLoanSet}, {@link FeeParams#forLoanPay}, {@link FeeParams#forOwnerReserve}) and the
 * {@link FeeBreakdown} attached by {@link FeeUtils#computeFee(FeeParams)}.
 */
public class FeeParamsScopedApiTest {

  private static final Address ALICE = Address.of("r3kmLJN5D28dHuH8vZNUZpMC43pEHpaocV");
  private static final Address BOB = Address.of("r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC");
  private static final Address CAROL = Address.of("r3ubyDp4gPGKH5bJx9KMmzpTSTW7EtRixS");
  private static final Address DAVE = Address.of("r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z");

  private static final PublicKey PUBLIC_KEY = PublicKey.fromBase16EncodedPublicKey(
    "ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A"
  );

  private static final XrpCurrencyAmount OWNER_RESERVE = XrpCurrencyAmount.ofDrops(200000);

  // /////////////////
  // FeeParams.of — the generic entry point
  // /////////////////

  @Test
  void ofPricesAGenericTransaction() {
    assertFeeUnits(FeeParams.of(feeResult(), payment()).build(), 1);
    assertFeeUnits(FeeParams.of(feeResult(), payment()).signersCount(UnsignedInteger.valueOf(3)).build(), 4);
  }

  @Test
  void ofSteersABatchToForBatch() {
    assertThatThrownBy(() -> FeeParams.of(feeResult(), batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1))))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Use FeeParams.forBatch");
  }

  @Test
  void ofSteersALoanSetToForLoanSet() {
    assertThatThrownBy(() -> FeeParams.of(feeResult(), loanSet()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Use FeeParams.forLoanSet");
  }

  @Test
  void ofSteersALoanPayToForLoanPay() {
    assertThatThrownBy(() -> FeeParams.of(feeResult(), loanPay()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Use FeeParams.forLoanPay");
  }

  @Test
  void ofSteersAnOwnerReserveTypeToForOwnerReserve() {
    assertThatThrownBy(() -> FeeParams.of(feeResult(), accountDelete()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Use FeeParams.forOwnerReserve");
  }

  @Test
  void ofValidatesSignatureCountsEagerly() {
    assertThatThrownBy(() -> FeeParams.of(feeResult(), payment()).signersCount(UnsignedInteger.valueOf(33)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("signersCount must be between 0 and 32");
  }

  // /////////////////
  // FeeParams.forBatch
  // /////////////////

  @Test
  void forBatchMatchesTheFlatBuilder() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    // 2 outer + 2 batchSigners + (1 + 1) inners
    assertFeeUnits(FeeParams.forBatch(feeResult(), batch).build(), 6);
    // Forecast carol multi-signing with 3 keys: 2 + (1 + 3) + (1 + 1)
    assertFeeUnits(
      FeeParams.forBatch(feeResult(), batch).signaturesFor(CAROL, UnsignedInteger.valueOf(3)).build(),
      8
    );
  }

  @Test
  void forBatchRejectsANonRequiredSignerAtTheCallThatNamedIt() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertThatThrownBy(() -> FeeParams.forBatch(feeResult(), batch).signaturesFor(DAVE, UnsignedInteger.ONE))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("is not required to sign this Batch");
  }

  @Test
  void forBatchRejectsAForecastOnceSignaturesExist() {
    Batch signed = Batch.builder().from(batch(ALICE, innerPayment(BOB, 1), innerPayment(BOB, 2)))
      .batchSigners(Lists.newArrayList(singleSignature(BOB)))
      .build();
    assertThatThrownBy(() -> FeeParams.forBatch(feeResult(), signed).signaturesFor(BOB, UnsignedInteger.valueOf(2)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("before its signatures exist");
  }

  @Test
  void forBatchSuppliesTheOwnerReserveForOwnerReserveInners() {
    Batch batch = batch(ALICE, innerPayment(ALICE, 1), innerAccountDelete(ALICE, 2));
    ComputedNetworkFees fees = FeeUtils.computeFee(
      FeeParams.forBatch(feeResult(), batch).ownerReserve(OWNER_RESERVE).build()
    );
    // 2 outer + 1 payment inner = 3 base fees, plus one flat owner reserve.
    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(3 * 1000 + 200000));
  }

  // /////////////////
  // Strict mode: requireExplicitSignatureCounts
  // /////////////////

  @Test
  void strictModeRefusesToPriceOnAnAssumedSignatureCount() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertThatThrownBy(() -> FeeParams.forBatch(feeResult(), batch)
      .requireExplicitSignatureCounts()
      .signaturesFor(CAROL, UnsignedInteger.valueOf(3))
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("no signature count was supplied for [" + BOB + "]");
  }

  @Test
  void strictModeBuildsOnceEveryRequiredSignerHasACount() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    FeeParams feeParams = FeeParams.forBatch(feeResult(), batch)
      .requireExplicitSignatureCounts()
      .signaturesFor(BOB, UnsignedInteger.ONE)
      .signaturesFor(CAROL, UnsignedInteger.valueOf(3))
      .build();
    assertFeeUnits(feeParams, 8);
  }

  @Test
  void strictModeIsANoOpOnceSignaturesAreCollected() {
    // Collected signature counts are facts, not assumptions, so there is nothing for strict mode to demand.
    Batch signed = Batch.builder().from(batch(ALICE, innerPayment(BOB, 1), innerPayment(BOB, 2)))
      .batchSigners(Lists.newArrayList(singleSignature(BOB)))
      .build();
    // 2 outer + 1 collected batch signature + (1 + 1) inners
    assertFeeUnits(FeeParams.forBatch(feeResult(), signed).requireExplicitSignatureCounts().build(), 5);
  }

  // /////////////////
  // FeeParams.forLoanSet / forLoanPay / forOwnerReserve
  // /////////////////

  @Test
  void forLoanSetPricesTheCounterparty() {
    // Absent: a single counterparty signature is assumed, and charged: 1 + 1.
    assertFeeUnits(FeeParams.forLoanSet(feeResult(), loanSet()).build(), 2);
    // 1 + 2 own + 2 sponsor + 3 counterparty
    assertFeeUnits(
      FeeParams.forLoanSet(feeResult(), loanSet())
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(2))
        .counterpartySignatureCount(UnsignedInteger.valueOf(3))
        .build(),
      8
    );
  }

  @Test
  void forLoanSetValidatesTheCounterpartyCountEagerly() {
    assertThatThrownBy(
      () -> FeeParams.forLoanSet(feeResult(), loanSet()).counterpartySignatureCount(UnsignedInteger.ZERO)
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("counterpartySignatureCount must be between 1 and 32");
  }

  @Test
  void forLoanPayPricesTheIncrements() {
    assertFeeUnits(FeeParams.forLoanPay(feeResult(), loanPay()).build(), 1);
    // 4 increments x (1 + 2 own signatures)
    assertFeeUnits(
      FeeParams.forLoanPay(feeResult(), loanPay())
        .loanPaymentFeeIncrements(UnsignedInteger.valueOf(4))
        .signersCount(UnsignedInteger.valueOf(2))
        .build(),
      12
    );
  }

  @Test
  void forOwnerReservePricesFlatWithNoOtherInputs() {
    ComputedNetworkFees fees = FeeUtils.computeFee(
      FeeParams.forOwnerReserve(feeResult(), accountDelete(), OWNER_RESERVE)
    );
    assertThat(fees.feeLow()).isEqualTo(OWNER_RESERVE);
    assertThat(fees.feeMedium()).isEqualTo(OWNER_RESERVE);
    assertThat(fees.feeHigh()).isEqualTo(OWNER_RESERVE);
  }

  @Test
  void forOwnerReserveRejectsAGenericallyPricedType() {
    assertThatThrownBy(() -> FeeParams.forOwnerReserve(feeResult(), payment(), OWNER_RESERVE))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("forOwnerReserve applies only to");
  }

  // /////////////////
  // Optional type-specific fields: the "explicit default" hole is closed
  // /////////////////

  @Test
  void anExplicitDefaultValuedFieldOnTheWrongTypeIsRejected() {
    // Under @Value.Default these were indistinguishable from the default and slipped through silently.
    assertThatThrownBy(() -> FeeParams.builder()
      .feeResult(feeResult())
      .transaction(payment())
      .counterpartySignatureCount(UnsignedInteger.ONE)
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a LoanSet");

    assertThatThrownBy(() -> FeeParams.builder()
      .feeResult(feeResult())
      .transaction(payment())
      .loanPaymentFeeIncrements(UnsignedInteger.ONE)
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a LoanPay");
  }

  // /////////////////
  // FeeBreakdown
  // /////////////////

  @Test
  void computeFeeAttachesABreakdownWhoseTotalsAreTheFee() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    ComputedNetworkFees fees = FeeUtils.computeFee(
      FeeParams.forBatch(feeResult(), batch).signaturesFor(CAROL, UnsignedInteger.valueOf(3)).build()
    );

    assertThat(fees.feeBreakdown()).isPresent();
    FeeBreakdown breakdown = fees.feeBreakdown().get();
    assertThat(breakdown.totalFeeUnits()).isEqualTo(8);
    assertThat(breakdown.totalFlatAmount()).isEmpty();
    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(8 * 1000));
  }

  @Test
  void breakdownTagsAssumedAndSpecifiedBatchSigners() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    FeeBreakdown breakdown = FeeUtils.computeFeeBreakdown(
      FeeParams.forBatch(feeResult(), batch).signaturesFor(CAROL, UnsignedInteger.valueOf(3)).build()
    );

    assertThat(termFor(breakdown, BOB.value()).provenance()).isEqualTo(FeeTerm.Provenance.ASSUMED);
    assertThat(termFor(breakdown, BOB.value()).description()).contains("signaturesFor");
    assertThat(termFor(breakdown, CAROL.value()).provenance()).isEqualTo(FeeTerm.Provenance.SPECIFIED);
    assertThat(termFor(breakdown, CAROL.value()).feeUnits()).isEqualTo(3);
  }

  @Test
  void breakdownTagsCollectedBatchSignaturesAsDerived() {
    Batch signed = Batch.builder().from(batch(ALICE, innerPayment(BOB, 1), innerPayment(BOB, 2)))
      .batchSigners(Lists.newArrayList(singleSignature(BOB)))
      .build();
    FeeBreakdown breakdown = FeeUtils.computeFeeBreakdown(FeeParams.forBatch(feeResult(), signed).build());

    assertThat(termFor(breakdown, BOB.value()).provenance()).isEqualTo(FeeTerm.Provenance.DERIVED);
    assertThat(termFor(breakdown, BOB.value()).description()).contains("collected");
  }

  @Test
  void breakdownSurfacesTheAssumedLoanSetCounterparty() {
    FeeBreakdown breakdown = FeeUtils.computeFeeBreakdown(FeeParams.forLoanSet(feeResult(), loanSet()).build());
    FeeTerm counterpartyTerm = termFor(breakdown, "counterparty");
    assertThat(counterpartyTerm.provenance()).isEqualTo(FeeTerm.Provenance.ASSUMED);
    assertThat(counterpartyTerm.feeUnits()).isEqualTo(1);
    assertThat(counterpartyTerm.description()).contains("counterpartySignatureCount");
  }

  @Test
  void breakdownCarriesFlatOwnerReserveTerms() {
    Batch batch = batch(ALICE, innerPayment(ALICE, 1), innerAccountDelete(ALICE, 2));
    FeeBreakdown breakdown = FeeUtils.computeFeeBreakdown(
      FeeParams.forBatch(feeResult(), batch).ownerReserve(OWNER_RESERVE).build()
    );
    assertThat(breakdown.totalFeeUnits()).isEqualTo(3);
    assertThat(breakdown.totalFlatAmount()).contains(OWNER_RESERVE);
    assertThat(termFor(breakdown, "AccountDelete").flatAmount()).contains(OWNER_RESERVE);
  }

  @Test
  void breakdownMentionsTheSponsorOnlyWhenTheTransactionCarriesOne() {
    FeeBreakdown unsponsored = FeeUtils.computeFeeBreakdown(FeeParams.of(feeResult(), payment()).build());
    assertThat(unsponsored.terms()).noneMatch(term -> term.description().contains("sponsor"));

    Payment sponsored = Payment.builder().from(payment())
      .sponsor(DAVE)
      .sponsorFlags(org.xrpl.xrpl4j.model.flags.SponsorFlags.SPONSOR_FEE)
      .build();
    FeeBreakdown breakdown = FeeUtils.computeFeeBreakdown(FeeParams.of(feeResult(), sponsored).build());
    FeeTerm sponsorTerm = termFor(breakdown, "sponsor");
    assertThat(sponsorTerm.provenance()).isEqualTo(FeeTerm.Provenance.ASSUMED);
    assertThat(sponsorTerm.feeUnits()).isEqualTo(0);
  }

  @Test
  void computeNetworkFeesCarriesNoBreakdown() {
    assertThat(FeeUtils.computeNetworkFees(feeResult()).feeBreakdown()).isEmpty();
  }

  @Test
  void breakdownDoesNotDisturbEquality() {
    // feeBreakdown is auxiliary: two ComputedNetworkFees with the same levels are equal regardless of it.
    ComputedNetworkFees withBreakdown = FeeUtils.computeFee(FeeParams.of(feeResult(), payment()).build());
    ComputedNetworkFees without = ComputedNetworkFees.builder().from(withBreakdown)
      .feeBreakdown(java.util.Optional.empty())
      .build();
    assertThat(withBreakdown).isEqualTo(without);
  }

  @Test
  void summaryRendersOneLinePerTermAndATotal() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    String summary = FeeUtils.computeFeeBreakdown(
      FeeParams.forBatch(feeResult(), batch).signaturesFor(CAROL, UnsignedInteger.valueOf(3)).build()
    ).summary();

    assertThat(summary).contains("[assumed]");
    assertThat(summary).contains("[specified]");
    assertThat(summary).contains("[derived]");
    assertThat(summary).contains("total: 8 x base fee");
    // Uncomment to eyeball the rendering:
    // System.out.println(summary);
  }

  // /////////////////
  // Helpers
  // /////////////////

  private FeeTerm termFor(final FeeBreakdown breakdown, final String descriptionFragment) {
    return breakdown.terms().stream()
      .filter(term -> term.description().contains(descriptionFragment))
      .findFirst()
      .orElseThrow(() -> new AssertionError(
        "No term mentioning '" + descriptionFragment + "' in:\n" + breakdown.summary()
      ));
  }

  private void assertFeeUnits(final FeeParams feeParams, final long expectedUnits) {
    assertThat(FeeUtils.computeFee(feeParams).feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(expectedUnits * 1000));
  }

  private Payment payment() {
    return Payment.builder()
      .account(ALICE)
      .destination(BOB)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private Payment innerPayment(final Address account, final int sequence) {
    return Payment.builder()
      .account(account)
      .destination(DAVE)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.valueOf(sequence))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();
  }

  private AccountDelete innerAccountDelete(final Address account, final int sequence) {
    return AccountDelete.builder()
      .account(account)
      .destination(DAVE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.valueOf(sequence))
      .flags(org.xrpl.xrpl4j.model.flags.TransactionFlags.INNER_BATCH_TXN)
      .build();
  }

  private AccountDelete accountDelete() {
    return AccountDelete.builder()
      .account(ALICE)
      .destination(BOB)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private LoanSet loanSet() {
    return LoanSet.builder()
      .account(ALICE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .loanBrokerId(Hash256.of("C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"))
      .principalRequested(Amount.of("1000000"))
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private LoanPay loanPay() {
    return LoanPay.builder()
      .account(ALICE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .loanId(Hash256.of("C031EFE677CDEF1C5F43475B374A16F990EE184F76015CB7548D34B500F72BFB"))
      .amount(XrpCurrencyAmount.ofDrops(50000))
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private Batch batch(final Address outerAccount, final Transaction... inners) {
    List<RawTransactionWrapper> wrappers = Lists.newArrayList();
    for (Transaction inner : inners) {
      wrappers.add(RawTransactionWrapper.of(inner));
    }
    return Batch.builder()
      .account(outerAccount)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY)
      .rawTransactions(wrappers)
      .build();
  }

  private BatchSignerWrapper singleSignature(final Address account) {
    return BatchSignerWrapper.of(BatchSigner.builder()
      .account(account)
      .signingPublicKey(PUBLIC_KEY)
      .transactionSignature(Signature.fromBase16("ABCD"))
      .build());
  }

  private FeeResult feeResult() {
    return FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(1))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();
  }
}
