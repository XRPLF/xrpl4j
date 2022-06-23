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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

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
    assertThat(result.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(result.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(result.transactionResult().hash());
    logger.info("DepositPreauth transaction successful. https://testnet.xrpl.org/transactions/{}",
      result.transactionResult().hash()
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
    // Validate that the `deposit_authorized` client call is implemented properly by ensuring it aligns with the
    // result found in the account object.
    final boolean depositAuthorized = xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
      .sourceAccount(senderWallet.classicAddress())
      .destinationAccount(receiverWallet.classicAddress())
      .build()).depositAuthorized();
    assertThat(depositAuthorized).isTrue();

    /////////////////////////
    // Send a Payment from the sender wallet to the receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress())
    );
    Payment payment = Payment.builder()
      .account(senderWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderWallet.publicKey())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(receiverWallet.classicAddress())
      .build();

    SubmitResult<Payment> paymentResult = xrplClient.submit(senderWallet, payment);
    assertThat(result.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(result.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(result.transactionResult().hash());
    logger.info("Payment transaction successful. https://testnet.xrpl.org/transactions/{}",
      paymentResult.transactionResult().hash()
    );

    /////////////////////////
    // Validate that the Payment was included in a validated ledger
    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(
        paymentResult.transactionResult().hash(),
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
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress())
    );
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
    assertThat(paymentResult.result()).isEqualTo("tecNO_PERMISSION");
  }

  @Test
  public void updateDepositPreAuthWithLedgerIndex() throws JsonRpcClientErrorException {
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccount();
    Wallet senderWallet = createRandomAccount();

    assertThat(
      xrplClient.depositAuthorized(
        DepositAuthorizedRequestParams.builder()
          .sourceAccount(senderWallet.classicAddress())
          .destinationAccount(receiverWallet.classicAddress())
          .ledgerSpecifier(LedgerSpecifier.CURRENT)
          .build()
      ).depositAuthorized()
    ).isTrue();
  }

  @Test
  public void updateDepositPreAuthWithLedgerHash() {
    // Create random sender/receiver accounts
    Wallet receiverWallet = createRandomAccount();
    Wallet senderWallet = createRandomAccount();

    Assertions.assertThrows(JsonRpcClientErrorException.class, () -> {
        xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
          .sourceAccount(senderWallet.classicAddress())
          .destinationAccount(receiverWallet.classicAddress())
          .ledgerSpecifier(
            LedgerSpecifier.of(Hash256.of("19DB20F9037D75361582E804233C517532C1DC5F3158845A9332190342009795"))
          )
          .build()).depositAuthorized();
      },
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
  ) throws JsonRpcClientErrorException {
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    );
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(fee)
      .sequence(accountInfoResult.accountData().sequence())
      .signingPublicKey(wallet.publicKey())
      .setFlag(AccountSet.AccountSetFlag.DEPOSIT_AUTH)
      .build();

    SubmitResult<AccountSet> accountSetResult = xrplClient.submit(wallet, accountSet);
    assertThat(accountSetResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(accountSetResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(accountSetResult.transactionResult().hash());
    logger.info("AccountSet to enable Deposit Preauth successful. https://testnet.xrpl.org/transactions/{}",
      accountSetResult.transactionResult().hash()
    );
    return this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress()),
      accountInfo -> accountInfo.accountData().flags().lsfDepositAuth()
    );
  }
}
