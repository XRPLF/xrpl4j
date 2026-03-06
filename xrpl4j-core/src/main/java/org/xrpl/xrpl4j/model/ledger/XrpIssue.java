package org.xrpl.xrpl4j.model.ledger;

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

/**
 * Represents XRP as an asset on the ledger without an amount.
 *
 * <p>This is one of the three implementations of {@link Issue}. An {@link XrpIssue} represents
 * the native XRP currency, which is identified solely by the currency code "XRP" with no issuer.</p>
 *
 * @see Issue
 * @see IouIssue
 * @see MptIssue
 */
@Value.Immutable
@JsonSerialize(as = ImmutableXrpIssue.class)
@JsonDeserialize(as = ImmutableXrpIssue.class)
public interface XrpIssue extends Issue {

  /**
   * Singleton constant {@link XrpIssue} representing XRP.
   */
  XrpIssue XRP = XrpIssue.builder().build();

  /**
   * Construct a {@code XrpIssue} builder.
   *
   * @return An {@link ImmutableXrpIssue.Builder}.
   */
  static ImmutableXrpIssue.Builder builder() {
    return ImmutableXrpIssue.builder();
  }

  /**
   * The currency code, which is always "XRP" for {@link XrpIssue}.
   *
   * @return A {@link String} containing "XRP".
   */
  @JsonProperty("currency")
  @Value.Default
  default String currency() {
    return "XRP";
  }

  /**
   * Validate that the currency is "XRP".
   */
  @Value.Check
  default void checkCurrencyIsXrp() {
    if (!"XRP".equals(currency())) {
      throw new IllegalStateException("XrpIssue currency must be 'XRP', but was: " + currency());
    }
  }
}

