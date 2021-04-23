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
    String nullStr = null;
    UnsignedLong nullUL = null;
    assertThrows(
      NumberFormatException.class,
      () -> LedgerIndex.of(nullStr)
    );
    assertThrows(
      NumberFormatException.class,
      () -> LedgerIndex.of(nullUL)
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
  void createInvalidNumericalLedgerIndex() {
    final LedgerIndex fooLedgerIndex = LedgerIndex.of("foo");
    assertThrows(
      NumberFormatException.class,
      fooLedgerIndex::unsignedLongValue
    );

    final LedgerIndex negativeLedgerIndex = LedgerIndex.of("-1");
    assertThrows(
      NumberFormatException.class,
      negativeLedgerIndex::unsignedLongValue
    );

    assertThrows(
      NumberFormatException.class,
      () -> fooLedgerIndex.plus(negativeLedgerIndex)
    );
  }
}
