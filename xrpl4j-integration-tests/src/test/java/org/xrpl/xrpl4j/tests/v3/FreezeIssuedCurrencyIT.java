package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.Flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.flags.Flags.TrustSetFlags.Builder;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.ImmutableAccountSet;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * An Integration Test to validate that an amount of issued currency held by a bad actor can be "frozen."
 */
@SuppressWarnings("deprecation")
public class FreezeIssuedCurrencyIT extends AbstractIT {

  private static final String TEN_THOUSAND = "10000";
  private static final String FIVE_THOUSAND = "5000";
  private static final String ISSUED_CURRENCY_CODE = Strings.padEnd(
    BaseEncoding.base16().encode("usd".getBytes()), 40, '0'
  );

  private Wallet issuerWallet;
  private Wallet badActorWallet;
  private Wallet goodActorWallet;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @BeforeEach
  public void setUp() throws JsonRpcClientErrorException, JsonProcessingException {

    // Create and fund random accounts for this harness.
    issuerWallet = this.createRandomAccountEd25519();
    // This is necessary for non-issuers to be able to send money to other non-issuers.
    this.enableDefaultRipple(issuerWallet);

    badActorWallet = this.createRandomAccountEd25519();
    goodActorWallet = this.createRandomAccountEd25519();
  }

