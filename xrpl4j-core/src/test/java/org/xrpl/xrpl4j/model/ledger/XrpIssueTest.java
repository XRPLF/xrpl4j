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

/**
 * Unit tests for {@link XrpIssue}.
 */
class XrpIssueTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testEquals() {
    XrpIssue xrp1 = ImmutableXrpIssue.builder().build();
    XrpIssue xrp2 = ImmutableXrpIssue.builder().build();

    assertThat(xrp1).isEqualTo(xrp2);
    assertThat(xrp1).isEqualTo(XrpIssue.XRP);
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    XrpIssue xrp = XrpIssue.XRP;

    String expectedJson = "{\"currency\":\"XRP\"}";
    String serialized = objectMapper.writeValueAsString(xrp);
    JSONAssert.assertEquals(expectedJson, serialized, JSONCompareMode.STRICT);

    XrpIssue deserialized = objectMapper.readValue(serialized, XrpIssue.class);
    assertThat(deserialized).isEqualTo(xrp);
  }
}

