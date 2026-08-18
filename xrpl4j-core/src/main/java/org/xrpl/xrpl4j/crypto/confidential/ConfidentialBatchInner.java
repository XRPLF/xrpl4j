package org.xrpl.xrpl4j.crypto.confidential;

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

import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Optional;

/**
 * One entry in a confidential Batch: either a {@link ConfidentialMptOp} the assembler builds into a transaction and
 * proof, or a pre-built plain (non-confidential) {@link Transaction} to interleave.
 *
 * <p>A plain inner is passed through untouched, so the caller must supply it already shaped as an inner-batch
 * transaction — {@code Fee: 0}, the {@code tfInnerBatchTxn} flag, an empty {@code SigningPubKey}, and its own
 * {@code Sequence} or {@code TicketSequence} (the assembler cannot re-shape an arbitrary immutable transaction, and
 * xrpl4j has no autofill). The {@code RawTransactionWrapper}/{@code Batch} validation rejects a malformed plain inner.
 * The assembler advances the submitter's sequence counter past a plain inner that consumed a regular sequence, so a
 * later confidential inner for the same account is numbered correctly.</p>
 */
@Value.Immutable
public interface ConfidentialBatchInner {

  /**
   * A confidential-operation inner.
   *
   * @param operation The {@link ConfidentialMptOp} to build.
   *
   * @return A {@link ConfidentialBatchInner} wrapping {@code operation}.
   */
  static ConfidentialBatchInner of(final ConfidentialMptOp operation) {
    return ImmutableConfidentialBatchInner.builder().operation(operation).build();
  }

  /**
   * A pre-built plain (non-confidential) inner, already shaped as an inner-batch transaction.
   *
   * @param plainTransaction The plain {@link Transaction} to pass through.
   *
   * @return A {@link ConfidentialBatchInner} wrapping {@code plainTransaction}.
   */
  static ConfidentialBatchInner ofPlain(final Transaction plainTransaction) {
    return ImmutableConfidentialBatchInner.builder().plainTransaction(plainTransaction).build();
  }

  /**
   * The confidential operation, if this inner is a confidential op-spec.
   *
   * @return An optionally-present {@link ConfidentialMptOp}.
   */
  Optional<ConfidentialMptOp> operation();

  /**
   * The plain transaction, if this inner is a pre-built plain transaction.
   *
   * @return An optionally-present {@link Transaction}.
   */
  Optional<Transaction> plainTransaction();

  /**
   * Enforces that exactly one of {@link #operation()} or {@link #plainTransaction()} is present.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      operation().isPresent() ^ plainTransaction().isPresent(),
      "a ConfidentialBatchInner must hold exactly one of a confidential operation or a plain transaction"
    );
  }
}
