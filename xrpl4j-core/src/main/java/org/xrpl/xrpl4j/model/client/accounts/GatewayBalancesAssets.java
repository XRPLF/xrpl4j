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
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Total amounts held that are issued by others. In the recommended configuration, the issuing address
 * should have none.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesAssets.class)
@JsonDeserialize(as = ImmutableGatewayBalancesAssets.class)
public interface GatewayBalancesAssets {

  /**
   * Constructs a builder for this class.
   * @return An {@link ImmutableGatewayBalancesAssets.Builder}.
   */
  static ImmutableGatewayBalancesAssets.Builder builder() {
    return ImmutableGatewayBalancesAssets.builder();
  }

  /**
   * The balances of issued currencies issued by the address which is the key of this map.
   *
   * @return A map of issued currencies, keyed by the issuer {@link Address} to a list of
   *   {@link GatewayBalancesIssuedCurrencyAmount}s of currencies issued
   */
  @Value.Default
  default Map<Address, List<GatewayBalancesIssuedCurrencyAmount>> balancesByIssuer() {
    return Collections.emptyMap();
  }

}
