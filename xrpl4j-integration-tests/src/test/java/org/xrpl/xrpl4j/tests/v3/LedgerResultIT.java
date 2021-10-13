package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;

/**
 * These tests ensure {@link LedgerResult}s can be constructed from all of the different JSON responses rippled sends
 * back.
 */
public class LedgerResultIT extends AbstractIT {

  @Test
  void getValidatedLedgerResult() throws JsonRpcClientErrorException {
    final LedgerResult ledgerResult = xrplClient.ledger(LedgerRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build());
    assertThat(ledgerResult.ledgerIndex()).isNotEmpty().get().isEqualTo(ledgerResult.ledgerIndexSafe());
    assertThat(ledgerResult.ledgerHash()).isNotEmpty().get().isEqualTo(ledgerResult.ledgerHashSafe());
    assertThat(ledgerResult.ledgerCurrentIndex()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      ledgerResult::ledgerCurrentIndexSafe
    );
    assertThat(ledgerResult.ledger().closeTimeHuman()).isNotEmpty();
    assertThat(ledgerResult.ledger().parentCloseTime()).isNotEmpty();
  }

  @Test
  void getCurrentLedgerResult() throws JsonRpcClientErrorException {
    final LedgerResult ledgerResult = xrplClient.ledger(LedgerRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build());
    assertThat(ledgerResult.ledgerIndex()).isEmpty();
    assertThat(ledgerResult.ledgerHash()).isEmpty();
    assertThat(ledgerResult.ledgerCurrentIndex()).isNotEmpty();
    assertThat(ledgerResult.ledger().closeTimeHuman()).isEmpty();
    assertThat(ledgerResult.ledger().parentCloseTime()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      ledgerResult::ledgerHashSafe
    );
    assertThrows(
      IllegalStateException.class,
      ledgerResult::ledgerIndexSafe
    );
  }

  @Test
  void getClosedLedgerResult() throws JsonRpcClientErrorException {
    final LedgerResult ledgerResult = xrplClient.ledger(LedgerRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.CLOSED)
      .build());
    assertThat(ledgerResult.ledgerIndex()).isNotEmpty();
    assertThat(ledgerResult.ledgerHash()).isNotEmpty();
    assertThat(ledgerResult.ledgerIndex()).isNotEmpty().get().isEqualTo(ledgerResult.ledgerIndexSafe());
    assertThat(ledgerResult.ledgerHash()).isNotEmpty().get().isEqualTo(ledgerResult.ledgerHashSafe());
    assertThat(ledgerResult.ledgerCurrentIndex()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      ledgerResult::ledgerCurrentIndexSafe
    );
    assertThat(ledgerResult.ledger().closeTimeHuman()).isNotEmpty();
    assertThat(ledgerResult.ledger().parentCloseTime()).isNotEmpty();
  }
}
