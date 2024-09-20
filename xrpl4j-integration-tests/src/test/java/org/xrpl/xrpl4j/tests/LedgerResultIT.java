package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.AmmBid;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

import java.util.Optional;

/**
 * These tests ensure {@link LedgerResult}s can be constructed from any JSON responses rippled sends back.
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

  /**
   * Pulls down ledger BAA4AF508E9A9CB7685D9E61B82486681E52E9B920904B0650499497F050D8FA, which had an
   * {@link org.xrpl.xrpl4j.model.transactions.AmmBid} transaction with an
   * {@link org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount} in the {@link AmmBid#bidMax()} field.
   */
  @Test
  void canDeserializeLedger_Issue529() throws JsonRpcClientErrorException {
    XrplClient mainnetClient = XrplEnvironment.getConfiguredMainnetEnvironment().getXrplClient();
    LedgerResult ledger = mainnetClient.ledger(
      LedgerRequestParams.builder()
        .transactions(true)
        .ledgerSpecifier(
          LedgerSpecifier.of(Hash256.of("BAA4AF508E9A9CB7685D9E61B82486681E52E9B920904B0650499497F050D8FA")))
        .build()
    );

    Optional<TransactionResult<? extends Transaction>> problematicTransaction = ledger.ledger().transactions().stream()
      .filter(transaction -> transaction.hash()
        .equals(Hash256.of("6A8BC948E1309219EA8E14905D0EA9BB94E24429DE5B15CDE8916CDBCE42034B")))
      .findFirst();

    assertThat(problematicTransaction).isNotEmpty();
  }
}
