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
import org.xrpl.xrpl4j.model.jackson.modules.XrpAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.amount.XrpAmount;

/**
 * Unit tests for {@link XrpAmountSerializer}.
 */
class XrpAmountSerializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void serializesPositiveDrops() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(XrpAmount.ofDrops(1_000_000L))).isEqualTo("\"1000000\"");
  }

  @Test
  void serializesZero() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(XrpAmount.ofDrops(0L))).isEqualTo("\"0\"");
  }

  @Test
  void serializesNegativeDrops() throws JsonProcessingException {
    // Negative values appear in transaction metadata
    assertThat(objectMapper.writeValueAsString(XrpAmount.ofDrops(-500L))).isEqualTo("\"-500\"");
  }

  @Test
  void serializesMaxDrops() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(XrpAmount.ofDrops(XrpAmount.MAX_XRP_IN_DROPS)))
      .isEqualTo("\"" + XrpAmount.MAX_XRP_IN_DROPS + "\"");
  }
}
