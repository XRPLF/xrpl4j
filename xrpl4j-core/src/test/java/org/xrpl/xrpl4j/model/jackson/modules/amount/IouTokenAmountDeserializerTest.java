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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.jackson.modules.IouTokenAmountDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.amount.IouAmount;
import org.xrpl.xrpl4j.model.transactions.amount.IouTokenAmount;

/**
 * Unit tests for {@link IouTokenAmountDeserializer}.
 */
class IouTokenAmountDeserializerTest {

  private static final String ISSUER = "rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void deserializesIouObject() throws JsonProcessingException {
    String json = "{\"value\":\"100.50\",\"currency\":\"USD\",\"issuer\":\"" + ISSUER + "\"}";
    IouTokenAmount iouTokenAmount = objectMapper.readValue(json, IouTokenAmount.class);
    assertThat(iouTokenAmount.amount().value()).isEqualTo("100.50");
    assertThat(iouTokenAmount.currency()).isEqualTo("USD");
    assertThat(iouTokenAmount.issuer()).isEqualTo(Address.of(ISSUER));
    assertThat(iouTokenAmount.amount().isNegative()).isFalse();
  }

  @Test
  void deserializesNegativeValue() throws JsonProcessingException {
    // Negative IOU values appear in transaction metadata
    String json = "{\"value\":\"-50.5\",\"currency\":\"EUR\",\"issuer\":\"" + ISSUER + "\"}";
    IouTokenAmount iouTokenAmount = objectMapper.readValue(json, IouTokenAmount.class);
    assertThat(iouTokenAmount.amount().value()).isEqualTo("-50.5");
    assertThat(iouTokenAmount.amount().isNegative()).isTrue();
  }

  @Test
  void deserializesScientificNotation() throws JsonProcessingException {
    String json = "{\"value\":\"1.23e10\",\"currency\":\"USD\",\"issuer\":\"" + ISSUER + "\"}";
    IouTokenAmount amount = objectMapper.readValue(json, IouTokenAmount.class);
    assertThat(amount.amount().value()).isEqualTo("1.23e10");
  }

  @Test
  void deserializesHexCurrencyCode() throws JsonProcessingException {
    // Non-standard 40-character hex currency codes must be preserved verbatim
    String hexCurrency = "7872706C346A436F696E00000000000000000000";
    String json = "{\"value\":\"15\",\"currency\":\"" + hexCurrency + "\",\"issuer\":\"" + ISSUER + "\"}";
    IouTokenAmount amount = objectMapper.readValue(json, IouTokenAmount.class);
    assertThat(amount.currency()).isEqualTo(hexCurrency);
  }

  @Test
  void roundTrip() throws JsonProcessingException {
    IouTokenAmount original = IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    String json = objectMapper.writeValueAsString(original);
    IouTokenAmount deserialized = objectMapper.readValue(json, IouTokenAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void roundTripNegative() throws JsonProcessingException {
    IouTokenAmount original = IouTokenAmount.builder()
      .amount(IouAmount.of("-50.5"))
      .currency("EUR")
      .issuer(Address.of(ISSUER))
      .build();
    String json = objectMapper.writeValueAsString(original);
    IouTokenAmount deserialized = objectMapper.readValue(json, IouTokenAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }
}
