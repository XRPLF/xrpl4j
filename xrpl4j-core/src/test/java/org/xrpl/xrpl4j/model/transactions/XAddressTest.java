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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.api.AssertionsForClassTypes;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link XAddress}.
 */
public class XAddressTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  private static final XAddress X_ADDRESS = XAddress.of("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ");

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> XAddress.of(null));
    AssertionsForClassTypes.assertThat(X_ADDRESS.equals(null)).isFalse();
  }

  @Test
  void testWithValidValue() {
    assertThat(X_ADDRESS.value()).isEqualTo("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ");
    assertThat(X_ADDRESS.toString()).isEqualTo("X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ");
    assertThat(X_ADDRESS.equals(null)).isFalse();
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    XAddressWrapper wrapper = XAddressWrapper.of(X_ADDRESS);

    // XAddress uses default Immutables serialization (as an object with "value" field)
    String json = "{\"value\":{\"value\":\"X7AcgcsBL6XDcUb289X4mJ8djcdyKaB5hJDWMArnXr61cqZ\"}}";
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    XAddressWrapper deserialized = objectMapper.readValue(serialized, XAddressWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableXAddressWrapper.class)
  @JsonDeserialize(as = ImmutableXAddressWrapper.class)
  public interface XAddressWrapper {

    static XAddressWrapper of(XAddress xAddress) {
      return ImmutableXAddressWrapper.builder().value(xAddress).build();
    }

    XAddress value();
  }
}
