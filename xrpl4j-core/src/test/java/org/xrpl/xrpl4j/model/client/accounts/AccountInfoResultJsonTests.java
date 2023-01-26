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
import org.xrpl.xrpl4j.model.flags.AccountRootFlags;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountInfoResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountInfoResult result = AccountInfoResult.builder()
      .accountData(AccountRootObject.builder()
        .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
        .balance(XrpCurrencyAmount.ofDrops(999999999960L))
        .flags(AccountRootFlags.of(8388608))
        .ownerCount(UnsignedInteger.ZERO)
        .previousTransactionId(Hash256.of("4294BEBE5B569A18C0A2702387C9B1E7146DC3A5850C1E87204951C6FDAA4C42"))
        .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3))
        .sequence(UnsignedInteger.valueOf(6))
        .index(Hash256.of("92FA6A9FC8EA6018D5D16532D7795C91BFB0831355BDFDA177E86C8BF997985F"))
        .build())
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(4)))
      .queueData(QueueData.builder()
        .authChangeQueued(true)
        .highestSequence(UnsignedInteger.valueOf(10))
        .lowestSequence(UnsignedInteger.valueOf(6))
        .maxSpendDropsTotal(XrpCurrencyAmount.ofDrops(500))
        .addTransactions(
          QueueTransaction.builder()
            .authChange(false)
            .fee(XrpCurrencyAmount.ofDrops(100))
            .feeLevel(XrpCurrencyAmount.ofDrops(2560))
            .maxSpendDrops(XrpCurrencyAmount.ofDrops(100))
            .sequence(UnsignedInteger.valueOf(6))
            .build(),
          QueueTransaction.builder()
            .authChange(true)
            .fee(XrpCurrencyAmount.ofDrops(100))
            .feeLevel(XrpCurrencyAmount.ofDrops(2560))
            .maxSpendDrops(XrpCurrencyAmount.ofDrops(100))
            .sequence(UnsignedInteger.valueOf(10))
            .build()
        )
        .transactionCount(UnsignedInteger.valueOf(5))
        .build())
      .status("success")
      .validated(false)
      .build();

    String json = "{\n" +
      "        \"account_data\": {\n" +
      "            \"Account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
      "            \"Balance\": \"999999999960\",\n" +
      "            \"Flags\": 8388608,\n" +
      "            \"LedgerEntryType\": \"AccountRoot\",\n" +
      "            \"OwnerCount\": 0,\n" +
      "            \"PreviousTxnID\": \"4294BEBE5B569A18C0A2702387C9B1E7146DC3A5850C1E87204951C6FDAA4C42\",\n" +
      "            \"PreviousTxnLgrSeq\": 3,\n" +
      "            \"Sequence\": 6,\n" +
      "            \"index\": \"92FA6A9FC8EA6018D5D16532D7795C91BFB0831355BDFDA177E86C8BF997985F\"\n" +
      "        },\n" +
      "        \"ledger_current_index\": 4,\n" +
      "        \"queue_data\": {\n" +
      "            \"auth_change_queued\": true,\n" +
      "            \"highest_sequence\": 10,\n" +
      "            \"lowest_sequence\": 6,\n" +
      "            \"max_spend_drops_total\": \"500\",\n" +
      "            \"transactions\": [\n" +
      "                {\n" +
      "                    \"auth_change\": false,\n" +
      "                    \"fee\": \"100\",\n" +
      "                    \"fee_level\": \"2560\",\n" +
      "                    \"max_spend_drops\": \"100\",\n" +
      "                    \"seq\": 6\n" +
      "                },\n" +
      "                {\n" +
      "                    \"auth_change\": true,\n" +
      "                    \"fee\": \"100\",\n" +
      "                    \"fee_level\": \"2560\",\n" +
      "                    \"max_spend_drops\": \"100\",\n" +
      "                    \"seq\": 10\n" +
      "                }\n" +
      "            ],\n" +
      "            \"txn_count\": 5\n" +
      "        },\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": false\n" +
      "    }";
    assertCanSerializeAndDeserialize(result, json);
  }
}