  /**
   * This test creates a Trustline between an issuer and a badActor, issues funds to the badActor, then freezes the
   * funds and validates that the badActor is unable to use those funds.
   *
   * @see "https://xrpl.org/freezes.html#individual-freeze"
   */
  @Test
  public void issueAndFreezeFundsIndividual() throws JsonRpcClientErrorException, JsonProcessingException {
    FeeResult feeResult = xrplClient.fee();

    // Create a Trust Line between issuer and the bad actor.
    TrustLine badActorTrustLine = this.createTrustLine(
      issuerWallet,
      badActorWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );
    assertThat(badActorTrustLine.freeze()).isFalse();
    assertThat(badActorTrustLine.freezePeer()).isFalse();
    assertThat(badActorTrustLine.noRipple()).isFalse();
    assertThat(badActorTrustLine.noRipplePeer()).isTrue();

    ///////////////////////////
    // Create a Trust Line between issuer and the good actor.
    TrustLine goodActorTrustLine = this.createTrustLine(
      issuerWallet,
      goodActorWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );
    assertThat(goodActorTrustLine.freeze()).isFalse();
    assertThat(goodActorTrustLine.freezePeer()).isFalse();
    assertThat(goodActorTrustLine.noRipple()).isFalse();
    assertThat(goodActorTrustLine.noRipplePeer()).isTrue();

    /////////////
    // Send Funds
    /////////////

    // Send funds from issuer to the badActor.
    sendFunds(
      TEN_THOUSAND, issuerWallet, badActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(() -> getValidatedAccountLines(issuerWallet.address(), badActorWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + TEN_THOUSAND))
    );

    // Send funds from badActor to the goodActor.
    sendFunds(
      FIVE_THOUSAND, badActorWallet, goodActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(() -> getValidatedAccountLines(issuerWallet.address(), goodActorWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + FIVE_THOUSAND))
    );

    // Individual-Freeze the trustline between the issuer and bad actor.
    badActorTrustLine = this.adjustTrustlineFreeze(
      issuerWallet,
      badActorWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee(),
      FREEZE
    );
    assertThat(badActorTrustLine.freeze()).isTrue();
    assertThat(badActorTrustLine.freezePeer()).isFalse();
    assertThat(badActorTrustLine.noRipple()).isFalse();
    assertThat(badActorTrustLine.noRipplePeer()).isTrue();

    /////////////
    // Assertions
    /////////////

    // 1) Payments can still occur directly between the two parties of the frozen trust line.
    // 2) The counterparty can only send the frozen currencies directly to the issuer (no where else)
    // 3) The counterparty can still receive payments from others on the frozen trust line.

    // Try to send funds from badActor to goodActor should not work because the badActor is frozen.
    sendFunds(
      FIVE_THOUSAND, badActorWallet, goodActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee(),
      "tecPATH_DRY"
    );

    // Sending from the badActor to the issuer should still work
    sendFunds(
      FIVE_THOUSAND, badActorWallet, issuerWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Sending from the goodActor to the badActor should still work
    sendFunds(
      FIVE_THOUSAND, goodActorWallet, badActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Unfreeze the bad actor.
    badActorTrustLine = this.adjustTrustlineFreeze(
      issuerWallet,
      badActorWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee(),
      UN_FREEZE
    );
    assertThat(badActorTrustLine.freeze()).isTrue();
    assertThat(badActorTrustLine.freezePeer()).isFalse();
    assertThat(badActorTrustLine.noRipple()).isFalse();
    assertThat(badActorTrustLine.noRipplePeer()).isTrue();
  }

  /**
   * This test creates a Trustline between an issuer and a badActor, issues funds to two counterparties, then globally
   * freezes the trustlines for the issuer. The test validates that neither the good nor the bad actor is able to send
   * funds, except back to the issuer.
   *
   * @see "https://xrpl.org/freezes.html#global-freeze"
   */
  @Test
  public void issueAndFreezeFundsGlobal() throws JsonRpcClientErrorException, JsonProcessingException {
    FeeResult feeResult = xrplClient.fee();

    // Create a Trust Line between issuer and the bad actor.
    TrustLine badActorTrustLine = this.createTrustLine(
      issuerWallet,
      badActorWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );
    assertThat(badActorTrustLine.freeze()).isFalse();
    assertThat(badActorTrustLine.freezePeer()).isFalse();
    assertThat(badActorTrustLine.noRipple()).isFalse();
    assertThat(badActorTrustLine.noRipplePeer()).isTrue();

    ///////////////////////////
    // Create a Trust Line between issuer and the good actor.
    TrustLine goodActorTrustLine = this.createTrustLine(
      issuerWallet,
      goodActorWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );
    assertThat(goodActorTrustLine.freeze()).isFalse();
    assertThat(goodActorTrustLine.freezePeer()).isFalse();
    assertThat(goodActorTrustLine.noRipple()).isFalse();
    assertThat(goodActorTrustLine.noRipplePeer()).isTrue();

    /////////////
    // Send Funds
    /////////////

    // Send funds from issuer to the badActor.
    sendFunds(
      TEN_THOUSAND, issuerWallet, badActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(() -> getValidatedAccountLines(issuerWallet.address(), badActorWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + TEN_THOUSAND))
    );

    // Send funds from badActor to the goodActor.
    sendFunds(
      FIVE_THOUSAND, badActorWallet, goodActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    ///////////////////////////
    // Validate that the TrustLine balance was updated as a result of the Payment.
    // The trust line returned is from the perspective of the issuer, so the balance should be negative.
    this.scanForResult(() -> getValidatedAccountLines(issuerWallet.address(), goodActorWallet.address()),
      linesResult -> linesResult.lines().stream()
        .anyMatch(line -> line.balance().equals("-" + FIVE_THOUSAND))
    );

    // Global-Freeze the trustline for the issuer.
    AccountInfoResult issuerAccountInfo = this.adjustGlobalTrustlineFreeze(
      issuerWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee(),
      FREEZE
    );
    assertThat(issuerAccountInfo.accountData().flags().lsfGlobalFreeze()).isTrue();

    /////////////
    // Assertions
    /////////////

    // 1) The counterparty can only send the frozen currencies directly to the issuer (no where else)
    // 2) The counterparty can still receive payments from others on the frozen trust line.

    // Try to send funds from badActor to goodActor should not work because the badActor is frozen.
    sendFunds(
      "500", badActorWallet, goodActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee(),
      "tecPATH_DRY"
    );
    // Sending from the goodActor to the badActor should not work
    sendFunds(
      "500", goodActorWallet, badActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee(),
      "tecPATH_DRY"
    );

    // Try to send funds from issuer to goodActor should work per
    // https://xrpl.org/enact-global-freeze.html#intermission-while-frozen
    sendFunds(
      "100", issuerWallet, goodActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Try to send funds from issuer to badActor should work per
    // https://xrpl.org/enact-global-freeze.html#intermission-while-frozen
    sendFunds(
      "100", issuerWallet, badActorWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Try to send funds from issuer to goodActor should work per
    // https://xrpl.org/enact-global-freeze.html#intermission-while-frozen
    sendFunds(
      FIVE_THOUSAND, badActorWallet, issuerWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Try to send funds from issuer to goodActor should work per
    // https://xrpl.org/enact-global-freeze.html#intermission-while-frozen
    sendFunds(
      FIVE_THOUSAND, goodActorWallet, issuerWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Unfreeze the bad actor.
    issuerAccountInfo = this.adjustGlobalTrustlineFreeze(
      issuerWallet,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee(),
      UN_FREEZE
    );
    assertThat(issuerAccountInfo.accountData().flags().lsfGlobalFreeze()).isFalse();
  }

  /**
   * Send issued currency funds from an issuer to a badActor.
   *
   * @param value     The amount of currency to send.
   * @param sender    The {@link Wallet} of the sender.
   * @param recipient The {@link Wallet} of the recipient.
   * @param fee       The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   * @throws JsonProcessingException     If there are any problems parsing JSON.
   */
  private void sendFunds(
    String value,
    Wallet sender,
    Wallet recipient,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    this.sendFunds(value, sender, recipient, fee, "tesSUCCESS");
  }

  /**
   * Send issued currency funds from an issuer to a badActor.
   *
   * @param valueToSend        The amount of currency to send.
   * @param sender             The {@link Wallet} of the sender.
   * @param recipient          The {@link Wallet} of the recipient.
   * @param fee                The current network fee, as an {@link XrpCurrencyAmount}.
   * @param expectedResultCode The expected result code after submitting a payment transaction.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   * @throws JsonProcessingException     If there are any problems parsing JSON.
   */
  private void sendFunds(
    String valueToSend,
    Wallet sender,
    Wallet recipient,
    XrpCurrencyAmount fee,
    String expectedResultCode
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(sender.address())
    );

    Payment payment = Payment.builder()
      .account(sender.address())
      .fee(fee)
      .sequence(senderAccountInfo.accountData().sequence())
      .destination(recipient.address())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerWallet.address())
        .currency(FreezeIssuedCurrencyIT.ISSUED_CURRENCY_CODE)
        .value(valueToSend)
        .build())
      .signingPublicKey(sender.publicKey().base16Value())
      .build();

    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(sender.privateKey(), payment);
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedPayment);
    assertThat(paymentResult.result()).isEqualTo(expectedResultCode);

    if (expectedResultCode.equals("tesSUCCESS")) {
      logger.info(
        "Payment transaction: https://testnet.xrpl.org/transactions/{}", paymentResult.transactionResult().hash()
      );
    }
    this.scanForResult(() -> getValidatedTransaction(paymentResult.transactionResult().hash(), Payment.class));
  }

  /**
   * Create a trustline between the given issuer and badActor accounts for the given currency code and with the given
   * limit.
   *
   * @param issuerWallet      The {@link Wallet} of the issuer account.
   * @param counterpartWallet The {@link Wallet} of the badActor account.
   * @param fee               The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @return The {@link TrustLine} that gets created.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   * @throws JsonProcessingException     If there are any problems parsing JSON.
   */
  private TrustLine createTrustLine(
    Wallet issuerWallet,
    Wallet counterpartWallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult badActorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartWallet.address())
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartWallet.address())
      .fee(fee)
      .sequence(badActorAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(FreezeIssuedCurrencyIT.ISSUED_CURRENCY_CODE)
        .issuer(issuerWallet.address())
        .value(FreezeIssuedCurrencyIT.TEN_THOUSAND)
        .build())
      .flags(TrustSetFlags.builder()
        .tfSetNoRipple()
        .build())
      .signingPublicKey(counterpartWallet.publicKey().base16Value())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(counterpartWallet.privateKey(), trustSet);
    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetSubmitResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      trustSetSubmitResult.transactionResult().hash()
    );

    return scanForResult(
      () ->
        getValidatedAccountLines(issuerWallet.address(), counterpartWallet.address()),
      linesResult -> !linesResult.lines().isEmpty()
    )
      .lines().get(0);
  }

