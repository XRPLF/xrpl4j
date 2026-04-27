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
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.jackson.modules.MptAmountDeserializer;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;

/**
 * Unit tests for {@link MptAmountDeserializer}.
 */
class MptAmountDeserializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void deserializesPositive() throws JsonProcessingException {
    MptAmount amount = objectMapper.readValue("\"1000\"", MptAmount.class);
    assertThat(amount.value()).isEqualTo("1000");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1000L));
  }

  @Test
  void deserializesZero() throws JsonProcessingException {
    MptAmount amount = objectMapper.readValue("\"0\"", MptAmount.class);
    assertThat(amount.value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.ZERO);
  }

  @Test
  void deserializesNegative() throws JsonProcessingException {
    // Negative values appear in transaction metadata
    MptAmount amount = objectMapper.readValue("\"-500\"", MptAmount.class);
    assertThat(amount.value()).isEqualTo("-500");
    assertThat(amount.isNegative()).isTrue();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  @Test
  void roundTrip() throws JsonProcessingException {
    MptAmount original = MptAmount.of(UnsignedLong.valueOf(1_000_000L));
    String json = objectMapper.writeValueAsString(original);
    MptAmount deserialized = objectMapper.readValue(json, MptAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void roundTripNegative() throws JsonProcessingException {
    MptAmount original = MptAmount.of(UnsignedLong.valueOf(500L), true);
    String json = objectMapper.writeValueAsString(original);
    MptAmount deserialized = objectMapper.readValue(json, MptAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }
}
