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
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesObligations;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableGatewayBalancesObligations;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom Jackson deserializer for {@link GatewayBalancesObligations}.
 */
public class GatewayBalancesObligationsDeserializer extends StdDeserializer<ImmutableGatewayBalancesObligations> {

  /**
   * No-args constructor.
   */
  public GatewayBalancesObligationsDeserializer() {
    super(GatewayBalancesObligations.class);
  }

  @Override
  public ImmutableGatewayBalancesObligations deserialize(
    JsonParser jsonParser,
    DeserializationContext deserializationContext
  ) throws IOException, JsonProcessingException {
    Map<String, String> rawBalances = jsonParser.readValueAs(new TypeReference<Map<String, String>>() {});

    List<GatewayBalancesIssuedCurrencyAmount> balances = rawBalances
      .entrySet()
      .stream()
      .map(e -> GatewayBalancesIssuedCurrencyAmount
        .builder()
        .currency(e.getKey())
        .value(e.getValue())
        .build()
      )
      .collect(Collectors.toList());

    return ImmutableGatewayBalancesObligations
      .builder()
      .balances(balances)
      .build();
  }
}
