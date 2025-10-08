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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import org.assertj.core.data.MapEntry;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Optional;

public class GatewayBalancesIT extends AbstractIT {

  @Test
  public void testGatewayBalances() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create random accounts for the issuer and the counterparty
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair counterpartyKeyPair = createRandomAccountEd25519();
    KeyPair frozenAccountKeyPair = createRandomAccountEd25519();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      counterpartyKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency(xrpl4jCoin)
        .value("10000")
        .build(),
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1))
    );

    ///////////////////////////
    // Send some xrpl4jCoin to the counterparty account.
    sendIssuedCurrency(
      issuerKeyPair, // <-- From
      counterpartyKeyPair, // <-- To
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency(xrpl4jCoin)
        .value(trustLine.limitPeer())
        .build(),
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1))
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    Address issuerAddress = issuerKeyPair.publicKey().deriveAddress();
    Address counterpartyAddress = counterpartyKeyPair.publicKey().deriveAddress();
    this.scanForResult(
      () -> getValidatedAccountLines(issuerAddress, counterpartyAddress),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + trustLine.limitPeer()))
    );

    GatewayBalancesResult result = xrplClient.gatewayBalances(GatewayBalancesRequestParams
      .builder()
      .account(issuerAddress)
      .addHotWallets(counterpartyAddress)
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build()
    );

    assertThat(result.account()).isEqualTo(issuerAddress);
    assertThat(result.status()).isEqualTo(Optional.of("success"));
    assertThat(result.assets().balancesByIssuer()).isEmpty();
    assertThat(result.obligations().balances()).isEmpty();
    assertThat(result.balances().balancesByHolder())
      .containsExactly(
        MapEntry.entry(
          counterpartyAddress,
          Lists.newArrayList(
            GatewayBalancesIssuedCurrencyAmount
              .builder()
              .value("10000")
              .currency(xrpl4jCoin)
              .build()
          )
        )
      );
    assertThat(result.frozenBalances().balancesByHolder()).isEmpty();

    ///////////////////////////
    // Create a Trust Line between issuer and frozenAccount
    TrustLine frozenTrustLine = createTrustLine(
      frozenAccountKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency(xrpl4jCoin)
        .value("5000")
        .build(),
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1))
    );

    ///////////////////////////
    // Send some xrpl4jCoin to the frozenAccount
    sendIssuedCurrency(
      issuerKeyPair,
      frozenAccountKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency(xrpl4jCoin)
        .value(frozenTrustLine.limitPeer())
        .build(),
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1))
    );

    Address frozenAccountAddress = frozenAccountKeyPair.publicKey().deriveAddress();
    this.scanForResult(
      () -> getValidatedAccountLines(issuerAddress, frozenAccountAddress),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + frozenTrustLine.limitPeer()))
    );

    ///////////////////////////
    // Freeze the trustline from the issuer's side
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerAddress)
    );

    TrustSet freezeTrustSet = TrustSet.builder()
      .account(issuerAddress)
      .fee(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1)))
      .sequence(issuerAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(xrpl4jCoin)
        .issuer(frozenAccountAddress)
        .value("0")
        .build())
      .flags(TrustSetFlags.builder().tfSetFreeze().build())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedFreezeTrustSet = signatureService.sign(
      issuerKeyPair.privateKey(),
      freezeTrustSet
    );
    SubmitResult<TrustSet> freezeSubmitResult = xrplClient.submit(signedFreezeTrustSet);
    assertThat(freezeSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    this.scanForResult(
      () -> getValidatedAccountLines(issuerAddress, frozenAccountAddress),
      linesResult -> linesResult.lines().stream()
        .anyMatch(TrustLine::freeze)
    );

    ///////////////////////////
    // Scenario 1: Call gatewayBalances with frozenAccount in hotWallets
    // Assert that frozenBalances is empty (frozen accounts in hotWallets are excluded from frozenBalances)
    GatewayBalancesResult resultWithFrozenInHotWallets = xrplClient.gatewayBalances(
      GatewayBalancesRequestParams.builder()
        .account(issuerAddress)
        .addHotWallets(frozenAccountAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    );

    assertThat(resultWithFrozenInHotWallets.frozenBalances().balancesByHolder()).isEmpty();

    ///////////////////////////
    // Scenario 2: Call gatewayBalances without hotWallets
    // Assert that frozenBalances has an entry for the frozen account
    GatewayBalancesResult resultWithoutHotWallets = xrplClient.gatewayBalances(
      GatewayBalancesRequestParams.builder()
        .account(issuerAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    );

    assertThat(resultWithoutHotWallets.frozenBalances().balancesByHolder())
      .containsKey(frozenAccountAddress);
    assertThat(resultWithoutHotWallets.frozenBalances().balancesByHolder().get(frozenAccountAddress))
      .containsExactly(
        GatewayBalancesIssuedCurrencyAmount.builder()
          .value("5000")
          .currency(xrpl4jCoin)
          .build()
      );
  }
}
