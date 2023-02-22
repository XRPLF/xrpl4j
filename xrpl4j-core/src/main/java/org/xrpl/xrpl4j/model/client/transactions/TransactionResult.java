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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.TimeUtils;
import org.xrpl.xrpl4j.model.jackson.modules.TransactionResultDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * The result of a tx rippled API method call.
 *
 * @param <TxnType> The type of {@link Transaction} that was requested.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTransactionResult.class)
@JsonDeserialize(using = TransactionResultDeserializer.class)
public interface TransactionResult<TxnType extends Transaction> extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @param <T> The actual type of {@link Transaction}.
   *
   * @return An {@link ImmutableTransactionResult.Builder}
   */
  static <T extends Transaction> ImmutableTransactionResult.Builder<T> builder() {
    return ImmutableTransactionResult.builder();
  }

  /**
   * The {@link Transaction} that was returned as a result of the "tx" call.
   *
   * @return A {@link Transaction} of type {@link TxnType}.
   */
  @JsonUnwrapped
  TxnType transaction();

  /**
   * The ledger index of the ledger that includes this {@link Transaction}.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * Get {@link #ledgerIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerIndex()} is empty.
   *
   * @return The value of {@link #ledgerIndex()}.
   * @throws IllegalStateException If {@link #ledgerIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerIndexSafe() {
    return ledgerIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerIndex."));
  }

  /**
   * The identifying hash of the {@link Transaction}.
   *
   * @return The {@link Hash256} of {@link #transaction()}.
   */
  Hash256 hash();

  /**
   * {@code true} if this data is from a validated ledger version; If {@code false}, this data is not final.
   *
   * @return {@code true} if this data is from a validated ledger version; If {@code false}, this data is not final.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  /**
   * Metadata about the transaction if this data is from a validated ledger version.
   *
   * @return metadata or empty for non-validated transactions.
   */
  @JsonProperty("meta")
  @JsonAlias("metaData")
  Optional<TransactionMetadata> metadata();

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
