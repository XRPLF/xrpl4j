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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * <p>Similar to, but deliberately different from {@code IssuedCurrencyAmount}.</p>
 *
 * <p>A type for handling balances of an issued currency that may or may not have information available
 * in the object being deserialized as to the owner address or issuer address. The gateway_balances method returns one
 * set of values specifying the issuer but as a string based key to the array of values this type can deserialize to,
 * and another set of values specifying the holder but as a string based key to the array of values this type can
 * deserialize to.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesIssuedCurrencyAmount.class)
@JsonDeserialize(as = ImmutableGatewayBalancesIssuedCurrencyAmount.class)
public interface GatewayBalancesIssuedCurrencyAmount {

  /**
   * Construct a builder.
   *
   * @return {@link ImmutableGatewayBalancesIssuedCurrencyAmount.Builder}
   */
  static ImmutableGatewayBalancesIssuedCurrencyAmount.Builder builder() {
    return ImmutableGatewayBalancesIssuedCurrencyAmount.builder();
  }

  /**
   * Quoted decimal representation of the amount of currency. This can include scientific notation, such as 1.23e11
   * meaning 123,000,000,000. Both e and E may be used. Note that while this implementation merely holds a {@link
   * String} with no value restrictions, the XRP Ledger does not tolerate unlimited precision values. Instead, non-XRP
   * values (i.e., values held in this object) can have up to 16 decimal digits of precision, with a maximum value of
   * 9999999999999999e80. The smallest positive non-XRP value is 1e-81.
   *
   * @return A {@link String} containing the amount of this issued currency.
   */
  String value();

  /**
   * Arbitrary code for currency to issue. Cannot be XRP.
   *
   * @return A {@link String} containing the currency code.
   */
  String currency();
}
