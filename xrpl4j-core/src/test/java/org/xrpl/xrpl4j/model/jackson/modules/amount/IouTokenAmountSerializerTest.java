package org.xrpl.xrpl4j.model.jackson.modules.amount;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.jackson.modules.IouTokenAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.amount.IouAmount;
import org.xrpl.xrpl4j.model.transactions.amount.IouTokenAmount;

/**
 * Unit tests for {@link IouTokenAmountSerializer}.
 */
class IouTokenAmountSerializerTest {

  private static final String ISSUER = "rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void serializesIouObject() throws JsonProcessingException, JSONException {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"100.50\",\"currency\":\"USD\",\"issuer\":\"" + ISSUER + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }

  @Test
  void serializesScientificNotation() throws JsonProcessingException, JSONException {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("1.23e10"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"1.23e10\",\"currency\":\"USD\",\"issuer\":\"" + ISSUER + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }

  @Test
  void serializesNegativeValue() throws JsonProcessingException, JSONException {
    // Negative IOU values appear in transaction metadata
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("-50.5"))
      .currency("EUR")
      .issuer(Address.of(ISSUER))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"-50.5\",\"currency\":\"EUR\",\"issuer\":\"" + ISSUER + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }

  @Test
  void serializesHexCurrencyCode() throws JsonProcessingException, JSONException {
    // Non-standard 40-character hex currency codes must be preserved verbatim
    String hexCurrency = "7872706C346A436F696E00000000000000000000";
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("15"))
      .currency(hexCurrency)
      .issuer(Address.of(ISSUER))
      .build();
    JSONAssert.assertEquals(
      "{\"value\":\"15\",\"currency\":\"" + hexCurrency + "\",\"issuer\":\"" + ISSUER + "\"}",
      objectMapper.writeValueAsString(amount),
      JSONCompareMode.STRICT
    );
  }
}
