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
import org.xrpl.xrpl4j.model.jackson.modules.MptTokenAmountDeserializer;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;
import org.xrpl.xrpl4j.model.transactions.amount.MptTokenAmount;

/**
 * Unit tests for {@link MptTokenAmountDeserializer}.
 */
class MptTokenAmountDeserializerTest {

  private static final String ISSUANCE_ID = "00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void deserializesMptObject() throws JsonProcessingException {
    String json = "{\"value\":\"1000\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}";
    MptTokenAmount amount = objectMapper.readValue(json, MptTokenAmount.class);
    assertThat(amount.amount().value()).isEqualTo("1000");
    assertThat(amount.mptIssuanceId()).isEqualTo(MpTokenIssuanceId.of(ISSUANCE_ID));
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1000L));
  }

  @Test
  void deserializesZeroValue() throws JsonProcessingException {
    String json = "{\"value\":\"0\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}";
    MptTokenAmount amount = objectMapper.readValue(json, MptTokenAmount.class);
    assertThat(amount.amount().value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void deserializesNegativeValue() throws JsonProcessingException {
    // Negative values appear in transaction metadata
    String json = "{\"value\":\"-500\",\"mpt_issuance_id\":\"" + ISSUANCE_ID + "\"}";
    MptTokenAmount amount = objectMapper.readValue(json, MptTokenAmount.class);
    assertThat(amount.amount().value()).isEqualTo("-500");
    assertThat(amount.isNegative()).isTrue();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  @Test
  void roundTrip() throws JsonProcessingException {
    MptTokenAmount original = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(1_000_000L)))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    String json = objectMapper.writeValueAsString(original);
    MptTokenAmount deserialized = objectMapper.readValue(json, MptTokenAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void roundTripNegative() throws JsonProcessingException {
    MptTokenAmount original = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(500L), true))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    String json = objectMapper.writeValueAsString(original);
    MptTokenAmount deserialized = objectMapper.readValue(json, MptTokenAmount.class);
    assertThat(deserialized).isEqualTo(original);
  }
}
