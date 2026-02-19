package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.BatchFlags;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Batch transaction allows multiple transactions to be grouped together and executed atomically according to the
 * specified batch mode.
 *
 * <p>This class will be marked {@link Beta} until the featureBatch amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBatch.class)
@JsonDeserialize(as = ImmutableBatch.class)
@Beta
public interface Batch extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableBatch.Builder}.
   */
  static ImmutableBatch.Builder builder() {
    return ImmutableBatch.builder();
  }

  /**
   * Set of {@link BatchFlags}s for this {@link Batch}, which define the batch execution mode.
   *
   * <p>Exactly one of the following modes must be set:
   * <ul>
   *   <li>{@link BatchFlags#ALL_OR_NOTHING} - All transactions must succeed, or all are reverted</li>
   *   <li>{@link BatchFlags#ONLY_ONE} - Only one transaction should succeed</li>
   *   <li>{@link BatchFlags#UNTIL_FAILURE} - Execute transactions until one fails</li>
   *   <li>{@link BatchFlags#INDEPENDENT} - Each transaction is independent</li>
   * </ul>
   *
   * @return The {@link BatchFlags} for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default BatchFlags flags() {
    return BatchFlags.of(BatchFlags.ALL_OR_NOTHING.getValue());
  }

  /**
   * The list of inner transactions to be executed as part of this batch.
   *
   * <p>Must contain between 2 and 8 transactions (inclusive). Inner transactions must also have, among other rules:
   * <ul>
   *   <li>Have the {@code tfInnerBatchTxn} flag set</li>
   *   <li>Have a fee of "0" (fees are paid by the outer Batch transaction)</li>
   *   <li>Have an empty SigningPubKey and no TxnSignature</li>
   *   <li>Not be Batch transactions themselves (no nesting)</li>
   * </ul>
   *
   * @return A {@link List} of {@link RawTransactionWrapper} containing the inner transactions.
   *
   * @see "https://xls.xrpl.org/xls/XLS-0056-batch.html#23-failure-conditions"
   */
  @JsonProperty("RawTransactions")
  List<RawTransactionWrapper> rawTransactions();

  /**
   * Optional list of batch signers for multi-account batch transactions.
   *
   * <p>When inner transactions come from multiple accounts, each account must sign the batch
   * and provide their signature in this array.
   *
   * @return A {@link List} of {@link BatchSignerWrapper} containing the batch signers.
   */
  @JsonProperty("BatchSigners")
  List<BatchSignerWrapper> batchSigners();

  /**
   * Validates that all inner transactions have a fee of 0.
   */
  @Value.Check
  default void validateInnerTransactionFees() {
    final Optional<Transaction> firstTransactionWithNonZeroFee = this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .filter(innerTransaction -> !innerTransaction.fee().equals(XrpCurrencyAmount.ofDrops(0)))
      .findFirst();

    Preconditions.checkArgument(
      !firstTransactionWithNonZeroFee.isPresent(),
      "Each inner transaction in a Batch must have a fee of 0. Found transaction with non-zero fee: %s",
      firstTransactionWithNonZeroFee.orElse(null)
    );
  }

  /**
   * Validates that all inner transactions have an empty SigningPublicKey.
   */
  @Value.Check
  default void validateInnerTransactionPublicKeys() {
    // TODO: Replace with constant once https://github.com/XRPLF/xrpl4j/issues/683 is merged.
    final PublicKey emptyPublicKey = PublicKey.builder().value(UnsignedByteArray.empty()).build();
    final Optional<Transaction> firstTransactionWithNonEmptyPublicKey = this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .filter(innerTransaction -> !innerTransaction.signingPublicKey().equals(emptyPublicKey))
      .findFirst();

    Preconditions.checkArgument(
      !firstTransactionWithNonEmptyPublicKey.isPresent(),
      "Each inner transaction in a Batch must have an empty SigningPublicKey. " +
        "Found transaction with non-empty SigningPublicKey: %s",
      firstTransactionWithNonEmptyPublicKey.orElse(null)
    );
  }

  /**
   * Validates that all inner transactions have no transaction signature.
   */
  @Value.Check
  default void validateNoInnerTransactionSignature() {
    final Optional<Transaction> firstTransactionWithSignature = this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .filter(innerTransaction -> innerTransaction.transactionSignature().isPresent())
      .findFirst();

    Preconditions.checkArgument(
      !firstTransactionWithSignature.isPresent(),
      "Each inner transaction in a Batch must have no signature. Found transaction with signature: %s",
      firstTransactionWithSignature.orElse(null)
    );
  }

  /**
   * Validates that all inner transactions have no signers.
   */
  @Value.Check
  default void validateNoInnerTransactionSigners() {
    final Optional<Transaction> firstTransactionWithSigners = this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .filter(innerTransaction -> !innerTransaction.signers().isEmpty())
      .findFirst();

    Preconditions.checkArgument(
      !firstTransactionWithSigners.isPresent(),
      "Each inner transaction in a Batch must have no signers. Found transaction with signers: %s",
      firstTransactionWithSigners.orElse(null)
    );
  }

  /**
   * Validates that the batch contains between 2 and 8 inner transactions.
   */
  @Value.Check
  default void validateRawTransactionsCount() {
    final int rawTransactionsSize = this.rawTransactions().size();
    Preconditions.checkArgument(
      rawTransactionsSize >= 2 && rawTransactionsSize <= 8,
      "RawTransactions must contain between 2 and 8 transactions, but contained %s.",
      rawTransactionsSize
    );
  }

  /**
   * Validates that exactly one batch mode flag is set.
   */
  @Value.Check
  default void validateBatchModeFlag() {
    final BatchFlags flags = this.flags();
    int modeCount = 0;
    if (flags.tfAllOrNothing()) {
      modeCount++;
    }
    if (flags.tfOnlyOne()) {
      modeCount++;
    }
    if (flags.tfUntilFailure()) {
      modeCount++;
    }
    if (flags.tfIndependent()) {
      modeCount++;
    }
    Preconditions.checkArgument(
      modeCount == 1,
      "Exactly one batch mode flag must be set (AllOrNothing, OnlyOne, UntilFailure, or Independent)."
    );
  }

  /**
   * Validates that inner transactions are not Batch transactions (no nesting).
   */
  @Value.Check
  default void validateNoNestedBatches() {
    final Optional<Transaction> firstNestedBatch = this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .filter(innerTransaction -> innerTransaction instanceof Batch)
      .findFirst();

    Preconditions.checkArgument(
      !firstNestedBatch.isPresent(),
      "Batch transactions cannot be nested inside other Batch transactions. Found nested Batch: %s",
      firstNestedBatch.orElse(null)
    );
  }

  /**
   * Validates that the `Batch.Account` is not included as a signer in `BatchSigners`.
   */
  @Value.Check
  default void validateOuterSigner() {
    final Optional<BatchSigner> firstSignerMatchingOuterAccount = this.batchSigners().stream()
      .map(BatchSignerWrapper::batchSigner)
      .filter(batchSigner -> {
        // Check single-sig
        if (batchSigner.account().equals(this.account())) {
          return true;
        }
        // Check multi-sig
        return batchSigner.signers().stream()
          .anyMatch(signerWrapper -> signerWrapper.signer().account().equals(this.account()));
      })
      .findFirst();

    Preconditions.checkArgument(
      !firstSignerMatchingOuterAccount.isPresent(),
      "The Account submitting a Batch transaction must not sign any inner transactions. " +
        "Found BatchSigner matching outer account: %s",
      firstSignerMatchingOuterAccount.orElse(null)
    );
  }

  /**
   * Validates that BatchSigners contains signatures from all accounts whose inner transactions are included, excluding
   * the account signing the outer transaction.
   *
   * <p>If more than one account has inner transactions in the Batch, BatchSigners must be provided and must
   * contain signatures from all such accounts (except the outer transaction signer).</p>
   */
  @Value.Check
  default void validateBatchSigners() {
    // We only enforce this check if the `batchSigners` object is non-empty. This is because it _is_ valid to construct
    // an unsigned batch, in which case `batchSigners` will be empty. However, in the case that `batchSigners` is
    // not empty, then these rules should be enforced.
    if (!this.batchSigners().isEmpty()) {
      // Collect all unique accounts from inner transactions
      final Set<Address> innerTransactionAccounts = this.rawTransactions().stream()
        .map(wrapper -> wrapper.rawTransaction().account())
        .collect(Collectors.toSet());

      // Determine which accounts need to provide BatchSigners (all inner tx accounts except the outer signer)
      final Set<Address> accountsRequiringSignatures = innerTransactionAccounts.stream()
        .filter(innerAccount -> !innerAccount.equals(this.account()))
        .collect(Collectors.toSet());

      // If there are accounts other than the outer signer that have inner transactions,
      // BatchSigners must contain signatures from all of them
      if (!accountsRequiringSignatures.isEmpty()) {
        // Collect all accounts that have provided BatchSigners
        final Set<Address> batchSignerAccounts = this.batchSigners().stream()
          .map(wrapper -> wrapper.batchSigner().account())
          .collect(Collectors.toSet());

        // Verify all required accounts have provided signatures
        final Set<Address> missingSigners = accountsRequiringSignatures.stream()
          .filter(requiredAccount -> !batchSignerAccounts.contains(requiredAccount))
          .collect(Collectors.toSet());

        Preconditions.checkArgument(
          missingSigners.isEmpty(),
          "BatchSigners must contain signatures from all accounts with inner transactions " +
            "(excluding the outer transaction signer). Missing signatures from: %s",
          missingSigners
        );
      }
    }
  }

  /**
   * Validates that there are no duplicate transactions in RawTransactions.
   */
  @Value.Check
  default void validateNoDuplicateTransactions() {
    final long uniqueTransactionCount = this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .distinct()
      .count();

    Preconditions.checkArgument(
      uniqueTransactionCount == this.rawTransactions().size(),
      "RawTransactions must not contain duplicate transactions. " +
        "Found %s unique transactions out of %s total.",
      uniqueTransactionCount,
      this.rawTransactions().size()
    );
  }

  /**
   * Validates that BatchSigners does not exceed the number of transactions in RawTransactions.
   */
  @Value.Check
  default void validateBatchSignersSize() {
    Preconditions.checkArgument(
      this.batchSigners().size() <= this.rawTransactions().size(),
      "BatchSigners must not contain more entries than RawTransactions. " +
        "Found %s BatchSigners but only %s RawTransactions.",
      this.batchSigners().size(),
      this.rawTransactions().size()
    );
  }

  /**
   * Validates that there are no duplicate signers in BatchSigners.
   */
  @Value.Check
  default void validateNoDuplicateBatchSigners() {
    final long uniqueSignerCount = this.batchSigners().stream()
      .map(wrapper -> wrapper.batchSigner().account())
      .distinct()
      .count();

    Preconditions.checkArgument(
      uniqueSignerCount == this.batchSigners().size(),
      "BatchSigners must not contain duplicate signers. Found %s unique signers out of %s total.",
      uniqueSignerCount,
      this.batchSigners().size()
    );
  }

  /**
   * Validates that all BatchSigners correspond to accounts that have inner transactions (excluding the outer signer).
   * Note: The outer signer is validated separately by validateOuterSigner().
   */
  @Value.Check
  default void validateBatchSignersHaveInnerTransactions() {
    if (!this.batchSigners().isEmpty()) {
      // Collect all unique accounts from inner transactions
      final Set<Address> innerTransactionAccounts = this.rawTransactions().stream()
        .map(wrapper -> wrapper.rawTransaction().account())
        .collect(Collectors.toSet());

      // Find any BatchSigner that doesn't have a corresponding inner transaction
      // (excluding the outer account, which is validated by validateOuterSigner)
      final Optional<Address> firstInvalidSigner = this.batchSigners().stream()
        .map(wrapper -> wrapper.batchSigner().account())
        .filter(signerAccount -> !signerAccount.equals(this.account())) // Exclude outer account
        .filter(signerAccount -> !innerTransactionAccounts.contains(signerAccount))
        .findFirst();

      Preconditions.checkArgument(
        !firstInvalidSigner.isPresent(),
        "BatchSigners must only contain signatures from accounts that have inner transactions. " +
          "Found BatchSigner with no inner transactions: %s",
        firstInvalidSigner.orElse(null)
      );
    }
  }

}
