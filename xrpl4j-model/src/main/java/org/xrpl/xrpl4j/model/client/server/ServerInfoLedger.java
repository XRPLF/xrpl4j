package org.xrpl.xrpl4j.model.client.server;

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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.math.BigDecimal;

/**
 * Information about a recent ledger, as represented in {@link ServerInfoResult}s.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLedger.class)
@JsonDeserialize(as = ImmutableServerInfoLedger.class)
public interface ServerInfoLedger {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableServerInfoLedger.Builder}.
   */
  static ImmutableServerInfoLedger.Builder builder() {
    return ImmutableServerInfoLedger.builder();
  }

  /**
   * The time since the ledger was closed, in seconds.
   *
   * @return An {@link UnsignedInteger} representing the age, in seconds.
   */
  UnsignedInteger age();

  /**
   * Unique hash for the ledger, as hexadecimal.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  Hash256 hash();

  /**
   * Minimum amount of XRP (not drops) necessary for every account to keep in reserve.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP to reserve.
   */
  @JsonProperty("reserve_base_xrp")
  BigDecimal reserveBaseXrp();

  /**
   * Amount of XRP (not drops) added to the account reserve for each object an account owns in the ledger.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP added.
   */
  @JsonProperty("reserve_inc_xrp")
  BigDecimal reserveIncXrp();

  /**
   * The ledger index of the ledger.
   *
   * @return A {@link LedgerIndex} indicating the sequence of the latest ledger.
   */
  @JsonProperty("seq")
  LedgerIndex sequence();

  /**
   * The base XRP cost of transaction.
   *
   * @return A {@link BigDecimal} representing base fee amount in XRP.
   */
  @JsonProperty("base_fee_xrp")
  BigDecimal baseFeeXrp();

}
