package org.xrpl.xrpl4j.model.client.common;

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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class LedgerIndexBoundTests {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void constructValidBounds() {
    LedgerIndexBound one = LedgerIndexBound.of(1);
    assertThat(one.value()).isEqualTo(1);

    LedgerIndexBound maxValue = LedgerIndexBound.of(UnsignedInteger.MAX_VALUE.longValue());
    assertThat(maxValue.value()).isEqualTo(UnsignedInteger.MAX_VALUE.longValue());

    LedgerIndexBound negativeOne = LedgerIndexBound.of(-1);
    assertThat(negativeOne.value()).isEqualTo(-1);
  }

  @Test
  void constructInvalidBounds() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(-2)
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(UnsignedInteger.MAX_VALUE.longValue() + 1)
    );
  }

  @Test
  void addLedgerIndexToBound() {
    LedgerIndexBound ledgerIndexBound = LedgerIndexBound.of(1);
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedInteger.valueOf(1000));
    LedgerIndexBound added = ledgerIndexBound.plus(ledgerIndex);
    assertThat(added.value()).isEqualTo(1001);
  }

  @Test
  void addLedgerIndexBoundToBound() {
    LedgerIndexBound ledgerIndexBound1 = LedgerIndexBound.of(1);
    LedgerIndexBound ledgerIndexBound2 = LedgerIndexBound.of(1000);
    LedgerIndexBound added1 = ledgerIndexBound1.plus(ledgerIndexBound2);
    assertThat(added1.value()).isEqualTo(1001);
    assertThat(added1).isEqualTo(LedgerIndexBound.of(1001));
    LedgerIndexBound added2 = ledgerIndexBound2.plus(ledgerIndexBound1);
    assertThat(added2.value()).isEqualTo(1001);
    assertThat(added2).isEqualTo(LedgerIndexBound.of(1001));

    assertThat(added2).isEqualTo(added1);
  }

  @Test
  void addLongToBound() {
    LedgerIndexBound ledgerIndexBound = LedgerIndexBound.of(1);
    final LedgerIndexBound added = ledgerIndexBound.plus(1000);
    assertThat(added).isEqualTo(LedgerIndexBound.of(1001));
    assertThat(added.value()).isEqualTo(1001);
  }

  @Test
  void subtractBoundFromBound() {
    LedgerIndexBound ledgerIndexBound1 = LedgerIndexBound.of(1000);
    LedgerIndexBound ledgerIndexBound2 = LedgerIndexBound.of(900);
    LedgerIndexBound subtracted = ledgerIndexBound1.minus(ledgerIndexBound2);
    assertThat(subtracted).isEqualTo(LedgerIndexBound.of(100));
    assertThat(subtracted.value()).isEqualTo(100);

    Assertions.assertDoesNotThrow(
      () -> ledgerIndexBound1.minus(LedgerIndexBound.of(999))
    );
  }

  @Test
  void subtractBoundTooLarge() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(1000).minus(LedgerIndexBound.of(1000))
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(1000).minus(LedgerIndexBound.of(10000))
    );
  }

  @Test
  void subtractLedgerIndexFromBound() {
    LedgerIndexBound ledgerIndexBound = LedgerIndexBound.of(1000);
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedInteger.valueOf(900));
    LedgerIndexBound subtracted = ledgerIndexBound.minus(ledgerIndex);
    assertThat(subtracted).isEqualTo(LedgerIndexBound.of(100));
    assertThat(subtracted.value()).isEqualTo(100L);

    Assertions.assertDoesNotThrow(
      () -> ledgerIndexBound.minus(LedgerIndex.of(UnsignedInteger.valueOf(999)))
    );
  }

  @Test
  void subtractLedgerIndexTooLarge() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(1000).minus(LedgerIndex.of(UnsignedInteger.valueOf(1000)))
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(1000).minus(LedgerIndex.of(UnsignedInteger.valueOf(10000)))
    );
  }

  @Test
  void subtractLongFromBound() {
    LedgerIndexBound ledgerIndexBound = LedgerIndexBound.of(1000);
    Long longValue = 900L;
    LedgerIndexBound subtractedLong = ledgerIndexBound.minus(longValue);
    assertThat(subtractedLong).isEqualTo(LedgerIndexBound.of(100));
    assertThat(subtractedLong.value()).isEqualTo(100L);

    Integer intValue = 900;
    LedgerIndexBound subtractedInt = ledgerIndexBound.minus(intValue);
    assertThat(subtractedInt).isEqualTo(LedgerIndexBound.of(100));
    assertThat(subtractedInt.value()).isEqualTo(100L);

    Assertions.assertDoesNotThrow(
      () -> ledgerIndexBound.minus(999)
    );

    Assertions.assertDoesNotThrow(
      () -> ledgerIndexBound.minus(999L)
    );
  }

  @Test
  void subtractUnsignedLongTooLarge() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(1000).minus(1000)
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndexBound.of(1000).minus(10000)
    );
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    LedgerIndexBound ledgerIndexBound = LedgerIndexBound.of(1);
    LedgerIndexBoundWrapper wrapper = LedgerIndexBoundWrapper.of(ledgerIndexBound);

    String json = "{\"ledgerIndexBound\": 1}";
    assertSerializesAndDeserializes(wrapper, json);

    LedgerIndexBound negativeLedgerIndexBound = LedgerIndexBound.of(-1);
    LedgerIndexBoundWrapper negativeWrapper = LedgerIndexBoundWrapper.of(negativeLedgerIndexBound);

    String negativeJson = "{\"ledgerIndexBound\": -1}";
    assertSerializesAndDeserializes(negativeWrapper, negativeJson);
  }

  private void assertSerializesAndDeserializes(
    LedgerIndexBoundWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    LedgerIndexBoundWrapper deserialized = objectMapper.readValue(serialized, LedgerIndexBoundWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerIndexBoundWrapper.class)
  @JsonDeserialize(as = ImmutableLedgerIndexBoundWrapper.class)
  interface LedgerIndexBoundWrapper {

    static LedgerIndexBoundWrapper of(LedgerIndexBound ledgerIndexBound) {
      return ImmutableLedgerIndexBoundWrapper.builder().ledgerIndexBound(ledgerIndexBound).build();
    }

    LedgerIndexBound ledgerIndexBound();

  }
}
