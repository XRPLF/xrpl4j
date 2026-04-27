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
import org.xrpl.xrpl4j.model.jackson.modules.IouAmountDeserializer;
import org.xrpl.xrpl4j.model.transactions.amount.IouAmount;

/**
 * Unit tests for {@link IouAmountDeserializer}.
 */
class IouAmountDeserializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void deserializesDecimal() throws JsonProcessingException {
    IouAmount amount = objectMapper.readValue("\"100.50\"", IouAmount.class);
    assertThat(amount.value()).isEqualTo("100.50");
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void deserializesWholeNumber() throws JsonProcessingException {
    IouAmount amount = objectMapper.readValue("\"15\"", IouAmount.class);
    assertThat(amount.value()).isEqualTo("15");
  }

  @Test
  void deserializesScientificNotation() throws JsonProcessingException {
    // Scientific notation is preserved verbatim
    IouAmount amount = objectMapper.readValue("\"1.23e10\"", IouAmount.class);
    assertThat(amount.value()).isEqualTo("1.23e10");
  }

  @Test
  void deserializesNegative() throws JsonProcessingException {
    IouAmount amount = objectMapper.readValue("\"-100.50\"", IouAmount.class);
    assertThat(amount.value()).isEqualTo("-100.50");
    assertThat(amount.isNegative()).isTrue();
  }

  @Test
  void deserializesZero() throws JsonProcessingException {
    IouAmount amount = objectMapper.readValue("\"0\"", IouAmount.class);
    assertThat(amount.value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void roundTrip() throws JsonProcessingException {
    IouAmount original = IouAmount.of("1234567890.123456");
    String json = objectMapper.writeValueAsString(original);
    IouAmount deserialized = objectMapper.readValue(json, IouAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void roundTripScientificNotation() throws JsonProcessingException {
    IouAmount original = IouAmount.of("9999999999999999e80");
    String json = objectMapper.writeValueAsString(original);
    IouAmount deserialized = objectMapper.readValue(json, IouAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }
}
