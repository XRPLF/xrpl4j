package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.client.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.client.accounts.AccountInfoResult;
import com.ripple.xrpl4j.model.client.accounts.AccountObjectsResult;
import com.ripple.xrpl4j.model.client.fees.FeeResult;
import com.ripple.xrpl4j.model.client.transactions.SubmitResult;
import com.ripple.xrpl4j.model.client.transactions.TransactionResult;
import com.ripple.xrpl4j.model.ledger.DepositPreAuthObject;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.DepositPreAuth;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
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
    AccountInfoResult receiverAccountInfo = enableDepositPreauth(receiverWallet, feeResult.drops().openLedgerFee());

    /////////////////////////
    // Give Preauthorization for the sender to send a funds to the receiver
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
        .account(receiverWallet.classicAddress())
        .fee(feeResult.drops().openLedgerFee())
        .sequence(receiverAccountInfo.accountData().sequence())
        .signingPublicKey(receiverWallet.publicKey())
        .authorize(senderWallet.classicAddress())
        .build();

    SubmitResult<DepositPreAuth> result = xrplClient.submit(receiverWallet, depositPreAuth);
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
        .fee(feeResult.drops().openLedgerFee())
        .sequence(senderAccountInfo.accountData().sequence())
        .signingPublicKey(senderWallet.publicKey())
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(receiverWallet.classicAddress())
        .build();

    SubmitResult<Payment> paymentResult = xrplClient.submit(senderWallet, payment);
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
          XrpCurrencyAmount expectedBalance = receiverAccountInfo.accountData().balance()
              .minus(depositPreAuth.fee())
              .plus(((XrpCurrencyAmount) validatedPayment.transaction().amount()));
          return info.accountData().balance().equals(expectedBalance);
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
    enableDepositPreauth(receiverWallet, feeResult.drops().openLedgerFee());

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
        .fee(feeResult.drops().openLedgerFee())
        .sequence(senderAccountInfo.accountData().sequence())
        .signingPublicKey(senderWallet.publicKey())
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(receiverWallet.classicAddress())
        .build();

    /////////////////////////
    // And validate that the transaction failed with a tecNO_PERMISSION error code
    SubmitResult<Payment> paymentResult = xrplClient.submit(senderWallet, payment);
    assertThat(paymentResult.engineResult()).isNotEmpty().get().isEqualTo("tecNO_PERMISSION");
  }

  /**
   * Enable the lsfDepositPreauth flag on a given account by submitting an {@link AccountSet} transaction.
   *
   * @param wallet The {@link Wallet} of the account to enable Deposit Preauthorization on.
   * @param fee    The {@link XrpCurrencyAmount} of the ledger fee for the AccountSet transaction.
   * @return The {@link AccountInfoResult} of the wallet once the {@link AccountSet} transaction has been applied.
   * @throws JsonRpcClientErrorException If {@code xrplClient} throws an error.
   */
  private AccountInfoResult enableDepositPreauth(Wallet wallet, XrpCurrencyAmount fee) throws JsonRpcClientErrorException {
    AccountInfoResult accountInfoResult = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));
    ;
    AccountSet accountSet = AccountSet.builder()
        .account(wallet.classicAddress())
        .fee(fee)
        .sequence(accountInfoResult.accountData().sequence())
        .signingPublicKey(wallet.publicKey())
        .setFlag(AccountSet.AccountSetFlag.DEPOSIT_AUTH)
        .build();

    SubmitResult<AccountSet> accountSetResult = xrplClient.submit(wallet, accountSet);
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
