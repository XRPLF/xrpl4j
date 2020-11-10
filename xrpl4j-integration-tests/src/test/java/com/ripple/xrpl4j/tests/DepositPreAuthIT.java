package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.DepositPreAuth;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrpl4j.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrpl4j.xrplj4.client.model.accounts.AccountObjectsResult;
import com.ripple.xrpl4j.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrpl4j.xrplj4.client.model.ledger.objects.DepositPreAuthObject;
import com.ripple.xrpl4j.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrpl4j.xrplj4.client.model.transactions.TransactionResult;
import com.ripple.xrpl4j.xrplj4.client.rippled.JsonRpcClientErrorException;
import org.junit.jupiter.api.Test;

public class DepositPreAuthIT extends AbstractIT {

  @Test
  public void preauthorizeAccountAndReceivePayment() throws JsonRpcClientErrorException {
    /////////////////////////
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccount();
    Wallet senderWallet = createRandomAccount();

    /////////////////////////
    // Enable Deposit Preauthorization on the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult receiverAccountInfo = enableDepositPreauth(receiverWallet, feeResult.drops().minimumFee());

    /////////////////////////
    // Give Preauthorization for the sender to send a funds to the receiver
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(receiverWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .signingPublicKey(receiverWallet.publicKey())
      .authorize(senderWallet.classicAddress())
      .build();

    SubmissionResult<DepositPreAuth> result = xrplClient.submit(receiverWallet, depositPreAuth);
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("DepositPreauth transaction successful. https://testnet.xrpl.org/transactions/{}",
      result.transaction().hash().orElse("n/a")
      );

    /////////////////////////
    // Validate that the DepositPreAuthObject was added to the receiver's account objects
    this.scanForResult(
      () -> this.getValidatedAccountObjects(receiverWallet.classicAddress()),
      accountObjects ->
        accountObjects.accountObjects().stream().anyMatch(object ->
          DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
            ((DepositPreAuthObject) object).authorize().equals(senderWallet.classicAddress())
        )
    );

    /////////////////////////
    // Send a Payment from the sender wallet to the receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(senderWallet.classicAddress()));
    Payment payment = Payment.builder()
      .account(senderWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderWallet.publicKey())
      .amount(XrpCurrencyAmount.of("12345"))
      .destination(receiverWallet.classicAddress())
      .build();

    SubmissionResult<Payment> paymentResult = xrplClient.submit(senderWallet, payment);
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("Payment transaction successful. https://testnet.xrpl.org/transactions/{}",
            paymentResult.transaction().hash().orElse("n/a")
    );

    /////////////////////////
    // Validate that the Payment was included in a validated ledger
    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(
        paymentResult.transaction()
          .hash()
          .orElseThrow(() -> new IllegalArgumentException("Transaction hash was not present.")),
        Payment.class)
    );

    /////////////////////////
    // And validate that the receiver's balance was updated correctly
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.classicAddress()),
      info -> {
        String expectedBalance = UnsignedLong.valueOf(receiverAccountInfo.accountData().balance().value())
          .minus(UnsignedLong.valueOf(depositPreAuth.fee().value()))
          .plus(UnsignedLong.valueOf(((XrpCurrencyAmount) validatedPayment.transaction().amount()).value())).toString();
        return info.accountData().balance().value().equals(expectedBalance);
      });
  }

  @Test
  public void accountUnableToReceivePaymentsWithoutPreauthorization() throws JsonRpcClientErrorException {
    /////////////////////////
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccount();
    Wallet senderWallet = createRandomAccount();

    /////////////////////////
    // Enable Deposit Preauthorization on the receiver account
    FeeResult feeResult = xrplClient.fee();
    enableDepositPreauth(receiverWallet, feeResult.drops().minimumFee());

    /////////////////////////
    // Validate that the receiver has not given authorization to anyone to send them Payments
    AccountObjectsResult receiverObjects = this.scanForResult(
      () -> this.getValidatedAccountObjects(receiverWallet.classicAddress()));
    assertThat(receiverObjects.accountObjects().stream()
      .anyMatch(ledgerObject ->
        DepositPreAuthObject.class.isAssignableFrom(ledgerObject.getClass())
      )
    ).isFalse();

    /////////////////////////
    // Try to send a Payment from sender wallet to receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(senderWallet.classicAddress()));
    Payment payment = Payment.builder()
      .account(senderWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderWallet.publicKey())
      .amount(XrpCurrencyAmount.of("12345"))
      .destination(receiverWallet.classicAddress())
      .build();

    /////////////////////////
    // And validate that the transaction failed with a tecNO_PERMISSION error code
    SubmissionResult<Payment> paymentResult = xrplClient.submit(senderWallet, payment);
    assertThat(paymentResult.engineResult()).isNotEmpty().get().isEqualTo("tecNO_PERMISSION");
  }

  /**
   * Enable the lsfDepositPreauth flag on a given account by submitting an {@link AccountSet} transaction.
   *
   * @param wallet The {@link Wallet} of the account to enable Deposit Preauthorization on.
   * @param fee The {@link XrpCurrencyAmount} of the ledger fee for the AccountSet transaction.
   * @return The {@link AccountInfoResult} of the wallet once the {@link AccountSet} transaction has been applied.
   * @throws JsonRpcClientErrorException If {@code xrplClient} throws an error.
   */
  private AccountInfoResult enableDepositPreauth(Wallet wallet, XrpCurrencyAmount fee) throws JsonRpcClientErrorException {
    AccountInfoResult accountInfoResult = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));;
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(fee)
      .sequence(accountInfoResult.accountData().sequence())
      .signingPublicKey(wallet.publicKey())
      .setFlag(AccountSet.AccountSetFlag.DEPOSIT_AUTH)
      .build();

    SubmissionResult<AccountSet> accountSetResult = xrplClient.submit(wallet, accountSet);
    assertThat(accountSetResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("AccountSet to enable Deposit Preauth successful. https://testnet.xrpl.org/transactions/{}",
      accountSetResult.transaction().hash().orElse("n/a")
    );
    return this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress()),
      accountInfo -> accountInfo.accountData().flags().lsfDepositAuth()
    );
  }
}
