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
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountSponsoringResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addSponsoredObjects(
        OfferObject.builder()
          .flags(OfferFlags.of(0))
          .account(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXf8"))
          .sequence(UnsignedInteger.valueOf(13))
          .takerPays(XrpCurrencyAmount.ofDrops(2000000))
          .takerGets(XrpCurrencyAmount.ofDrops(3000000))
          .bookDirectory(Hash256.of("4627DFFCFF8B5A265EDBD8AE8C14A52325DBFEDAF4F5C32E5E03E788E09BB35C"))
          .bookNode("0000000000000000")
          .ownerNode("0000000000000000")
          .previousTransactionId(Hash256.of("F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF"))
          .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14524914))
          .index(Hash256.of("96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797"))
          .build()
      )
      .ledgerHash(Hash256.of("4109C6F2045FC7EFF4CDE8F9905D19C28820D86304080FF886B299F0206E42B5"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .build();

    String json = "{\n" +
      "        \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "        \"sponsored_objects\": [\n" +
      "            {\n" +
      "                \"Flags\": 0,\n" +
      "                \"Account\": \"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXf8\",\n" +
      "                \"Sequence\": 13,\n" +
      "                \"TakerPays\": \"2000000\",\n" +
      "                \"TakerGets\": \"3000000\",\n" +
      "                \"BookDirectory\": \"4627DFFCFF8B5A265EDBD8AE8C14A52325DBFEDAF4F5C32E5E03E788E09BB35C\",\n" +
      "                \"BookNode\": \"0000000000000000\",\n" +
      "                \"OwnerNode\": \"0000000000000000\",\n" +
      "                \"PreviousTxnID\": \"F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF\",\n" +
      "                \"PreviousTxnLgrSeq\": 14524914,\n" +
      "                \"LedgerEntryType\": \"Offer\",\n" +
      "                \"index\": \"96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797\"\n" +
      "            }\n" +
      "        ],\n" +
      "        \"ledger_hash\": \"4109C6F2045FC7EFF4CDE8F9905D19C28820D86304080FF886B299F0206E42B5\",\n" +
      "        \"ledger_index\": 54300940,\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  public void testJsonWithLedgerCurrentIndex() throws JsonProcessingException, JSONException {
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(false)
      .build();

    // Note: Empty lists are not serialized due to JsonInclude.Include.NON_EMPTY configuration
    String json = "{\n" +
      "        \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "        \"ledger_current_index\": 54300940,\n" +
      "        \"validated\": false\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}

