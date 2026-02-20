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
   * Validates all properties of inner transactions in a single pass for efficiency. This combines multiple validations
   * to avoid iterating over rawTransactions multiple times.
   */
  @Value.Check
  default void checkRawTransactions() {
    // TODO: Replace with constant once https://github.com/XRPLF/xrpl4j/issues/683 is merged.
    final PublicKey emptyPublicKey = PublicKey.builder().value(UnsignedByteArray.empty()).build();
    final XrpCurrencyAmount zeroFee = XrpCurrencyAmount.ofDrops(0);

    // Check 1: Validate transaction count (must be between 2 and 8 inclusive)
    final int rawTransactionsSize = this.rawTransactions().size();
    Preconditions.checkArgument(
      rawTransactionsSize >= 2 && rawTransactionsSize <= 8,
      "RawTransactions must contain between 2 and 8 transactions, but contained %s.",
      rawTransactionsSize
    );

    // Check 2: Validate no duplicate transactions
    final long uniqueTransactionCount = this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .distinct()
      .count();

    Preconditions.checkArgument(
      uniqueTransactionCount == rawTransactionsSize,
      "RawTransactions must not contain duplicate transactions. " +
        "Found %s unique transactions out of %s total.",
      uniqueTransactionCount,
      rawTransactionsSize
    );

    // Check 3-8: Validate each inner transaction's properties in a single iteration
    for (RawTransactionWrapper wrapper : this.rawTransactions()) {
      final Transaction innerTransaction = wrapper.rawTransaction();

      // Check 3: Each inner transaction must have a fee of 0
      Preconditions.checkArgument(
        innerTransaction.fee().equals(zeroFee),
        "Each inner transaction in a Batch must have a fee of 0. Found transaction with non-zero fee: %s",
        innerTransaction
      );

      // Check 4: Each inner transaction must have an empty SigningPublicKey
      Preconditions.checkArgument(
        innerTransaction.signingPublicKey().equals(emptyPublicKey),
        "Each inner transaction in a Batch must have an empty SigningPublicKey. " +
          "Found transaction with non-empty SigningPublicKey: %s",
        innerTransaction
      );

      // Check 5: Each inner transaction must have no transaction signature
      Preconditions.checkArgument(
        !innerTransaction.transactionSignature().isPresent(),
        "Each inner transaction in a Batch must have no signature. Found transaction with signature: %s",
        innerTransaction
      );

      // Check 6: Each inner transaction must have no signers
      Preconditions.checkArgument(
        innerTransaction.signers().isEmpty(),
        "Each inner transaction in a Batch must have no signers. Found transaction with signers: %s",
        innerTransaction
      );

      // Check 7: Inner transactions cannot be Batch transactions (no nesting)
      Preconditions.checkArgument(
        !(innerTransaction instanceof Batch),
        "Batch transactions cannot be nested inside other Batch transactions. Found nested Batch: %s",
        innerTransaction
      );
    }
  }

  /**
   * Validates that exactly one batch mode flag is set.
   */
  @Value.Check
  default void checkBatchModeFlag() {
    final BatchFlags flags = this.flags();

    // Count how many batch mode flags are set
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

    // Exactly one mode must be set
    Preconditions.checkArgument(
      modeCount == 1,
      "Exactly one batch mode flag must be set (AllOrNothing, OnlyOne, UntilFailure, or Independent)."
    );
  }

  /**
   * Validates all BatchSigners-related constraints in a single pass for efficiency. This combines multiple validations
   * to avoid iterating over batchSigners and computing account sets multiple times.
   */
  @Value.Check
  default void checkBatchSigners() {
    // Check 1: Validate no duplicate batch signers
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

    // Check 2: Validate that the outer account is not included as a signer in BatchSigners
    // This applies to both single-sig and multi-sig scenarios
    final Optional<BatchSigner> firstSignerMatchingOuterAccount = this.batchSigners().stream()
      .map(BatchSignerWrapper::batchSigner)
      .filter(batchSigner -> {
        // Check single-sig: BatchSigner.Account matches outer account
        if (batchSigner.account().equals(this.account())) {
          return true;
        }
        // Check multi-sig: Any nested Signer.Account matches outer account
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

    // Checks 3-4: Validate BatchSigners completeness (no missing, no extra)
    // Only perform these checks if BatchSigners is non-empty
    if (!this.batchSigners().isEmpty()) {
      // Compute the set of accounts that require signatures (all inner transaction accounts except outer account)
      final Set<Address> requiredSignerAccounts = this.rawTransactions().stream()
        .map(wrapper -> wrapper.rawTransaction().account())
        .filter(account -> !account.equals(this.account()))
        .collect(Collectors.toSet());

      // Compute the set of accounts that actually provided signatures
      final Set<Address> actualSignerAccounts = this.batchSigners().stream()
        .map(wrapper -> wrapper.batchSigner().account())
        .collect(Collectors.toSet());

      // Check 3: Validate no missing BatchSigners
      // Every required account must have a corresponding BatchSigner
      final Optional<Address> missingSignerAccount = requiredSignerAccounts.stream()
        .filter(account -> !actualSignerAccounts.contains(account))
        .findFirst();

      Preconditions.checkArgument(
        !missingSignerAccount.isPresent(),
        "BatchSigners must contain signatures from all accounts with inner transactions " +
          "(excluding outer signer). Missing BatchSigner for account: %s",
        missingSignerAccount.orElse(null)
      );

      // Check 4: Validate no extra BatchSigners
      // Every BatchSigner must correspond to an inner transaction account
      final Optional<Address> extraSignerAccount = actualSignerAccounts.stream()
        .filter(signerAccount -> !requiredSignerAccounts.contains(signerAccount))
        .findFirst();

      Preconditions.checkArgument(
        !extraSignerAccount.isPresent(),
        "BatchSigners must only contain signatures from accounts that have inner transactions. " +
          "Found BatchSigner with no inner transactions: %s",
        extraSignerAccount.orElse(null)
      );
    }
  }

}
