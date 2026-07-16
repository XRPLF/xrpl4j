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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.BatchFlags;

import java.math.BigInteger;
import java.util.Comparator;
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
   * <p>Must contain between 2 and 8 transactions (inclusive). The following rules apply to every inner transaction
   * (enforced by xrpl4j where noted; otherwise enforced server-side):
   *
   * <ul>
   *   <li><b>Flag:</b> {@code tfInnerBatchTxn} ({@code 0x40000000}) must be set. A standalone transaction that carries
   *       this flag is rejected by rippled with {@code temINVALID_INNER_BATCH} to prevent submission of unsigned inner
   *       transactions as normal transactions. <em>(Enforced by {@link RawTransactionWrapper} at construction
   *       time.)</em></li>
   *   <li><b>Fee:</b> must be exactly {@code 0} XRP drops; the fee is paid by the outer {@link Batch} transaction.
   *       A non-zero or non-XRP fee yields {@code temBAD_FEE}. <em>(Enforced by xrpl4j.)</em></li>
   *   <li><b>TxnSignature:</b> must be absent. Presence yields {@code temBAD_SIGNATURE}.
   *       <em>(Enforced by xrpl4j.)</em></li>
   *   <li><b>SigningPubKey:</b> must be empty / zero-bytes. A non-empty value yields {@code temBAD_REGKEY}
   *       (note: not {@code temBAD_SIGNATURE} as one might expect). <em>(Enforced by xrpl4j.)</em></li>
   *   <li><b>Signers:</b> must be absent. Presence yields {@code temBAD_SIGNER}. <em>(Enforced by xrpl4j.)</em></li>
   *   <li><b>Sequence / TicketSequence:</b> exactly one of the two must be set; having both or neither yields
   *       {@code temSEQ_AND_TICKET}. <em>(Enforced server-side.)</em></li>
   *   <li><b>No nesting:</b> an inner transaction must not itself be a {@link Batch}. <em>(Enforced by
   *       xrpl4j.)</em></li>
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
   * <p>A {@link BatchSigner} entry is required for every account that has at least one inner transaction, <em>or</em>
   * that would ordinarily have to sign at least one of the inner transactions (e.g., a co-signer or delegate). When
   * multiple such accounts exist and no {@code BatchSigners} array is provided, rippled rejects the outer transaction
   * with {@code tefBAD_AUTH}.
   *
   * <p>Pseudo-accounts (e.g., vault or MPT-issuance accounts) cannot sign batch entries. A {@link BatchSigner} entry
   * that references a pseudo-account is rejected by rippled with {@code tefBAD_AUTH}. xrpl4j does not enforce this
   * at construction time because pseudo-account status is ledger state that is unknowable from an address alone.
   *
   * <p>Notes on the current ({@code V1_1}) design:
   * <ul>
   *   <li>Entries are auto-sorted ascending by {@code Account} address (required by the wire format).</li>
   *   <li>There is a hard cap of 8 entries, independent of the number of inner transactions.</li>
   *   <li>Extra signers are allowed: an account may sign without having its own inner transaction (e.g.,
   *       co-signer or delegate).</li>
   * </ul>
   *
   * @return A {@link List} of {@link BatchSignerWrapper} containing the batch signers.
   */
  @JsonProperty("BatchSigners")
  List<BatchSignerWrapper> batchSigners();

  /**
   * Internal flag used to prevent infinite recursion when auto-sorting {@link #batchSigners()}.
   *
   * @return {@code true} if {@link #batchSigners()} has already been sorted.
   */
  @JsonIgnore
  @Value.Default
  default boolean sortedBatchSigners() {
    return false;
  }

  /**
   * Validates all properties of inner transactions in a single pass for efficiency. This combines multiple validations
   * to avoid iterating over rawTransactions multiple times. Note: the {@code tfInnerBatchTxn} flag is enforced earlier,
   * by {@link RawTransactionWrapper#check()}, and is not re-checked here.
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
   * Validates BatchSigners constraints and auto-sorts entries by {@code Account} in ascending order (required by
   * V1_1). Follows the same normalizing {@link Value.Check} pattern as {@link BatchSigner#checkAndNormalize()}.
   *
   * <p>Notes on the current ({@code V1_1}) design:
   * <ul>
   *   <li>BatchSigners are auto-sorted ascending by Account (temBAD_SIGNER if out of order on-chain).</li>
   *   <li>There is a hard cap of 8 BatchSigners, independent of the number of inner transactions.</li>
   *   <li>No "extra signers" check is performed: co-signers/delegates are allowed without their own inner
   *       transaction.</li>
   * </ul>
   */
  @Value.Check
  default Batch checkBatchSigners() {
    // Check 1: Hard cap of 8 BatchSigners (V1_1)
    Preconditions.checkArgument(
      this.batchSigners().size() <= 8,
      "BatchSigners must not exceed 8 entries, but contained %s.",
      this.batchSigners().size()
    );

    // Check 2: No duplicate signers by account
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

    // Check 3: Outer account must not appear in BatchSigners (directly or via nested Signers)
    final Optional<BatchSigner> firstSignerMatchingOuterAccount = this.batchSigners().stream()
      .map(BatchSignerWrapper::batchSigner)
      .filter(batchSigner -> {
        if (batchSigner.account().equals(this.account())) {
          return true;
        }
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

    // Check 4: When BatchSigners is non-empty, every inner-transaction account (excluding the outer account)
    // must have a corresponding BatchSigner entry.
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

      // Find the first inner-transaction account (excluding the outer account) that has no BatchSigner entry.
      final Optional<Address> missingSignerAccount = requiredSignerAccounts.stream()
        .filter(account -> !actualSignerAccounts.contains(account))
        .findFirst();

      // Note: we intentionally do NOT check for extra BatchSigners (accounts with no inner transaction).
      // V1_1 allows co-signers and delegates who sign on behalf of an inner-transaction account without
      // having their own inner transaction. Whether an extra signer is legitimate requires ledger state
      // (e.g. SignerList entries), so that check belongs on the server, not here.
      Preconditions.checkArgument(
        !missingSignerAccount.isPresent(),
        "BatchSigners must contain signatures from all accounts with inner transactions " +
          "(excluding outer signer). Missing BatchSigner for account: %s",
        missingSignerAccount.orElse(null)
      );
    }

    // Auto-sort BatchSigners ascending by Account (V1_1 requirement). Uses the same normalizing
    // @Value.Check pattern as BatchSigner#checkAndNormalize to avoid infinite recursion.
    if (!this.batchSigners().isEmpty() && !sortedBatchSigners()) {
      final List<BatchSignerWrapper> sorted = this.batchSigners().stream()
        .sorted(Comparator.comparing(wrapper -> new BigInteger(
          AddressCodec.getInstance().decodeAccountId(wrapper.batchSigner().account()).hexValue(), 16
        )))
        .collect(Collectors.toList());

      return ImmutableBatch.builder()
        .from(this)
        .batchSigners(sorted)
        .sortedBatchSigners(true)
        .build();
    }

    return this;
  }

}
