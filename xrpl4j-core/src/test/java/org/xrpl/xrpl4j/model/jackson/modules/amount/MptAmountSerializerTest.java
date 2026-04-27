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
import org.xrpl.xrpl4j.model.jackson.modules.MptAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;

/**
 * Unit tests for {@link MptAmountSerializer}.
 */
class MptAmountSerializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void serializesPositive() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(MptAmount.of(UnsignedLong.valueOf(1000L)))).isEqualTo("\"1000\"");
  }

  @Test
  void serializesZero() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(MptAmount.of(UnsignedLong.ZERO))).isEqualTo("\"0\"");
  }

  @Test
  void serializesNegative() throws JsonProcessingException {
    // Negative values appear in transaction metadata
    assertThat(objectMapper.writeValueAsString(MptAmount.of(UnsignedLong.valueOf(500L), true))).isEqualTo("\"-500\"");
  }

  @Test
  void serializesLargeValue() throws JsonProcessingException {
    UnsignedLong large = UnsignedLong.valueOf(Long.MAX_VALUE);
    assertThat(objectMapper.writeValueAsString(MptAmount.of(large)))
      .isEqualTo("\"" + large + "\"");
  }
}
