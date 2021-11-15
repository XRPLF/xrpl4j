package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import org.assertj.core.data.MapEntry;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.util.Optional;

public class GatewayBalancesIT extends AbstractIT {

  @Test
  public void testGatewayBalances() throws JsonRpcClientErrorException {
    // Create random accounts for the issuer and the counterparty
    Wallet issuerWallet = createRandomAccount();
    Wallet counterpartyWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      "10000",
      issuerWallet,
      counterpartyWallet,
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Send some xrpl4jCoin to the counterparty account.
    sendIssuedCurrency(
      xrpl4jCoin,
      trustLine.limitPeer(),
      issuerWallet,
      counterpartyWallet,
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(
      () -> getValidatedAccountLines(issuerWallet.classicAddress(), counterpartyWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + trustLine.limitPeer()))
    );

    GatewayBalancesResult result = xrplClient.gatewayBalances(GatewayBalancesRequestParams
      .builder()
      .account(issuerWallet.classicAddress())
      .addHotWallets(counterpartyWallet.classicAddress())
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build()
    );

    assertThat(result.account()).isEqualTo(issuerWallet.classicAddress());
    assertThat(result.status()).isEqualTo(Optional.of("success"));
    assertThat(result.assets().balancesByIssuer()).isEmpty();
    assertThat(result.obligations().balances()).isEmpty();
    assertThat(result.balances().balancesByHolder())
      .containsExactly(
        MapEntry.entry(
          counterpartyWallet.classicAddress(),
          Lists.newArrayList(
            GatewayBalancesIssuedCurrencyAmount
              .builder()
              .value("10000")
              .currency(xrpl4jCoin)
              .build()
          )
        )
      );
  }
}
