package org.xrpl.xrpl4j.model.flags;

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

import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * A set of static {@link Flags} which could apply to any {@link Transaction}.
 */
public class TransactionFlags extends Flags {

  /**
   * Corresponds to the {@code tfFullyCanonicalSig} flag.
   */
  public static final TransactionFlags FULLY_CANONICAL_SIG = new TransactionFlags(0x80000000L);

  /**
   * Corresponds to the {@code tfInnerBatchTxn} flag. This flag should only be used if a transaction is an inner
   * transaction in a Batch transaction. It signifies that the transaction shouldn't be signed. Any normal transaction
   * that includes this flag should be rejected.
   *
   * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
   */
  public static final TransactionFlags INNER_BATCH_TXN = new TransactionFlags(0x40000000L);

  /**
   * Constant for an unset flag.
   */
  public static final TransactionFlags UNSET = new TransactionFlags(0L);

  /**
   * Constant empty {@link TransactionFlags}.
   */
  public static final TransactionFlags EMPTY = new TransactionFlags();

  TransactionFlags(long value) {
    super(value);
  }

  TransactionFlags() {
  }

  public static TransactionFlags of(long value) {
    return new TransactionFlags(value);
  }

  /**
   * Flags indicating that a fully-canonical signature is required. This flag is highly recommended.
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   *
   * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
   */
  public boolean tfFullyCanonicalSig() {
    return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
  }

  /**
   * Check if the {@code tfInnerBatchTxn} flag is set. This flag indicates that the transaction is an inner transaction
   * within a Batch transaction.
   *
   * @return {@code true} if {@code tfInnerBatchTxn} is set, otherwise {@code false}.
   *
   * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
   */
  public boolean tfInnerBatchTxn() {
    return this.isSet(TransactionFlags.INNER_BATCH_TXN);
  }

  /**
   * A builder class for {@link TransactionFlags} flags.
   */
  public static class Builder {

    /**
     * Build a {@link TransactionFlags}.
     *
     * @return {@link TransactionFlags}.
     */
    public TransactionFlags build() {
      return TransactionFlags.EMPTY;
    }
  }
}
