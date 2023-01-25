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

class LedgerIndexTest {

  @Test
  void createValidLedgerIndex() {
    Assertions.assertDoesNotThrow(() -> LedgerIndex.of("current"));
    Assertions.assertDoesNotThrow(() -> LedgerIndex.of("validated"));
    Assertions.assertDoesNotThrow(() -> LedgerIndex.of("closed"));
    Assertions.assertDoesNotThrow(() -> LedgerIndex.of("1"));
  }

  @Test
  void createInvalidLedgerIndex() {
    assertThrows(
      NullPointerException.class,
      () -> LedgerIndex.of((UnsignedInteger) null)
    );
    assertThrows(
      NullPointerException.class,
      () -> LedgerIndex.of((UnsignedInteger) null)
    );
    assertThrows(
      NumberFormatException.class,
      () -> LedgerIndex.of("foo")
    );
    assertThrows(
      NumberFormatException.class,
      () -> LedgerIndex.of("-1")
    );
  }

  @Test
  void testEquality() {
    LedgerIndex fromString = LedgerIndex.of("42");
    assertThat(fromString).isEqualTo(fromString);
    assertThat(fromString).isNotEqualTo("42");

    UnsignedInteger ui = UnsignedInteger.valueOf("42");
    LedgerIndex fromUnsignedInteger = LedgerIndex.of(ui);
    assertThat(fromString).isEqualTo(fromUnsignedInteger);
    assertThat(fromString).isNotEqualTo(LedgerIndex.CURRENT);
  }

  @Test
  void testToString() {
    LedgerIndex fromString = LedgerIndex.of("42");
    assertThat(fromString.toString()).isEqualTo("42");
    assertThat(LedgerIndex.CURRENT.toString()).isEqualTo("current");

    UnsignedInteger ui = UnsignedInteger.valueOf("42");
    LedgerIndex fromUnsignedInteger = LedgerIndex.of(ui);
    assertThat(fromString.toString()).isEqualTo(fromUnsignedInteger.toString());
  }

  @Test
  void createValidNumericalLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of("1");
    assertThat(ledgerIndex.value()).isEqualTo("1");

    final LedgerIndex fromUnsignedInteger = LedgerIndex.of(UnsignedInteger.ONE);
    assertThat(ledgerIndex).isEqualTo(fromUnsignedInteger);

    UnsignedInteger unsignedIntegerFromString = ledgerIndex.unsignedIntegerValue();
    UnsignedInteger unsignedIntegerFromUnsignedInteger = fromUnsignedInteger.unsignedIntegerValue();
    assertThat(unsignedIntegerFromString).isEqualTo(unsignedIntegerFromUnsignedInteger);

