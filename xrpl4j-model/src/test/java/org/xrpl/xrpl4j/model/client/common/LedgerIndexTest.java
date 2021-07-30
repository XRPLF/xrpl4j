package org.xrpl.xrpl4j.model.client.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

class LedgerIndexTest {

  @Test
  void createValidLedgerIndex() {
    assertDoesNotThrow(() -> LedgerIndex.of("current"));
    assertDoesNotThrow(() -> LedgerIndex.of("validated"));
    assertDoesNotThrow(() -> LedgerIndex.of("closed"));
    assertDoesNotThrow(() -> LedgerIndex.of("1"));
  }

  @Test
  void createInvalidLedgerIndex() {
    assertThrows(
      NullPointerException.class,
      () -> LedgerIndex.of((String) null)
    );
    assertThrows(
      NullPointerException.class,
      () -> LedgerIndex.of((UnsignedLong) null)
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

    UnsignedLong ul = UnsignedLong.valueOf("42");
    LedgerIndex fromUnsignedLong = LedgerIndex.of(ul);
    assertThat(fromString).isEqualTo(fromUnsignedLong);
    assertThat(fromString).isNotEqualTo(LedgerIndex.CURRENT);
  }

  @Test
  void testToString() {
    LedgerIndex fromString = LedgerIndex.of("42");
    assertThat(fromString.toString()).isEqualTo("42");
    assertThat(LedgerIndex.CURRENT.toString()).isEqualTo("current");

    UnsignedLong ul = UnsignedLong.valueOf("42");
    LedgerIndex fromUnsignedLong = LedgerIndex.of(ul);
    assertThat(fromString.toString()).isEqualTo(fromUnsignedLong.toString());
  }

  @Test
  void createValidNumericalLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of("1");
    assertThat(ledgerIndex.value()).isEqualTo("1");

    final LedgerIndex fromUnsignedLong = LedgerIndex.of(UnsignedLong.ONE);
    assertThat(ledgerIndex).isEqualTo(fromUnsignedLong);

    UnsignedLong unsignedLongFromString = ledgerIndex.unsignedLongValue();
    UnsignedLong unsignedLongFromUnsignedLong = fromUnsignedLong.unsignedLongValue();
    assertThat(unsignedLongFromString).isEqualTo(unsignedLongFromUnsignedLong);

    final LedgerIndex added = ledgerIndex.plus(fromUnsignedLong);
    assertThat(added).isEqualTo(LedgerIndex.of("2"));
  }

  @Test
  public void constructLedgerIndex() {
    LedgerIndex minLedgerIndex = LedgerIndex.of(UnsignedLong.ONE);
    assertThat(minLedgerIndex.unsignedLongValue()).isEqualTo(UnsignedLong.ONE);

    LedgerIndex maxLedgerIndex = LedgerIndex.of(UnsignedLong.MAX_VALUE);
    assertThat(maxLedgerIndex.unsignedLongValue()).isEqualTo(UnsignedLong.MAX_VALUE);
  }

  @Test
  public void addTwoLedgerIndexes() {
    LedgerIndex ledgerIndex1 = LedgerIndex.of(UnsignedLong.valueOf(1000));
    LedgerIndex ledgerIndex2 = LedgerIndex.of(UnsignedLong.valueOf(100));
    LedgerIndex added = ledgerIndex1.plus(ledgerIndex2);
    assertThat(added.unsignedLongValue())
      .isEqualTo(ledgerIndex1.unsignedLongValue().plus(ledgerIndex2.unsignedLongValue()));

    assertDoesNotThrow(
      () -> ledgerIndex1.plus(LedgerIndex.of(UnsignedLong.MAX_VALUE.minus(UnsignedLong.valueOf(1001))))
    );
  }

  @Test
  public void addUnsignedLongToLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedLong.valueOf(1000));
    UnsignedLong toAdd = UnsignedLong.valueOf(100);
    final LedgerIndex added = ledgerIndex.plus(toAdd);
    assertThat(added.unsignedLongValue()).isEqualTo(ledgerIndex.unsignedLongValue().plus(toAdd));

    assertDoesNotThrow(
      () -> LedgerIndex.of(UnsignedLong.MAX_VALUE.minus(UnsignedLong.valueOf(1000))).plus(UnsignedLong.valueOf(1000))
    );
  }

  @Test
  void addTooLargeLedgerIndex() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedLong.valueOf(1000))
        .plus(LedgerIndex.of(UnsignedLong.MAX_VALUE.minus(UnsignedLong.valueOf(999))))
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedLong.valueOf(1000))
        .plus(LedgerIndex.of(UnsignedLong.MAX_VALUE.minus(UnsignedLong.valueOf(1))))
    );
  }

  @Test
  void addTooLargeUnsignedLong() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedLong.MAX_VALUE.minus(UnsignedLong.valueOf(1000))).plus(UnsignedLong.valueOf(1001))
    );

    assertThrows(
      IllegalArgumentException.class,
      () ->
        LedgerIndex.of(UnsignedLong.MAX_VALUE.minus(UnsignedLong.valueOf(1))).plus(UnsignedLong.valueOf(1000))
    );
  }

  @Test
  void subtractTwoLedgerIndexes() {
    LedgerIndex ledgerIndex1 = LedgerIndex.of(UnsignedLong.valueOf(1000));
    LedgerIndex ledgerIndex2 = LedgerIndex.of(UnsignedLong.valueOf(100));
    LedgerIndex subtracted = ledgerIndex1.minus(ledgerIndex2);
    assertThat(subtracted).isEqualTo(LedgerIndex.of(UnsignedLong.valueOf(900)));
    assertThat(subtracted.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(900));

    assertDoesNotThrow(
      () -> LedgerIndex.of(UnsignedLong.valueOf(1000)).minus(LedgerIndex.of(UnsignedLong.valueOf(1000)))
    );
  }

  @Test
  void subtractUnsignedLongFromLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedLong.valueOf(1000));
    UnsignedLong unsignedLong = UnsignedLong.valueOf(100);
    LedgerIndex subtracted = ledgerIndex.minus(unsignedLong);
    assertThat(subtracted).isEqualTo(LedgerIndex.of(UnsignedLong.valueOf(900)));
    assertThat(subtracted.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(900));

    assertDoesNotThrow(
      () -> LedgerIndex.of(UnsignedLong.valueOf(1000)).minus(UnsignedLong.valueOf(1000))
    );
  }

  @Test
  void subtractLedgerIndexTooLarge() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedLong.valueOf(100)).minus(LedgerIndex.of(UnsignedLong.valueOf(1000)))
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedLong.valueOf(999)).minus(LedgerIndex.of(UnsignedLong.valueOf(1000)))
    );
  }

  @Test
  void subtractUnsignedLongTooLarge() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedLong.valueOf(100)).minus(UnsignedLong.valueOf(1000))
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerIndex.of(UnsignedLong.valueOf(999)).minus(UnsignedLong.valueOf(1000))
    );
  }

  @Test
  void testJsonValueIsNumber() throws JsonProcessingException, JSONException {
    ObjectMapper objectMapper = ObjectMapperFactory.create();
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedLong.ONE);
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
