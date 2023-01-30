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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * The result of an account_channels rippled call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountChannelsResult.class)
@JsonDeserialize(as = ImmutableAccountChannelsResult.class)
public interface AccountChannelsResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountChannelsResult.Builder}.
   */
  static ImmutableAccountChannelsResult.Builder builder() {
    return ImmutableAccountChannelsResult.builder();
  }

  /**
   * The {@link Address} of the source/owner of the {@link #channels()}. This corresponds to the
   * {@link AccountChannelsRequestParams#account()} field of the request.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * Payment channels owned by this {@link #account()}.
   *
   * @return A {@link List} of {@link PaymentChannelResultObject}s.
   */
  List<PaymentChannelResultObject> channels();

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
   * @throws IllegalStateException If {@link #ledgerHash()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Hash256 ledgerHashSafe() {
    return ledgerHash()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerHash."));
  }

  /**
   * The Ledger Index of the ledger version used to generate this response. Only present in responses to requests
   * with ledger_index = "validated" or "closed".
   *
   * @return A {@link LedgerIndex}.
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
   * The ledger index of the current open ledger, which was used when retrieving this information. Only present
   * in responses to requests with ledger_index = "current".
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
   * @throws IllegalStateException If {@link #ledgerCurrentIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerCurrentIndexSafe() {
    return ledgerCurrentIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerCurrentIndex."));
  }


  /**
   * If true, the information in this response comes from a validated ledger version.
   * Otherwise, the information is subject to change.
   *
   * @return {@code true} if the information in this response comes from a validated ledger version, {@code false}
   *   if not.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  /**
   * The limit to how many {@link #channels()} were actually returned by this request.
   *
   * @return An optionally-present {@link UnsignedInteger} containing the request limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Server-defined value for pagination. Pass this to the next call to resume getting results where this
   * call left off. Omitted when there are no additional pages after this one.
   *
   * @return An optionally-present {@link String} containing the response marker.
   */
  Optional<Marker> marker();

}
