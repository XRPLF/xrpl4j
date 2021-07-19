package org.xrpl.xrpl4j.model.client.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LedgerIndex;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

  @Test
  void handlesAllCorrectly() {
    List<LedgerSpecifier> ledgerSpecifiers = Lists.newArrayList(
      LedgerSpecifier.ledgerHash(Hash256.of(LEDGER_HASH)),
      LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)),
      LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
    );

    ledgerSpecifiers.forEach(this::assertHandlesCorrectly);
  }

  @Test
  void handleThrowsWithNullHandlers() {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(Hash256.of(LEDGER_HASH));
    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        null,
        $ -> {
        },
        $ -> {
        }
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        $ -> {
        },
        null,
        $ -> {
        }
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        $ -> {
        },
        $ -> {
        },
        null
      )
    );
  }

  @Test
  void mapsAllCorrectly() {
    List<LedgerSpecifier> ledgerSpecifiers = Lists.newArrayList(
      LedgerSpecifier.ledgerHash(Hash256.of(LEDGER_HASH)),
      LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)),
      LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
    );

    ledgerSpecifiers.forEach(
      specifier -> {
        final String mapped = specifier.map(
          ledgerHash -> "ledgerHash",
          ledgerIndex -> "ledgerIndex",
          ledgerIndexShortcut -> "ledgerIndexShortcut"
        );

        assertThat(mapped).isNotNull();

        if (specifier.ledgerHash().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerHash");
        } else if (specifier.ledgerIndex().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerIndex");
        } else if (specifier.ledgerIndexShortcut().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerIndexShortcut");
        }
      }
    );
  }

  @Test
  void mapThrowsWithNullMappers() {
    final LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(Hash256.of(LEDGER_HASH));
    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        null,
        $ -> "",
        $ -> ""
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        $ -> "",
        null,
        $ -> ""
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        $ -> "",
        $ -> "",
        null
      )
    );
  }

  private void assertHandlesCorrectly(LedgerSpecifier ledgerSpecifier) {
    AtomicBoolean ledgerHashHandled = new AtomicBoolean(false);
    AtomicBoolean ledgerIndexHandled = new AtomicBoolean(false);
    AtomicBoolean ledgerIndexShortcutHandled = new AtomicBoolean(false);

    ledgerSpecifier.handle(
      ledgerHash -> ledgerHashHandled.set(true),
      ledgerIndex -> ledgerIndexHandled.set(true),
      ledgerIndexShortcut -> ledgerIndexShortcutHandled.set(true)
    );

    if (ledgerSpecifier.ledgerHash().isPresent()) {
      assertThat(ledgerHashHandled).isTrue();
    } else {
      assertThat(ledgerHashHandled).isFalse();
    }

    if (ledgerSpecifier.ledgerIndex().isPresent()) {
      assertThat(ledgerIndexHandled).isTrue();
    } else {
      assertThat(ledgerIndexHandled).isFalse();
    }

    if (ledgerSpecifier.ledgerIndexShortcut().isPresent()) {
      assertThat(ledgerIndexShortcutHandled).isTrue();
    } else {
      assertThat(ledgerIndexShortcutHandled).isFalse();
    }
  }
}
