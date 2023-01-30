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
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * The result of an account_currencies rippled call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountCurrenciesResult.class)
@JsonDeserialize(as = ImmutableAccountCurrenciesResult.class)
public interface AccountCurrenciesResult extends XrplResult {

  /**
   * Construct a builder.
   *
   * @return {@link ImmutableAccountCurrenciesResult.Builder}
   */
  static ImmutableAccountCurrenciesResult.Builder builder() {
    return ImmutableAccountCurrenciesResult.builder();
  }

  /**
   * The identifying Hash of the ledger version used to generate this response.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Get {@link #ledgerHash()}, or throw an {@link IllegalStateException} if {@link #ledgerHash()} is empty.
   *
   * @return The value of {@link #ledgerHash()}.
   *
   * @throws IllegalStateException If {@link #ledgerHash()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Hash256 ledgerHashSafe() {
    return ledgerHash()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerHash."));
  }

  /**
   * The Ledger Index of the ledger version used to generate this response.
   *
   * @return A {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * Get {@link #ledgerIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerIndex()} is null.
   *
   * @return The value of {@link #ledgerIndex()}.
   *
   * @throws IllegalStateException If {@link #ledgerIndex()} is null.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerIndexSafe() {
    return ledgerIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerIndex."));
  }

  /**
   * The ledger index of the current open ledger, which was used when retrieving this information. Only present in
   * responses to requests with ledger_index = "current".
   *
   * @return An optionally-present {@link LedgerIndex} representing the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * Get {@link #ledgerCurrentIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerCurrentIndex()} is
   * empty.
   *
   * @return The value of {@link #ledgerCurrentIndex()}.
   *
   * @throws IllegalStateException If {@link #ledgerCurrentIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerCurrentIndexSafe() {
    return ledgerCurrentIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerCurrentIndex."));
  }

  /**
   * If true, the information in this response comes from a validated ledger version. Otherwise, the information is
   * subject to change.
   *
   * @return {@code true} if the information in this response comes from a validated ledger version, {@code false} if
   *   not.
   */
  boolean validated();

  /**
   * Array of currency codes for currencies that this account can receive.
   *
   * @return Array of currencies that this account can receive.
   */
  @JsonProperty("receive_currencies")
  List<String> receiveCurrencies();

  /**
   * Array of currency codes for currencies that this account can send.
   *
   * @return Array of currencies that this account can send.
   */
  @JsonProperty("send_currencies")
  List<String> sendCurrencies();
}
