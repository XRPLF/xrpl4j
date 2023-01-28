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
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountLinesRequestParamsJsonTests extends AbstractJsonTest {

  public static final Hash256 HASH_256 = Hash256.of("92FA6A9FC8EA6018D5D16532D7795C91BFB0831355BDFDA177E86C8BF997985F");
  
  @Test
  public void testWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
      .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .build();

    String json = "{\n" +
      "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"ledger_index\": 1\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {

    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
      .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();

    String json = "{\n" +
      "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"ledger_index\": \"current\"\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {

    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
        .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
        .ledgerSpecifier(LedgerSpecifier.of(HASH_256))
        .peer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .limit(UnsignedInteger.ONE)
        .marker(Marker.of("marker"))
        .build();

    String json = "{\n" +
        "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
        "            \"ledger_hash\": \"" + HASH_256 + "\",\n" +
        "            \"peer\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "            \"limit\": 1,\n" +
        "            \"marker\": \"marker\"\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
