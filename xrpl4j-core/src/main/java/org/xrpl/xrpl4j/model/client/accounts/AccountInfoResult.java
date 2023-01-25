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
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;

import java.util.Optional;

/**
 * The result of an "account_info" rippled API call, containing information about a given account on the XRP Ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoResult.class)
@JsonDeserialize(as = ImmutableAccountInfoResult.class)
public interface AccountInfoResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountInfoResult.Builder}.
   */
  static ImmutableAccountInfoResult.Builder builder() {
    return ImmutableAccountInfoResult.builder();
  }

  /**
   * The {@link AccountRootObject} ledger object with this account's information, as stored in the ledger.
   *
   * @return The returned {@link AccountRootObject}.
   */
  @JsonProperty("account_data")
  AccountRootObject accountData();

  /**
   * (Omitted if ledger_current_index is provided instead) The ledger index of the ledger version used when
   * retrieving this information. The information does not contain any changes from ledger versions newer than this one.
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
   * (Omitted if ledger_index is provided instead) The ledger index of the current in-progress ledger,
   * which was used when retrieving this information.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * Get {@link #ledgerCurrentIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerCurrentIndex()} is
   * empty.
   *
   * @return The value of {@link #ledgerCurrentIndex()}.
   * @throws IllegalStateException If {@link #ledgerCurrentIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerCurrentIndexSafe() {
    return ledgerCurrentIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerCurrentIndex."));
  }

  /**
   * (Omitted unless queue specified as true and querying the current open ledger.)
   * Information about queued transactions sent by this account. This information describes the state of
   * the local rippled server, which may be different from other servers in the peer-to-peer XRP Ledger network.
   * Some fields may be omitted because the values are calculated "lazily" by the queuing mechanism.
   *
   * @return An optionally-present {@link QueueData}.
   */
  @JsonProperty("queue_data")
  Optional<QueueData> queueData();

  /**
   * True if this data is from a validated ledger version; if false, this data is not final.
   *
   * @return {@code true} if this data is from a validated ledger version, otherwise {@code false}.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

}
