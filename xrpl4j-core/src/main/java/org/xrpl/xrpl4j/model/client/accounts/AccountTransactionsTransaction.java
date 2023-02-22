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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.TimeUtils;
import org.xrpl.xrpl4j.model.jackson.modules.AccountTransactionsTransactionDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Represents a transaction that is returned as part of the result of an {@code account_tx} rippled method call.
 *
 * @param <T> The type of {@link Transaction} contained in this class.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountTransactionsTransaction.class)
@JsonDeserialize(
  as = ImmutableAccountTransactionsTransaction.class,
  using = AccountTransactionsTransactionDeserializer.class
)
public interface AccountTransactionsTransaction<T extends Transaction> {
  
  /**
   * Construct a builder for this class.
   *
   * @param <T> The type of {@link Transaction} to include in the builder.
   *
   * @return A new {@link ImmutableAccountTransactionsTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableAccountTransactionsTransaction.Builder<T> builder() {
    return ImmutableAccountTransactionsTransaction.builder();
  }

  /**
   * The {@link Transaction}.
   *
   * @return A {@link T} with the transaction fields.
   */
  @JsonUnwrapped
  T transaction();

  /**
   * The transaction hash of this transaction.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  Hash256 hash();

  /**
   * The index of the ledger that this transaction was included in.
   *
   * @return The {@link LedgerIndex} that this transaction was included in.
   */
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  /**
   * The approximate close time (using Ripple Epoch) of the ledger containing this transaction.
   * This is an undocumented field.
   *
   * @return An optionally-present {@link UnsignedLong}.
   */
  @JsonProperty("date")
  Optional<UnsignedLong> closeDate();

  /**
   * The approximate close time in UTC offset.
   * This is derived from undocumented field.
   *
   * @return An optionally-present {@link ZonedDateTime}.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Optional<ZonedDateTime> closeDateHuman() {
    return closeDate().map(TimeUtils::xrplTimeToZonedDateTime);
  }
  
}
