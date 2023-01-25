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

import java.util.Collections;
import java.util.List;

/**
 * Total amounts issued to addresses not included in the original request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesObligations.class)
@JsonDeserialize(as = ImmutableGatewayBalancesObligations.class)
public interface GatewayBalancesObligations {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGatewayBalancesObligations.Builder}
   */
  static ImmutableGatewayBalancesObligations.Builder builder() {
    return ImmutableGatewayBalancesObligations.builder();
  }

  /**
   * The balances of issued currencies from the issuer in the results that are not
   * includes in the hotwallet balances.
   *
   * @return A list of {@link GatewayBalancesIssuedCurrencyAmount}s for issued currencies not
   *   included in the hotwallet balances.
   */
  @Value.Default
  default List<GatewayBalancesIssuedCurrencyAmount> balances() {
    return Collections.emptyList();
  }
}
