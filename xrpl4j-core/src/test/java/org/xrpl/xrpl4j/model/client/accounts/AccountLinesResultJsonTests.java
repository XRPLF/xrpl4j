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
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;

public class AccountLinesResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountLinesResult result = AccountLinesResult.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .status("success")
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .addLines(
        TrustLine.builder()
          .account(Address.of("r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z"))
          .balance("0")
          .currency("ASP")
          .limit("0")
          .limitPeer("10")
          .qualityIn(UnsignedInteger.ZERO)
          .qualityOut(UnsignedInteger.ZERO)
          .build(),
        TrustLine.builder()
          .account(Address.of("rs9M85karFkCRjvc6KMWn8Coigm9cbcgcx"))
          .balance("0")
          .currency("015841551A748AD2C1F76FF6ECB0CCCD00000000")
          .limit("10.01037626125837")
          .limitPeer("0")
          .noRipple(true)
          .qualityIn(UnsignedInteger.ZERO)
          .qualityOut(UnsignedInteger.ZERO)
          .build()
      )
      .build();

    String json = "{\n" +
      "        \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "        \"ledger_index\": 1,\n" +
      "        \"lines\": [\n" +
      "            {\n" +
      "                \"account\": \"r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z\",\n" +
      "                \"balance\": \"0\",\n" +
      "                \"currency\": \"ASP\",\n" +
      "                \"limit\": \"0\",\n" +
      "                \"limit_peer\": \"10\",\n" +
      "                \"no_ripple\": false,\n" +
      "                \"no_ripple_peer\": false,\n" +
      "                \"authorized\": false,\n" +
      "                \"peer_authorized\": false,\n" +
      "                \"freeze\": false,\n" +
      "                \"freeze_peer\": false,\n" +
      "                \"deep_freeze\": false,\n" +
      "                \"deep_freeze_peer\": false,\n" +
      "                \"quality_in\": 0,\n" +
      "                \"quality_out\": 0\n" +
      "            },\n" +
      "            {\n" +
      "                \"account\": \"rs9M85karFkCRjvc6KMWn8Coigm9cbcgcx\",\n" +
      "                \"balance\": \"0\",\n" +
      "                \"currency\": \"015841551A748AD2C1F76FF6ECB0CCCD00000000\",\n" +
      "                \"limit\": \"10.01037626125837\",\n" +
      "                \"limit_peer\": \"0\",\n" +
      "                \"no_ripple\": true,\n" +
      "                \"no_ripple_peer\": false,\n" +
      "                \"authorized\": false,\n" +
      "                \"peer_authorized\": false,\n" +
      "                \"freeze\": false,\n" +
      "                \"freeze_peer\": false,\n" +
      "                \"deep_freeze\": false,\n" +
      "                \"deep_freeze_peer\": false,\n" +
      "                \"quality_in\": 0,\n" +
      "                \"quality_out\": 0\n" +
      "            }\n" +
      "        ],\n" +
      "        \"status\": \"success\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
