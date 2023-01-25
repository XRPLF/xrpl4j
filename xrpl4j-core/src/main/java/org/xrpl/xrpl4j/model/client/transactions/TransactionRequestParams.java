package org.xrpl.xrpl4j.model.client.transactions;

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
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Request parameters for the "tx" rippled API method.
 *
 * <p>The "tx" method may successfully find the {@link TransactionRequestParams#transaction()} even if it is included in
 * a ledger outside the range of {@link TransactionRequestParams#minLedger()} to
 * {@link TransactionRequestParams#maxLedger()}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTransactionRequestParams.class)
@JsonDeserialize(as = ImmutableTransactionRequestParams.class)
public interface TransactionRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableTransactionRequestParams.Builder}.
   */
  static ImmutableTransactionRequestParams.Builder builder() {
    return ImmutableTransactionRequestParams.builder();
  }

  /**
   * Convenience constructor for creating {@link TransactionRequestParams} with only a transaction hash.
   *
   * @param transactionHash A {@link Hash256} containing the transaction hash of the transaction to look up.
   *
   * @return {@link TransactionRequestParams} with {@link TransactionRequestParams#transaction()} set to
   *   {@code transactionHash}
   */
  static TransactionRequestParams of(Hash256 transactionHash) {
    return builder().transaction(transactionHash).build();
  }

  /**
   * The 256-bit hash of the transaction in hexadecimal form.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  Hash256 transaction();

  /**
   * Whether or not to return transaction data and metadata as binary serialized to hexadecimal strings. Always
   * {@code false}.
   *
   * @return Always {@code false}.
   */
  @Value.Derived
  default boolean binary() {
    return false;
  }

  /**
   * Use this with {@link TransactionRequestParams#maxLedger()} to specify a range of up to 1000 ledger indexes,
   * starting with this ledger (inclusive).
   *
   * <p>If the server cannot find the transaction, it confirms whether it was able to search all the
   * ledgers in this range.
   *
   * @return An optionally-present {@link UnsignedLong} indicating the minimum ledger to search.
   */
  @JsonProperty("min_ledger")
  Optional<UnsignedLong> minLedger();

  /**
   * Use this with {@link TransactionRequestParams#minLedger()} to specify a range of up to 1000 ledger indexes,
   * ending with this ledger (inclusive).
   *
   * <p>If the server cannot find the transaction, it confirms whether it was able to search all the ledgers in the
   * requested range.
   *
   * @return An optionally-present {@link UnsignedLong} indicating the maximum ledger to search.
   */
  @JsonProperty("max_ledger")
  Optional<UnsignedLong> maxLedger();

}