    final LedgerIndex added = ledgerIndex.plus(fromUnsignedInteger);
    assertThat(added).isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(2)));
  }

  @Test
  public void constructLedgerIndex() {
    LedgerIndex minLedgerIndex = LedgerIndex.of(UnsignedInteger.ONE);
    assertThat(minLedgerIndex.unsignedIntegerValue()).isEqualTo(UnsignedInteger.ONE);

    LedgerIndex maxLedgerIndex = LedgerIndex.of(UnsignedInteger.MAX_VALUE);
    assertThat(maxLedgerIndex.unsignedIntegerValue()).isEqualTo(UnsignedInteger.MAX_VALUE);
  }

  @Test
  public void addTwoLedgerIndexes() {
    LedgerIndex ledgerIndex1 = LedgerIndex.of(UnsignedInteger.valueOf(1000));
    LedgerIndex ledgerIndex2 = LedgerIndex.of(UnsignedInteger.valueOf(100));
    LedgerIndex added = ledgerIndex1.plus(ledgerIndex2);
    assertThat(added.unsignedIntegerValue())
      .isEqualTo(ledgerIndex1.unsignedIntegerValue().plus(ledgerIndex2.unsignedIntegerValue()));

    Assertions.assertDoesNotThrow(
      () -> ledgerIndex1.plus(LedgerIndex.of(UnsignedInteger.MAX_VALUE.minus(UnsignedInteger.valueOf(1001))))
    );
  }

  @Test
  public void addUnsignedLongToLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedInteger.valueOf(1000));
    UnsignedInteger toAdd = UnsignedInteger.valueOf(100);
    final LedgerIndex added = ledgerIndex.plus(toAdd);
    assertThat(added.unsignedIntegerValue()).isEqualTo(ledgerIndex.unsignedIntegerValue().plus(toAdd));

    Assertions.assertDoesNotThrow(
      () -> LedgerIndex.of(UnsignedInteger.MAX_VALUE.minus(UnsignedInteger.valueOf(1000)))
        .plus(UnsignedInteger.valueOf(1000))
    );
  }

  @Test
  void addTooLargeLedgerIndex() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedInteger.valueOf(1000))
        .plus(LedgerIndex.of(UnsignedInteger.MAX_VALUE.minus(UnsignedInteger.valueOf(999))))
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedInteger.valueOf(1000))
        .plus(LedgerIndex.of(UnsignedInteger.MAX_VALUE.minus(UnsignedInteger.valueOf(1))))
    );
  }

  @Test
  void addTooLargeUnsignedLong() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedInteger.MAX_VALUE.minus(UnsignedInteger.valueOf(1000)))
        .plus(UnsignedInteger.valueOf(1001))
    );

    assertThrows(
      IllegalArgumentException.class,
      () ->
        LedgerIndex.of(UnsignedInteger.MAX_VALUE.minus(UnsignedInteger.valueOf(1)))
          .plus(UnsignedInteger.valueOf(1000))
    );
  }

  @Test
  void subtractTwoLedgerIndexes() {
    LedgerIndex ledgerIndex1 = LedgerIndex.of(UnsignedInteger.valueOf(1000));
    LedgerIndex ledgerIndex2 = LedgerIndex.of(UnsignedInteger.valueOf(100));
    LedgerIndex subtracted = ledgerIndex1.minus(ledgerIndex2);
    assertThat(subtracted).isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(900)));
    assertThat(subtracted.unsignedIntegerValue()).isEqualTo(UnsignedInteger.valueOf(900));

    Assertions.assertDoesNotThrow(
      () -> LedgerIndex.of(UnsignedInteger.valueOf(1000)).minus(LedgerIndex.of(UnsignedInteger.valueOf(1000)))
    );
  }

  @Test
  void subtractUnsignedLongFromLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedInteger.valueOf(1000));
    UnsignedInteger unsignedInteger = UnsignedInteger.valueOf(100);
    LedgerIndex subtracted = ledgerIndex.minus(unsignedInteger);
    assertThat(subtracted).isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(900)));
    assertThat(subtracted.unsignedIntegerValue()).isEqualTo(UnsignedInteger.valueOf(900));

    Assertions.assertDoesNotThrow(
      () -> LedgerIndex.of(UnsignedInteger.valueOf(1000)).minus(UnsignedInteger.valueOf(1000))
    );
  }

  @Test
  void subtractLedgerIndexTooLarge() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedInteger.valueOf(100)).minus(LedgerIndex.of(UnsignedInteger.valueOf(1000)))
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedInteger.valueOf(999)).minus(LedgerIndex.of(UnsignedInteger.valueOf(1000)))
    );
  }

  @Test
  void subtractUnsignedLongTooLarge() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedInteger.valueOf(100)).minus(UnsignedInteger.valueOf(1000))
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedInteger.valueOf(999)).minus(UnsignedInteger.valueOf(1000))
    );
  }

  @Test
  void testJsonValueIsNumber() throws JsonProcessingException, JSONException {
    ObjectMapper objectMapper = ObjectMapperFactory.create();
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedInteger.ONE);
    LedgerIndexWrapper ledgerIndexWrapper = LedgerIndexWrapper.of(ledgerIndex);

    String json = "{\"ledgerIndex\": 1}";
    String serialized = objectMapper.writeValueAsString(ledgerIndexWrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    LedgerIndexWrapper deserialized = objectMapper.readValue(serialized, LedgerIndexWrapper.class);
    assertThat(deserialized).isEqualTo(ledgerIndexWrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerIndexWrapper.class)
  @JsonDeserialize(as = ImmutableLedgerIndexWrapper.class)
  interface LedgerIndexWrapper {

    static LedgerIndexWrapper of(LedgerIndex ledgerIndex) {
      return ImmutableLedgerIndexWrapper.builder().ledgerIndex(ledgerIndex).build();
    }

    LedgerIndex ledgerIndex();

  }
}
