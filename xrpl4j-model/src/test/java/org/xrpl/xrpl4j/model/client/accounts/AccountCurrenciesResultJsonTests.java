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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class AccountCurrenciesResultJsonTests extends AbstractJsonTest {

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addSendCurrencies("USD")
      .addReceiveCurrencies("EUR")
      .addReceiveCurrencies("USD")
      .build();

    String json = "{\n" +
      "        \"send_currencies\": [\n" +
      "            \"USD\"\n" +
      "        ],\n" +
      "        \"receive_currencies\": [\n" +
      "            \"EUR\",\n" +
      "            \"USD\"\n" +
      "        ],\n" +
      "        \"ledger_hash\": \"B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D\",\n" +
      "        \"ledger_index\": 37230600,\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testCurrentJson() throws JSONException, JsonProcessingException {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(66467750)))
      .status("success")
      .validated(false)
      .receiveCurrencies(Lists.newArrayList("BTC", "CNY", "015841551A748AD2C1F76FF6ECB0CCCD00000000"))
      .sendCurrencies(Lists.newArrayList("ASP", "BTC", "USD"))
      .build();

    String json = "{\n" +
      "        \"ledger_current_index\": 66467750,\n" +
      "        \"receive_currencies\": [\n" +
      "            \"BTC\",\n" +
      "            \"CNY\",\n" +
      "            \"015841551A748AD2C1F76FF6ECB0CCCD00000000\"\n" +
      "        ],\n" +
      "        \"send_currencies\": [\n" +
      "            \"ASP\",\n" +
      "            \"BTC\",\n" +
      "            \"USD\"\n" +
      "        ],\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": false\n" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }
}
