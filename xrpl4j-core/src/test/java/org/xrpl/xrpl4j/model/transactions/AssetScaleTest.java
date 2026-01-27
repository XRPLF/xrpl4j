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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link AssetScale}.
 */
public class AssetScaleTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testBounds() {
    AssetScale assetScale = AssetScale.of(UnsignedInteger.ZERO);
    assertThat(assetScale.value()).isEqualTo(UnsignedInteger.ZERO);

    assetScale = AssetScale.of(UnsignedInteger.MAX_VALUE);
    assertThat(assetScale.value()).isEqualTo(UnsignedInteger.MAX_VALUE);
  }

  @Test
  void testToString() {
    AssetScale assetScale = AssetScale.of(UnsignedInteger.valueOf(10));
    assertThat(assetScale.toString()).isEqualTo("10");
    assertThat(assetScale.equals(null)).isFalse();
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    AssetScale assetScale = AssetScale.of(UnsignedInteger.valueOf(5));
    AssetScaleWrapper wrapper = AssetScaleWrapper.of(assetScale);

    String json = "{\"assetScale\": 5}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    AssetScaleWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    AssetScaleWrapper deserialized = objectMapper.readValue(
      serialized, AssetScaleWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetScaleWrapper.class)
  @JsonDeserialize(as = ImmutableAssetScaleWrapper.class)
  interface AssetScaleWrapper {

    static AssetScaleWrapper of(AssetScale assetScale) {
      return ImmutableAssetScaleWrapper.builder().assetScale(assetScale).build();
    }

    AssetScale assetScale();

  }
}
