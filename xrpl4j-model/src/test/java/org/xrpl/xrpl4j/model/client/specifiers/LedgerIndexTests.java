package org.xrpl.xrpl4j.model.client.specifiers;

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

public class LedgerIndexTests {

  @Test
  public void constructLedgerIndex() {
    LedgerIndex minLedgerIndex = LedgerIndex.of(UnsignedLong.ONE);
    assertThat(minLedgerIndex.value()).isEqualTo(UnsignedLong.ONE);

    LedgerIndex maxLedgerIndex = LedgerIndex.of(UnsignedLong.MAX_VALUE);
    assertThat(maxLedgerIndex.value()).isEqualTo(UnsignedLong.MAX_VALUE);
  }

  @Test
  public void addTwoLedgerIndexes() {
    LedgerIndex ledgerIndex1 = LedgerIndex.of(UnsignedLong.valueOf(1000));
    LedgerIndex ledgerIndex2 = LedgerIndex.of(UnsignedLong.valueOf(100));
    LedgerIndex added = ledgerIndex1.plus(ledgerIndex2);
    assertThat(added.value()).isEqualTo(ledgerIndex1.value().plus(ledgerIndex2.value()));

    assertDoesNotThrow(
      () -> ledgerIndex1.plus(LedgerIndex.of(UnsignedLong.MAX_VALUE.minus(UnsignedLong.valueOf(1001))))
    );
  }

  @Test
  public void addUnsignedLongToLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedLong.valueOf(1000));
    UnsignedLong toAdd = UnsignedLong.valueOf(100);
    final LedgerIndex added = ledgerIndex.plus(toAdd);
    assertThat(added.value()).isEqualTo(ledgerIndex.value().plus(toAdd));

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
    assertThat(subtracted.value()).isEqualTo(UnsignedLong.valueOf(900));

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
    assertThat(subtracted.value()).isEqualTo(UnsignedLong.valueOf(900));

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
