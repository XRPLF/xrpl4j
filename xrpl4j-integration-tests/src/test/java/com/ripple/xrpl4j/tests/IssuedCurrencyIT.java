package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.ripple.xrpl4j.model.client.accounts.AccountInfoResult;
import com.ripple.xrpl4j.model.client.accounts.TrustLine;
import com.ripple.xrpl4j.model.client.fees.FeeResult;
import com.ripple.xrpl4j.model.client.transactions.SubmitResult;
import com.ripple.xrpl4j.client.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.PathStep;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.TrustSet;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import org.junit.jupiter.api.Test;

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

    TrustLine trustLine = createTrustLine(xrpl4jCoin, "10000", issuerWallet, counterpartyWallet, feeResult.drops().minimumFee());

    ///////////////////////////
    // Send some xrpl4jCoin to the counterparty account.
    issueBalance(xrpl4jCoin, trustLine.limitPeer(), issuerWallet, counterpartyWallet, feeResult.drops().minimumFee());

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(
      () -> getValidatedAccountLines(issuerWallet.classicAddress(), counterpartyWallet.classicAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + trustLine.limitPeer()))
    );
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
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Create a TrustLine between bob and the issuer
    TrustLine bobTrustLine = createTrustLine(
      "USD",
      "10000",
      issuerWallet,
      bobWallet,
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Issuer issues 50 USD to alice
    issueBalance("USD", "50", issuerWallet, aliceWallet, feeResult.drops().minimumFee());

    ///////////////////////////
    // Issuer issues 50 USD to bob
    issueBalance("USD", "50", issuerWallet, bobWallet, feeResult.drops().minimumFee());

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
      .fee(feeResult.drops().minimumFee())
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
    assertThat(paymentResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" + paymentResult.transaction().hash()
        .orElse("n/a")
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
  public void sendMultiHopSameCurrencyPayment() throws JsonRpcClientErrorException {
    ///////////////////////////
    // Create two issuer wallets and three non-issuer wallets
    Wallet issuerAWallet = createRandomAccount();
    Wallet issuerBWallet = createRandomAccount();
    Wallet charlieWallet = createRandomAccount();
    Wallet emilyWallet = createRandomAccount();
    Wallet danielWallet = createRandomAccount();

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
    TrustLine charlieTrustLineWithIssuerA = createTrustLine(
      "USD",
      "10000",
      issuerAWallet,
      charlieWallet,
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerA
    TrustLine emilyTrustLineWithIssuerA = createTrustLine(
      "USD",
      "10000",
      issuerAWallet,
      emilyWallet,
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerB
    TrustLine emilyTrustLineWithIssuerB = createTrustLine(
      "USD",
      "10000",
      issuerBWallet,
      emilyWallet,
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Create a Trustline between daniel and issuerB
    TrustLine danielTrustLineWithIssuerB = createTrustLine(
      "USD",
      "10000",
      issuerBWallet,
      danielWallet,
      feeResult.drops().minimumFee()
    );

    ///////////////////////////
    // Issue 10 USD from issuerA to charlie.
    // IssuerA now owes Charlie 10 USD.
    issueBalance("USD", "10", issuerAWallet, charlieWallet, feeResult.drops().minimumFee());

    ///////////////////////////
    // Issue 1 USD from issuerA to emily.
    // IssuerA now owes Emily 1 USD
    issueBalance("USD", "1", issuerAWallet, emilyWallet, feeResult.drops().minimumFee());

    ///////////////////////////
    // Issue 100 USD from issuerB to emily.
    // IssuerB now owes Emily 100 USD
    issueBalance("USD", "100", issuerBWallet, emilyWallet, feeResult.drops().minimumFee());

    ///////////////////////////
    // Issue 2 USD from issuerB to daniel.
    // IssuerB now owes Daniel 2 USD
    issueBalance("USD", "2", issuerBWallet, danielWallet, feeResult.drops().minimumFee());

    ///////////////////////////
    // Look for a payment path from charlie to daniel.
    List<List<PathStep>> pathSteps  = scanForResult(
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
      .fee(feeResult.drops().minimumFee())
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
    assertThat(paymentResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" + paymentResult.transaction().hash()
        .orElse("n/a")
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

  public void setDefaultRipple(Wallet issuerWallet, FeeResult feeResult) throws JsonRpcClientErrorException {
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerWallet.classicAddress())
    );

    AccountSet setDefaultRipple = AccountSet.builder()
      .account(issuerWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerWallet.publicKey())
      .setFlag(AccountSet.AccountSetFlag.DEFAULT_RIPPLE)
      .build();

    SubmitResult<AccountSet> setResult = xrplClient.submit(issuerWallet, setDefaultRipple);
    assertThat(setResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/" + setResult.transaction().hash()
        .orElse("n/a")
    );

    scanForResult(
      () -> getValidatedAccountInfo(issuerWallet.classicAddress()),
      info -> info.accountData().flags().lsfDefaultRipple()
    );
  }

  public void issueBalance(
    String currency,
    String value,
    Wallet issuerWallet,
    Wallet counterpartyWallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException {
    ///////////////////////////
    // Issuer sends a payment with the issued currency to the counterparty
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(issuerWallet.classicAddress())
    );

    Payment fundCounterparty = Payment.builder()
      .account(issuerWallet.classicAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(counterpartyWallet.classicAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerWallet.classicAddress())
        .currency(currency)
        .value(value)
        .build())
      .signingPublicKey(issuerWallet.publicKey())
      .build();

    SubmitResult<Payment> paymentResult = xrplClient.submit(issuerWallet, fundCounterparty);
    assertThat(paymentResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" + paymentResult.transaction().hash()
        .orElse("n/a")
    );

    this.scanForResult(
      () -> getValidatedTransaction(
        paymentResult.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Cannot look up transaction because no hash was returned.")),
        Payment.class)
    );

  }

  public TrustLine createTrustLine(
    String currency,
    String value,
    Wallet issuerWallet,
    Wallet counterpartyWallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException {
    AccountInfoResult counterpartyAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartyWallet.classicAddress())
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartyWallet.classicAddress())
      .fee(fee)
      .sequence(counterpartyAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(currency)
        .issuer(issuerWallet.classicAddress())
        .value(value)
        .build())
      .signingPublicKey(counterpartyWallet.publicKey())
      .build();

    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(counterpartyWallet, trustSet);
    assertThat(trustSetSubmitResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/" + trustSetSubmitResult.transaction().hash()
        .orElse("n/a")
    );

    return scanForResult(
      () ->
        getValidatedAccountLines(issuerWallet.classicAddress(), counterpartyWallet.classicAddress()),
      linesResult -> !linesResult.lines().isEmpty()
    )
      .lines().get(0);
  }

}
