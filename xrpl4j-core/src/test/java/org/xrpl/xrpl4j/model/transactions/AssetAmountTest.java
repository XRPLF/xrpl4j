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
 * Unit tests for {@link AssetAmount}.
 */
public class AssetAmountTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    AssetAmount assetAmount = AssetAmount.of("0");
    assertThat(assetAmount.toString()).isEqualTo("0");

    assetAmount = AssetAmount.of("1000000");
    assertThat(assetAmount.toString()).isEqualTo("1000000");

    assetAmount = AssetAmount.of("123456789012345678901234567890");
    assertThat(assetAmount.toString()).isEqualTo("123456789012345678901234567890");
  }

  @Test
  void testValue() {
    AssetAmount assetAmount = AssetAmount.of("500");
    assertThat(assetAmount.value()).isEqualTo("500");

    assetAmount = AssetAmount.of("999999999999999999");
    assertThat(assetAmount.value()).isEqualTo("999999999999999999");
  }

  @Test
  void testJsonSerializationInWrapper() throws JsonProcessingException, JSONException {
    AssetAmountWrapper wrapper = AssetAmountWrapper.of(AssetAmount.of("5000"));
    String json = "{\"amount\":\"5000\"}";
    
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    
    AssetAmountWrapper deserialized = objectMapper.readValue(serialized, AssetAmountWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Test
  void testEquality() {
    AssetAmount amount1 = AssetAmount.of("1000");
    AssetAmount amount2 = AssetAmount.of("1000");
    AssetAmount amount3 = AssetAmount.of("2000");

    assertThat(amount1).isEqualTo(amount2);
    assertThat(amount1).isNotEqualTo(amount3);
    assertThat(amount1.hashCode()).isEqualTo(amount2.hashCode());
  }

  /**
   * Test wrapper class for JSON serialization testing.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetAmountWrapper.class)
  @JsonDeserialize(as = ImmutableAssetAmountWrapper.class)
  interface AssetAmountWrapper {
    static AssetAmountWrapper of(AssetAmount amount) {
      return ImmutableAssetAmountWrapper.builder().amount(amount).build();
    }

    AssetAmount amount();
  }
}

