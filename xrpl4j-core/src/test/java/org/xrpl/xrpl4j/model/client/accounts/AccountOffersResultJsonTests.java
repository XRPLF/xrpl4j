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
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

public class AccountOffersResultJsonTests extends AbstractJsonTest {

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {
    AccountOffersResult result = AccountOffersResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .marker(Marker.of("marker"))
      .addOffers(
        OfferResultObject.builder()
          .flags(OfferCreateFlags.of(65536L))
          .seq(UnsignedInteger.valueOf(13))
          .takerGets(
            IssuedCurrencyAmount.builder()
              .value("1")
              .currency("USD")
              .issuer(Address.of("rN6n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
              .build()
          )
          .takerPays(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2L)))
          .quality("1")
          .expiration(UnsignedInteger.valueOf(100))
          .build()
      )
      .addOffers(
        OfferResultObject.builder()
          .flags(OfferCreateFlags.of(65536L))
          .seq(UnsignedInteger.valueOf(13))
          .takerGets(
            IssuedCurrencyAmount.builder()
              .value("1")
              .currency("USD")
              .issuer(Address.of("rN6n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
              .build()
          )
          .takerPays(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2L)))
          .quality("1")
          .build()
      )
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_hash\": \"B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D\",\n" +
      "        \"ledger_index\": 37230600,\n" +
      "        \"status\": \"success\",\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"offers\": [\n" +
      "           {\n" +
      "               \"flags\": 65536,\n" +
      "               \"seq\": 13,\n" +
      "               \"taker_gets\": {\n" +
      "                   \"value\": \"1\",\n" +
      "                   \"currency\": \"USD\",\n" +
      "                   \"issuer\": \"rN6n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"\n" +
      "               },\n" +
      "               \"taker_pays\": \"2000000\",\n" +
      "               \"quality\": \"1\",\n" +
      "               \"expiration\": 100\n" +
      "           },\n" +
      "           {\n" +
      "               \"flags\": 65536,\n" +
      "               \"seq\": 13,\n" +
      "               \"taker_gets\": {\n" +
      "                   \"value\": \"1\",\n" +
      "                   \"currency\": \"USD\",\n" +
      "                   \"issuer\": \"rN6n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"\n" +
      "               },\n" +
      "               \"taker_pays\": \"2000000\",\n" +
      "               \"quality\": \"1\"\n" +
      "           }\n" +
      "        ]\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  public void testFullWithLedgerCurrentIndex() throws JsonProcessingException, JSONException {
    AccountOffersResult result = AccountOffersResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_current_index\": 37230600,\n" +
      "        \"status\": \"success\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
