package org.xrpl.xrpl4j.model.client.accounts;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.NfTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

public class AccountNftsResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountNftsResult result = AccountNftsResult.builder()
      .account(Address.of("rDbRjvZQ882xGDcP18geuNhJSfR6nX3u1x"))
      .status("success")
      .validated(true)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(80784024)))
      .ledgerHash(Hash256.of("DDE2A3016AA50BD846EB5083FE191CE5568396C9823BCC6856D5D083E372CB2C"))
      .limit(UnsignedInteger.valueOf(100))
      .addAccountNfts(
        NfTokenObject.builder()
          .nfTokenId(NfTokenId.of("00082710A25A9B38CE017A6FAD435ABA4B9894088C25EC69D29F8A3A00000079"))
          .uri(NfTokenUri.of("697066733A2F2F6261666B72656962706F6B6F777470756775697766626465673572707364" +
            "71677A6471346F36626761377669766D32736C75716166747678366965"))
          .flags(NfTokenFlags.TRANSFERABLE)
          .issuer(Address.of("rEoTrW1kYccRxZSTRrYfGSzALXGhf6YcF3"))
          .taxon(UnsignedInteger.valueOf(22830))
          .nftSerial(UnsignedInteger.valueOf(121))
          .build(),
        NfTokenObject.builder()
          .nfTokenId(NfTokenId.of("00082710A25A9B38CE017A6FAD435ABA4B9894088C25EC69EDA5F0240000006F"))
          .uri(NfTokenUri.of("697066733A2F2F6261666B7265696876766437726F67686F6E6E626A796272776B32627963" +
            "6E3637617A69686D367937656C3737336C6762657A3578776174796361"))
          .flags(NfTokenFlags.TRANSFERABLE)
          .issuer(Address.of("rEoTrW1kYccRxZSTRrYfGSzALXGhf6YcF3"))
          .taxon(UnsignedInteger.valueOf(22830))
          .nftSerial(UnsignedInteger.valueOf(111))
          .transferFee(TransferFee.of(UnsignedInteger.valueOf(10000)))
          .build()
      )
      .build();

    String json = "{\n" +
      "        \"ledger_hash\": \"DDE2A3016AA50BD846EB5083FE191CE5568396C9823BCC6856D5D083E372CB2C\",\n" +
      "        \"ledger_index\": 80784024,\n" +
      "        \"validated\": true,\n" +
      "        \"account\": \"rDbRjvZQ882xGDcP18geuNhJSfR6nX3u1x\",\n" +
      "        \"account_nfts\": [\n" +
      "            {\n" +
      "                \"NFTokenID\": \"00082710A25A9B38CE017A6FAD435ABA4B9894088C25EC69D29F8A3A00000079\",\n" +
      "                \"URI\": \"697066733A2F2F6261666B72656962706F6B6F77747075677569776662646567357270736471677A6471346F36626761377669766D32736C75716166747678366965\",\n" +
      "                \"Flags\": 8,\n" +
      "                \"Issuer\": \"rEoTrW1kYccRxZSTRrYfGSzALXGhf6YcF3\",\n" +
      "                \"NFTokenTaxon\": 22830,\n" +
      "                \"nft_serial\": 121\n" +
      "            },\n" +
      "            {\n" +
      "                \"NFTokenID\": \"00082710A25A9B38CE017A6FAD435ABA4B9894088C25EC69EDA5F0240000006F\",\n" +
      "                \"URI\": \"697066733A2F2F6261666B7265696876766437726F67686F6E6E626A796272776B326279636E3637617A69686D367937656C3737336C6762657A3578776174796361\",\n" +
      "                \"Flags\": 8,\n" +
      "                \"Issuer\": \"rEoTrW1kYccRxZSTRrYfGSzALXGhf6YcF3\",\n" +
      "                \"NFTokenTaxon\": 22830,\n" +
      "                \"nft_serial\": 111,\n" +
      // FIXME: TransferFee serializes to String, but rippled returns an int
      "                \"TransferFee\": \"10000\"\n" +
      "            }\n" +
      "        ],\n" +
      "        \"limit\": 100,\n" +
      "        \"status\": \"success\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  void testLedgerHashLedgerIndexSafe() {
    AccountNftsResult result = AccountNftsResult.builder()
      .account(Address.of("rDbRjvZQ882xGDcP18geuNhJSfR6nX3u1x"))
      .status("success")
      .validated(true)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(80784024)))
      .ledgerHash(Hash256.of("DDE2A3016AA50BD846EB5083FE191CE5568396C9823BCC6856D5D083E372CB2C"))
      .limit(UnsignedInteger.valueOf(100))
      .build();
    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
    assertThatThrownBy(result::ledgerCurrentIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerCurrentIndex.");
  }

  @Test
  void testLedgerIndexCurrentSafe() {
    AccountNftsResult result = AccountNftsResult.builder()
      .account(Address.of("rDbRjvZQ882xGDcP18geuNhJSfR6nX3u1x"))
      .status("success")
      .validated(false)
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(80784024)))
      .limit(UnsignedInteger.valueOf(100))
      .build();
    assertThatThrownBy(result::ledgerHashSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerHash.");
    assertThatThrownBy(result::ledgerIndexSafe)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Result did not contain a ledgerIndex.");
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
  }
}
