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
import org.xrpl.xrpl4j.model.jackson.modules.XrpTokenAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.amount.XrpTokenAmount;

/**
 * Unit tests for {@link XrpTokenAmountSerializer}.
 */
class XrpTokenAmountSerializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void serializesDropsAsString() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(XrpTokenAmount.ofDrops(1_000_000L))).isEqualTo("\"1000000\"");
  }

  @Test
  void serializesZeroDrops() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(XrpTokenAmount.ofDrops(0L))).isEqualTo("\"0\"");
  }

  @Test
  void serializesNegativeDrops() throws JsonProcessingException {
    // Negative values appear in transaction metadata
    assertThat(objectMapper.writeValueAsString(XrpTokenAmount.ofDrops(-500L))).isEqualTo("\"-500\"");
  }

  @Test
  void serializesFromUnsignedLong() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(XrpTokenAmount.ofDrops(UnsignedLong.valueOf(1_000_000L))))
      .isEqualTo("\"1000000\"");
  }
}