  /**
   * Set the `asfRequireAuth` AccountSet flag so that only approved counterparties can hold currency from the issuer.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   * @throws JsonProcessingException     If there are any problems parsing JSON.
   * @see "https://xrpl.org/become-an-xrp-ledger-gateway.html#default-ripple"
   */
  protected void enableDefaultRipple(final Wallet wallet)
    throws JsonRpcClientErrorException, JsonProcessingException {
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerWalletAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.address())
    );

    AccountSet accountSet = AccountSet.builder()
      .sequence(issuerWalletAccountInfo.accountData().sequence())
      .account(wallet.address())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .setFlag(AccountSetFlag.DEFAULT_RIPPLE)
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();

    SingleSignedTransaction<AccountSet> signedTrustSet = signatureService.sign(wallet.privateKey(), accountSet);
    SubmitResult<AccountSet> trustSetSubmitResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetSubmitResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      trustSetSubmitResult.transactionResult().hash()
    );

    scanForResult(
      () -> getValidatedAccountInfo(wallet.address()),
      accountInfoResult -> accountInfoResult.accountData().flags().lsfDefaultRipple()
    );
  }

  private static final boolean FREEZE = true;
  private static final boolean UN_FREEZE = false;

  /**
   * Freeze an individual trustline that exists between the specified issuer and the specified counterparty for the
   * {@link #ISSUED_CURRENCY_CODE} (which is the only currency code this test uses).
   *
   * @param issuerWallet       The {@link Wallet} of the trustline issuer.
   * @param counterpartyWallet The {@link Wallet} of the trustline counterparty.
   * @param fee                The fee to spend to get the "freeze" transaction into the ledger.
   * @param freeze             A boolean to toggle the trustline operation (i.e., {@code false} to unfreeze and {@code
   *                           true} to freeze).
   *
   * @return The {@link TrustLine} that was frozen or unfrozen.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   * @throws JsonProcessingException     If there are any problems parsing JSON.
   */
  private TrustLine adjustTrustlineFreeze(
    Wallet issuerWallet,
    Wallet counterpartyWallet,
    XrpCurrencyAmount fee,
    boolean freeze
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerWallet.address())
    );

    Builder flagsBuilder = TrustSetFlags.builder();
    if (freeze) {
      flagsBuilder.tfSetFreeze();
    }

    TrustSet trustSet = TrustSet.builder()
      .account(issuerWallet.address())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(FreezeIssuedCurrencyIT.ISSUED_CURRENCY_CODE)
        .issuer(counterpartyWallet.address())
        .value("0")
        .build())
      .flags(flagsBuilder.build())
      .signingPublicKey(issuerWallet.publicKey().base16Value())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(issuerWallet.privateKey(), trustSet);
    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetSubmitResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      trustSetSubmitResult.transactionResult().hash()
    );

    return scanForResult(
      () -> getValidatedAccountLines(issuerWallet.address(), counterpartyWallet.address()),
      accountLineResult -> accountLineResult.lines().stream()
        .filter(trustLine -> trustLine.account().equals(counterpartyWallet.address()))
        .anyMatch(TrustLine::freeze)
    )
      .lines().get(0);

  }

  /**
   * Globally freeze all trustlines that exists between the specified issuer and any counterparty.
   *
   * @param issuerWallet The {@link Wallet} of the trustline issuer.
   * @param fee          The fee to spend to get the "freeze" transaction into the ledger.
   * @param freeze       A boolean to toggle the trustline operation (i.e., {@code false} to unfreeze and {@code true}
   *                     to freeze).
   *
   * @return The {@link AccountInfoResult} of the issuer account after the operation has been validated by the ledger.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   * @throws JsonProcessingException     If there are any problems parsing JSON.
   */
  private AccountInfoResult adjustGlobalTrustlineFreeze(
    Wallet issuerWallet,
    XrpCurrencyAmount fee,
    boolean freeze
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerWallet.address())
    );

    ImmutableAccountSet.Builder builder = AccountSet.builder()
      .account(issuerWallet.address())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerWallet.publicKey().base16Value());
    if (freeze) {
      builder.setFlag(AccountSetFlag.GLOBAL_FREEZE);
    } else {
      builder.clearFlag(AccountSetFlag.GLOBAL_FREEZE);
    }
    AccountSet accountSet = builder.build();

    SingleSignedTransaction<AccountSet> signedTrustSet = signatureService.sign(issuerWallet.privateKey(), accountSet);
    SubmitResult<AccountSet> transactionResult = xrplClient.submit(signedTrustSet);
    assertThat(transactionResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/{}",
      transactionResult.transactionResult().hash()
    );

    return scanForResult(
      () -> getValidatedAccountInfo(issuerWallet.address()),
      accountInfoResult -> accountInfoResult.accountData().flags().lsfGlobalFreeze() == freeze
    );

  }
}
