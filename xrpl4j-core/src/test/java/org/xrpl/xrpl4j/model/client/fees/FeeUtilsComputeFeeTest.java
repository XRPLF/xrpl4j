package org.xrpl.xrpl4j.model.client.fees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSigner;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutableEscrowFinish;
import org.xrpl.xrpl4j.model.transactions.LoanPay;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;

/**
 * Unit tests for {@link FeeUtils#computeFee(FeeParams)}, mirroring the cases documented for the fee model.
 *
 * <p>The {@link #feeResult()} fixture yields unscaled fee levels of 1000 / 5008 / 10000 drops, so a transaction
 * costing {@code n} base fees is asserted as {@code n × 1000} on {@code feeLow}.
 */
class FeeUtilsComputeFeeTest {

  private static final Address ALICE = Address.of("r3kmLJN5D28dHuH8vZNUZpMC43pEHpaocV");
  private static final Address BOB = Address.of("r3nCVTbZGGYoWvZ58BcxDmiMUU7ChMa1eC");
  private static final Address CAROL = Address.of("r3ubyDp4gPGKH5bJx9KMmzpTSTW7EtRixS");
  private static final Address DAVE = Address.of("r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z");
  private static final Address FRANK = Address.of("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK");

  private static final PublicKey PUBLIC_KEY = PublicKey.fromBase16EncodedPublicKey(
    "ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A"
  );

  // /////////////////
  // Generic — the types with no special rule
  // /////////////////

  @Test
  void singleSignedPaymentCostsOneBaseFee() {
    assertFeeUnits(paramsFor(payment()), 1);
  }

  @Test
  void multiSignedPaymentChargesEachAdditionalSignature() {
    assertFeeUnits(
      FeeParams.builder().feeResult(feeResult()).transaction(payment()).signersCount(UnsignedInteger.valueOf(3)),
      4
    );
  }

  @Test
  void singleKeySponsorIsFree() {
    // The sponsor signature lives in SponsorSignature.TxnSignature, which rippled does not charge for.
    assertFeeUnits(paramsFor(sponsoredPayment()), 1);
  }

  @Test
  void multiSignedSponsorChargesEachSponsorSignature() {
    assertFeeUnits(
      FeeParams.builder().feeResult(feeResult()).transaction(sponsoredPayment())
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      4
    );
  }

  @Test
  void ownAndSponsorSignaturesBothCount() {
    assertFeeUnits(
      FeeParams.builder().feeResult(feeResult()).transaction(sponsoredPayment())
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      6
    );
  }

