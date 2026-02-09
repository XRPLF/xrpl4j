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
 * Unit tests for {@link Marker}.
 */
public class MarkerTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    Marker marker = Marker.of("");
    assertThat(marker.toString()).isEqualTo("");

    marker = Marker.of("someMarkerValue");
    assertThat(marker.toString()).isEqualTo("someMarkerValue");

    marker = Marker.of("{\"ledger\":123,\"seq\":456}");
    assertThat(marker.toString()).isEqualTo("{\"ledger\":123,\"seq\":456}");
  }

  @Test
  void testWithValidValue() {
    Marker marker = Marker.of("test_marker_value");
    assertThat(marker.value()).isEqualTo("test_marker_value");
    assertThat(marker.toString()).isEqualTo("test_marker_value");
    assertThat(marker.equals(null)).isFalse();
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    Marker marker = Marker.of("marker123");
    MarkerWrapper wrapper = MarkerWrapper.of(marker);

    String json = "{\"marker\":\"marker123\"}";
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    MarkerWrapper deserialized = objectMapper.readValue(serialized, MarkerWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableMarkerWrapper.class)
  @JsonDeserialize(as = ImmutableMarkerWrapper.class)
  public interface MarkerWrapper {

    static MarkerWrapper of(Marker marker) {
      return ImmutableMarkerWrapper.builder().marker(marker).build();
    }

    Marker marker();
  }
}
