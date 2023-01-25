package org.xrpl.xrpl4j.model.client.accounts;

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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;

import java.util.Optional;

/**
 * The transaction that gets returned as part of a response to the account_tx rippled method.
 *
 * @param <T> The type of {@link Transaction}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountTransactionsTransactionResult.class)
@JsonDeserialize(as = ImmutableAccountTransactionsTransactionResult.class)
public interface AccountTransactionsTransactionResult<T extends Transaction> extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @param <T> The type of {@link Transaction} contained in this result.
   *
   * @return An {@link ImmutableAccountTransactionsTransactionResult.Builder}.
   */
  static <T extends Transaction> ImmutableAccountTransactionsTransactionResult.Builder<T> builder() {
    return ImmutableAccountTransactionsTransactionResult.builder();
  }

  /**
   * The {@link Transaction}.
   *
   * @return A {@link T} with the transaction fields.
   * @deprecated This field will be removed in a future release. The {@link Transaction} can be found in {@link
   *   #resultTransaction()}, and the transaction's {@code hash} and {@code ledgerIndex} can be found in
   *   {@code resultTransaction().hash()} and {@code resultTransaction().ledgerIndex()}, respectively.
   */
  @Deprecated
  @Value.Derived
  @JsonIgnore
  default T transaction() {
    return resultTransaction().transaction();
  }

  /**
   * The {@link Transaction}, wrapped in a {@link AccountTransactionsTransaction}, which includes the transaction's
   * ledger index and hash.
   *
   * @return A {@link AccountTransactionsTransaction} containing the {@link Transaction}, its hash, and the
   *   ledger index that it was included in.
   */
  @JsonProperty("tx")
  AccountTransactionsTransaction<T> resultTransaction();

  /**
   * Metadata about the transaction if this data is from a validated ledger version.
   *
   * @return {@link TransactionMetadata} or empty for non-validated transactions.
   */
  @JsonProperty("meta")
  Optional<TransactionMetadata> metadata();

  /**
   * Whether or not this transaction came from a validated ledger.
   *
   * @return {@code true} if from a validated ledger, otherwise {@code false}.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }
}
