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
import org.xrpl.xrpl4j.model.flags.RippleStateFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

public class RippleStateObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    RippleStateObject object = RippleStateObject.builder()
      .balance(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rrrrrrrrrrrrrrrrrrrrBZbvji"))
        .value("-10")
        .build())
      .flags(RippleStateFlags.of(393216))
      .highLimit(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .value("110")
        .build())
      .highNode("0000000000000000")
      .lowLimit(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
        .value("0")
        .build())
      .lowNode("0000000000000000")
      .previousTransactionId(Hash256.of("E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14090896))
      .index(Hash256.of("9CA88CDEDFF9252B3DE183CE35B038F57282BC9503CDFA1923EF9A95DF0D6F7B"))
      .build();

    String json = "{\n" +
      "    \"Balance\": {\n" +
      "        \"currency\": \"USD\",\n" +
      "        \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
      "        \"value\": \"-10\"\n" +
      "    },\n" +
      "    \"Flags\": 393216,\n" +
      "    \"HighLimit\": {\n" +
      "        \"currency\": \"USD\",\n" +
      "        \"issuer\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "        \"value\": \"110\"\n" +
      "    },\n" +
      "    \"HighNode\": \"0000000000000000\",\n" +
      "    \"LedgerEntryType\": \"RippleState\",\n" +
      "    \"LowLimit\": {\n" +
      "        \"currency\": \"USD\",\n" +
      "        \"issuer\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "        \"value\": \"0\"\n" +
      "    },\n" +
      "    \"LowNode\": \"0000000000000000\",\n" +
      "    \"PreviousTxnID\": \"E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879\",\n" +
      "    \"PreviousTxnLgrSeq\": 14090896,\n" +
      "    \"index\": \"9CA88CDEDFF9252B3DE183CE35B038F57282BC9503CDFA1923EF9A95DF0D6F7B\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
