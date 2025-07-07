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
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.PathStep;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class RipplePathFindResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    RipplePathFindResult result = RipplePathFindResult.builder()
      .status("success")
      .addAlternatives(
        PathAlternative.builder()
          .addPathsComputed(
            Lists.newArrayList(
              PathStep.builder()
                .currency("USD")
                .issuer(Address.of("rpDMez6pm6dBve2TJsmDpv7Yae6V5Pyvy2"))
                .build(),
              PathStep.builder()
                .account(Address.of("rpDMez6pm6dBve2TJsmDpv7Yae6V5Pyvy2"))
                .build(),
              PathStep.builder()
                .account(Address.of("rfDeu7TPUmyvUrffexjMjq3mMcSQHZSYyA"))
                .build(),
              PathStep.builder()
                .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
                .build()
            ),

            Lists.newArrayList(
              PathStep.builder()
                .currency("USD")
                .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
                .build(),
              PathStep.builder()
                .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
                .build()
            )
          )
          .sourceAmount(XrpCurrencyAmount.ofDrops(207414))
          .build()
      )
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addDestinationCurrencies(
        "USD",
        "JOE",
        "BTC",
        "DYM",
        "CNY",
        "EUR",
        "015841551A748AD2C1F76FF6ECB0CCCD00000000",
        "MXN",
        "XRP"
      )
      .build();

    String json = "{" +
      "  \"alternatives\": [" +
      "    {" +
      "      \"paths_computed\": [" +
      "        [" +
      "          {" +
      "            \"currency\": \"USD\"," +
      "            \"issuer\": \"rpDMez6pm6dBve2TJsmDpv7Yae6V5Pyvy2\"" +
      "          }," +
      "          {" +
      "            \"account\": \"rpDMez6pm6dBve2TJsmDpv7Yae6V5Pyvy2\"" +
      "          }," +
      "          {" +
      "            \"account\": \"rfDeu7TPUmyvUrffexjMjq3mMcSQHZSYyA\"" +
      "          }," +
      "          {" +
      "            \"account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"" +
      "          }" +
      "        ]," +
      "        [" +
      "          {" +
      "            \"currency\": \"USD\"," +
      "            \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"" +
      "          }," +
      "          {" +
      "            \"account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"" +
      "          }" +
      "        ]" +
      "      ]," +
      "      \"source_amount\": \"207414\"" +
      "    }" +
      "  ]," +
      "  \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "  \"destination_currencies\": [" +
      "    \"USD\"," +
      "    \"JOE\"," +
      "    \"BTC\"," +
      "    \"DYM\"," +
      "    \"CNY\"," +
      "    \"EUR\"," +
      "    \"015841551A748AD2C1F76FF6ECB0CCCD00000000\"," +
      "    \"MXN\"," +
      "    \"XRP\"" +
      "  ]," +
      "  \"status\": \"success\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }
}
