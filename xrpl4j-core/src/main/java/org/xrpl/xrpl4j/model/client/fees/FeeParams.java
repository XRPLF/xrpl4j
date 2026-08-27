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

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
 * {@code FeeParams.builder().feeResult(feeResult).transaction(transaction).build()}.
 *
 * <p>This class will be marked {@link Beta} until the featureBatch and featureSponsorship amendments are enabled on
 * mainnet. Its API is subject to change.</p>
 */
@Value.Immutable
@Beta
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
   * <p>Zero for a single-signed transaction: rippled charges only for the <em>additional</em> signatures of a
   * multi-signature, since the first is already covered by the base fee.
   *
   * @return An {@link UnsignedInteger} number of signatures, defaulting to zero.
   */
  @Value.Default
  default UnsignedInteger signersCount() {
    return UnsignedInteger.ZERO;
  }

  /**
   * The number of signatures the transaction's sponsor will supply in its {@code SponsorSignature.Signers} array.
   *
   * <p>Zero when the transaction is unsponsored, and also when the sponsor signs with a single key: rippled counts
   * only {@code SponsorSignature.Signers} entries, and a lone sponsor signature is carried in
   * {@code SponsorSignature.TxnSignature} instead.
   *
   * @return An {@link UnsignedInteger} number of signatures, defaulting to zero.
   */
  @Value.Default
  default UnsignedInteger sponsorSignersCount() {
    return UnsignedInteger.ZERO;
  }

  /**
   * The total number of signatures in a {@code LoanSet}'s {@code CounterpartySignature}.
   *
   * <p>Unlike {@link #signersCount()} and {@link #sponsorSignersCount()}, this is a total rather than a count of
   * additional signatures: rippled charges a base fee even for a single counterparty signature. One is therefore the
   * correct value for a counterparty signing with a single key, and is the default.
   *
   * @return An {@link UnsignedInteger} number of signatures, defaulting to one.
   */
  @Value.Default
  default UnsignedInteger counterpartySignatureCount() {
    return UnsignedInteger.ONE;
  }

  /**
   * The number of fee increments a {@code LoanPay} transaction will be charged, being one increment per
   * {@code kLoanPaymentsPerFeeIncrement} payments the transaction is estimated to make.
   *
   * <p>Deriving this requires the {@code Loan}, {@code LoanBroker} and {@code Vault} ledger objects, so it is supplied
   * rather than computed. One — a single payment — is the default.
   *
   * @return An {@link UnsignedInteger} number of fee increments, defaulting to one.
   */
  @Value.Default
  default UnsignedInteger loanPaymentFeeIncrements() {
    return UnsignedInteger.ONE;
  }

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
   * How many signatures each {@link org.xrpl.xrpl4j.model.transactions.Batch Batch} participant will supply, for
   * participants that multi-sign.
   *
   * <p>This is needed only when pricing a Batch <em>before</em> its {@code BatchSigners} exist — a wallet displaying
   * a fee up front, for instance. Once signatures have been collected, the counts are read from
   * {@link org.xrpl.xrpl4j.model.transactions.Batch#batchSigners() Batch#batchSigners()} and this map is neither
   * needed nor permitted. Participants absent from the map are
   * counted as signing with a single key.
   *
   * <p>Keys must be members of
   * {@link org.xrpl.xrpl4j.model.transactions.Batch#requiredSigners() Batch#requiredSigners()} — the accounts that
   * must sign, derived from the inner
   * transactions. Note that a required signer is not always an inner's {@code Account}: a delegated inner is signed by
   * its {@code Delegate}, and a sponsored inner also requires its {@code Sponsor}.
   *
   * @return A {@link Map} from a Batch participant's {@link Address} to the number of signatures it will supply,
   *   defaulting to empty.
   */
  Map<Address, UnsignedInteger> signaturesPerBatchSigner();

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

    checkSignatureCount(this.signersCount(), "signersCount");
    checkSignatureCount(this.sponsorSignersCount(), "sponsorSignersCount");
    checkSignatureCount(this.counterpartySignatureCount(), "counterpartySignatureCount");

    Preconditions.checkArgument(
      transactionType == TransactionType.LOAN_SET ||
        this.counterpartySignatureCount().equals(UnsignedInteger.ONE),
      "counterpartySignatureCount applies only to a LoanSet, but the transaction is a %s.", transactionType
    );

    Preconditions.checkArgument(
      transactionType == TransactionType.LOAN_PAY ||
        this.loanPaymentFeeIncrements().equals(UnsignedInteger.ONE),
      "loanPaymentFeeIncrements applies only to a LoanPay, but the transaction is a %s.", transactionType
    );
    Preconditions.checkArgument(
      this.loanPaymentFeeIncrements().compareTo(UnsignedInteger.ONE) >= 0 &&
        this.loanPaymentFeeIncrements().compareTo(MAX_LOAN_PAYMENT_FEE_INCREMENTS) <= 0,
      "loanPaymentFeeIncrements must be between 1 and %s, but was %s.",
      MAX_LOAN_PAYMENT_FEE_INCREMENTS, this.loanPaymentFeeIncrements()
    );

    Preconditions.checkArgument(
      this.ownerReserve().isPresent() == this.requiresOwnerReserve(),
      "ownerReserve must be supplied for %s, and only for those types, whether standalone or as a Batch inner " +
        "transaction. Transaction was a %s.", OWNER_RESERVE_TRANSACTION_TYPES, transactionType
    );

    this.checkSignaturesPerBatchSigner(transactionType);
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
   * Validates {@link #signaturesPerBatchSigner()} against the transaction being priced.
   *
   * @param transactionType The {@link TransactionType} of {@link #transaction()}.
   */
  default void checkSignaturesPerBatchSigner(final TransactionType transactionType) {
    if (this.signaturesPerBatchSigner().isEmpty()) {
      return;
    }

    Preconditions.checkArgument(
      transactionType == TransactionType.BATCH,
      "signaturesPerBatchSigner applies only to a Batch, but the transaction is a %s.", transactionType
    );

    final Batch batch = (Batch) this.transaction();
    Preconditions.checkArgument(
      batch.batchSigners().isEmpty(),
      "signaturesPerBatchSigner is only for pricing a Batch before its signatures exist. This Batch already has " +
        "BatchSigners, so the counts are read from them instead."
    );

    final Set<Address> requiredSigners = batch.requiredSigners();
    this.signaturesPerBatchSigner().forEach((address, count) -> {
      Preconditions.checkArgument(
        requiredSigners.contains(address),
        "%s is not required to sign this Batch, so a signature count for it would be ignored. The accounts that " +
          "must sign are %s. Note that a required signer is not always an inner transaction's Account: a delegated " +
          "inner is signed by its Delegate, and a sponsored inner also requires its Sponsor.",
        address, requiredSigners
      );
      checkSignatureCount(count, "signaturesPerBatchSigner[" + address + "]");
    });
  }

  /**
   * Asserts that a supplied signature count does not exceed the XRPL signer list limit.
   *
   * @param signatureCount An {@link UnsignedInteger} number of signatures.
   * @param fieldName      The name of the field being checked, for use in the failure message.
   */
  static void checkSignatureCount(final UnsignedInteger signatureCount, final String fieldName) {
    Preconditions.checkArgument(
      signatureCount.compareTo(MAX_SIGNER_LIST_SIZE) <= 0,
      "%s must not exceed %s (the XRPL signer list limit), but was %s.",
      fieldName, MAX_SIGNER_LIST_SIZE, signatureCount
    );
  }
}
