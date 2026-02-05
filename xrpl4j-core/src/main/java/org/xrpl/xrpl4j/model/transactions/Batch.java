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
import org.immutables.value.Value.Check;
import org.xrpl.xrpl4j.model.flags.BatchFlags;

import java.util.List;
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
   * <p>Must contain between 2 and 8 transactions (inclusive). Inner transactions must:
   * <ul>
   *   <li>Have the {@code tfInnerBatchTxn} flag set</li>
   *   <li>Have fee of "0" (fees are paid by the outer Batch transaction)</li>
   *   <li>Have an empty SigningPubKey and no TxnSignature</li>
   *   <li>Not be Batch transactions themselves (no nesting)</li>
   * </ul>
   *
   * @return A {@link List} of {@link RawTransactionWrapper} containing the inner transactions.
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
   * Validates that the batch contains between 2 and 8 inner transactions.
   */
  @Value.Check
  default void validateRawTransactionsCount() {
    Preconditions.checkArgument(
      rawTransactions().size() >= 2 && rawTransactions().size() <= 8,
      "RawTransactions must contain between 2 and 8 transactions, but contained %s.",
      rawTransactions().size()
    );
  }

  /**
   * Validates that exactly one batch mode flag is set.
   */
  @Value.Check
  default void validateBatchModeFlag() {
    int modeCount = 0;
    if (flags().tfAllOrNothing()) {
      modeCount++;
    }
    if (flags().tfOnlyOne()) {
      modeCount++;
    }
    if (flags().tfUntilFailure()) {
      modeCount++;
    }
    if (flags().tfIndependent()) {
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
    for (RawTransactionWrapper wrapper : rawTransactions()) {
      Preconditions.checkArgument(
        !(wrapper.rawTransaction() instanceof Batch),
        "Batch transactions cannot be nested inside other Batch transactions."
      );
    }
  }

  /**
   * Validates that the `Batch.Account` is not included as a signer in `BatchSigners`.
   */
  @Value.Check
  default void validateOuterSigner() {

    final boolean anySignerMatchesOuterAccount = this.batchSigners().stream()
      .map(BatchSignerWrapper::batchSigner)
      .anyMatch(batchSigner -> {
        // Check single-sig
        if (batchSigner.account().equals(this.account())) {
          return true;
        } else {
          // Check multi-sig
          return batchSigner.signers().stream()
            .anyMatch(signerWrapper -> signerWrapper.signer().account().equals(this.account()));
        }
      });

    // Rule: If there is any BatchSigner then the outer signer must not be in that list, either single or multi-sig.
    Preconditions.checkArgument(
      !anySignerMatchesOuterAccount,
      "The Account submitting a Batch transaction must not sign any inner transactions."
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
    // and unsigned batch, in which case `batchSigners` will be empty. However, in the case that `batchSigners` is
    // not empty, then these rules should be enforced.
    if (!this.batchSigners().isEmpty()) {

      // Collect all unique accounts from inner transactions
      Set<Address> innerTransactionAccounts = rawTransactions().stream()
        .map(wrapper -> wrapper.rawTransaction().account())
        .collect(Collectors.toSet());

      // Determine which accounts need to provide BatchSigners (all inner tx accounts except the outer signer)
      Set<Address> accountsRequiringSignatures = innerTransactionAccounts.stream()
        .filter(innerAccount -> !innerAccount.equals(this.account()))
        .collect(Collectors.toSet());

      // If there are accounts other than the outer signer that have inner transactions,
      // BatchSigners must contain signatures from all of them
      if (!accountsRequiringSignatures.isEmpty()) {
        // Collect all accounts that have provided BatchSigners
        Set<Address> batchSignerAccounts = batchSigners().stream()
          .map(wrapper -> wrapper.batchSigner().account())
          .collect(Collectors.toSet());

        // Verify all required accounts have provided signatures
        Set<Address> missingSigners = accountsRequiringSignatures.stream()
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

}
