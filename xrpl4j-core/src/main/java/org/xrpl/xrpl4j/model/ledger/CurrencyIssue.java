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
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

/**
 * Represents a currency-based asset (XRP or IOU) on the ledger without an amount.
 *
 * <p>This is one of the two implementations of {@link Issue}. A {@link CurrencyIssue} identifies
 * an asset by its currency code and optional issuer address. XRP is represented by the currency
 * code "XRP" with no issuer. IOUs are represented by a currency code and an issuer address.</p>
 *
 * @see Issue
 * @see MptIssue
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCurrencyIssue.class)
@JsonDeserialize(as = ImmutableCurrencyIssue.class)
public interface CurrencyIssue extends Issue {

  /**
   * Constant {@link CurrencyIssue} representing XRP.
   */
  CurrencyIssue XRP = CurrencyIssue.builder().currency("XRP").build();

  /**
   * Construct a {@code CurrencyIssue} builder.
   *
   * @return An {@link ImmutableCurrencyIssue.Builder}.
   */
  static ImmutableCurrencyIssue.Builder builder() {
    return ImmutableCurrencyIssue.builder();
  }

  /**
   * Either a 3 character currency code, or a 40 character hexadecimal encoded currency code value.
   *
   * @return A {@link String} containing the currency code.
   */
  @JsonProperty("currency")
  String currency();

  /**
   * The {@link Address} of the issuer of the currency, or empty if the currency is XRP.
   *
   * @return The {@link Address} of the issuer account.
   */
  @JsonProperty("issuer")
  Optional<Address> issuer();
}

