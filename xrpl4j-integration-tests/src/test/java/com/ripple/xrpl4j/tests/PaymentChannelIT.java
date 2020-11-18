package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.client.model.accounts.AccountInfoResult;
import com.ripple.xrpl4j.client.model.accounts.PaymentChannelResultObject;
import com.ripple.xrpl4j.client.model.fees.FeeResult;
import com.ripple.xrpl4j.client.model.ledger.objects.PayChannelObject;
import com.ripple.xrpl4j.client.model.transactions.SubmissionResult;
import com.ripple.xrpl4j.client.rippled.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.PaymentChannelCreate;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import org.junit.jupiter.api.Test;

public class PaymentChannelIT extends AbstractIT {

  @Test
  public void createPaymentChannel() throws JsonRpcClientErrorException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.classicAddress()));

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate createPaymentChannel = PaymentChannelCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.of("10000"))
      .destination(destinationWallet.classicAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey())
      .cancelAfter(UnsignedInteger.valueOf(533171558))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmissionResult<PaymentChannelCreate> createResult = xrplClient.submit(sourceWallet, createPaymentChannel);
    assertThat(createResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transaction().hash().orElse("n/a")
    );

    //////////////////////////
    // Wait for the payment channel to exist in a validated ledger
    // and validate its fields
    PaymentChannelResultObject paymentChannel = scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.classicAddress()),
      channels -> channels.channels().stream()
        .anyMatch(channel -> channel.destinationAccount().equals(destinationWallet.classicAddress()))
    )
      .channels().stream()
      .filter(channel -> channel.destinationAccount().equals(destinationWallet.classicAddress()))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Could not find payment channel for destination address."));

    assertThat(paymentChannel.amount()).isEqualTo(createPaymentChannel.amount());
    assertThat(paymentChannel.settleDelay()).isEqualTo(createPaymentChannel.settleDelay());
    assertThat(paymentChannel.publicKeyHex()).isNotEmpty().get().isEqualTo(createPaymentChannel.publicKey());
    assertThat(paymentChannel.cancelAfter()).isNotEmpty().get().isEqualTo(createPaymentChannel.cancelAfter().get());

    //////////////////////////
    // Also validate that the channel exists in the account's objects
    scanForResult(
      () -> getValidatedAccountObjects(sourceWallet.classicAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          PayChannelObject.class.isAssignableFrom(object.getClass()) &&
            ((PayChannelObject) object).destination().equals(destinationWallet.classicAddress())
        )
    );


    //////////////////////////
    // Validate that the amount of the payment channel was deducted from the source
    // accounts XRP balance
    AccountInfoResult senderAccountInfoAfterCreate = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress()),
      accountInfo -> accountInfo.ledgerIndex()
        .orElseThrow(() -> new RuntimeException("Ledger index was not present."))
        .equals(senderAccountInfo.ledgerIndex()
          .orElseThrow(() -> new RuntimeException("Ledger index was not present.")).plus(UnsignedInteger.ONE))
    );

    assertThat(senderAccountInfoAfterCreate.accountData().balance().asBigInteger())
      .isEqualTo(senderAccountInfo.accountData().balance().asBigInteger()
        .subtract(createPaymentChannel.amount().asBigInteger())
        .subtract(createPaymentChannel.fee().asBigInteger())
      );
  }

}
