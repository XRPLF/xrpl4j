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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.LoanPay;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The inputs to {@link FeeUtils#computeFee(FeeParams)}.
 *
 * <p>Everything that can be read from the {@link #transaction()} is read from it — the transaction type and therefore
 * any per-type surcharge, an {@code EscrowFinish}'s fulfillment, a
 * {@link org.xrpl.xrpl4j.model.transactions.Batch Batch}'s inner transactions and the accounts
 * that must sign it. The remaining fields are the things that are not knowable from the transaction: how each party
 * intends to sign, which cannot be derived because the fee is signed over and the signatures therefore do not exist
 * yet, plus two values that {@link FeeResult} does not carry.
 *
 * <p>Every field other than {@link #feeResult()} and {@link #transaction()} has a default, so the common case is
 * {@code FeeParams.of(feeResult, transaction).build()}.
 *
 * <p>Prefer the type-scoped entry points, each of which exposes only the inputs that apply to the transaction being
 * priced — so the builder's method list doubles as the checklist of what that transaction type needs:
 * <ul>
 *   <li>{@link #of(FeeResult, Transaction)} for any generically-priced transaction;</li>
 *   <li>{@link #forBatch(FeeResult, Batch)} for a {@code Batch};</li>
 *   <li>{@link #forLoanSet(FeeResult, LoanSet)} for a {@code LoanSet};</li>
 *   <li>{@link #forLoanPay(FeeResult, LoanPay)} for a {@code LoanPay}; and</li>
 *   <li>{@link #forOwnerReserve(FeeResult, Transaction, XrpCurrencyAmount)} for an {@code AccountDelete} or
 *       {@code AMMCreate}.</li>
 * </ul>
 * The flat {@link #builder()} remains available and accepts every input, validating their mutual consistency at
 * {@code build()} time.
 */
@Value.Immutable
public interface FeeParams {

  /**
   * The maximum number of entries in an XRPL signer list, and therefore the most signatures any one party can supply.
   */
  UnsignedInteger MAX_SIGNER_LIST_SIZE = UnsignedInteger.valueOf(32);

  /**
   * The largest number of fee increments a {@code LoanPay} transaction can be charged, being
   * {@code kLoanMaximumPaymentsPerTransaction / kLoanPaymentsPerFeeIncrement}.
   */
  UnsignedInteger MAX_LOAN_PAYMENT_FEE_INCREMENTS = UnsignedInteger.valueOf(20);

  /**
   * The {@link TransactionType}s whose fee is one owner reserve increment rather than a multiple of the base fee.
   */
  Set<TransactionType> OWNER_RESERVE_TRANSACTION_TYPES = ImmutableSet.of(
    TransactionType.ACCOUNT_DELETE,
    TransactionType.AMM_CREATE
  );

  /**
   * The pseudo-transaction {@link TransactionType}s, which are created by consensus rather than submitted, and
   * therefore carry no fee.
   */
  Set<TransactionType> PSEUDO_TRANSACTION_TYPES = ImmutableSet.of(
    TransactionType.ENABLE_AMENDMENT,
    TransactionType.SET_FEE,
    TransactionType.UNL_MODIFY
  );

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableFeeParams.Builder}.
   */
  static ImmutableFeeParams.Builder builder() {
    return ImmutableFeeParams.builder();
  }

  /**
   * Begin pricing a transaction whose fee follows the generic formula — one base fee, plus one per own or sponsor
   * multi-signature, plus any surcharge derived from the transaction itself (an {@code EscrowFinish} fulfillment, a
   * confidential MPT type). The returned builder exposes only the two universal inputs,
   * {@link GenericBuilder#signersCount(UnsignedInteger)} and
   * {@link GenericBuilder#sponsorSignersCount(UnsignedInteger)}.
   *
   * <p>A transaction type with pricing inputs of its own is refused here, with a pointer to its entry point, so that
   * an input it needs cannot be silently defaulted: use {@link #forBatch(FeeResult, Batch)},
   * {@link #forLoanSet(FeeResult, LoanSet)}, {@link #forLoanPay(FeeResult, LoanPay)} or
   * {@link #forOwnerReserve(FeeResult, Transaction, XrpCurrencyAmount)} for those.
   *
   * @param feeResult   The current network fee levels.
   * @param transaction The {@link Transaction} to price.
   *
   * @return A {@link GenericBuilder}.
   */
  static GenericBuilder of(final FeeResult feeResult, final Transaction transaction) {
    Objects.requireNonNull(feeResult);
    Objects.requireNonNull(transaction);

    final TransactionType transactionType = transaction.transactionType();
    Preconditions.checkArgument(
      transactionType != TransactionType.BATCH,
      "A Batch has pricing inputs of its own. Use FeeParams.forBatch(feeResult, batch), which exposes a signature " +
        "count per batch signer and an owner reserve for AccountDelete/AMMCreate inner transactions."
    );
    Preconditions.checkArgument(
      transactionType != TransactionType.LOAN_SET,
      "A LoanSet has a pricing input of its own. Use FeeParams.forLoanSet(feeResult, loanSet), which exposes " +
        "counterpartySignatureCount."
    );
    Preconditions.checkArgument(
      transactionType != TransactionType.LOAN_PAY,
      "A LoanPay has a pricing input of its own. Use FeeParams.forLoanPay(feeResult, loanPay), which exposes " +
        "loanPaymentFeeIncrements."
    );
    Preconditions.checkArgument(
      !OWNER_RESERVE_TRANSACTION_TYPES.contains(transactionType),
      "%s costs one owner reserve increment rather than a multiple of the base fee. Use " +
        "FeeParams.forOwnerReserve(feeResult, transaction, ownerReserve), which requires the increment up front.",
      transactionType
    );
    return new GenericBuilder(feeResult, transaction);
  }

  /**
   * Begin pricing a {@link Batch}. The returned builder exposes the outer account's own and sponsor signature counts,
   * and an owner reserve for {@code AccountDelete}/{@code AMMCreate} inner transactions.
   *
   * <p>A Batch must already carry its {@link Batch#batchSigners()} to be priced, because every batch signature costs
   * one base fee and the count is read from those entries rather than forecast. This is not a restriction on the
   * signing order: {@code serializeBatch} excludes the outer {@code Fee} from what the participants sign, so the
   * sequence is collect the batch signatures, price the Batch, attach the fee, then have the outer account sign last.
   * A Batch whose every inner belongs to the outer account has no {@link Batch#requiredSigners()} and therefore needs
   * no batch signers.
   *
   * @param feeResult The current network fee levels.
   * @param batch     The {@link Batch} to price.
   *
   * @return A {@link BatchBuilder}.
   */
  static BatchBuilder forBatch(final FeeResult feeResult, final Batch batch) {
    return new BatchBuilder(Objects.requireNonNull(feeResult), Objects.requireNonNull(batch));
  }

  /**
   * Begin pricing a {@link LoanSet}, whose counterparty's signature — unlike the transaction's own — is charged even
   * when it is a single one. The returned builder exposes
   * {@link LoanSetBuilder#counterpartySignatureCount(UnsignedInteger)} alongside the two universal signature counts;
   * leave it unset when the counterparty signs with a single key.
   *
   * @param feeResult The current network fee levels.
   * @param loanSet   The {@link LoanSet} to price.
   *
   * @return A {@link LoanSetBuilder}.
   */
  static LoanSetBuilder forLoanSet(final FeeResult feeResult, final LoanSet loanSet) {
    return new LoanSetBuilder(Objects.requireNonNull(feeResult), Objects.requireNonNull(loanSet));
  }

  /**
   * Begin pricing a {@link LoanPay}, whose whole fee is multiplied by its number of fee increments. The returned
   * builder exposes {@link LoanPayBuilder#loanPaymentFeeIncrements(UnsignedInteger)} alongside the two universal
   * signature counts; leave it unset for a single payment.
   *
   * @param feeResult The current network fee levels.
   * @param loanPay   The {@link LoanPay} to price.
   *
   * @return A {@link LoanPayBuilder}.
   */
  static LoanPayBuilder forLoanPay(final FeeResult feeResult, final LoanPay loanPay) {
    return new LoanPayBuilder(Objects.requireNonNull(feeResult), Objects.requireNonNull(loanPay));
  }

  /**
   * Price a transaction that costs one owner reserve increment flat — an {@code AccountDelete} or {@code AMMCreate}.
   * The increment replaces the base-fee formula entirely, so there are no other inputs: signature counts are ignored
   * by rippled for these types, and this method therefore returns a finished {@link FeeParams} rather than a builder.
   *
   * @param feeResult    The current network fee levels.
   * @param transaction  The {@link Transaction} to price, which must be one of
   *                     {@link #OWNER_RESERVE_TRANSACTION_TYPES}.
   * @param ownerReserve The owner reserve increment, e.g. {@code serverInfo.validatedLedger().get().reserveIncXrp()}.
   *
   * @return A {@link FeeParams}.
   */
  static FeeParams forOwnerReserve(
    final FeeResult feeResult, final Transaction transaction, final XrpCurrencyAmount ownerReserve
  ) {
    Objects.requireNonNull(feeResult);
    Objects.requireNonNull(transaction);
    Objects.requireNonNull(ownerReserve);
    Preconditions.checkArgument(
      OWNER_RESERVE_TRANSACTION_TYPES.contains(transaction.transactionType()),
      "forOwnerReserve applies only to %s, but the transaction is a %s. Use FeeParams.of(feeResult, transaction) " +
        "for a generically-priced type.",
      OWNER_RESERVE_TRANSACTION_TYPES, transaction.transactionType()
    );
    return builder().feeResult(feeResult).transaction(transaction).ownerReserve(ownerReserve).build();
  }

  /**
   * The current network fee levels, obtained by querying the ledger (e.g., via {@code XrplClient#fee()}).
   *
   * @return A {@link FeeResult}.
   */
  FeeResult feeResult();

  /**
   * The transaction being priced. It need not be signed, and its {@code Fee} field is ignored; a placeholder of zero
   * is customary while the real fee is being computed.
   *
   * @return The {@link Transaction} to price.
   */
  Transaction transaction();

  /**
   * The number of signatures the transaction's own account will supply in its {@code Signers} array.
   *
   * <p>Zero for a single-signed transaction: rippled counts only {@code Signers} entries, and a lone signature is
   * carried in {@code TxnSignature} instead. For a multi-signature this is the full size of the {@code Signers}
   * array (e.g. 4 for a 4-of-N signer list), not one fewer — rippled charges {@code base × (1 + signersCount)}.
   *
   * <p>An absent value is priced as zero. Leaving it absent (rather than defaulting it to zero) lets a computed
   * {@link FeeBreakdown} distinguish "the caller did not say" from "the caller confirmed a single signature": the
   * former is reported as {@link FeeTerm.Provenance#ASSUMED}, the latter as {@link FeeTerm.Provenance#SPECIFIED}. Pass
   * {@link UnsignedInteger#ZERO} explicitly to assert that this account single-signs.
   *
   * @return An optionally-present {@link UnsignedInteger} number of signatures, priced as zero when absent.
   */
  Optional<UnsignedInteger> signersCount();

  /**
   * The number of signatures the transaction's sponsor will supply in its {@code SponsorSignature.Signers} array.
   *
   * <p>Zero when the transaction is unsponsored, and also when the sponsor signs with a single key: rippled counts
   * only {@code SponsorSignature.Signers} entries, and a lone sponsor signature is carried in
   * {@code SponsorSignature.TxnSignature} instead.
   *
   * <p>An absent value is priced as zero, and is distinguished from an explicit zero in a computed
   * {@link FeeBreakdown} exactly as {@link #signersCount()} is.
   *
   * @return An optionally-present {@link UnsignedInteger} number of signatures, priced as zero when absent.
   */
  Optional<UnsignedInteger> sponsorSignersCount();

  /**
   * The total number of signatures in a {@code LoanSet}'s {@code CounterpartySignature}, applicable only when
   * {@link #transaction()} is a {@link org.xrpl.xrpl4j.model.transactions.LoanSet}.
   *
   * <p>Unlike {@link #signersCount()} and {@link #sponsorSignersCount()} — which are zero when the lone signature
   * rides in {@code TxnSignature} — a single counterparty signature is itself charged: rippled counts
   * {@code CounterpartySignature.TxnSignature} as one. An absent value is therefore priced as one, the correct count
   * for a counterparty signing with a single key. Leaving the value absent (rather than defaulting it to one) lets
   * {@link #check()} reject the field on any non-{@code LoanSet} transaction, even when the supplied value happens to
   * equal the default.
   *
   * @return An optionally-present {@link UnsignedInteger} number of signatures, priced as one when absent.
   */
  Optional<UnsignedInteger> counterpartySignatureCount();

  /**
   * The number of fee increments a {@code LoanPay} transaction will be charged, being one increment per
   * {@code kLoanPaymentsPerFeeIncrement} payments the transaction is estimated to make. Applicable only when
   * {@link #transaction()} is a {@link org.xrpl.xrpl4j.model.transactions.LoanPay}.
   *
   * <p>Deriving this requires the {@code Loan}, {@code LoanBroker} and {@code Vault} ledger objects, so it is
   * supplied rather than computed. An absent value is priced as one — a single payment. Leaving the value absent
   * (rather than defaulting it to one) lets {@link #check()} reject the field on any non-{@code LoanPay} transaction,
   * even when the supplied value happens to equal the default.
   *
   * @return An optionally-present {@link UnsignedInteger} number of fee increments, priced as one when absent.
   */
  Optional<UnsignedInteger> loanPaymentFeeIncrements();

  /**
   * The owner reserve increment, required when pricing an {@code AccountDelete} or {@code AMMCreate} — including one
   * that is an inner transaction of a {@link org.xrpl.xrpl4j.model.transactions.Batch Batch}.
   *
   * <p>Those transactions cost exactly one owner reserve increment rather than a multiple of the base fee, and that
   * increment is not carried on {@link FeeResult}. It can be read from {@code ServerInfo} (e.g.
   * {@code serverInfo.validatedLedger().get().reserveIncXrp()}).
   *
   * @return An optionally-present {@link XrpCurrencyAmount}.
   */
  Optional<XrpCurrencyAmount> ownerReserve();

  /**
   * Validates that the supplied fields are consistent with the {@link #transaction()} being priced, so that a value
   * which would be silently ignored is rejected instead.
   */
  @Value.Check
  default void check() {
    final TransactionType transactionType = this.transaction().transactionType();

    Preconditions.checkArgument(
      !PSEUDO_TRANSACTION_TYPES.contains(transactionType),
      "%s is a pseudo-transaction. Pseudo-transactions are created by consensus rather than submitted, and carry " +
        "no fee.", transactionType
    );
    Preconditions.checkArgument(
      transactionType != TransactionType.UNKNOWN,
      "The fee of an unknown transaction type cannot be computed, because its fee rules are not known."
    );

    this.signersCount().ifPresent(count -> checkSignatureCount(count, UnsignedInteger.ZERO, "signersCount"));
    this.sponsorSignersCount().ifPresent(
      count -> checkSignatureCount(count, UnsignedInteger.ZERO, "sponsorSignersCount")
    );

    Preconditions.checkArgument(
      transactionType == TransactionType.LOAN_SET || !this.counterpartySignatureCount().isPresent(),
      "counterpartySignatureCount applies only to a LoanSet, but the transaction is a %s.", transactionType
    );
    this.counterpartySignatureCount().ifPresent(
      count -> checkSignatureCount(count, UnsignedInteger.ONE, "counterpartySignatureCount")
    );

    Preconditions.checkArgument(
      transactionType == TransactionType.LOAN_PAY || !this.loanPaymentFeeIncrements().isPresent(),
      "loanPaymentFeeIncrements applies only to a LoanPay, but the transaction is a %s.", transactionType
    );
    this.loanPaymentFeeIncrements().ifPresent(increments -> Preconditions.checkArgument(
      increments.compareTo(UnsignedInteger.ONE) >= 0 &&
        increments.compareTo(MAX_LOAN_PAYMENT_FEE_INCREMENTS) <= 0,
      "loanPaymentFeeIncrements must be between 1 and %s, but was %s.",
      MAX_LOAN_PAYMENT_FEE_INCREMENTS, increments
    ));

    Preconditions.checkArgument(
      this.ownerReserve().isPresent() == this.requiresOwnerReserve(),
      "ownerReserve must be supplied for %s, and only for those types, whether standalone or as a Batch inner " +
        "transaction. Transaction was a %s.", OWNER_RESERVE_TRANSACTION_TYPES, transactionType
    );

    this.checkBatchIsSigned(transactionType);
  }

  /**
   * Determines whether the transaction being priced costs an owner reserve increment, either because it is one of
   * {@link #OWNER_RESERVE_TRANSACTION_TYPES} or because it is a {@link org.xrpl.xrpl4j.model.transactions.Batch Batch}
   * containing one.
   *
   * @return {@code true} if {@link #ownerReserve()} is required.
   */
  @Value.Derived
  default boolean requiresOwnerReserve() {
    if (OWNER_RESERVE_TRANSACTION_TYPES.contains(this.transaction().transactionType())) {
      return true;
    }

    return this.transaction() instanceof Batch &&
      ((Batch) this.transaction()).rawTransactions().stream()
        .map(RawTransactionWrapper::rawTransaction)
        .map(Transaction::transactionType)
        .anyMatch(OWNER_RESERVE_TRANSACTION_TYPES::contains);
  }

  /**
   * Asserts that a {@link Batch} being priced already carries the {@code BatchSigners} its inner transactions require.
   *
   * <p>Every batch signature costs one base fee, and the only exact source of that count is
   * {@link Batch#batchSigners()}. Forecasting it would silently under-price a Batch whose participant multi-signs, so
   * an unsigned Batch is refused instead. This constrains the order of operations, not the design: because
   * {@code serializeBatch} excludes the outer {@code Fee} from what the participants sign, the batch signatures can be
   * collected first and the fee attached afterwards.
   *
   * @param transactionType The {@link TransactionType} of {@link #transaction()}.
   */
  default void checkBatchIsSigned(final TransactionType transactionType) {
    if (transactionType != TransactionType.BATCH) {
      return;
    }

    final Batch batch = (Batch) this.transaction();
    final Set<Address> requiredSigners = batch.requiredSigners();
    Preconditions.checkArgument(
      !batch.batchSigners().isEmpty() || requiredSigners.isEmpty(),
      "A Batch cannot be priced until its BatchSigners are collected, because each batch signature costs one base " +
        "fee. %s must sign first. Note that serializeBatch excludes the outer Fee from what the participants sign, " +
        "so the order is: collect the batch signatures, price the Batch, attach the fee, then have the outer account " +
        "sign last.", requiredSigners
    );
  }

  /**
   * Asserts that a supplied signature count lies within the range a signer list allows: at least {@code minimum} —
   * zero for a count of additional signatures, one for a total that always includes a first signature — and no more
   * than the {@link #MAX_SIGNER_LIST_SIZE XRPL signer list limit}.
   *
   * @param signatureCount An {@link UnsignedInteger} number of signatures.
   * @param minimum        The smallest permitted value, either {@link UnsignedInteger#ZERO} or
   *                       {@link UnsignedInteger#ONE}.
   * @param fieldName      The name of the field being checked, for use in the failure message.
   */
  static void checkSignatureCount(
    final UnsignedInteger signatureCount, final UnsignedInteger minimum, final String fieldName
  ) {
    Preconditions.checkArgument(
      signatureCount.compareTo(minimum) >= 0 && signatureCount.compareTo(MAX_SIGNER_LIST_SIZE) <= 0,
      "%s must be between %s and %s (the XRPL signer list limit), but was %s.",
      fieldName, minimum, MAX_SIGNER_LIST_SIZE, signatureCount
    );
  }

  /**
   * A builder for pricing a generically-priced transaction, obtained from {@link #of(FeeResult, Transaction)}. It
   * exposes only the two inputs every transaction type shares — the transaction's own and its sponsor's signature
   * counts.
   */
  class GenericBuilder {

    private final FeeResult feeResult;
    private final Transaction transaction;
    private UnsignedInteger signersCount;
    private UnsignedInteger sponsorSignersCount;

    private GenericBuilder(final FeeResult feeResult, final Transaction transaction) {
      this.feeResult = feeResult;
      this.transaction = transaction;
    }

    /**
     * The number of signatures the transaction's own account will supply in its {@code Signers} array; see
     * {@link FeeParams#signersCount()}. Leave unset for a single-signed transaction.
     *
     * @param signersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public GenericBuilder signersCount(final UnsignedInteger signersCount) {
      FeeParams.checkSignatureCount(Objects.requireNonNull(signersCount), UnsignedInteger.ZERO, "signersCount");
      this.signersCount = signersCount;
      return this;
    }

    /**
     * The number of signatures the transaction's sponsor will supply in its {@code SponsorSignature.Signers} array;
     * see {@link FeeParams#sponsorSignersCount()}. Leave unset when the transaction is unsponsored or the sponsor
     * signs with a single key.
     *
     * @param sponsorSignersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public GenericBuilder sponsorSignersCount(final UnsignedInteger sponsorSignersCount) {
      FeeParams.checkSignatureCount(
        Objects.requireNonNull(sponsorSignersCount), UnsignedInteger.ZERO, "sponsorSignersCount"
      );
      this.sponsorSignersCount = sponsorSignersCount;
      return this;
    }

    /**
     * Builds the {@link FeeParams}.
     *
     * @return A {@link FeeParams}.
     */
    public FeeParams build() {
      return FeeParams.builder()
        .feeResult(feeResult)
        .transaction(transaction)
        .signersCount(Optional.ofNullable(signersCount))
        .sponsorSignersCount(Optional.ofNullable(sponsorSignersCount))
        .build();
    }
  }

  /**
   * A builder for pricing a {@link Batch}, obtained from {@link #forBatch(FeeResult, Batch)}. Its inputs are
   * validated eagerly, per call, so a mistake is reported at the line that made it. The per-batch-signature counts are
   * not inputs: they are read from {@link Batch#batchSigners()}, which the Batch must already carry.
   */
  class BatchBuilder {

    private final FeeResult feeResult;
    private final Batch batch;
    private UnsignedInteger signersCount;
    private UnsignedInteger sponsorSignersCount;
    private XrpCurrencyAmount ownerReserve;

    private BatchBuilder(final FeeResult feeResult, final Batch batch) {
      this.feeResult = feeResult;
      this.batch = batch;
    }

    /**
     * The number of signatures the outer account will supply in the Batch's own {@code Signers} array; see
     * {@link FeeParams#signersCount()}. Leave unset for a single-signed Batch.
     *
     * @param signersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public BatchBuilder signersCount(final UnsignedInteger signersCount) {
      FeeParams.checkSignatureCount(Objects.requireNonNull(signersCount), UnsignedInteger.ZERO, "signersCount");
      this.signersCount = signersCount;
      return this;
    }

    /**
     * The number of signatures the Batch's sponsor will supply in its {@code SponsorSignature.Signers} array; see
     * {@link FeeParams#sponsorSignersCount()}.
     *
     * @param sponsorSignersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public BatchBuilder sponsorSignersCount(final UnsignedInteger sponsorSignersCount) {
      FeeParams.checkSignatureCount(
        Objects.requireNonNull(sponsorSignersCount), UnsignedInteger.ZERO, "sponsorSignersCount"
      );
      this.sponsorSignersCount = sponsorSignersCount;
      return this;
    }

    /**
     * The owner reserve increment, required when (and only when) an inner transaction is an {@code AccountDelete} or
     * {@code AMMCreate}; see {@link FeeParams#ownerReserve()}.
     *
     * @param ownerReserve An {@link XrpCurrencyAmount}.
     *
     * @return This builder.
     */
    public BatchBuilder ownerReserve(final XrpCurrencyAmount ownerReserve) {
      this.ownerReserve = Objects.requireNonNull(ownerReserve);
      return this;
    }

    /**
     * Builds the {@link FeeParams}.
     *
     * @return A {@link FeeParams}.
     */
    public FeeParams build() {
      final ImmutableFeeParams.Builder builder = FeeParams.builder()
        .feeResult(feeResult)
        .transaction(batch)
        .signersCount(Optional.ofNullable(signersCount))
        .sponsorSignersCount(Optional.ofNullable(sponsorSignersCount));
      if (ownerReserve != null) {
        builder.ownerReserve(ownerReserve);
      }
      return builder.build();
    }
  }

  /**
   * A builder for pricing a {@link LoanSet}, obtained from {@link #forLoanSet(FeeResult, LoanSet)}.
   */
  class LoanSetBuilder {

    private final FeeResult feeResult;
    private final LoanSet loanSet;
    private UnsignedInteger signersCount;
    private UnsignedInteger sponsorSignersCount;
    private UnsignedInteger counterpartySignatureCount;

    private LoanSetBuilder(final FeeResult feeResult, final LoanSet loanSet) {
      this.feeResult = feeResult;
      this.loanSet = loanSet;
    }

    /**
     * The total number of signatures in the {@code LoanSet}'s {@code CounterpartySignature}; see
     * {@link FeeParams#counterpartySignatureCount()}. Leave unset when the counterparty signs with a single key,
     * which is charged as one.
     *
     * @param counterpartySignatureCount An {@link UnsignedInteger} number of signatures, at least one.
     *
     * @return This builder.
     */
    public LoanSetBuilder counterpartySignatureCount(final UnsignedInteger counterpartySignatureCount) {
      FeeParams.checkSignatureCount(
        Objects.requireNonNull(counterpartySignatureCount), UnsignedInteger.ONE, "counterpartySignatureCount"
      );
      this.counterpartySignatureCount = counterpartySignatureCount;
      return this;
    }

    /**
     * The number of signatures the transaction's own account will supply in its {@code Signers} array; see
     * {@link FeeParams#signersCount()}.
     *
     * @param signersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public LoanSetBuilder signersCount(final UnsignedInteger signersCount) {
      FeeParams.checkSignatureCount(Objects.requireNonNull(signersCount), UnsignedInteger.ZERO, "signersCount");
      this.signersCount = signersCount;
      return this;
    }

    /**
     * The number of signatures the transaction's sponsor will supply in its {@code SponsorSignature.Signers} array;
     * see {@link FeeParams#sponsorSignersCount()}.
     *
     * @param sponsorSignersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public LoanSetBuilder sponsorSignersCount(final UnsignedInteger sponsorSignersCount) {
      FeeParams.checkSignatureCount(
        Objects.requireNonNull(sponsorSignersCount), UnsignedInteger.ZERO, "sponsorSignersCount"
      );
      this.sponsorSignersCount = sponsorSignersCount;
      return this;
    }

    /**
     * Builds the {@link FeeParams}.
     *
     * @return A {@link FeeParams}.
     */
    public FeeParams build() {
      final ImmutableFeeParams.Builder builder = FeeParams.builder()
        .feeResult(feeResult)
        .transaction(loanSet)
        .signersCount(Optional.ofNullable(signersCount))
        .sponsorSignersCount(Optional.ofNullable(sponsorSignersCount));
      if (counterpartySignatureCount != null) {
        builder.counterpartySignatureCount(counterpartySignatureCount);
      }
      return builder.build();
    }
  }

  /**
   * A builder for pricing a {@link LoanPay}, obtained from {@link #forLoanPay(FeeResult, LoanPay)}.
   */
  class LoanPayBuilder {

    private final FeeResult feeResult;
    private final LoanPay loanPay;
    private UnsignedInteger signersCount;
    private UnsignedInteger sponsorSignersCount;
    private UnsignedInteger loanPaymentFeeIncrements;

    private LoanPayBuilder(final FeeResult feeResult, final LoanPay loanPay) {
      this.feeResult = feeResult;
      this.loanPay = loanPay;
    }

    /**
     * The number of fee increments the {@code LoanPay} will be charged; see
     * {@link FeeParams#loanPaymentFeeIncrements()}. Leave unset for a single payment, which is charged as one
     * increment.
     *
     * @param loanPaymentFeeIncrements An {@link UnsignedInteger} number of increments, between 1 and
     *                                 {@link FeeParams#MAX_LOAN_PAYMENT_FEE_INCREMENTS}.
     *
     * @return This builder.
     */
    public LoanPayBuilder loanPaymentFeeIncrements(final UnsignedInteger loanPaymentFeeIncrements) {
      Objects.requireNonNull(loanPaymentFeeIncrements);
      Preconditions.checkArgument(
        loanPaymentFeeIncrements.compareTo(UnsignedInteger.ONE) >= 0 &&
          loanPaymentFeeIncrements.compareTo(MAX_LOAN_PAYMENT_FEE_INCREMENTS) <= 0,
        "loanPaymentFeeIncrements must be between 1 and %s, but was %s.",
        MAX_LOAN_PAYMENT_FEE_INCREMENTS, loanPaymentFeeIncrements
      );
      this.loanPaymentFeeIncrements = loanPaymentFeeIncrements;
      return this;
    }

    /**
     * The number of signatures the transaction's own account will supply in its {@code Signers} array; see
     * {@link FeeParams#signersCount()}.
     *
     * @param signersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public LoanPayBuilder signersCount(final UnsignedInteger signersCount) {
      FeeParams.checkSignatureCount(Objects.requireNonNull(signersCount), UnsignedInteger.ZERO, "signersCount");
      this.signersCount = signersCount;
      return this;
    }

    /**
     * The number of signatures the transaction's sponsor will supply in its {@code SponsorSignature.Signers} array;
     * see {@link FeeParams#sponsorSignersCount()}.
     *
     * @param sponsorSignersCount An {@link UnsignedInteger} number of signatures.
     *
     * @return This builder.
     */
    public LoanPayBuilder sponsorSignersCount(final UnsignedInteger sponsorSignersCount) {
      FeeParams.checkSignatureCount(
        Objects.requireNonNull(sponsorSignersCount), UnsignedInteger.ZERO, "sponsorSignersCount"
      );
      this.sponsorSignersCount = sponsorSignersCount;
      return this;
    }

    /**
     * Builds the {@link FeeParams}.
     *
     * @return A {@link FeeParams}.
     */
    public FeeParams build() {
      final ImmutableFeeParams.Builder builder = FeeParams.builder()
        .feeResult(feeResult)
        .transaction(loanPay)
        .signersCount(Optional.ofNullable(signersCount))
        .sponsorSignersCount(Optional.ofNullable(sponsorSignersCount));
      if (loanPaymentFeeIncrements != null) {
        builder.loanPaymentFeeIncrements(loanPaymentFeeIncrements);
      }
      return builder.build();
    }
  }
}
