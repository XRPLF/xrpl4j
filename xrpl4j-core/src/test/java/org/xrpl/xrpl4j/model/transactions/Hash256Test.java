package org.xrpl.xrpl4j.model.transactions;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
 * Unit tests for {@link Hash256}.
 */
public class Hash256Test {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  private static final Hash256 HASH_256_MAX = Hash256.of(
    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
  private static final Hash256 HASH_256_ZERO = Hash256.of(
    "0000000000000000000000000000000000000000000000000000000000000000");

  @Test
  void hashEquality() {
    assertThrows(NullPointerException.class, () -> Hash256.of(null));
    assertThat(HASH_256_MAX).isEqualTo(HASH_256_MAX);
    assertThat(HASH_256_MAX).isEqualTo(Hash256.of("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
    assertThat(HASH_256_ZERO).isNotEqualTo(HASH_256_MAX);
    assertThat(HASH_256_ZERO.equals(null)).isFalse();
    assertThat(HASH_256_ZERO).isNotEqualTo(new Object());
  }

  @Test
  void hashHashcode() {
    assertThat(HASH_256_MAX.hashCode()).isEqualTo(HASH_256_MAX.hashCode());

    assertThat(HASH_256_MAX.hashCode()).isEqualTo(
      Hash256.of("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff").hashCode());

    assertThat(HASH_256_ZERO.hashCode()).isNotEqualTo(HASH_256_MAX.hashCode());
  }

  @Test
  void testToString() {
    assertThat(HASH_256_ZERO.toString()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    Hash256 hash = Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
    Hash256Wrapper wrapper = ImmutableHash256Wrapper.builder()
      .hash(hash)
      .build();

    String json = "{\"hash\":\"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(Hash256Wrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    Hash256Wrapper deserialized = objectMapper.readValue(serialized, Hash256Wrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableHash256Wrapper.class)
  @JsonDeserialize(as = ImmutableHash256Wrapper.class)
  interface Hash256Wrapper {

    Hash256 hash();
  }

}
