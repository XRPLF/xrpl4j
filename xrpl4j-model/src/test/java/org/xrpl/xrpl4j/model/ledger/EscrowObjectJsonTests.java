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
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class EscrowObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    EscrowObject object = EscrowObject.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .cancelAfter(UnsignedLong.valueOf(545440232))
      .destination(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .finishAfter(UnsignedLong.valueOf(545354132))
      .ownerNode("0000000000000000")
      .destinationNode("0000000000000000")
      .previousTransactionId(Hash256.of("C44F2EB84196B9AD820313DBEBA6316A15C9A2D35787579ED172B87A30131DA7"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(28991004))
      .sourceTag(UnsignedInteger.valueOf(11747))
      .index(Hash256.of("DC5F3851D8A1AB622F957761E5963BC5BD439D5C24AC6AD7AC4523F0640244AC"))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Amount\": \"10000\",\n" +
      "    \"CancelAfter\": 545440232,\n" +
      "    \"Destination\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"DestinationTag\": 23480,\n" +
      "    \"FinishAfter\": 545354132,\n" +
      "    \"Flags\": 0,\n" +
      "    \"LedgerEntryType\": \"Escrow\",\n" +
      "    \"OwnerNode\": \"0000000000000000\",\n" +
      "    \"DestinationNode\": \"0000000000000000\",\n" +
      "    \"PreviousTxnID\": \"C44F2EB84196B9AD820313DBEBA6316A15C9A2D35787579ED172B87A30131DA7\",\n" +
      "    \"PreviousTxnLgrSeq\": 28991004,\n" +
      "    \"SourceTag\": 11747,\n" +
      "    \"index\": \"DC5F3851D8A1AB622F957761E5963BC5BD439D5C24AC6AD7AC4523F0640244AC\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
