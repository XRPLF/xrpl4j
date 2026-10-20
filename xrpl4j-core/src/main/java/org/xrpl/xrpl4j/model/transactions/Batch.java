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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
   *   <li>There is a hard cap of 24 entries, independent of the number of inner transactions.</li>
   *   <li>Extra signers are allowed: an account may sign without having its own inner transaction (e.g.,
   *       co-signer or delegate).</li>
   * </ul>
   *
   * @return A {@link List} of {@link BatchSignerWrapper} containing the batch signers.
   */
  @JsonProperty("BatchSigners")
  List<BatchSignerWrapper> batchSigners();

  /**
   * The set of accounts that must appear in {@link #batchSigners()}, derived from the inner transactions.
   *
   * <p>xrpld builds the identical set in {@code Batch::preflightSigValidated}, which requires the
   * {@code BatchSigners} to match it exactly — rejecting an array that omits a required signer <em>or</em> carries an
   * extra one (XLS-56 §2.3, BatchSigners items 5 and 6). {@link #checkBatchSigners()} enforces the same exact match
   * whenever {@code BatchSigners} is non-empty. Each inner contributes:
   * <ul>
   *   <li>its <em>initiator</em> — its {@code Delegate} when one is present, otherwise its {@code Account};</li>
   *   <li>its {@code Sponsor}, when the inner also carries a {@code SponsorSignature}; and</li>
   *   <li>a {@code LoanSet}'s {@code Counterparty}, when present.</li>
   * </ul>
   * The outer {@link #account()} is excluded throughout, because it authorises its own inner transactions with the
   * signature it puts on the Batch itself.
   *
   * <p>Each account in this set also contributes at least one base fee to the Batch's transaction cost. When pricing
   * a Batch before its signatures exist, use
   * {@link org.xrpl.xrpl4j.model.client.fees.FeeParams#forBatch(org.xrpl.xrpl4j.model.client.fees.FeeResult, Batch)}
   * to declare how many signatures each of these accounts will supply.
   *
   * @return An unmodifiable {@link Set} of {@link Address}es that must sign this Batch, which may be empty when every
   *   inner transaction belongs to the outer account.
   */
  @JsonIgnore
  default Set<Address> requiredSigners() {
    final Address outerAccount = this.account();
    return this.rawTransactions().stream()
      .map(RawTransactionWrapper::rawTransaction)
      .flatMap(innerTransaction -> {
        final Stream.Builder<Address> signers = Stream.builder();
        // Initiator: the Delegate signs on behalf of the account holder when present, otherwise the Account.
        signers.add(innerTransaction.delegate().orElseGet(innerTransaction::account));
        // A Sponsor must also sign when the inner carries a SponsorSignature.
        if (innerTransaction.sponsorSignature().isPresent()) {
          innerTransaction.sponsor().ifPresent(signers::add);
        }
        // A LoanSet's Counterparty must also sign.
        if (innerTransaction instanceof LoanSet) {
          ((LoanSet) innerTransaction).counterparty().ifPresent(signers::add);
        }
        return signers.build();
      })
      .filter(address -> !address.equals(outerAccount))
      .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
  }

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
   * <p>Each rule below maps to the {@code BatchSigners} failure conditions in XLS-56 §2.3 and to
   * {@code Batch::preflight} / {@code Batch::preflightSigValidated} in xrpld:
   * <ul>
   *   <li>Item 1: at most 24 BatchSigners, independent of the number of inner transactions.</li>
   *   <li>Item 2: the outer {@link #account()} must not be a BatchSigner {@code Account}.</li>
   *   <li>Item 3: no duplicate BatchSigner {@code Account}s.</li>
   *   <li>Item 4: BatchSigners must be strictly ascending by {@code Account}. xrpld rejects an unsorted array;
   *       xrpl4j instead normalizes by auto-sorting on construction, so callers need not pre-sort. This cannot
   *       invalidate any signature: a BatchSigner's signature covers only the outer Account, sequence, Flags, the
   *       inner transaction IDs and the signer's own Account (never the BatchSigners array), and the outer
   *       signature is always computed over an already-constructed, and therefore already-sorted, Batch.</li>
   *   <li>Items 5 and 6: when BatchSigners is non-empty it must equal {@link #requiredSigners()} exactly, with no
   *       extra and no missing entries.</li>
   * </ul>
   *
   * <p>Deliberate deviation: XLS-56 item 6 also rejects a Batch whose {@code BatchSigners} is absent while inner
   * transactions require signers. xrpl4j does <em>not</em> enforce that at construction time, because the
   * multi-account signing workflow must first build the Batch without any BatchSigners in order to produce the
   * payload that each BatchSigner signs, and only afterwards rebuild it with the collected signatures. xrpld makes
   * the same allowance for unsigned proposals (the {@code TapProposal} exemption in
   * {@code Batch::preflightSigValidated}); a submitted Batch that still lacks required BatchSigners is rejected by
   * the server with {@code temBAD_SIGNER}.
   */
  @Value.Check
  default Batch checkBatchSigners() {
    // Check 1: Hard cap of 24 BatchSigners (V1_1)
    Preconditions.checkArgument(
      this.batchSigners().size() <= 24,
      "BatchSigners must not exceed 24 entries, but contained %s.",
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

    // Check 3: Outer account must not appear as a BatchSigner Account (XLS-56 §2.3 item 1.2; xrpld returns
    // temBAD_SIGNER). Only the BatchSigner's own Account is compared, mirroring xrpld's Batch::preflight. The outer
    // account MAY legitimately appear inside another BatchSigner's nested `Signers` array: there it is merely a
    // member of that account's SignerList contributing quorum weight, not a signer of an inner transaction. xrpld
    // only forbids a nested signer from being the BatchSigner account itself (see STTx::checkBatchMultiSign), and
    // Testnet has validated Batch transactions with the outer account nested in this way.
    final Optional<BatchSigner> firstSignerMatchingOuterAccount = this.batchSigners().stream()
      .map(BatchSignerWrapper::batchSigner)
      .filter(batchSigner -> batchSigner.account().equals(this.account()))
      .findFirst();

    Preconditions.checkArgument(
      !firstSignerMatchingOuterAccount.isPresent(),
      "The Account submitting a Batch transaction must not sign any inner transactions. " +
        "Found BatchSigner matching outer account: %s",
      firstSignerMatchingOuterAccount.orElse(null)
    );

    // Checks 4 and 5: When BatchSigners is non-empty it must match the required-signer set exactly (XLS-56 §2.3,
    // BatchSigners items 5 and 6; xrpld returns temBAD_SIGNER for either a missing or an extra entry). The
    // required-signer set (initiator, sponsor, and LoanSet counterparty, with the outer account excluded) is derived
    // by requiredSigners(), which is the single source of truth. That set depends only on the inner transactions'
    // fields, never on ledger state, so it can be checked here. Note that the co-signers of a multi-signing
    // BatchSigner belong in that entry's nested `Signers` array, not as additional BatchSigner entries.
    if (!this.batchSigners().isEmpty()) {
      final Set<Address> requiredSignerAccounts = this.requiredSigners();
      final Set<Address> actualSignerAccounts = this.batchSigners().stream()
        .map(wrapper -> wrapper.batchSigner().account())
        .collect(Collectors.toSet());

      // Check 4: Find the first required signer that has no BatchSigner entry.
      final Optional<Address> missingSignerAccount = requiredSignerAccounts.stream()
        .filter(account -> !actualSignerAccounts.contains(account))
        .findFirst();

      Preconditions.checkArgument(
        !missingSignerAccount.isPresent(),
        "BatchSigners must contain signatures from all accounts required to sign an inner transaction " +
          "(excluding outer signer). Missing BatchSigner for account: %s",
        missingSignerAccount.orElse(null)
      );

      // Check 5: Find the first BatchSigner entry whose account is not a required signer.
      final Optional<Address> extraSignerAccount = actualSignerAccounts.stream()
        .filter(account -> !requiredSignerAccounts.contains(account))
        .findFirst();

      Preconditions.checkArgument(
        !extraSignerAccount.isPresent(),
        "BatchSigners must only contain signatures from accounts required to sign an inner transaction. " +
          "Found BatchSigner for account that is not a required signer: %s",
        extraSignerAccount.orElse(null)
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
