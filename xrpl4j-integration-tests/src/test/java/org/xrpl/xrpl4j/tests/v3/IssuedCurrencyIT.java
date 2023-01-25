package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
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
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.TestnetEnvironment;

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
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair counterpartyKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      IssuedCurrencyAmount.MAX_VALUE,
      issuerKeyPair,
      counterpartyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("9999999999999999e80");
  }

  @Test
  void createTrustlineWithMaxLimitMinusOneExponent() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair counterpartyKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      new BigDecimal(IssuedCurrencyAmount.MAX_VALUE).scaleByPowerOfTen(-1).toEngineeringString(),
      issuerKeyPair,
      counterpartyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("9999999999999999e79");
  }

  @Test
  void createTrustlineWithSmallestPositiveLimit() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair counterpartyKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      IssuedCurrencyAmount.MIN_POSITIVE_VALUE,
      issuerKeyPair,
      counterpartyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("1000000000000000e-96");
  }

  @Test
  void createTrustlineWithSmalletPositiveLimitPlusOne() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair counterpartyKeyPair = createRandomAccountEd25519();

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
      issuerKeyPair,
      counterpartyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    assertThat(trustLine.limitPeer()).isEqualTo("1100000000000000e-96");
  }

  @Test
  public void issueIssuedCurrencyBalance() throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair counterpartyKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty denominated in a custom currency
    // by submitting a TrustSet transaction
    String xrpl4jCoin = Strings.padEnd(BaseEncoding.base16().encode("xrpl4jCoin".getBytes()), 40, '0');

    TrustLine trustLine = createTrustLine(
      xrpl4jCoin,
      "10000",
      issuerKeyPair,
      counterpartyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Send some xrpl4jCoin to the counterparty account.
    issueBalance(
      xrpl4jCoin, trustLine.limitPeer(), issuerKeyPair, counterpartyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(() -> getValidatedAccountLines(issuerKeyPair.publicKey().deriveAddress(),
        counterpartyKeyPair.publicKey().deriveAddress()),
      linesResult -> linesResult.lines().stream().anyMatch(line -> line.balance().equals("-" + trustLine.limitPeer()))
    );

    ///////////////////////////
    // We can also retrieve the currencies that the counterparty can send/recieve using the accountCurrencies
    // method.
    AccountCurrenciesResult counterpartyCurrencies = xrplClient.accountCurrencies(
      AccountCurrenciesRequestParams.builder()
        .account(counterpartyKeyPair.publicKey().deriveAddress())
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
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair aliceKeyPair = createRandomAccountEd25519();
    KeyPair bobKeyPair = createRandomAccountEd25519();

    ///////////////////////////
    // Set the DefaultRipple account flag on the issuer wallet, so that
    // its TrustLines allow rippling
    FeeResult feeResult = xrplClient.fee();
    setDefaultRipple(issuerKeyPair, feeResult);

    ///////////////////////////
    // Create a TrustLine between alice and the issuer
    createTrustLine(
      "USD",
      "10000",
      issuerKeyPair,
      aliceKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a TrustLine between bob and the issuer
    TrustLine bobTrustLine = createTrustLine(
      "USD",
      "10000",
      issuerKeyPair,
      bobKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issuer issues 50 USD to alice
    issueBalance("USD", "50", issuerKeyPair, aliceKeyPair, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issuer issues 50 USD to bob
    issueBalance("USD", "50", issuerKeyPair, bobKeyPair, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Try to find a path for this Payment.
    IssuedCurrencyAmount pathDestinationAmount = IssuedCurrencyAmount.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(bobTrustLine.currency())
      .value("10")
      .build();

    ///////////////////////////
    // Validate that there exists a path such that this Payment can succeed.
    // Note that because this path consists of all implied paths, rippled will not return any path steps
    scanForResult(
      () -> getValidatedRipplePath(aliceKeyPair, bobKeyPair, pathDestinationAmount),
      path -> path.alternatives().size() > 0 // rippled will return a PathAlternative without any path steps
    );

    ///////////////////////////
    // Send 10 USD from Alice to Bob by rippling through the issuer
    AccountInfoResult aliceAccountInfo = getValidatedAccountInfo(aliceKeyPair.publicKey().deriveAddress());
    Payment aliceToBobPayment = Payment.builder()
      .account(aliceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(aliceAccountInfo.accountData().sequence())
      .destination(bobKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("10")
        .build())
      .signingPublicKey(aliceKeyPair.publicKey().base16Value())
      .build();

    SingleSignedTransaction<Payment> signedAliceToBobPayment = signatureService.sign(
      aliceKeyPair.privateKey(), aliceToBobPayment
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
      () -> getValidatedAccountLines(aliceKeyPair.publicKey().deriveAddress(),
        issuerKeyPair.publicKey().deriveAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("40"))
    );

    scanForResult(
      () -> getValidatedAccountLines(bobKeyPair.publicKey().deriveAddress(), issuerKeyPair.publicKey().deriveAddress()),
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
    // NOTE: Only run this on non-testnet and non-devnet evironmens.
    if (TestnetEnvironment.class.isAssignableFrom(xrplEnvironment.getClass())) {
      return;
    }

    ///////////////////////////
    // Create two issuer wallets and three non-issuer wallets
    final KeyPair issuerAKeyPair = createRandomAccountEd25519();
    final KeyPair issuerBKeyPair = createRandomAccountEd25519();
    final KeyPair charlieKeyPair = createRandomAccountEd25519();
    final KeyPair danielKeyPair = createRandomAccountEd25519();
    final KeyPair emilyKeyPair = createRandomAccountEd25519();

    ///////////////////////////
    // Set the lsfDefaultRipple AccountRoot flag so that all trustlines in this topography allow rippling
    FeeResult feeResult = xrplClient.fee();
    setDefaultRipple(issuerAKeyPair, feeResult);
    setDefaultRipple(issuerBKeyPair, feeResult);
    setDefaultRipple(charlieKeyPair, feeResult);
    setDefaultRipple(danielKeyPair, feeResult);
    setDefaultRipple(emilyKeyPair, feeResult);

    ///////////////////////////
    // Create a Trustline between charlie and issuerA
    final TrustLine charlieTrustLineWithIssuerA = createTrustLine(
      "USD",
      "10000",
      issuerAKeyPair,
      charlieKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerA
    createTrustLine(
      "USD",
      "10000",
      issuerAKeyPair,
      emilyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between emily and issuerB
    createTrustLine(
      "USD",
      "10000",
      issuerBKeyPair,
      emilyKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Create a Trustline between daniel and issuerB
    final TrustLine danielTrustLineWithIssuerB = createTrustLine(
      "USD",
      "10000",
      issuerBKeyPair,
      danielKeyPair,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Issue 10 USD from issuerA to charlie.
    // IssuerA now owes Charlie 10 USD.
    issueBalance("USD", "10", issuerAKeyPair, charlieKeyPair, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issue 1 USD from issuerA to emily.
    // IssuerA now owes Emily 1 USD
    issueBalance("USD", "1", issuerAKeyPair, emilyKeyPair, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issue 100 USD from issuerB to emily.
    // IssuerB now owes Emily 100 USD
    issueBalance("USD", "100", issuerBKeyPair, emilyKeyPair, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Issue 2 USD from issuerB to daniel.
    // IssuerB now owes Daniel 2 USD
    issueBalance("USD", "2", issuerBKeyPair, danielKeyPair, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    ///////////////////////////
    // Look for a payment path from charlie to daniel.
    List<List<PathStep>> pathSteps = scanForResult(
      () -> getValidatedRipplePath(charlieKeyPair, danielKeyPair, IssuedCurrencyAmount.builder()
        .issuer(issuerBKeyPair.publicKey().deriveAddress())
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
              .issuer(charlieKeyPair.publicKey().deriveAddress())
              .currency(charlieTrustLineWithIssuerA.currency())
              .value("10")
              .build()
          )
      )
      .findFirst().orElseThrow(() -> new RuntimeException("No path found."))
      .pathsComputed();

    ///////////////////////////
    // Send a Payment from charlie to Daniel using the previously found paths.
    AccountInfoResult charlieAccountInfo = getValidatedAccountInfo(charlieKeyPair.publicKey().deriveAddress());
    Payment charlieToDanielPayment = Payment.builder()
      .account(charlieKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(charlieAccountInfo.accountData().sequence())
      .destination(danielKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerBKeyPair.publicKey().deriveAddress())
        .currency(danielTrustLineWithIssuerB.currency())
        .value("10")
        .build())
      .paths(pathSteps)
      .signingPublicKey(charlieKeyPair.publicKey().base16Value())
      .build();

    SingleSignedTransaction<Payment> signedCharlieToDanielPayment = signatureService.sign(
      charlieKeyPair.privateKey(), charlieToDanielPayment
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
      () -> getValidatedAccountLines(charlieKeyPair.publicKey().deriveAddress(),
        issuerAKeyPair.publicKey().deriveAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("0"))
    );

    scanForResult(
      () -> getValidatedAccountLines(emilyKeyPair.publicKey().deriveAddress(),
        issuerAKeyPair.publicKey().deriveAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("11"))
    );

    scanForResult(
      () -> getValidatedAccountLines(emilyKeyPair.publicKey().deriveAddress(),
        issuerBKeyPair.publicKey().deriveAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("90"))
    );

    scanForResult(
      () -> getValidatedAccountLines(danielKeyPair.publicKey().deriveAddress(),
        issuerBKeyPair.publicKey().deriveAddress()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("12"))
    );

  }

  /**
   * Set the {@code lsfDefaultRipple} flag on an issuer account.
   *
   * @param issuerKeyPair The {@link KeyPair} containing the address of the issuer account.
   * @param feeResult     The current {@link FeeResult}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void setDefaultRipple(KeyPair issuerKeyPair, FeeResult feeResult)
    throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet setDefaultRipple = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey().base16Value())
      .setFlag(AccountSet.AccountSetFlag.DEFAULT_RIPPLE)
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      issuerKeyPair.privateKey(), setDefaultRipple
    );
    SubmitResult<AccountSet> setResult = xrplClient.submit(signedAccountSet);
    assertThat(setResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      setResult.transactionResult().hash()
    );

    scanForResult(
      () -> getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress()),
      info -> info.accountData().flags().lsfDefaultRipple()
    );
  }

  /**
   * Send issued currency funds from an issuer to a counterparty.
   *
   * @param currency            The currency code to send.
   * @param value               The amount of currency to send.
   * @param issuerKeyPair       The {@link KeyPair} of the issuer account.
   * @param counterpartyKeyPair The {@link KeyPair} of the counterparty account.
   * @param fee                 The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void issueBalance(
    String currency,
    String value,
    KeyPair issuerKeyPair,
    KeyPair counterpartyKeyPair,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    ///////////////////////////
    // Issuer sends a payment with the issued currency to the counterparty
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    Payment fundCounterparty = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(counterpartyKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency(currency)
        .value(value)
        .build())
      .signingPublicKey(issuerKeyPair.publicKey().base16Value())
      .build();

    SingleSignedTransaction<Payment> signedFundCounterparty = signatureService.sign(
      issuerKeyPair.privateKey(), fundCounterparty
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
   * @param currency            The currency code of the trustline to create.
   * @param value               The trustline limit of the trustline to create.
   * @param issuerKeyPair       The {@link KeyPair} of the issuer account.
   * @param counterpartyKeyPair The {@link KeyPair} of the counterparty account.
   * @param fee                 The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @return The {@link TrustLine} that gets created.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public TrustLine createTrustLine(
    String currency,
    String value,
    KeyPair issuerKeyPair,
    KeyPair counterpartyKeyPair,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult counterpartyAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartyKeyPair.publicKey().deriveAddress())
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartyKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(counterpartyAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(currency)
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value(value)
        .build())
      .signingPublicKey(counterpartyKeyPair.publicKey().base16Value())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(counterpartyKeyPair.privateKey(),
      trustSet);
    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetSubmitResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      trustSetSubmitResult.transactionResult().hash()
    );

    return scanForResult(
      () ->
        getValidatedAccountLines(issuerKeyPair.publicKey().deriveAddress(),
          counterpartyKeyPair.publicKey().deriveAddress()),
      linesResult -> !linesResult.lines().isEmpty()
    )
      .lines().get(0);
  }

}
