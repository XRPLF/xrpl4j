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
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link MpTokenNumericAmount}.
 */
public class MpTokenNumericAmountTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testBounds() {
    MpTokenNumericAmount amount = MpTokenNumericAmount.of(UnsignedLong.ZERO);
    assertThat(amount.value()).isEqualTo(UnsignedLong.ZERO);

    amount = MpTokenNumericAmount.of(UnsignedLong.MAX_VALUE);
    assertThat(amount.value()).isEqualTo(UnsignedLong.MAX_VALUE);
  }

  @Test
  void testToString() {
    MpTokenNumericAmount amount = MpTokenNumericAmount.of(UnsignedLong.ZERO);
    assertThat(amount.toString()).isEqualTo("0");

    amount = MpTokenNumericAmount.of(100000L);
    assertThat(amount.toString()).isEqualTo("100000");

    amount = MpTokenNumericAmount.of(UnsignedLong.MAX_VALUE);
    assertThat(amount.toString()).isEqualTo("18446744073709551615");
  }

  @Test
  void testOfLong() {
    MpTokenNumericAmount amount = MpTokenNumericAmount.of(500L);
    assertThat(amount.value()).isEqualTo(UnsignedLong.valueOf(500));
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    MpTokenNumericAmount amount = MpTokenNumericAmount.of(UnsignedLong.valueOf(999));
    MpTokenNumericAmountWrapper wrapper = MpTokenNumericAmountWrapper.of(amount);

    String json = "{\"amount\": \"999\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testMaxJson() throws JSONException, JsonProcessingException {
    MpTokenNumericAmount amount = MpTokenNumericAmount.of(UnsignedLong.MAX_VALUE);
    MpTokenNumericAmountWrapper wrapper = MpTokenNumericAmountWrapper.of(amount);

    String json = "{\"amount\": \"18446744073709551615\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    MpTokenNumericAmountWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    MpTokenNumericAmountWrapper deserialized = objectMapper.readValue(
      serialized, MpTokenNumericAmountWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableMpTokenNumericAmountWrapper.class)
  @JsonDeserialize(as = ImmutableMpTokenNumericAmountWrapper.class)
  interface MpTokenNumericAmountWrapper {

    static MpTokenNumericAmountWrapper of(MpTokenNumericAmount amount) {
      return ImmutableMpTokenNumericAmountWrapper.builder().amount(amount).build();
    }

    MpTokenNumericAmount amount();

  }
}
