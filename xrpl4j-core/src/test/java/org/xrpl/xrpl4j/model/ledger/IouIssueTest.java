package org.xrpl.xrpl4j.model.ledger;

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
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Unit tests for {@link IouIssue}.
 */
class IouIssueTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  private static final String USD = "USD";
  private static final Address ISSUER = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMbjmjY");

  @Test
  void testEquals() {
    IouIssue issue1 = IouIssue.builder().currency(USD).issuer(ISSUER).build();
    IouIssue issue2 = IouIssue.builder().currency(USD).issuer(ISSUER).build();

    assertThat(issue1).isEqualTo(issue2);
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    IouIssue issue = IouIssue.builder()
      .currency(USD)
      .issuer(ISSUER)
      .build();

    String expectedJson = "{\"currency\":\"USD\",\"issuer\":\"" + ISSUER.value() + "\"}";
    String serialized = objectMapper.writeValueAsString(issue);
    JSONAssert.assertEquals(expectedJson, serialized, JSONCompareMode.STRICT);

    IouIssue deserialized = objectMapper.readValue(serialized, IouIssue.class);
    assertThat(deserialized).isEqualTo(issue);
  }
}