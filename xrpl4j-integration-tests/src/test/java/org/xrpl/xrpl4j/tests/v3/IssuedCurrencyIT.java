package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeFees;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.PathStep;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.List;

/**
 * An Integration Test to validate submission of issued currency transactions.
 */
public class IssuedCurrencyIT extends AbstractIT {

  // Test cases: MAX, MAX - 1, MAX + 1 (fail), MIN, MIN + 1, MIN - 1 (fail), 0, 1, -1
  @Test
  void createTrustlineWithMaxLimit() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    Wallet issuerWallet = createRandomAccountEd25519();
    Wallet counterpartyWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      IssuedCurrencyAmount.MAX_VALUE,
      issuerWallet,
      counterpartyWallet,
      computeFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("9999999999999999e80");
  }

  @Test
  void createTrustlineWithMaxLimitMinusOneExponent() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    Wallet issuerWallet = createRandomAccountEd25519();
    Wallet counterpartyWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      new BigDecimal(IssuedCurrencyAmount.MAX_VALUE).scaleByPowerOfTen(-1).toEngineeringString(),
      issuerWallet,
      counterpartyWallet,
      computeFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("9999999999999999e79");
  }

  @Test
  void createTrustlineWithSmallestPositiveLimit() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    Wallet issuerWallet = createRandomAccountEd25519();
    Wallet counterpartyWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      IssuedCurrencyAmount.MIN_POSITIVE_VALUE,
      issuerWallet,
      counterpartyWallet,
      computeFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("1000000000000000e-96");
  }

  @Test
  void createTrustlineWithSmalletPositiveLimitPlusOne() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    Wallet issuerWallet = createRandomAccountEd25519();
    Wallet counterpartyWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    BigDecimal limitValue = new BigDecimal(IssuedCurrencyAmount.MIN_POSITIVE_VALUE)
      .add(new BigDecimal(IssuedCurrencyAmount.MIN_POSITIVE_VALUE).scaleByPowerOfTen(-1));
    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      limitValue.toString(),
      issuerWallet,
      counterpartyWallet,
      computeFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("1100000000000000e-96");
  }

  @Test
  public void issueIssuedCurrencyBalance() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    Wallet issuerWallet = createRandomAccountEd25519();
    Wallet counterpartyWallet = createRandomAccountEd25519();

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
      computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Send some xrpl4jCoin to the counterparty account.
    issueBalance(
      xrpl4jCoin, trustLine.limitPeer(), issuerWallet, counterpartyWallet, computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(() -> getValidatedAccountLines(issuerWallet.address(), counterpartyWallet.address()),
      linesResult -> linesResult.lines().stream().anyMatch(line -> line.balance().equals("-" + trustLine.limitPeer()))
    );

    ///////////////////////////
    // We can also retrieve the currencies that the counterparty can send/recieve using the accountCurrencies
    // method.
    AccountCurrenciesResult counterpartyCurrencies = xrplClient.accountCurrencies(
      AccountCurrenciesRequestParams.builder()
        .account(counterpartyWallet.address())
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(counterpartyCurrencies.sendCurrencies()).asList().containsOnly(xrpl4jCoin);
    assertThat(counterpartyCurrencies.ledgerCurrentIndex()).isNotEmpty().get()
      .isEqualTo(counterpartyCurrencies.ledgerCurrentIndexSafe());
  }

  @Test
  public void sendSimpleRipplingIssuedCurrencyPayment() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create a gateway (issuer) account and two normal accounts
    Wallet issuerWallet = createRandomAccountEd25519();
    Wallet aliceWallet = createRandomAccountEd25519();
    Wallet bobWallet = createRandomAccountEd25519();

    ///////////////////////////
    // Set the DefaultRipple account flag on the issuer wallet, so that
    // its TrustLines allow rippling
    FeeResult feeResult = xrplClient.fee();
    setDefaultRipple(issuerWallet, feeResult);

    ///////////////////////////
    // Create a TrustLine between alice and the issuer
    createTrustLine(
      "USD",
      "10000",
      issuerWallet,
      aliceWallet,
      computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a TrustLine between bob and the issuer
    TrustLine bobTrustLine = createTrustLine(
      "USD",
      "10000",
      issuerWallet,
      bobWallet,
      computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issuer issues 50 USD to alice
    issueBalance("USD", "50", issuerWallet, aliceWallet, computeFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issuer issues 50 USD to bob
    issueBalance("USD", "50", issuerWallet, bobWallet, computeFees(feeResult).recommendedFee());

    ///////////////////////////
    // Try to find a path for this Payment.
    IssuedCurrencyAmount pathDestinationAmount = IssuedCurrencyAmount.builder()
      .issuer(issuerWallet.address())
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
    AccountInfoResult aliceAccountInfo = getValidatedAccountInfo(aliceWallet.address());
    Payment aliceToBobPayment = Payment.builder()
      .account(aliceWallet.address())
      .fee(computeFees(feeResult).recommendedFee())
      .sequence(aliceAccountInfo.accountData().sequence())
      .destination(bobWallet.address())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerWallet.address())
        .currency("USD")
        .value("10")
        .build())
      .signingPublicKey(aliceWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<Payment> signedAliceToBobPayment = signatureService.sign(
      aliceWallet.privateKey(), aliceToBobPayment
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedAliceToBobPayment);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/{}",
      paymentResult.transactionResult().hash()
    );

    ///////////////////////////
    // Validate that bob and alice's trust line balances have been updated appropriately
    scanForResult(
      () -> getValidatedAccountLines(aliceWallet.address(), issuerWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("40"))
    );

    scanForResult(
      () -> getValidatedAccountLines(bobWallet.address(), issuerWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("60"))
    );
  }

  /**
   * Send and verify a multi-hop payment that transits the following path.
   *
   * <pre>
   * ┌────────────┐         ┌────────────┐        ┌────────────┐         ┌────────────┐        ┌────────────┐
   * │  Charlie   │◁───TL──▷│  IssuerA   │◁──TL──▷│   Emily    │◁──TL───▷│  IssuerB   │◁──TL──▷│   Daniel   │
   * └────────────┘         └────────────┘        └────────────┘         └────────────┘        └────────────┘
   * </pre>
   */
  @Test
  public void sendMultiHopSameCurrencyPayment() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create two issuer wallets and three non-issuer wallets
    final Wallet issuerAWallet = createRandomAccountEd25519();
    final Wallet issuerBWallet = createRandomAccountEd25519();
    final Wallet charlieWallet = createRandomAccountEd25519();
    final Wallet danielWallet = createRandomAccountEd25519();
    final Wallet emilyWallet = createRandomAccountEd25519();

    ///////////////////////////
    // Set the lsfDefaultRipple AccountRoot flag so that all trustlines in this topography allow rippling
    FeeResult feeResult = xrplClient.fee();
    setDefaultRipple(issuerAWallet, feeResult);
    setDefaultRipple(issuerBWallet, feeResult);
    setDefaultRipple(charlieWallet, feeResult);
    setDefaultRipple(danielWallet, feeResult);
    setDefaultRipple(emilyWallet, feeResult);

    ///////////////////////////
    // Create a Trustline between charlie and issuerA
    final TrustLine charlieTrustLineWithIssuerA = createTrustLine(
      "USD",
      "10000",
      issuerAWallet,
      charlieWallet,
      computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerA
    createTrustLine(
      "USD",
      "10000",
      issuerAWallet,
      emilyWallet,
      computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerB
    createTrustLine(
      "USD",
      "10000",
      issuerBWallet,
      emilyWallet,
      computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between daniel and issuerB
    final TrustLine danielTrustLineWithIssuerB = createTrustLine(
      "USD",
      "10000",
      issuerBWallet,
      danielWallet,
      computeFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issue 10 USD from issuerA to charlie.
    // IssuerA now owes Charlie 10 USD.
    issueBalance("USD", "10", issuerAWallet, charlieWallet, computeFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issue 1 USD from issuerA to emily.
    // IssuerA now owes Emily 1 USD
    issueBalance("USD", "1", issuerAWallet, emilyWallet, computeFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issue 100 USD from issuerB to emily.
    // IssuerB now owes Emily 100 USD
    issueBalance("USD", "100", issuerBWallet, emilyWallet, computeFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issue 2 USD from issuerB to daniel.
    // IssuerB now owes Daniel 2 USD
    issueBalance("USD", "2", issuerBWallet, danielWallet, computeFees(feeResult).recommendedFee());

    ///////////////////////////
    // Look for a payment path from charlie to daniel.
    List<List<PathStep>> pathSteps = scanForResult(
      () -> getValidatedRipplePath(charlieWallet, danielWallet, IssuedCurrencyAmount.builder()
        .issuer(issuerBWallet.address())
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
              .issuer(charlieWallet.address())
              .currency(charlieTrustLineWithIssuerA.currency())
              .value("10")
              .build()
          )
      )
      .findFirst().orElseThrow(() -> new RuntimeException("No path found."))
      .pathsComputed();

    ///////////////////////////
    // Send a Payment from charlie to Daniel using the previously found paths.
    AccountInfoResult charlieAccountInfo = getValidatedAccountInfo(charlieWallet.address());
    Payment charlieToDanielPayment = Payment.builder()
      .account(charlieWallet.address())
      .fee(computeFees(feeResult).recommendedFee())
      .sequence(charlieAccountInfo.accountData().sequence())
      .destination(danielWallet.address())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerBWallet.address())
        .currency(danielTrustLineWithIssuerB.currency())
        .value("10")
        .build())
      .paths(pathSteps)
      .signingPublicKey(charlieWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<Payment> signedCharlieToDanielPayment = signatureService.sign(
      charlieWallet.privateKey(), charlieToDanielPayment
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedCharlieToDanielPayment);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/{}",
      paymentResult.transactionResult().hash()
    );

    ///////////////////////////
    // Validate that everyone's trust line balances have been updated appropriately
    scanForResult(
      () -> getValidatedAccountLines(charlieWallet.address(), issuerAWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("0"))
    );

    scanForResult(
      () -> getValidatedAccountLines(emilyWallet.address(), issuerAWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("11"))
    );

    scanForResult(
      () -> getValidatedAccountLines(emilyWallet.address(), issuerBWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("90"))
    );

    scanForResult(
      () -> getValidatedAccountLines(danielWallet.address(), issuerBWallet.address()),
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
  public void setDefaultRipple(Wallet issuerWallet, FeeResult feeResult)
    throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerWallet.address())
    );

    AccountSet setDefaultRipple = AccountSet.builder()
      .account(issuerWallet.address())
      .fee(computeFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerWallet.publicKey().base16Value())
      .setFlag(AccountSet.AccountSetFlag.DEFAULT_RIPPLE)
      .build();

    SingleSingedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      issuerWallet.privateKey(), setDefaultRipple
    );
    SubmitResult<AccountSet> setResult = xrplClient.submit(signedAccountSet);
    assertThat(setResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      setResult.transactionResult().hash()
    );

    scanForResult(
      () -> getValidatedAccountInfo(issuerWallet.address()),
      info -> info.accountData().flags().lsfDefaultRipple()
    );
  }

  /**
   * Send issued currency funds from an issuer to a counterparty.
   *
   * @param currency           The currency code to send.
   * @param value              The amount of currency to send.
   * @param issuerWallet       The {@link Wallet} of the issuer account.
   * @param counterpartyWallet The {@link Wallet} of the counterparty account.
   * @param fee                The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void issueBalance(
    String currency,
    String value,
    Wallet issuerWallet,
    Wallet counterpartyWallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Issuer sends a payment with the issued currency to the counterparty
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(issuerWallet.address())
    );

    Payment fundCounterparty = Payment.builder()
      .account(issuerWallet.address())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(counterpartyWallet.address())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerWallet.address())
        .currency(currency)
        .value(value)
        .build())
      .signingPublicKey(issuerWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<Payment> signedFundCounterparty = signatureService.sign(
      issuerWallet.privateKey(), fundCounterparty
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedFundCounterparty);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/{}",
      paymentResult.transactionResult().hash()
    );

    this.scanForResult(() -> getValidatedTransaction(paymentResult.transactionResult().hash(), Payment.class));
  }

  /**
   * Create a trustline between the given issuer and counterparty accounts for the given currency code and with the
   * given limit.
   *
   * @param currency           The currency code of the trustline to create.
   * @param value              The trustline limit of the trustline to create.
   * @param issuerWallet       The {@link Wallet} of the issuer account.
   * @param counterpartyWallet The {@link Wallet} of the counterparty account.
   * @param fee                The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @return The {@link TrustLine} that gets created.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public TrustLine createTrustLine(
    String currency,
    String value,
    Wallet issuerWallet,
    Wallet counterpartyWallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult counterpartyAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartyWallet.address())
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartyWallet.address())
      .fee(fee)
      .sequence(counterpartyAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(currency)
        .issuer(issuerWallet.address())
        .value(value)
        .build())
      .signingPublicKey(counterpartyWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<TrustSet> signedTrustSet = signatureService.sign(counterpartyWallet.privateKey(), trustSet);
    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetSubmitResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      trustSetSubmitResult.transactionResult().hash()
    );

    return scanForResult(
      () ->
        getValidatedAccountLines(issuerWallet.address(), counterpartyWallet.address()),
      linesResult -> !linesResult.lines().isEmpty()
    )
      .lines().get(0);
  }

}
