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
import org.xrpl.xrpl4j.model.jackson.modules.IouAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.amount.IouAmount;

/**
 * Unit tests for {@link IouAmountSerializer}.
 */
class IouAmountSerializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void serializesDecimal() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(IouAmount.of("100.50"))).isEqualTo("\"100.50\"");
  }

  @Test
  void serializesWholeNumber() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(IouAmount.of("15"))).isEqualTo("\"15\"");
  }

  @Test
  void serializesScientificNotation() throws JsonProcessingException {
    // Scientific notation is preserved verbatim as returned by the XRPL RPC
    assertThat(objectMapper.writeValueAsString(IouAmount.of("1.23e10"))).isEqualTo("\"1.23e10\"");
  }

  @Test
  void serializesNegative() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(IouAmount.of("-100.50"))).isEqualTo("\"-100.50\"");
  }

  @Test
  void serializesZero() throws JsonProcessingException {
    assertThat(objectMapper.writeValueAsString(IouAmount.of("0"))).isEqualTo("\"0\"");
  }
}
