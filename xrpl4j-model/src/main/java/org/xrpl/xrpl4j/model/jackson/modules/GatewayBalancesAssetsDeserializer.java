package org.xrpl.xrpl4j.model.jackson.modules;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesAssets;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableGatewayBalancesAssets;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Custom Jackson deserializer for {@link GatewayBalancesAssets}.
 */
public class GatewayBalancesAssetsDeserializer extends StdDeserializer<ImmutableGatewayBalancesAssets> {

  /**
   * No-args constructor.
   */
  public GatewayBalancesAssetsDeserializer() {
    super(ImmutableGatewayBalancesAssets.class);
  }

  @Override
  public ImmutableGatewayBalancesAssets deserialize(
    JsonParser jsonParser,
    DeserializationContext context
  ) throws IOException, JsonProcessingException {
    Map<Address, List<GatewayBalancesIssuedCurrencyAmount>> balances = jsonParser
      .readValueAs(new TypeReference<Map<Address, List<GatewayBalancesIssuedCurrencyAmount>>>() {});

    ImmutableGatewayBalancesAssets assets = GatewayBalancesAssets.builder()
      .balancesByIssuer(balances)
      .build();
    return assets;
  }

}
