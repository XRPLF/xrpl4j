package org.xrpl.xrpl4j.model.client.fees;

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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * A sub-object of {@link FeeResult} containing various information about the transaction cost in fee levels
 * for the current open ledger.
 *
 * <p>The ratio in fee levels applies to any transaction relative to the minimum cost of that particular transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFeeLevels.class)
@JsonDeserialize(as = ImmutableFeeLevels.class)
public interface FeeLevels {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableFeeLevels.Builder}.
   */
  static ImmutableFeeLevels.Builder builder() {
    return ImmutableFeeLevels.builder();
  }

  /**
   * The median transaction cost among transactions in the previous validated ledger, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the median level.
   */
  @JsonProperty("median_level")
  XrpCurrencyAmount medianLevel();

  /**
   * The minimum transaction cost required to be queued for a future ledger, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the minimum level.
   */
  @JsonProperty("minimum_level")
  XrpCurrencyAmount minimumLevel();

  /**
   * The minimum transaction cost required to be included in the current open ledger, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the open ledger level.
   */
  @JsonProperty("open_ledger_level")
  XrpCurrencyAmount openLedgerLevel();

  /**
   * The equivalent of the minimum transaction cost, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the reference level.
   */
  @JsonProperty("reference_level")
  XrpCurrencyAmount referenceLevel();

}
