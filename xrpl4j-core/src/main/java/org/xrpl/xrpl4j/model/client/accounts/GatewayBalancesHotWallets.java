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
import org.xrpl.xrpl4j.model.jackson.modules.GatewayBalancesHotWalletsDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Amounts issued to the hotwallet addresses from the request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesHotWallets.class)
@JsonDeserialize(as = ImmutableGatewayBalancesHotWallets.class, using = GatewayBalancesHotWalletsDeserializer.class)
public interface GatewayBalancesHotWallets {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGatewayBalancesHotWallets.Builder}.
   */
  static ImmutableGatewayBalancesHotWallets.Builder builder() {
    return ImmutableGatewayBalancesHotWallets.builder();
  }

  /**
   * Map of addresses of currencies holders to the balances of the currencies held as issued by the
   * issuer in the full response.
   *
   * @return A map of the {@link Address}es of holders of issued currencies to a list of
   *   {@link GatewayBalancesIssuedCurrencyAmount}s specifying balances of issued currencies from the issuer.
   */
  @Value.Default
  default Map<Address, List<GatewayBalancesIssuedCurrencyAmount>> balancesByHolder() {
    return Collections.emptyMap();
  }
}
