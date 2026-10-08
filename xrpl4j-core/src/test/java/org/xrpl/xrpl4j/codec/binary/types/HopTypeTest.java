package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;

class HopTypeTest {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
  private static final HopType codec = new HopType();

  @Test
  void issuerFieldPreservedOnRoundTrip() throws JsonProcessingException {
    String hopJson = "{\"account\":\"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}";
    JsonNode inputNode = objectMapper.readTree(hopJson);
    HopType encoded = codec.fromJson(inputNode);
    JsonNode outputNode = encoded.toJson();
    Hop outputHop = objectMapper.treeToValue(outputNode, Hop.class);
    assertThat(outputHop.issuer()).isPresent();
  }

  @Test
  void accountFieldNotOverwrittenByIssuerData() throws JsonProcessingException {
    String hopJson = "{\"account\":\"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}";
    JsonNode inputNode = objectMapper.readTree(hopJson);
    HopType encoded = codec.fromJson(inputNode);
    JsonNode outputNode = encoded.toJson();
    Hop outputHop = objectMapper.treeToValue(outputNode, Hop.class);
    assertThat(outputHop.account()).isPresent();
    assertThat(outputHop.account().get().asText()).isEqualTo("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
    assertThat(outputHop.issuer().get().asText()).isEqualTo("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw");
  }

}
