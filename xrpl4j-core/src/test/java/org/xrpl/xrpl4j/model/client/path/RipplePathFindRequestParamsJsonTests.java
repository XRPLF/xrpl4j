package org.xrpl.xrpl4j.model.client.path;

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
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

public class RipplePathFindRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    RipplePathFindRequestParams params = RipplePathFindRequestParams.builder()
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
        .value("0.001")
        .build())
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addSourceCurrencies(
        PathCurrency.of("XRP"),
        PathCurrency.of("USD")
      )
      .domain(Hash256.of("96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();

    String json = "{" +
      "  \"ledger_index\": \"current\"," +
      "  \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "  \"destination_amount\": {" +
      "    \"currency\": \"USD\"," +
      "    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"," +
      "    \"value\": \"0.001\"" +
      "  }," +
      "  \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "  \"domain\": \"96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797\"," +
      "  \"source_currencies\": [" +
      "    {" +
      "      \"currency\": \"XRP\"" +
      "    }," +
      "    {" +
      "      \"currency\": \"USD\"" +
      "    }" +
      "  ]" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }
}
