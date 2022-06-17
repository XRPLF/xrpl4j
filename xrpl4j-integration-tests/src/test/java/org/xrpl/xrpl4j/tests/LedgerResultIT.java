package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import org.xrpl.xrpl4j.tests.environment.MainnetEnvironment;

/**
 * These tests ensure {@link LedgerResult}s can be constructed from all of the different JSON responses
 * rippled sends back.
 */
public class LedgerResultIT extends BaseIT {

  @Test
  void getValidatedLedgerResult() throws JsonRpcClientErrorException {
    final LedgerResult ledgerResult = xrplClient().ledger(LedgerRequestParams.builder()
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
    final LedgerResult ledgerResult = xrplClient().ledger(LedgerRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build());

    assertThat(ledgerResult.ledgerIndex()).isEmpty();
    assertThat(ledgerResult.ledgerHash()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      ledgerResult::ledgerHashSafe
    );
    assertThrows(
      IllegalStateException.class,
      ledgerResult::ledgerIndexSafe
    );
    assertThat(ledgerResult.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(ledgerResult.ledgerCurrentIndexSafe());
    assertThat(ledgerResult.ledger().closeTimeHuman()).isEmpty();
    assertThat(ledgerResult.ledger().parentCloseTime()).isEmpty();
  }

  @Test
  void getClosedLedgerResult() throws JsonRpcClientErrorException {
    final LedgerResult ledgerResult = xrplClient().ledger(LedgerRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.CLOSED)
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
  void getLedgerResult() throws JsonRpcClientErrorException {

    final XrplClient mainnetClient = new MainnetEnvironment().getXrplClient();
    final LedgerResult ledgerResult = mainnetClient.ledger(LedgerRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .transactions(true)
      .build());

    assertThat(ledgerResult.ledger().transactions().stream().map(TransactionResult::metadata)
      .count()).isEqualTo(ledgerResult.ledger().transactions().size());
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