  @Test
  void allThreeFeeLevelsAreScaled() {
    ComputedNetworkFees fees = FeeUtils.computeFee(paramsFor(payment()).signersCount(UnsignedInteger.ONE).build());
    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(2000));
    assertThat(fees.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(10016));
    assertThat(fees.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(20000));
  }

  // /////////////////
  // EscrowFinish
  // /////////////////

  @Test
  void escrowFinishWithoutFulfillmentCostsOneBaseFee() {
    assertFeeUnits(paramsFor(escrowFinish(false)), 1);
  }

  @Test
  void escrowFinishWithFulfillmentAddsTheSizeSurcharge() {
    // 1 + (32 + 32/16) = 35
    assertFeeUnits(paramsFor(escrowFinish(true)), 35);
  }

  @Test
  void escrowFinishSurchargeIsAddedToTheSignatureTerms() {
    // (1 + 2) + (32 + 32/16) = 37 — the signer terms EscrowFinish.computeFee omits.
    assertFeeUnits(
      FeeParams.builder().feeResult(feeResult()).transaction(escrowFinish(true))
        .signersCount(UnsignedInteger.valueOf(2)),
      37
    );
  }

  // /////////////////
  // Lending
  // /////////////////

  @Test
  void loanSetChargesASingleCounterpartySignature() {
    // 1 + 1. Unlike the transaction's own signature, a lone counterparty signature is charged, which is why
    // counterpartySignatureCount defaults to one rather than zero.
    assertFeeUnits(paramsFor(loanSet()), 2);
  }

  @Test
  void loanSetChargesOwnSponsorAndCounterpartySignatures() {
    // 1 + 2 own + 2 sponsor + 3 counterparty
    assertFeeUnits(
      paramsFor(loanSet())
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(2))
        .counterpartySignatureCount(UnsignedInteger.valueOf(3)),
      8
    );
  }

  @Test
  void loanPayMultipliesTheWholeFeeByItsIncrements() {
    // 4 increments x (1 + 0 + 0)
    assertFeeUnits(paramsFor(loanPay()).loanPaymentFeeIncrements(UnsignedInteger.valueOf(4)), 4);
  }

  @Test
  void loanPayIncrementsMultiplyTheSignatureTermsToo() {
    // 2 increments x (1 + 2 own)
    assertFeeUnits(
      paramsFor(loanPay())
        .loanPaymentFeeIncrements(UnsignedInteger.valueOf(2))
        .signersCount(UnsignedInteger.valueOf(2)),
      6
    );
  }

  @Test
  void rejectsLoanPaymentFeeIncrementsOutsideTheAllowedRange() {
    assertThatThrownBy(() -> paramsFor(loanPay()).loanPaymentFeeIncrements(UnsignedInteger.ZERO).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be between 1 and 20");

    assertThatThrownBy(() -> paramsFor(loanPay()).loanPaymentFeeIncrements(UnsignedInteger.valueOf(21)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be between 1 and 20");
  }

  // /////////////////
  // Owner reserve
  // /////////////////

  @Test
  void accountDeleteCostsOneOwnerReserveFlat() {
    ComputedNetworkFees fees = FeeUtils.computeFee(FeeParams.builder()
      .feeResult(feeResult())
      .transaction(accountDelete())
      .ownerReserve(XrpCurrencyAmount.ofDrops(200000))
      .build());

    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
    assertThat(fees.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
    assertThat(fees.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
  }

  @Test
  void accountDeleteIgnoresSignatureCounts() {
    // calculateOwnerReserveFee returns the increment flat, so a multi-signed AccountDelete costs the same.
    ComputedNetworkFees fees = FeeUtils.computeFee(FeeParams.builder()
      .feeResult(feeResult())
      .transaction(accountDelete())
      .ownerReserve(XrpCurrencyAmount.ofDrops(200000))
      .signersCount(UnsignedInteger.valueOf(5))
      .build());

    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(200000));
  }

  // /////////////////
  // Batch
  // /////////////////

  @Test
  void singleAccountBatchRequiresNoBatchSigners() {
    // 2 outer + 0 batchSigners + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(ALICE, 1), innerPayment(ALICE, 2));
    assertThat(batch.requiredSigners()).isEmpty();
    assertFeeUnits(paramsFor(batch), 4);
  }

  @Test
  void multiAccountBatchDerivesItsSignersFromTheInners() {
    // 2 outer + 2 batchSigners + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertThat(batch.requiredSigners()).containsExactlyInAnyOrder(BOB, CAROL);
    assertFeeUnits(paramsFor(batch), 6);
  }

  @Test
  void batchReadsSignatureCountsFromCollectedBatchSigners() {
    // 2 outer + (1 bob + 3 carol) + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    Batch signed = Batch.builder().from(batch)
      .batchSigners(Lists.newArrayList(singleSignature(BOB), multiSignature(CAROL, 3)))
      .build();

    assertFeeUnits(paramsFor(signed), 8);
  }

  @Test
  void batchPricedBeforeSigningCanBeToldAParticipantWillMultiSign() {
    // The same 8, forecast rather than counted.
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertFeeUnits(
      paramsFor(batch).putSignaturesPerBatchSigner(CAROL, UnsignedInteger.valueOf(3)),
      8
    );
  }

  @Test
  void batchRequiredSignerNeedNotBeAnInnerAccount() {
    // 2 outer + 3 batchSigners + (1 + 1 + 1) inners, where the signers are {carol, dave, frank}.
    Batch batch = batch(ALICE,
      innerPayment(CAROL, 1),
      Payment.builder().from(innerPayment(BOB, 1)).delegate(DAVE).build(),
      Payment.builder().from(innerPayment(CAROL, 2))
        .sponsor(FRANK)
        .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
        .sponsorSignature(org.xrpl.xrpl4j.model.transactions.SponsorSignature.builder().build())
        .build()
    );

    assertThat(batch.requiredSigners()).containsExactlyInAnyOrder(CAROL, DAVE, FRANK);
    assertFeeUnits(paramsFor(batch), 8);
  }

  @Test
  void batchPricesConfidentialInnersAtTenBaseFeesEach() {
    // 2 outer + 0 batchSigners + (10 + 10) inners
    Batch batch = batch(ALICE, innerConfidentialSend(ALICE, 1), innerConfidentialSend(ALICE, 2));
    assertFeeUnits(paramsFor(batch), 22);
  }

  @Test
  void batchPricesAMixOfConfidentialAndRegularInners() {
    // 2 outer + 2 batchSigners + 2 outer signers + 3 sponsor signers + (10 + 1) inners
    Batch batch = batch(ALICE, innerConfidentialSend(BOB, 1), innerPayment(CAROL, 1));
    assertFeeUnits(
      paramsFor(batch)
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      20
    );
  }

  @Test
  void standaloneConfidentialSendCostsTenBaseFees() {
    assertFeeUnits(paramsFor(innerConfidentialSend(ALICE, 1)), 10);
  }

  @Test
  void confidentialSendChargesOwnAndSponsorSignaturesOnTopOfTheSurcharge() {
    // 1 + 2 own + 3 sponsor + 9 = 15 — the combination that has no answer today.
    assertFeeUnits(
      paramsFor(innerConfidentialSend(ALICE, 1))
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      15
    );
  }

  @Test
  void batchChargesOuterSignatureAndSponsorTerms() {
    // 2 outer + 2 batchSigners + 2 outer signers + 3 sponsor signers + (1 + 1) inners
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertFeeUnits(
      paramsFor(batch)
        .signersCount(UnsignedInteger.valueOf(2))
        .sponsorSignersCount(UnsignedInteger.valueOf(3)),
      11
    );
  }

  @Test
  void batchAddsAnOwnerReserveForEachOwnerReserveInner() {
    // 2 outer + 0 batchSigners + 1 payment inner = 3 base fees, plus one flat owner reserve.
    Batch batch = batch(ALICE, innerPayment(ALICE, 1), innerAccountDelete(ALICE, 2));
    ComputedNetworkFees fees = FeeUtils.computeFee(FeeParams.builder()
      .feeResult(feeResult())
      .transaction(batch)
      .ownerReserve(XrpCurrencyAmount.ofDrops(200000))
      .build());

    assertThat(fees.feeLow()).isEqualTo(XrpCurrencyAmount.ofDrops(3 * 1000 + 200000));
    assertThat(fees.feeMedium()).isEqualTo(XrpCurrencyAmount.ofDrops(3 * 5008 + 200000));
    assertThat(fees.feeHigh()).isEqualTo(XrpCurrencyAmount.ofDrops(3 * 10000 + 200000));
  }

  // /////////////////
  // FeeParams validation
  // /////////////////

  @Test
  void rejectsASignatureCountForAnAccountThatNeedNotSign() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    assertThatThrownBy(() -> paramsFor(batch).putSignaturesPerBatchSigner(DAVE, UnsignedInteger.valueOf(3)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("is not required to sign this Batch");
  }

  @Test
  void rejectsAForecastWhenSignaturesHaveAlreadyBeenCollected() {
    Batch batch = batch(ALICE, innerPayment(BOB, 1), innerPayment(CAROL, 1));
    Batch signed = Batch.builder().from(batch)
      .batchSigners(Lists.newArrayList(singleSignature(BOB), singleSignature(CAROL)))
      .build();

    assertThatThrownBy(() -> paramsFor(signed).putSignaturesPerBatchSigner(BOB, UnsignedInteger.valueOf(3)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("before its signatures exist");
  }

  @Test
  void rejectsFieldsThatDoNotApplyToTheTransaction() {
    assertThatThrownBy(() -> paramsFor(payment()).counterpartySignatureCount(UnsignedInteger.valueOf(2)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a LoanSet");

    assertThatThrownBy(() -> paramsFor(payment()).loanPaymentFeeIncrements(UnsignedInteger.valueOf(2)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a LoanPay");

    assertThatThrownBy(() -> paramsFor(payment()).putSignaturesPerBatchSigner(BOB, UnsignedInteger.ONE).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("applies only to a Batch");
  }

  @Test
  void rejectsAMissingOrSuperfluousOwnerReserve() {
    assertThatThrownBy(() -> paramsFor(accountDelete()).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ownerReserve must be supplied");

    assertThatThrownBy(() -> paramsFor(payment()).ownerReserve(XrpCurrencyAmount.ofDrops(1)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ownerReserve must be supplied");
  }

  @Test
  void rejectsAPseudoTransaction() {
    Transaction setFee = mock(Transaction.class);
    when(setFee.transactionType()).thenReturn(TransactionType.SET_FEE);
    assertThatThrownBy(() -> paramsFor(setFee).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("pseudo-transaction");
  }

  @Test
  void rejectsAnUnknownTransactionType() {
    Transaction unknown = mock(Transaction.class);
    when(unknown.transactionType()).thenReturn(TransactionType.UNKNOWN);
    assertThatThrownBy(() -> paramsFor(unknown).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("fee rules are not known");
  }

  @Test
  void rejectsSignatureCountsBeyondTheSignerListLimit() {
    assertThatThrownBy(() -> paramsFor(payment()).signersCount(UnsignedInteger.valueOf(33)).build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must not exceed 32");
  }

  // /////////////////
  // Helpers
  // /////////////////

  private void assertFeeUnits(final ImmutableFeeParams.Builder builder, final long expectedUnits) {
    assertThat(FeeUtils.computeFee(builder.build()).feeLow())
      .isEqualTo(XrpCurrencyAmount.ofDrops(expectedUnits * 1000));
  }

  private ImmutableFeeParams.Builder paramsFor(final Transaction transaction) {
    return FeeParams.builder().feeResult(feeResult()).transaction(transaction);
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

  private Payment sponsoredPayment() {
    return Payment.builder().from(payment())
      .sponsor(FRANK)
      .sponsorFlags(SponsorFlags.SPONSOR_FEE)
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

  private ConfidentialMptSend innerConfidentialSend(final Address account, final int sequence) {
    return ConfidentialMptSend.builder()
      .account(account)
      .destination(DAVE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.valueOf(sequence))
      .flags(org.xrpl.xrpl4j.model.flags.TransactionFlags.INNER_BATCH_TXN)
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179" + Strings.repeat("11", 20)))
      .senderEncryptedAmount(EncryptedAmount.of(Strings.repeat("AB", 66)))
      .destinationEncryptedAmount(EncryptedAmount.of(Strings.repeat("CD", 66)))
      .issuerEncryptedAmount(EncryptedAmount.of(Strings.repeat("EF", 66)))
      .zkProof(ConfidentialMptSendProof.fromHex(Strings.repeat("34", 946)))
      .amountCommitment(Commitment.of(Strings.repeat("02", 33)))
      .balanceCommitment(Commitment.of(Strings.repeat("03", 33)))
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

  private AccountDelete accountDelete() {
    return AccountDelete.builder()
      .account(ALICE)
      .destination(BOB)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY)
      .build();
  }

  private EscrowFinish escrowFinish(final boolean withFulfillment) {
    ImmutableEscrowFinish.Builder builder = EscrowFinish.builder()
      .account(ALICE)
      .owner(BOB)
      .offerSequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PUBLIC_KEY);
    return withFulfillment ?
      builder.fulfillment(PreimageSha256Fulfillment.from(new byte[32])).build() : builder.build();
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

  private BatchSignerWrapper multiSignature(final Address account, final int signatureCount) {
    List<SignerWrapper> signers = Lists.newArrayList();
    for (int i = 0; i < signatureCount; i++) {
      signers.add(SignerWrapper.of(Signer.builder()
        .signingPublicKey(PUBLIC_KEY)
        .transactionSignature(Signature.fromBase16("ABCD"))
        .build()));
    }
    return BatchSignerWrapper.of(BatchSigner.builder().account(account).signers(signers).build());
  }

  private FeeResult feeResult() {
    return FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.ONE)
      .drops(FeeDrops.builder()
        .baseFee(XrpCurrencyAmount.ofDrops(10))
        .medianFee(XrpCurrencyAmount.ofDrops(10000))
        .minimumFee(XrpCurrencyAmount.ofDrops(10))
        .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
        .build())
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(FeeLevels.builder()
        .medianLevel(XrpCurrencyAmount.ofDrops(256000))
        .minimumLevel(XrpCurrencyAmount.ofDrops(256))
        .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
        .referenceLevel(XrpCurrencyAmount.ofDrops(256))
        .build())
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();
  }
}
