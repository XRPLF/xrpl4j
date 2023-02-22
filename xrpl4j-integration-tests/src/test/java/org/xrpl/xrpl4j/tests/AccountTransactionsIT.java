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
import static org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.primitives.UnsignedInteger;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.tests.environment.MainnetEnvironment;

import java.util.Optional;

/**
 * An Integration Test to validate submission of Account transactions.
 */
public class AccountTransactionsIT {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // an arbitrary address on xrpl mainnet that has a decent amount of transaction history
  public static final Address MAINNET_ADDRESS = Address.of("r9m6MwViR4GnUNqoGXGa8eroBrZ9FAPHFS");

  private final XrplClient mainnetClient = new MainnetEnvironment().getXrplClient();

  @Test
  public void listTransactionsDefaultWithPagination() throws JsonRpcClientErrorException {
    AccountTransactionsResult results = mainnetClient.accountTransactions(MAINNET_ADDRESS);

    // The default value for reporting mode is 200; but clio it's 50. However, the important things to test here is
    // that a marker exists, so we only assert on that value.
    assertThat(results.marker()).isNotEmpty();
  }

  @Test
  @Timeout(30)
  public void listTransactionsPagination() throws JsonRpcClientErrorException {
    final int expectedTransactions = 748;
    // known ledger index range for this account that is known to have exactly 748 transactions
    LedgerIndexBound minLedger = LedgerIndexBound.of(61400000);
    LedgerIndexBound maxLedger = LedgerIndexBound.of(61487000);
    AccountTransactionsResult results = mainnetClient.accountTransactions(
      AccountTransactionsRequestParams.builder(minLedger, maxLedger)
        .account(MAINNET_ADDRESS)
        .build()
    );
    assertThat(results.transactions()).isNotEmpty();
    assertThat(results.marker()).isNotEmpty();

    int transactionsFound = results.transactions().size();
    int pages = 0;
    while (results.marker().isPresent() &&
      // Needed because clio is broken. See https://github.com/XRPLF/clio/issues/195#issuecomment-1247412892
      results.transactions().size() > 0
    ) {
      results = mainnetClient.accountTransactions(AccountTransactionsRequestParams.builder(minLedger, maxLedger)
        .account(MAINNET_ADDRESS)
        .limit(UnsignedInteger.valueOf(200L))
        .marker(results.marker().get())
        .build());
      transactionsFound += results.transactions().size();
      pages++;
      logger.info("Retrieved {} ledgers (marker={} page={})",
        transactionsFound,
        results.marker().map($ -> $.value()).orElseGet(() -> "n/a"),
        pages
      );
    }

    assertThat(transactionsFound).isEqualTo(expectedTransactions);
  }

  @Test
  public void listTransactionWithIndexRange() {
    // also arbitrary indexes chosen because they have known transaction results we can check
    LedgerIndexBound minLedger = LedgerIndexBound.of(61486000);
    LedgerIndexBound maxLedger = LedgerIndexBound.of(61487000);

    // Sometimes we will get a "server busy" error back in this test, so if we do get that, we should just wait
    // a few seconds until asking again.
    AccountTransactionsResult results = getAccountTransactions(
      AccountTransactionsRequestParams.builder(minLedger, maxLedger)
        .account(MAINNET_ADDRESS)
        .build()
    );

    assertThat(results.transactions()).hasSize(16);
    // results are returned in descending sorted order by ledger index
    assertThat(results.transactions().get(0).resultTransaction().ledgerIndex())
      .isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(61486994)));
    assertThat(results.transactions().get(0).resultTransaction().closeDate()).isNotEmpty();
    assertThat(results.transactions().get(15).resultTransaction().ledgerIndex())
      .isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(61486026)));
  }

  @Test
  void listTransactionsWithLedgerSpecifiers() throws JsonRpcClientErrorException {
    AccountTransactionsResult resultByShortcut = getAccountTransactions(
      AccountTransactionsRequestParams.builder(LedgerSpecifier.VALIDATED)
        .account(MAINNET_ADDRESS)
        .build()
    );

    LedgerResult ledger = mainnetClient.ledger(
      LedgerRequestParams.builder()
        .ledgerSpecifier(
          LedgerSpecifier.of(
            LedgerIndex.of(
              UnsignedInteger.valueOf(resultByShortcut.ledgerIndexMinimum().value()))
          )
        )
        .build()
    );

    Hash256 validatedLedgerHash = ledger.ledgerHash()
      .orElseThrow(() -> new RuntimeException("ledgerHash not present."));
    LedgerIndex validatedLedgerIndex = ledger.ledgerIndex()
      .orElseThrow(() -> new RuntimeException("ledgerIndex not present."));

    AccountTransactionsResult resultByLedgerIndex = getAccountTransactions(
      AccountTransactionsRequestParams.builder(LedgerSpecifier.of(validatedLedgerIndex))
        .account(MAINNET_ADDRESS)
        .build()
    );

    AccountTransactionsResult resultByLedgerHash = getAccountTransactions(
      AccountTransactionsRequestParams.builder(LedgerSpecifier.of(validatedLedgerHash))
        .account(MAINNET_ADDRESS)
        .build()
    );

    assertThat(resultByShortcut.ledgerIndexMinimum().value())
      .isEqualTo(validatedLedgerIndex.unsignedIntegerValue().longValue());

    assertThat(resultByShortcut).isEqualTo(resultByLedgerIndex);
    assertThat(resultByLedgerIndex).isEqualTo(resultByLedgerHash);
  }

  private AccountTransactionsResult getAccountTransactions(AccountTransactionsRequestParams params) {
    return given()
      .pollInterval(Durations.FIVE_SECONDS)
      .await()
      .until(() -> {
        try {
          return mainnetClient.accountTransactions(params);
        } catch (JsonRpcClientErrorException e) {
          if (e.getMessage().equals("The server is too busy to help you now.")) {
            return null;
          } else {
            throw new RuntimeException(e);
          }
        }
      }, is(notNullValue()));
  }
}
