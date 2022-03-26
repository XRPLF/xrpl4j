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
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountRootObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountRootObject object = AccountRootObject.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .accountTransactionId(Hash256.of("0D5FB50FA65C9FE1538FD7E398FFFE9D1908DFA4576D8D7A020040686F93C77D"))
      .balance(XrpCurrencyAmount.ofDrops(148446663))
      .domain("6D64756F31332E636F6D")
      .emailHash("98B4375E1D753E5B91627516F6D70977")
      .flags(Flags.AccountRootFlags.of(8388608))
      .messageKey("0000000000000000000000070000000300")
      .ownerCount(UnsignedInteger.valueOf(3))
      .previousTransactionId(Hash256.of("0D5FB50FA65C9FE1538FD7E398FFFE9D1908DFA4576D8D7A020040686F93C77D"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14091160))
      .sequence(UnsignedInteger.valueOf(336))
      .transferRate(UnsignedInteger.valueOf(1004999999))
      .index(Hash256.of("13F1A95D7AAB7108D5CE7EEAF504B2894B8C674E6D68499076441C4837282BF8"))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"AccountTxnID\": \"0D5FB50FA65C9FE1538FD7E398FFFE9D1908DFA4576D8D7A020040686F93C77D\",\n" +
      "    \"Balance\": \"148446663\",\n" +
      "    \"Domain\": \"6D64756F31332E636F6D\",\n" +
      "    \"EmailHash\": \"98B4375E1D753E5B91627516F6D70977\",\n" +
      "    \"Flags\": 8388608,\n" +
      "    \"LedgerEntryType\": \"AccountRoot\",\n" +
      "    \"MessageKey\": \"0000000000000000000000070000000300\",\n" +
      "    \"OwnerCount\": 3,\n" +
      "    \"PreviousTxnID\": \"0D5FB50FA65C9FE1538FD7E398FFFE9D1908DFA4576D8D7A020040686F93C77D\",\n" +
      "    \"PreviousTxnLgrSeq\": 14091160,\n" +
      "    \"Sequence\": 336,\n" +
      "    \"TransferRate\": 1004999999,\n" +
      "    \"index\": \"13F1A95D7AAB7108D5CE7EEAF504B2894B8C674E6D68499076441C4837282BF8\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
