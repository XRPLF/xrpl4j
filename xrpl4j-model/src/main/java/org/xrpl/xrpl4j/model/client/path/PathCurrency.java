package org.xrpl.xrpl4j.model.client.path;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

/**
 * Represents a currency that an account holds on the XRPL, which can be used to specify the source currencies in
 * {@link RipplePathFindRequestParams}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathCurrency.class)
@JsonDeserialize(as = ImmutablePathCurrency.class)
public interface PathCurrency {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutablePathCurrency.Builder}.
   */
  static ImmutablePathCurrency.Builder builder() {
    return ImmutablePathCurrency.builder();
  }

  /**
   * Construct a {@link PathCurrency} with the specified currency code and no issuer.
   *
   * @param currency A {@link String} of either a 3 character currency code, or a 40 character hexadecimal encoded
   *                 currency code value.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(String currency) {
    return builder()
      .currency(currency)
      .build();
  }

  /**
   * Either a 3 character currency code, or a 40 character hexadecimal encoded currency code value.
   *
   * @return A {@link String} containing the currency code.
   */
  String currency();

  /**
   * The {@link Address} of the issuer of the currency.
   *
   * @return The optionally-present {@link Address} of the issuer account.
   */
  Optional<Address> issuer();

}
