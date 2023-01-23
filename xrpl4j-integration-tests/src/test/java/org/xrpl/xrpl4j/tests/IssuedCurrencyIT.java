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
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.PathStep;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.util.List;

public class IssuedCurrencyIT extends AbstractIT {

  @Test
  public void issueIssuedCurrencyBalance() throws JsonRpcClientErrorException {
    ///////////////////////////
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
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Send some xrpl4jCoin to the counterparty account.
    sendIssuedCurrency(
      xrpl4jCoin,
      trustLine.limitPeer(),
      issuerWallet,
      counterpartyWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(
      () -> getValidatedAccountLines(issuerWallet.classicAddress(), counterpartyWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + trustLine.limitPeer()))
    );

    ///////////////////////////
    // We can also retrieve the currencies that the counterparty can send/recieve using the accountCurrencies
    // method.
    AccountCurrenciesResult counterpartyCurrencies = xrplClient.accountCurrencies(
      AccountCurrenciesRequestParams.builder()
        .account(counterpartyWallet.classicAddress())
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(counterpartyCurrencies.sendCurrencies()).asList().containsOnly(xrpl4jCoin);
    assertThat(counterpartyCurrencies.ledgerCurrentIndex()).isNotEmpty().get()
      .isEqualTo(counterpartyCurrencies.ledgerCurrentIndexSafe());
  }

  @Test
  public void sendSimpleRipplingIssuedCurrencyPayment() throws JsonRpcClientErrorException {
    ///////////////////////////
    // Create a gateway (issuer) account and two normal accounts
    Wallet issuerWallet = createRandomAccount();
    Wallet aliceWallet = createRandomAccount();
    Wallet bobWallet = createRandomAccount();

    ///////////////////////////
    // Set the DefaultRipple account flag on the issuer wallet, so that
    // its TrustLines allow rippling
    FeeResult feeResult = xrplClient.fee();
    setDefaultRipple(issuerWallet, feeResult);

    ///////////////////////////
    // Create a TrustLine between alice and the issuer
    TrustLine aliceTrustLine = createTrustLine(
      "USD",
      "10000",
      issuerWallet,
      aliceWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a TrustLine between bob and the issuer
    TrustLine bobTrustLine = createTrustLine(
      "USD",
      "10000",
      issuerWallet,
      bobWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issuer issues 50 USD to alice
    sendIssuedCurrency("USD", "50", issuerWallet, aliceWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issuer issues 50 USD to bob
    sendIssuedCurrency("USD", "50", issuerWallet, bobWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Try to find a path for this Payment.
    IssuedCurrencyAmount pathDestinationAmount = IssuedCurrencyAmount.builder()
      .issuer(issuerWallet.classicAddress())
      .currency(bobTrustLine.currency())
      .value("10")
      .build();

    ///////////////////////////
    // Validate that there exists a path such that this Payment can succeed.
    // Note that because this path consists of all implied paths, rippled will not return any path steps
    scanForResult(
      () -> getValidatedRipplePath(aliceWallet, bobWallet, pathDestinationAmount),
      path -> path.alternatives().size() > 0 // rippled will return a PathAlternative without any path steps
    );

    ///////////////////////////
    // Send 10 USD from Alice to Bob by rippling through the issuer
    AccountInfoResult aliceAccountInfo = getValidatedAccountInfo(aliceWallet.classicAddress());
    Payment aliceToBobPayment = Payment.builder()
      .account(aliceWallet.classicAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(aliceAccountInfo.accountData().sequence())
      .destination(bobWallet.classicAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerWallet.classicAddress())
        .currency("USD")
        .value("10")
        .build())
      .signingPublicKey(aliceWallet.publicKey())
      .build();

    SubmitResult<Payment> paymentResult = xrplClient.submit(aliceWallet, aliceToBobPayment);
    assertThat(paymentResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(paymentResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(paymentResult.transactionResult().hash());

    logInfo(
      paymentResult.transactionResult().transaction().transactionType(),
      paymentResult.transactionResult().hash()
    );

    ///////////////////////////
    // Validate that bob and alice's trust line balances have been updated appropriately
    scanForResult(
      () -> getValidatedAccountLines(aliceWallet.classicAddress(), issuerWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("40"))
    );

    scanForResult(
      () -> getValidatedAccountLines(bobWallet.classicAddress(), issuerWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("60"))
    );
  }

  @Test
  @Disabled
  public void sendMultiHopSameCurrencyPayment() throws JsonRpcClientErrorException {
    ///////////////////////////
    // Create two issuer wallets and three non-issuer wallets
    final Wallet issuerAWallet = createRandomAccount();
    final Wallet issuerBWallet = createRandomAccount();
    final Wallet charlieWallet = createRandomAccount();
    final Wallet emilyWallet = createRandomAccount();
    final Wallet danielWallet = createRandomAccount();

    ///////////////////////////
    // Set the lsfDefaultRipple AccountRoot flag so that all trustlines in this topography allow rippling
    FeeResult feeResult = xrplClient.fee();
    setDefaultRipple(issuerAWallet, feeResult);
    setDefaultRipple(issuerBWallet, feeResult);
    setDefaultRipple(charlieWallet, feeResult);
    setDefaultRipple(emilyWallet, feeResult);
    setDefaultRipple(danielWallet, feeResult);

    ///////////////////////////
    // Create a Trustline between charlie and issuerA
    final TrustLine charlieTrustLineWithIssuerA = createTrustLine(
      "USD",
      "10000",
      issuerAWallet,
      charlieWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerA
    final TrustLine emilyTrustLineWithIssuerA = createTrustLine(
      "USD",
      "10000",
      issuerAWallet,
      emilyWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerB
    final TrustLine emilyTrustLineWithIssuerB = createTrustLine(
      "USD",
      "10000",
      issuerBWallet,
      emilyWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between daniel and issuerB
    final TrustLine danielTrustLineWithIssuerB = createTrustLine(
      "USD",
      "10000",
      issuerBWallet,
      danielWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issue 10 USD from issuerA to charlie.
    // IssuerA now owes Charlie 10 USD.
    sendIssuedCurrency(
      "USD", "10", issuerAWallet, charlieWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issue 1 USD from issuerA to emily.
    // IssuerA now owes Emily 1 USD
    sendIssuedCurrency(
      "USD", "1", issuerAWallet, emilyWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issue 100 USD from issuerB to emily.
    // IssuerB now owes Emily 100 USD
    sendIssuedCurrency(
      "USD", "100", issuerBWallet, emilyWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issue 2 USD from issuerB to daniel.
    // IssuerB now owes Daniel 2 USD
    sendIssuedCurrency(
      "USD", "2", issuerBWallet, danielWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Look for a payment path from charlie to daniel.
    List<List<PathStep>> pathSteps = scanForResult(
      () -> getValidatedRipplePath(charlieWallet, danielWallet, IssuedCurrencyAmount.builder()
        .issuer(issuerBWallet.classicAddress())
        .currency(charlieTrustLineWithIssuerA.currency())
        .value("10")
        .build()),
      path -> path.alternatives().size() > 0
    )
      .alternatives().stream()
      .filter(alt ->
        !alt.pathsComputed().isEmpty() &&
          alt.sourceAmount().equals(
            IssuedCurrencyAmount.builder()
              .issuer(charlieWallet.classicAddress())
              .currency(charlieTrustLineWithIssuerA.currency())
              .value("10")
              .build()
          )
      )
      .findFirst().orElseThrow(() -> new RuntimeException("No path found."))
      .pathsComputed();


    ///////////////////////////
    // Send a Payment from charlie to Daniel using the previously found paths.
    AccountInfoResult charlieAccountInfo = getValidatedAccountInfo(charlieWallet.classicAddress());
    Payment charlieToDanielPayment = Payment.builder()
      .account(charlieWallet.classicAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(charlieAccountInfo.accountData().sequence())
      .destination(danielWallet.classicAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerBWallet.classicAddress())
        .currency(danielTrustLineWithIssuerB.currency())
        .value("10")
        .build())
      .paths(pathSteps)
      .signingPublicKey(charlieWallet.publicKey())
      .build();

    SubmitResult<Payment> paymentResult = xrplClient.submit(charlieWallet, charlieToDanielPayment);
    assertThat(paymentResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(paymentResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(paymentResult.transactionResult().hash());

    logInfo(
      paymentResult.transactionResult().transaction().transactionType(),
      paymentResult.transactionResult().hash()
    );

    ///////////////////////////
    // Validate that everyone's trust line balances have been updated appropriately
    scanForResult(
      () -> getValidatedAccountLines(charlieWallet.classicAddress(), issuerAWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("0"))
    );

    scanForResult(
      () -> getValidatedAccountLines(emilyWallet.classicAddress(), issuerAWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("11"))
    );

    scanForResult(
      () -> getValidatedAccountLines(emilyWallet.classicAddress(), issuerBWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("90"))
    );

    scanForResult(
      () -> getValidatedAccountLines(danielWallet.classicAddress(), issuerBWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("12"))
    );

  }

  /**
   * Set the {@code lsfDefaultRipple} flag on an issuer account.
   *
   * @param issuerWallet The {@link Wallet} containing the address of the issuer account.
   * @param feeResult    The current {@link FeeResult}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void setDefaultRipple(Wallet issuerWallet, FeeResult feeResult) throws JsonRpcClientErrorException {
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerWallet.classicAddress())
    );

    AccountSet setDefaultRipple = AccountSet.builder()
      .account(issuerWallet.classicAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerWallet.publicKey())
      .setFlag(AccountSet.AccountSetFlag.DEFAULT_RIPPLE)
      .build();

    SubmitResult<AccountSet> setResult = xrplClient.submit(issuerWallet, setDefaultRipple);
    assertThat(setResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(setResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(setResult.transactionResult().hash());

    logInfo(
      setResult.transactionResult().transaction().transactionType(),
      setResult.transactionResult().hash()
    );

    scanForResult(
      () -> getValidatedAccountInfo(issuerWallet.classicAddress()),
      info -> info.accountData().flags().lsfDefaultRipple()
    );
  }

}
