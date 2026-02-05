package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link MpTokenIssuanceId}.
 */
public class MpTokenIssuanceIdTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testWithValidValue() {
    MpTokenIssuanceId id = MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3E");
    assertThat(id.value()).isEqualTo("000004C463C52827307480341125DA0577DEFC38405B0E3E");
    assertThat(id.toString()).isEqualTo("000004C463C52827307480341125DA0577DEFC38405B0E3E");
    assertThat(id.equals(null)).isFalse();
  }

  @Test
  void testHashCode() {
    MpTokenIssuanceId idUpper = MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3E");
    MpTokenIssuanceId idLower = MpTokenIssuanceId.of("000004c463c52827307480341125da0577defc38405b0e3e");
    MpTokenIssuanceId idDifferent = MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3F");

    // Same value should have same hashCode
    assertThat(idUpper.hashCode()).isEqualTo(idUpper.hashCode());

    // Case-insensitive: upper and lower case should have same hashCode
    assertThat(idUpper.hashCode()).isEqualTo(idLower.hashCode());

    // Different values should have different hashCodes
    assertThat(idUpper.hashCode()).isNotEqualTo(idDifferent.hashCode());
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    MpTokenIssuanceId id = MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3E");
    MpTokenIssuanceIdWrapper wrapper = MpTokenIssuanceIdWrapper.of(id);

    String json = "{\"id\":\"000004C463C52827307480341125DA0577DEFC38405B0E3E\"}";
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    MpTokenIssuanceIdWrapper deserialized = objectMapper.readValue(serialized, MpTokenIssuanceIdWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableMpTokenIssuanceIdWrapper.class)
  @JsonDeserialize(as = ImmutableMpTokenIssuanceIdWrapper.class)
  public interface MpTokenIssuanceIdWrapper {

    static MpTokenIssuanceIdWrapper of(MpTokenIssuanceId id) {
      return ImmutableMpTokenIssuanceIdWrapper.builder().id(id).build();
    }

    MpTokenIssuanceId id();
  }
}
