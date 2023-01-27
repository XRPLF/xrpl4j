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
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class AccountObjectsRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testWithLedgerHash() throws JsonProcessingException, JSONException {
    AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerSpecifier(
        LedgerSpecifier.of(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
      )
      .type(AccountObjectsRequestParams.AccountObjectType.STATE)
      .deletionBlockersOnly(false)
      .limit(UnsignedInteger.valueOf(10))
      .build();

    String json = "{\n" +
      "            \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"ledger_hash\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
      "            \"type\": \"state\",\n" +
      "            \"deletion_blockers_only\": false,\n" +
      "            \"limit\": 10\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerSpecifier(
        LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE))
      )
      .type(AccountObjectsRequestParams.AccountObjectType.STATE)
      .deletionBlockersOnly(false)
      .limit(UnsignedInteger.valueOf(10))
      .build();

    String json = "{\n" +
      "            \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"ledger_index\": 1,\n" +
      "            \"type\": \"state\",\n" +
      "            \"deletion_blockers_only\": false,\n" +
      "            \"limit\": 10\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndexShortcut() throws JsonProcessingException, JSONException {
    AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .type(AccountObjectsRequestParams.AccountObjectType.STATE)
        .deletionBlockersOnly(false)
        .limit(UnsignedInteger.valueOf(10))
        .build();

    String json = "{\n" +
      "            \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"ledger_index\": \"validated\",\n" +
      "            \"type\": \"state\",\n" +
      "            \"deletion_blockers_only\": false,\n" +
      "            \"limit\": 10\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
