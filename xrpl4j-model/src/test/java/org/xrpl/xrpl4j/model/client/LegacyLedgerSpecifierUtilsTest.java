package org.xrpl.xrpl4j.model.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

@Deprecated // Remove once LegacyLedgerSpecifierUtils is removed
class LegacyLedgerSpecifierUtilsTest {

  static final Hash256 LEDGER_HASH = Hash256.of(Strings.repeat("0", 64));

  @Test
  void computeLedgerSpecifierWithHash() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.of(LEDGER_HASH),
      LedgerIndex.VALIDATED
    );

    assertThat(ledgerSpecifier.ledgerHash()).isNotEmpty().get().isEqualTo(LEDGER_HASH);
  }

  @Test
  void computeLedgerSpecifierWithNumericalIndex() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.of(UnsignedInteger.ONE)
    );

    assertThat(ledgerSpecifier.ledgerIndex()).isNotEmpty().get().isEqualTo(LedgerIndex.of(UnsignedInteger.ONE));
  }

  @Test
  void computeLedgerSpecifierWithShortcut() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.VALIDATED
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.VALIDATED);

    ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.CURRENT
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.CURRENT);

    ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.CLOSED
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.CLOSED);
  }

  @Test
  void computeDefaultLedgerSpecifier() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      null
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.CURRENT);
  }
}
