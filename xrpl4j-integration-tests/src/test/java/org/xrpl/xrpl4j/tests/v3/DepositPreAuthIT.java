package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * An Integration Test to validate submission of DepositPreAuth transactions.
 */
public class DepositPreAuthIT extends AbstractIT {

  @Test
  public void preauthorizeAccountAndReceivePayment() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccountEd25519();
    Wallet senderWallet = createRandomAccountEd25519();

    /////////////////////////
    // Enable Deposit Preauthorization on the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult receiverAccountInfo =
      enableDepositPreauth(receiverWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    /////////////////////////
    // Give Preauthorization for the sender to send a funds to the receiver
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(receiverWallet.address())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .signingPublicKey(receiverWallet.publicKey().base16Value())
      .authorize(senderWallet.address())
      .build();

    SingleSingedTransaction<DepositPreAuth> singedDepositPreAuth = this.signatureService.sign(
      receiverWallet.privateKey(), depositPreAuth
    );

    SubmitResult<DepositPreAuth> result = xrplClient.submit(singedDepositPreAuth);

    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "DepositPreauth transaction successful. https://testnet.xrpl.org/transactions/{}",
      result.transactionResult().hash()
    );

    /////////////////////////
    // Validate that the DepositPreAuthObject was added to the receiver's account objects
    this.scanForResult(
      () -> this.getValidatedAccountObjects(receiverWallet.address()),
      accountObjects ->
        accountObjects.accountObjects().stream().anyMatch(object ->
          DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
            ((DepositPreAuthObject) object).authorize().equals(senderWallet.address())
        )
    );

    /////////////////////////
    // Validate that the `deposit_authorized` client call is implemented properly by ensuring it aligns with the
    // result found in the account object.
    final boolean depositAuthorized = xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
      .sourceAccount(senderWallet.address())
      .destinationAccount(receiverWallet.address())
      .build()).depositAuthorized();
    assertThat(depositAuthorized).isTrue();

    /////////////////////////
    // Send a Payment from the sender wallet to the receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address())
    );
    Payment payment = Payment.builder()
      .account(senderWallet.address())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(receiverWallet.address())
      .build();

    SingleSingedTransaction<Payment> singedPayment = signatureService.sign(
      senderWallet.privateKey(), payment
    );

    SubmitResult<Payment> paymentResult = xrplClient.submit(singedPayment);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful. https://testnet.xrpl.org/transactions/{}",
      paymentResult.transactionResult().hash()
    );

    /////////////////////////
    // Validate that the Payment was included in a validated ledger
    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(paymentResult.transactionResult().hash(), Payment.class)
    );

    /////////////////////////
    // And validate that the receiver's balance was updated correctly
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.address()),
      info -> {
        XrpCurrencyAmount expectedBalance = receiverAccountInfo.accountData().balance()
          .minus(depositPreAuth.fee())
          .plus(((XrpCurrencyAmount) validatedPayment.transaction().amount()));
        return info.accountData().balance().equals(expectedBalance);
      });
  }

  @Test
  public void accountUnableToReceivePaymentsWithoutPreauthorization()
    throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccountEd25519();
    Wallet senderWallet = createRandomAccountEd25519();

    /////////////////////////
    // Enable Deposit Preauthorization on the receiver account
    FeeResult feeResult = xrplClient.fee();
    enableDepositPreauth(receiverWallet, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    /////////////////////////
    // Validate that the receiver has not given authorization to anyone to send them Payments
    AccountObjectsResult receiverObjects = this.scanForResult(
      () -> this.getValidatedAccountObjects(receiverWallet.address()));
    assertThat(receiverObjects.accountObjects().stream()
      .anyMatch(ledgerObject ->
        DepositPreAuthObject.class.isAssignableFrom(ledgerObject.getClass())
      )
    ).isFalse();

    /////////////////////////
    // Try to send a Payment from sender wallet to receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address())
    );
    Payment payment = Payment.builder()
      .account(senderWallet.address())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(receiverWallet.address())
      .build();

    /////////////////////////
    // And validate that the transaction failed with a tecNO_PERMISSION error code
    SingleSingedTransaction<Payment> singedPayment = signatureService.sign(
      senderWallet.privateKey(), payment
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(singedPayment);
    assertThat(paymentResult.result()).isEqualTo("tecNO_PERMISSION");
  }

  @Test
  public void updateDepositPreAuthWithLedgerIndex() throws JsonRpcClientErrorException {
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccountEd25519();
    Wallet senderWallet = createRandomAccountEd25519();

    assertThat(
      xrplClient.depositAuthorized(
        DepositAuthorizedRequestParams.builder()
          .sourceAccount(senderWallet.address())
          .destinationAccount(receiverWallet.address())
          .ledgerSpecifier(LedgerSpecifier.CURRENT)
          .build()
      ).depositAuthorized()
    ).isTrue();
  }

  @Test
  public void updateDepositPreAuthWithLedgerHash() {
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccountEd25519();
    Wallet senderWallet = createRandomAccountEd25519();

    Assertions.assertThrows(JsonRpcClientErrorException.class,
      () -> xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
        .sourceAccount(senderWallet.address())
        .destinationAccount(receiverWallet.address())
        .ledgerSpecifier(
          LedgerSpecifier.of(Hash256.of("19DB20F9037D75361582E804233C517532C1DC5F3158845A9332190342009795"))
        )
        .build()).depositAuthorized(),
      "org.xrpl.xrpl4j.client.JsonRpcClientErrorException: ledgerNotFound"
    );
  }

  /**
   * Enable the lsfDepositPreauth flag on a given account by submitting an {@link AccountSet} transaction.
   *
   * @param wallet The {@link Wallet} of the account to enable Deposit Preauthorization on.
   * @param fee    The {@link XrpCurrencyAmount} of the ledger fee for the AccountSet transaction.
   *
   * @return The {@link AccountInfoResult} of the wallet once the {@link AccountSet} transaction has been applied.
   *
   * @throws JsonRpcClientErrorException If {@code xrplClient} throws an error.
   */
  private AccountInfoResult enableDepositPreauth(
    Wallet wallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.address())
    );
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.address())
      .fee(fee)
      .sequence(accountInfoResult.accountData().sequence())
      .signingPublicKey(wallet.publicKey().base16Value())
      .setFlag(AccountSet.AccountSetFlag.DEPOSIT_AUTH)
      .build();

    SingleSingedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      wallet.privateKey(), accountSet
    );
    SubmitResult<AccountSet> accountSetResult = xrplClient.submit(signedAccountSet);
    assertThat(accountSetResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet to enable Deposit Preauth successful. https://testnet.xrpl.org/transactions/{}",
      accountSetResult.transactionResult().hash()
    );
    return this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.address()),
      accountInfo -> accountInfo.accountData().flags().lsfDepositAuth()
    );
  }
}
