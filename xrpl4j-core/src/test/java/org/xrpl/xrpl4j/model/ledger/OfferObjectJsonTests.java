package org.xrpl.xrpl4j.model.ledger;

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
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OfferObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    OfferObject object = OfferObject.builder()
      .account(Address.of("rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt"))
      .bookDirectory(Hash256.of("ACC27DE91DBA86FC509069EAF4BC511D73128B780F2E54BF5E07A369E2446000"))
      .bookNode("0000000000000000")
      .flags(OfferFlags.of(131072))
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14524914))
      .sequence(UnsignedInteger.valueOf(866))
      .takerGets(IssuedCurrencyAmount.builder()
        .currency("XAG")
        .issuer(Address.of("r9Dr5xwkeLegBeXq6ujinjSBLQzQ1zQGjH"))
        .value("37")
        .build())
      .takerPays(XrpCurrencyAmount.ofDrops(79550000000L))
      .index(Hash256.of("96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797"))
      .build();

    String json =
      "{\n" +
      "    \"Account\": \"rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt\",\n" +
      "    \"BookDirectory\": \"ACC27DE91DBA86FC509069EAF4BC511D73128B780F2E54BF5E07A369E2446000\",\n" +
      "    \"BookNode\": \"0000000000000000\",\n" +
      "    \"Flags\": 131072,\n" +
      "    \"LedgerEntryType\": \"Offer\",\n" +
      "    \"OwnerNode\": \"0000000000000000\",\n" +
      "    \"PreviousTxnID\": \"F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF\",\n" +
      "    \"PreviousTxnLgrSeq\": 14524914,\n" +
      "    \"Sequence\": 866,\n" +
      "    \"TakerGets\": {\n" +
      "        \"currency\": \"XAG\",\n" +
      "        \"issuer\": \"r9Dr5xwkeLegBeXq6ujinjSBLQzQ1zQGjH\",\n" +
      "        \"value\": \"37\"\n" +
      "    },\n" +
      "    \"TakerPays\": \"79550000000\",\n" +
      "    \"index\": \"96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {
    List<AdditionalBook> additionalBookList = IntStream.range(0, 1)
      .mapToObj(i -> AdditionalBook.builder()
        .bookDirectory(Hash256.of("ACC27DE91DBA86FC509069EAF4BC511D73128B780F2E54BF5E07A369E244600" + i))
        .bookNode(String.valueOf(i))
        .build())
      .collect(Collectors.toList());

    OfferObject object = OfferObject.builder()
      .account(Address.of("rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt"))
      .bookDirectory(Hash256.of("ACC27DE91DBA86FC509069EAF4BC511D73128B780F2E54BF5E07A369E2446000"))
      .bookNode("0000000000000000")
      .flags(OfferFlags.of(262144))
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14524914))
      .sequence(UnsignedInteger.valueOf(866))
      .takerGets(IssuedCurrencyAmount.builder()
        .currency("XAG")
        .issuer(Address.of("r9Dr5xwkeLegBeXq6ujinjSBLQzQ1zQGjH"))
        .value("37")
        .build())
      .takerPays(XrpCurrencyAmount.ofDrops(79550000000L))
      .index(Hash256.of("96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797"))
      .domainId(Hash256.of("A6F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1798"))
      .expiration(UnsignedInteger.valueOf(1749214498))
      .additionalBooks(additionalBookList)
      .build();

    String json =
      "{\n" +
      "    \"Account\": \"rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt\",\n" +
      "    \"BookDirectory\": \"ACC27DE91DBA86FC509069EAF4BC511D73128B780F2E54BF5E07A369E2446000\",\n" +
      "    \"BookNode\": \"0000000000000000\",\n" +
      "    \"Flags\": 262144,\n" +
      "    \"LedgerEntryType\": \"Offer\",\n" +
      "    \"OwnerNode\": \"0000000000000000\",\n" +
      "    \"PreviousTxnID\": \"F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF\",\n" +
      "    \"PreviousTxnLgrSeq\": 14524914,\n" +
      "    \"Sequence\": 866,\n" +
      "    \"TakerGets\": {\n" +
      "        \"currency\": \"XAG\",\n" +
      "        \"issuer\": \"r9Dr5xwkeLegBeXq6ujinjSBLQzQ1zQGjH\",\n" +
      "        \"value\": \"37\"\n" +
      "    },\n" +
      "    \"TakerPays\": \"79550000000\",\n" +
      "    \"index\": \"96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797\",\n" +
      "    \"DomainID\": \"A6F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1798\",\n" +
      "    \"Expiration\": 1749214498,\n" +
      "    \"AdditionalBooks\": " + objectMapper.writeValueAsString(additionalBookList) + "\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
