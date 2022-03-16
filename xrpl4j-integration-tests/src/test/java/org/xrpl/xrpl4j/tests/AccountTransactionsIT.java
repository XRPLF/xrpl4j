package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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

import java.util.concurrent.TimeUnit;

public class AccountTransactionsIT {

  // an arbitrary address on xrpl mainnet that has a decent amount of transaction history
  public static final Address MAINNET_ADDRESS = Address.of("r9m6MwViR4GnUNqoGXGa8eroBrZ9FAPHFS");

  private final XrplClient mainnetClient = new MainnetEnvironment().getXrplClient();

  @Test
  public void listTransactionsDefaultWithPagination() throws JsonRpcClientErrorException {
    AccountTransactionsResult results = mainnetClient.accountTransactions(MAINNET_ADDRESS);

    assertThat(results.transactions()).hasSize(200);
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
    int pages = 1;
    while (results.marker().isPresent()) {
      results = mainnetClient.accountTransactions(AccountTransactionsRequestParams
          .builder(minLedger, maxLedger)
        .account(MAINNET_ADDRESS)
        .marker(results.marker().get())
        .build());
      assertThat(results.transactions()).isNotEmpty();
      transactionsFound += results.transactions().size();
      pages++;
    }

    assertThat(transactionsFound).isEqualTo(expectedTransactions);
    assertThat(pages).isEqualTo(4);
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

    assertThat(results.ledgerIndexMinimum()).isEqualTo(minLedger);
    assertThat(results.ledgerIndexMaximum()).isEqualTo(maxLedger);
    assertThat(results.transactions()).hasSize(16);
    // results are returned in descending sorted order by ledger index
    assertThat(results.transactions().get(0).transaction().ledgerIndex()).isNotEmpty().get()
      .isEqualTo(results.transactions().get(0).resultTransaction().ledgerIndex());
    assertThat(results.transactions().get(0).resultTransaction().ledgerIndex())
      .isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(61486994)));

    assertThat(results.transactions().get(15).transaction().ledgerIndex()).isNotEmpty().get()
      .isEqualTo(results.transactions().get(15).resultTransaction().ledgerIndex());
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
    LedgerIndex validatedLedgerIndex = ledger.ledgerIndexSafe();

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

    assertThat(resultByShortcut.ledgerIndexMinimum()).isEqualTo(resultByShortcut.ledgerIndexMaximum());
    assertThat(resultByShortcut.ledgerIndexMinimum().value())
      .isEqualTo(validatedLedgerIndex.unsignedIntegerValue().longValue());

    assertThat(resultByShortcut).isEqualTo(resultByLedgerIndex);
    assertThat(resultByLedgerIndex).isEqualTo(resultByLedgerHash);
  }

  private AccountTransactionsResult getAccountTransactions(AccountTransactionsRequestParams params) {
    return given()
      .pollInterval(5, TimeUnit.SECONDS)
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
