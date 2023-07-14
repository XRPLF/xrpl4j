package org.xrpl.xrpl4j.model.client.nft;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import org.xrpl.xrpl4j.model.flags.NfTokenOfferFlags;
import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class NfTokenOfferObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJsonWithXrpAmount() throws JsonProcessingException, JSONException {
    NfTokenOfferObject object = NfTokenOfferObject.builder()
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .owner(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .previousTransactionId(Hash256.of("E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14090896))
      .nfTokenId(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"))
      .flags(NfTokenOfferFlags.BUY_TOKEN)
      .build();

    String json = "{\n" +
      "    \"Flags\": 1,\n" +
      "    \"Amount\": \"10000\",\n" +
      "    \"Owner\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"NFTokenID\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"Destination\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"PreviousTxnID\": \"E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879\",\n" +
      "    \"PreviousTxnLgrSeq\": 14090896,\n" +
      "    \"LedgerEntryType\": \"NFTokenOffer\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testJsonWithIssuedCurrencyAmount() throws JsonProcessingException, JSONException {
    NfTokenOfferObject object = NfTokenOfferObject.builder()
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rsjYGpMWQeNBXbUTkVz4ZKzHefgZSr6rys"))
          .value("10")
          .build()
      )
      .destination(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .owner(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .previousTransactionId(Hash256.of("E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14090896))
      .nfTokenId(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"))
      .flags(NfTokenOfferFlags.BUY_TOKEN)
      .build();

    String json = "{\n" +
      "    \"Flags\": 1,\n" +
      "    \"Amount\": {" +
      "       \"currency\": \"USD\",\n" +
      "       \"issuer\": \"rsjYGpMWQeNBXbUTkVz4ZKzHefgZSr6rys\"," +
      "       \"value\": \"10\"" +
      "    },\n" +
      "    \"Owner\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"NFTokenID\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"Destination\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"PreviousTxnID\": \"E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879\",\n" +
      "    \"PreviousTxnLgrSeq\": 14090896,\n" +
      "    \"LedgerEntryType\": \"NFTokenOffer\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
