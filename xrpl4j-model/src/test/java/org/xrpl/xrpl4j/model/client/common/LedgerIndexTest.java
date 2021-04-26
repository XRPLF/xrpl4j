package org.xrpl.xrpl4j.model.client.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

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
  void testAddition() {
    LedgerIndex fromString = LedgerIndex.of("42");
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
}
