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
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.math.BigDecimal;
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
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1))
    );

    ///////////////////////////
    // Send some xrpl4jCoin to the counterparty account.
    sendIssuedCurrency(
      xrpl4jCoin,
      trustLine.limitPeer(),
      issuerWallet,
      counterpartyWallet,
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1))
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
