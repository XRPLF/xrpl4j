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

import static org.assertj.core.api.Assertions.assertThat;

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
      .flags(NfTokenOfferFlags.SELL_NFTOKEN)
      .index(Hash256.of("AEBABA4FAC212BF28E0F9A9C3788A47B085557EC5D1429E7A8266FB859C863B3"))
      .build();

    String json = "{\n" +
      "    \"Flags\": 1,\n" +
      "    \"Amount\": \"10000\",\n" +
      "    \"Owner\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"NFTokenID\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"Destination\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"PreviousTxnID\": \"E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879\",\n" +
      "    \"PreviousTxnLgrSeq\": 14090896,\n" +
      "    \"index\": \"AEBABA4FAC212BF28E0F9A9C3788A47B085557EC5D1429E7A8266FB859C863B3\",\n" +
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
      .flags(NfTokenOfferFlags.SELL_NFTOKEN)
      .index(Hash256.of("AEBABA4FAC212BF28E0F9A9C3788A47B085557EC5D1429E7A8266FB859C863B3"))
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
      "    \"index\": \"AEBABA4FAC212BF28E0F9A9C3788A47B085557EC5D1429E7A8266FB859C863B3\",\n" +
      "    \"LedgerEntryType\": \"NFTokenOffer\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void flagsReportCorrectSellBuyDirection() throws JsonProcessingException {
    String sellOfferJson = "{\n" +
      "    \"Flags\": 1,\n" +
      "    \"Amount\": \"80000000\",\n" +
      "    \"Owner\": \"rfGEPwpQ6DQpPEcecLLBNHaX5KWTtecGBe\",\n" +
      "    \"NFTokenID\": \"00181F40ADEB1F9F4AE8BC62F84A2DD733640CB5F6777F5F8990D16F061CB3D4\",\n" +
      "    \"PreviousTxnID\": \"52EC48014B45BCA6AE51357BA4A93E59810316BC1453BC33B08003DA7BC8F642\",\n" +
      "    \"PreviousTxnLgrSeq\": 103898046,\n" +
      "    \"index\": \"00003FF5234D24916ADA6D01E145F3B33B7916A861D408D84B09C63AC641A454\",\n" +
      "    \"LedgerEntryType\": \"NFTokenOffer\"\n" +
      "}";

    String buyOfferJson = "{\n" +
      "    \"Flags\": 0,\n" +
      "    \"Amount\": \"437290000\",\n" +
      "    \"Owner\": \"rBT4Ma3zFFY1rLSqtPz6TJSvnTk9jZEkaZ\",\n" +
      "    \"NFTokenID\": \"00081B58E2E050A72D3C0E44193986521A6880F43BDA1AA080824C5F00000DB1\",\n" +
      "    \"PreviousTxnID\": \"575E221B8F8178DB7BD8EC86F4AE2D52CFD09F9286F099133FECEE2F1F44055D\",\n" +
      "    \"PreviousTxnLgrSeq\": 99704767,\n" +
      "    \"index\": \"000016C85DDAE57543C9852EA1B95FFEA615FEE19679AB47F62BDE9264DF29D6\",\n" +
      "    \"LedgerEntryType\": \"NFTokenOffer\"\n" +
      "}";

    NfTokenOfferObject sellOffer = objectMapper.readValue(sellOfferJson, NfTokenOfferObject.class);
    NfTokenOfferObject buyOffer = objectMapper.readValue(buyOfferJson, NfTokenOfferObject.class);

    assertThat(sellOffer.flags().lsfSellNfToken())
      .as("Flags value 1 (lsfSellNFToken) must be reported as a sell offer")
      .isTrue();
    assertThat(buyOffer.flags().lsfSellNfToken())
      .as("Flags value 0 (no lsfSellNFToken bit) must be reported as a buy offer")
      .isFalse();
  }
}
