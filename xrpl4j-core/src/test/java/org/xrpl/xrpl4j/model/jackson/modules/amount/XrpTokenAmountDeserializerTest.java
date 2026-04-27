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
import org.xrpl.xrpl4j.model.jackson.modules.XrpTokenAmountDeserializer;
import org.xrpl.xrpl4j.model.transactions.amount.XrpAmount;
import org.xrpl.xrpl4j.model.transactions.amount.XrpTokenAmount;

/**
 * Unit tests for {@link XrpTokenAmountDeserializer}.
 */
class XrpTokenAmountDeserializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void deserializesPositiveDrops() throws JsonProcessingException {
    XrpTokenAmount xrpTokenAmount = objectMapper.readValue("\"1000000\"", XrpTokenAmount.class);
    assertThat(xrpTokenAmount.amount().value()).isEqualTo("1000000");
    assertThat(xrpTokenAmount.amount().isNegative()).isFalse();
    assertThat(xrpTokenAmount.amount().unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
  }

  @Test
  void deserializesZeroDrops() throws JsonProcessingException {
    XrpTokenAmount xrpTokenAmount = objectMapper.readValue("\"0\"", XrpTokenAmount.class);
    assertThat(xrpTokenAmount.amount().value()).isEqualTo("0");
    assertThat(xrpTokenAmount.amount().isNegative()).isFalse();
  }

  @Test
  void deserializesNegativeDrops() throws JsonProcessingException {
    // Negative drop counts appear in transaction metadata
    XrpTokenAmount xrpTokenAmount = objectMapper.readValue("\"-500\"", XrpTokenAmount.class);
    assertThat(xrpTokenAmount.amount().value()).isEqualTo("-500");
    assertThat(xrpTokenAmount.amount().isNegative()).isTrue();
    assertThat(xrpTokenAmount.amount().unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  @Test
  void roundTrip() throws JsonProcessingException {
    XrpTokenAmount original = XrpTokenAmount.ofDrops(1_000_000L);
    String json = objectMapper.writeValueAsString(original);
    XrpTokenAmount deserialized = objectMapper.readValue(json, XrpTokenAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void roundTripNegative() throws JsonProcessingException {
    XrpTokenAmount original = XrpTokenAmount.ofDrops(-500L);
    String json = objectMapper.writeValueAsString(original);
    XrpTokenAmount deserialized = objectMapper.readValue(json, XrpTokenAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void ofXrpAmountFactory() throws JsonProcessingException {
    // XrpTokenAmount.of(XrpAmount) is the wrap factory used by XrpTokenAmountDeserializer
    XrpAmount xrpAmount = XrpAmount.ofDrops(1_000_000L);
    XrpTokenAmount wrapped = XrpTokenAmount.of(xrpAmount);
    assertThat(wrapped.amount()).isEqualTo(xrpAmount);
    // Verify it serializes correctly
    assertThat(objectMapper.writeValueAsString(wrapped)).isEqualTo("\"1000000\"");
  }
}
