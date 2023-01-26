package org.xrpl.xrpl4j.model.transactions.json;

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
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountSetJsonTests extends AbstractJsonTest {

  @Test
  public void fullyPopulatedAccountSet() throws JSONException, JsonProcessingException {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .domain("6578616D706C652E636F6D")
      .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
      .messageKey("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB")
      .transferRate(UnsignedInteger.valueOf(1000000001))
      .tickSize(UnsignedInteger.valueOf(15))
      .clearFlag(AccountSetFlag.DEFAULT_RIPPLE)
      .emailHash("f9879d71855b5ff21e4963273a886bfc")
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"TransactionType\":\"AccountSet\",\n" +
      "    \"Account\":\"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\":\"12\",\n" +
      "    \"Sequence\":5,\n" +
      "    \"Flags\":2147483648,\n" +
      "    \"Domain\":\"6578616D706C652E636F6D\",\n" +
      "    \"SetFlag\":5,\n" +
      "    \"MessageKey\":\"03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB\",\n" +
      "    \"TransferRate\":1000000001,\n" +
      "    \"TickSize\":15,\n" +
      "    \"ClearFlag\":8,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"EmailHash\":\"f9879d71855b5ff21e4963273a886bfc\"\n" +
      "}";

    assertCanSerializeAndDeserialize(accountSet, json);
  }
}
