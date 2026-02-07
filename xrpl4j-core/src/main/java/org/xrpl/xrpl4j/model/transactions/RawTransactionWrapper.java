package org.xrpl.xrpl4j.model.transactions;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Check;

/**
 * Wrapper object for a {@link Transaction} in the {@code RawTransactions} array of a {@link Batch} transaction, so that
 * the JSON representation matches the XRPL binary serialization specification.
 *
 * <p>This class will be marked {@link Beta} until the featureBatch amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRawTransactionWrapper.class)
@JsonDeserialize(as = ImmutableRawTransactionWrapper.class)
@Beta
public interface RawTransactionWrapper {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableRawTransactionWrapper.Builder}.
   */
  static ImmutableRawTransactionWrapper.Builder builder() {
    return ImmutableRawTransactionWrapper.builder();
  }

  /**
   * Construct a {@link RawTransactionWrapper} wrapping the given {@link Transaction}.
   *
   * @param transaction A {@link Transaction} to wrap.
   *
   * @return A {@link RawTransactionWrapper}.
   */
  static RawTransactionWrapper of(Transaction transaction) {
    return builder().rawTransaction(transaction).build();
  }

  /**
   * The inner transaction. This transaction must:
   * <ul>
   *   <li>Have the {@code tfInnerBatchTxn} flag set</li>
   *   <li>Have a fee of 0</li>
   *   <li>Have an empty {@code SigningPubKey}</li>
   *   <li>Not have a {@code TxnSignature}</li>
   * </ul>
   *
   * @return A {@link Transaction}.
   */
  @JsonProperty("RawTransaction")
  Transaction rawTransaction();

  //  @Check
  //  default void check() {
  //    Preconditions.checkArgument(
  //      rawTransaction().flags().tfInnerBatchTxn(),
  //      "Inner transaction must have the `tfInnerBatchTxn` flag set."
  //    );
  //  }
}
