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
 * Unit tests for {@link MpTokenMetadata}.
 */
public class MpTokenMetadataTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testWithValidValue() {
    MpTokenMetadata metadata = MpTokenMetadata.of("68747470733A2F2F6578616D706C652E636F6D");
    assertThat(metadata.value()).isEqualTo("68747470733A2F2F6578616D706C652E636F6D");
    assertThat(metadata.toString()).isEqualTo("68747470733A2F2F6578616D706C652E636F6D");
    assertThat(metadata.equals(null)).isFalse();
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    MpTokenMetadata metadata = MpTokenMetadata.of("ABCDEF1234567890");
    MpTokenMetadataWrapper wrapper = MpTokenMetadataWrapper.of(metadata);

    String json = "{\"metadata\":\"ABCDEF1234567890\"}";
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    MpTokenMetadataWrapper deserialized = objectMapper.readValue(serialized, MpTokenMetadataWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableMpTokenMetadataWrapper.class)
  @JsonDeserialize(as = ImmutableMpTokenMetadataWrapper.class)
  public interface MpTokenMetadataWrapper {

    static MpTokenMetadataWrapper of(MpTokenMetadata metadata) {
      return ImmutableMpTokenMetadataWrapper.builder().metadata(metadata).build();
    }

    MpTokenMetadata metadata();
  }
}
