package org.xrpl.xrpl4j.model.client.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LedgerIndex;

class LedgerSpecifierTest {

  public static final String LEDGER_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

  @Test
  void specifyOneSpecifier() {
    assertDoesNotThrow(
      () -> LedgerSpecifier.builder()
        .ledgerHash(Hash256.of(LEDGER_HASH))
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
        .ledgerHash(Hash256.of(LEDGER_HASH))
        .ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> LedgerSpecifier.builder()
        .ledgerHash(Hash256.of(LEDGER_HASH))
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
        .ledgerHash(Hash256.of(LEDGER_HASH))
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

  @Test
  void specifyUsingUtilityConstructors() {
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerHash(Hash256.of(LEDGER_HASH)));
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)));
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CURRENT));
  }

}
