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
import org.xrpl.xrpl4j.model.jackson.modules.PathCurrencyDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.PathCurrencySerializer;
import org.xrpl.xrpl4j.model.ledger.CurrencyIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Represents a currency that an account holds on the XRPL, which can be used to specify the source currencies in
 * {@link RipplePathFindRequestParams}.
 *
 * <p>This class wraps an {@link Issue} to support both traditional currencies (XRP and IOUs) and MPTokens.
 * For traditional currencies, use {@link CurrencyIssue}. For MPTokens, use
 * {@link org.xrpl.xrpl4j.model.ledger.MptIssue}.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathCurrency.class, using = PathCurrencySerializer.class)
@JsonDeserialize(as = ImmutablePathCurrency.class, using = PathCurrencyDeserializer.class)
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
   * <p>This is a convenience method for creating a {@link PathCurrency} for XRP or a currency without
   * specifying an issuer. For IOUs with an issuer or MPTokens, use {@link #of(Issue)} instead.</p>
   *
   * @param currency A {@link String} of either a 3 character currency code, or a 40 character hexadecimal encoded
   *                 currency code value.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(String currency) {
    return builder()
      .issue(CurrencyIssue.builder().currency(currency).build())
      .build();
  }

  /**
   * Construct a {@link PathCurrency} from an {@link Issue}.
   *
   * <p>This method supports both {@link CurrencyIssue} (for XRP and IOUs) and
   * {@link org.xrpl.xrpl4j.model.ledger.MptIssue} (for MPTokens).</p>
   *
   * @param issue An {@link Issue} representing the currency.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(Issue issue) {
    return builder()
      .issue(issue)
      .build();
  }

  /**
   * Construct a {@link PathCurrency} with the specified currency code and issuer.
   *
   * <p>This is a convenience method for creating a {@link PathCurrency} for an IOU with an issuer.
   * For MPTokens, use {@link #of(Issue)} with an {@link org.xrpl.xrpl4j.model.ledger.MptIssue} instead.</p>
   *
   * @param currency A {@link String} of either a 3 character currency code, or a 40 character hexadecimal encoded
   *                 currency code value.
   * @param issuer The {@link Address} of the issuer of the currency.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(String currency, Address issuer) {
    return builder()
      .issue(CurrencyIssue.builder().currency(currency).issuer(issuer).build())
      .build();
  }

  /**
   * The asset that this path currency represents. This can be either a
   * {@link org.xrpl.xrpl4j.model.ledger.CurrencyIssue} (for XRP or IOUs) or an
   * {@link org.xrpl.xrpl4j.model.ledger.MptIssue} (for MPTokens).
   *
   * <p>The {@link Issue} fields will be unwrapped and serialized directly into the PathCurrency JSON object
   * by the custom {@link org.xrpl.xrpl4j.model.jackson.modules.PathCurrencySerializer}:
   * <ul>
   *   <li>For {@link org.xrpl.xrpl4j.model.ledger.CurrencyIssue}:
   *       {@code {"currency": "...", "issuer": "..."}}</li>
   *   <li>For {@link org.xrpl.xrpl4j.model.ledger.MptIssue}:
   *       {@code {"mpt_issuance_id": "..."}}</li>
   * </ul>
   *
   * @return An {@link Issue}.
   */
  Issue issue();

}
