package org.xrpl.xrpl4j.model.client.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LedgerIndex;

class LedgerSpecifierTest {

  @Test
  void specifyOneSpecifier() {
    assertDoesNotThrow(
      () -> LedgerSpecifier.builder()
        .ledgerHash(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000"))
        .build()
    );

    assertDoesNotThrow(
      () -> LedgerSpecifier.builder()
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .build()
    );

    assertDoesNotThrow(
      () -> LedgerSpecifier.builder()
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );
  }

  @Test
  void specifyMoreThanOneThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerHash(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000"))
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerHash(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000"))
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerHash(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000"))
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );
  }

  @Test
  void specifyNoneThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder().build()
    );
  }
}
