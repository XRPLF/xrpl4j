package org.xrpl.xrpl4j.client.dex;

import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.dex.model.Balance;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DexClient {

  private XrplClient xrplClient;

  public DexClient(XrplClient xrplClient) {
    this.xrplClient = xrplClient;
  }

  public List<Balance> getBalances(Address address) throws JsonRpcClientErrorException {
    AccountLinesResult accountLines = xrplClient.accountLines(AccountLinesRequestParams.builder()
      .account(address)
      .ledgerIndex(LedgerIndex.VALIDATED)
      .build());

    // TODO figure out amount that is locked on order book
    Map<String, List<Balance>> balances = accountLines.lines()
      .stream()
      .map(line -> {
        BigDecimal total = new BigDecimal(line.balance());
        // TODO figure out amount that is locked on order book
        BigDecimal locked = BigDecimal.ZERO;
        BigDecimal available = total.subtract(locked);

        return Balance.builder()
          .available(available)
          .locked(locked)
          .total(total)
          .currency(line.currency())
          .build();
      })
      .collect(Collectors.groupingBy(Balance::currency));

    return balances.entrySet().stream()
      .map(entry ->
        entry.getValue().stream()
          .reduce(Balance.zeroBalance(entry.getKey()), (left, right) -> left.add(right))
      )
      .collect(Collectors.toList());
  }

}
