package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.tests.environment.MainnetEnvironment;

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
    int expectedTransactions = 748;
    // known ledger index range for this account that is known to have exactly 748 transactions
    LedgerIndex minLedger = LedgerIndex.of(UnsignedLong.valueOf(61400000));
    LedgerIndex maxLedger = LedgerIndex.of(UnsignedLong.valueOf(61487000));
    AccountTransactionsResult results = mainnetClient.accountTransactions(AccountTransactionsRequestParams.builder()
      .account(MAINNET_ADDRESS)
      .ledgerIndexMin(minLedger)
      .ledgerIndexMax(maxLedger)
      .build());
    assertThat(results.transactions()).isNotEmpty();
    assertThat(results.marker()).isNotEmpty();

    int transactionsFound = results.transactions().size();
    int pages = 1;
    while (results.marker().isPresent()) {
      results = mainnetClient.accountTransactions(AccountTransactionsRequestParams.builder()
        .account(MAINNET_ADDRESS)
        .ledgerIndexMin(minLedger)
        .ledgerIndexMax(maxLedger)
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
  public void listTransactionWithIndexRange() throws JsonRpcClientErrorException {
    // also arbitrary indexes chosen because they have known transaction results we can checl
    LedgerIndex minLedger = LedgerIndex.of(UnsignedLong.valueOf(61486000));
    LedgerIndex maxLedger = LedgerIndex.of(UnsignedLong.valueOf(61487000));
    AccountTransactionsResult results = mainnetClient.accountTransactions(AccountTransactionsRequestParams.builder()
      .account(MAINNET_ADDRESS)
      .ledgerIndexMin(minLedger)
      .ledgerIndexMax(maxLedger)
      .build());

    assertThat(results.ledgerIndexMin()).isEqualTo(minLedger);
    assertThat(results.ledgerIndexMax()).isEqualTo(maxLedger);
    assertThat(results.transactions()).hasSize(16);
    // results are returned in descending sorted order by ledger index
    assertThat(results.transactions().get(0).transaction().ledgerIndex()).contains(LedgerIndex.of("61486994"));
    assertThat(results.transactions().get(15).transaction().ledgerIndex()).contains(LedgerIndex.of("61486026"));
  }

}
