package org.xrpl.xrpl4j.model.client.transactions;

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
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class TransactionRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {

    TransactionRequestParams params = TransactionRequestParams.builder()
      .transaction(Hash256.of("C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9"))
      .build();

    String json = "{\n" +
      "            \"transaction\": \"C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9\",\n" +
      "            \"binary\": false\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {

    TransactionRequestParams params = TransactionRequestParams.builder()
      .transaction(Hash256.of("C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9"))
      .minLedger(UnsignedLong.ZERO)
      .maxLedger(UnsignedLong.ONE)
      .build();

    String json = "{\n" +
      "            \"transaction\": \"C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9\",\n" +
      "            \"min_ledger\": 0,\n" +
      "            \"max_ledger\": 1,\n" +
      "            \"binary\": false\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
