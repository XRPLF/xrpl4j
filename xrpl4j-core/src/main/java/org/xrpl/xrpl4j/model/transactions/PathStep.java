package org.xrpl.xrpl4j.model.transactions;

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

import java.util.Optional;

/**
 * A step in a Path for cross-currency payments on the XRP Ledger.
 *
 * @see "https://xrpl.org/paths.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathStep.class)
@JsonDeserialize(as = ImmutablePathStep.class)
public interface PathStep {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutablePathStep.Builder}.
   */
  static ImmutablePathStep.Builder builder() {
    return ImmutablePathStep.builder();
  }

  /**
   * If present, this {@link PathStep} represents rippling through the specified {@link Address}.
   * MUST NOT be provided if this {@link PathStep} specifies the {@link PathStep#currency()} or
   * {@link PathStep#issuer()} fields.
   *
   * @return An {@link Optional} of type {@link Address}.
   */
  Optional<Address> account();

  /**
   * If present, this {@link PathStep} represents changing currencies through an order book.
   * The currency specified indicates the new currency. MUST NOT be provided if this {@link PathStep} specifies the
   * {@link PathStep#account()} field.
   *
   * @return An {@link Optional} of type {@link String} containing the currency code.
   */
  Optional<String> currency();

  /**
   * If present, this path step represents changing currencies and this address defines the issuer of the new currency.
   * If omitted in a step with a non-XRP currency, a previous step of the path defines the issuer.
   * If present when currency is omitted, indicates a path step that uses an order book between same-named
   * currencies with different issuers.
   * MUST be omitted if the currency is XRP. MUST NOT be provided if this step specifies the {@link PathStep#account()}
   * field.
   *
   * @return The {@link Optional} {@link Address} of the currency issuer.
   */
  Optional<Address> issuer();

}
