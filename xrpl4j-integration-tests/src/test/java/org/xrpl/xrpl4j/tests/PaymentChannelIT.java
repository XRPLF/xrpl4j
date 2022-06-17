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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.PaymentChannelResultObject;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyRequestParams;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyResult;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.time.Duration;
import java.time.Instant;

public class PaymentChannelIT extends BaseIT {

  private final XrplBinaryCodec binaryCodec = new XrplBinaryCodec();
  private final KeyPairService keyPairService = DefaultKeyPairService.getInstance();
  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  public void createPaymentChannel() throws JsonRpcClientErrorException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient().fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate createPaymentChannel = PaymentChannelCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(destinationWallet.classicAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey())
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmitResult<PaymentChannelCreate> createResult = xrplClient().submit(sourceWallet, createPaymentChannel);
    assertThat(createResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(createResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(createResult.transactionResult().hash());
    logger.info("PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
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
      accountInfo -> accountInfo.ledgerIndexSafe().equals(senderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.ONE))
    );

    assertThat(senderAccountInfoAfterCreate.accountData().balance())
      .isEqualTo(senderAccountInfo.accountData().balance()
        .minus(createPaymentChannel.amount())
        .minus(createPaymentChannel.fee())
      );
  }

  @Test
  void createAndClaimPaymentChannel() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient().fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate createPaymentChannel = PaymentChannelCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000000))
      .destination(destinationWallet.classicAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey())
      .cancelAfter(this.instantToXrpTimestamp(Instant.now().plus(Duration.ofMinutes(1))))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmitResult<PaymentChannelCreate> createResult = xrplClient().submit(sourceWallet, createPaymentChannel);
    assertThat(createResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(createResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(createResult.transactionResult().hash());
    logger.info("PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
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

    AccountInfoResult destinationAccountInfo = scanForResult(
      () -> getValidatedAccountInfo(destinationWallet.classicAddress())
    );

    //////////////////////////
    // Source account signs a claim
    UnsignedClaim unsignedClaim = UnsignedClaim.builder()
      .channel(paymentChannel.channelId())
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .build();

    String signature = keyPairService.sign(
      binaryCodec.encodeForSigningClaim(
        objectMapper.writeValueAsString(unsignedClaim)
      ),
      sourceWallet.privateKey()
        .orElseThrow(
          () -> new IllegalArgumentException("Cannot sign claim because source wallet does not have private key.")
        )
    );

    //////////////////////////
    // Destination account verifies the claim signature
    ChannelVerifyRequestParams channelVerifyRequestParams = ChannelVerifyRequestParams.builder()
      .amount(unsignedClaim.amount())
      .channelId(paymentChannel.channelId())
      .publicKey(sourceWallet.publicKey())
      .signature(signature)
      .build();

    ChannelVerifyResult channelVerifyResult = xrplClient.channelVerify(channelVerifyRequestParams);
    assertThat(channelVerifyResult.signatureVerified()).isTrue();

    //////////////////////////
    // Destination account submits the signed claim to the ledger to get their XRP
    PaymentChannelClaim signedClaim = PaymentChannelClaim.builder()
      .account(destinationWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(destinationAccountInfo.accountData().sequence())
      .channel(paymentChannel.channelId())
      .balance(paymentChannel.balance().plus(unsignedClaim.amount()))
      .amount(unsignedClaim.amount())
      .signature(signature)
      .publicKey(sourceWallet.publicKey())
      .signingPublicKey(destinationWallet.publicKey())
      .build();

    SubmitResult<PaymentChannelClaim> claimResult = xrplClient.submit(destinationWallet, signedClaim);
    assertThat(claimResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(claimResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(claimResult.transactionResult().hash());
    logger.info("PaymentChannelClaim transaction successful. https://testnet.xrpl.org/transactions/{}",
      claimResult.transactionResult().hash()
    );

    //////////////////////////
    // Validate that the destination account balance has gone up by the claim amount
    this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationWallet.classicAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        destinationAccountInfo.accountData().balance()
          .minus(signedClaim.fee())
          .plus(signedClaim.balance().get())
      )
    );
  }

  @Test
  void createAddFundsAndSetExpirationToPaymentChannel() throws JsonRpcClientErrorException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate createPaymentChannel = PaymentChannelCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000000))
      .destination(destinationWallet.classicAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey())
      .cancelAfter(this.instantToXrpTimestamp(Instant.now().plus(Duration.ofMinutes(1))))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmitResult<PaymentChannelCreate> createResult = xrplClient.submit(sourceWallet, createPaymentChannel);
    assertThat(createResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(createResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(createResult.transactionResult().hash());
    logger.info("PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
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

    PaymentChannelFund addFunds = PaymentChannelFund.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(sourceWallet.publicKey())
      .channel(paymentChannel.channelId())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmitResult<PaymentChannelFund> fundResult = xrplClient.submit(sourceWallet, addFunds);
    assertThat(fundResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(fundResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(fundResult.transactionResult().hash());
    logger.info("PaymentChannelFund transaction successful. https://testnet.xrpl.org/transactions/{}",
      fundResult.transactionResult().hash()
    );

    //////////////////////////
    // Validate that the amount in the channel increased by the fund amount
    scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.classicAddress()),
      channelsResult -> channelsResult.channels().stream()
        .anyMatch(
          channel ->
            channel.channelId().equals(paymentChannel.channelId()) &&
              channel.amount().equals(paymentChannel.amount().plus(addFunds.amount()))
        )
    );

    //////////////////////////
    // Then set a new expiry on the channel by submitting a PaymentChannelFund
    // transaction with an expiration and 1 drop of XRP in the amount field
    UnsignedLong newExpiry = instantToXrpTimestamp(Instant.now())
      .plus(UnsignedLong.valueOf(paymentChannel.settleDelay().longValue()))
      .plus(UnsignedLong.valueOf(30));

    PaymentChannelFund setExpiry = PaymentChannelFund.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(sourceWallet.publicKey())
      .channel(paymentChannel.channelId())
      .amount(XrpCurrencyAmount.ofDrops(1))
      .expiration(newExpiry)
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmitResult<PaymentChannelFund> expiryResult = xrplClient.submit(sourceWallet, setExpiry);
    assertThat(expiryResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(expiryResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(expiryResult.transactionResult().hash());
    logger.info("PaymentChannelFund transaction successful. https://testnet.xrpl.org/transactions/{}",
      expiryResult.transactionResult().hash()
    );

    //////////////////////////
    // Validate that the expiration was set properly
    scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.classicAddress()),
      channelsResult -> channelsResult.channels().stream()
        .anyMatch(
          channel ->
            channel.channelId().equals(paymentChannel.channelId()) &&
              channel.expiration().isPresent() &&
              channel.expiration().get().equals(newExpiry)
        )
    );
  }

  @Test
  void testCurrentAccountChannels() throws JsonRpcClientErrorException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate createPaymentChannel = PaymentChannelCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(destinationWallet.classicAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey())
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmitResult<PaymentChannelCreate> createResult = xrplClient.submit(sourceWallet, createPaymentChannel);
    assertThat(createResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(createResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(createResult.transactionResult().hash());
    logger.info("PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////////
    // Wait for the payment channel to exist in a validated ledger
    // and validate its fields
    AccountChannelsResult accountChannelsResult = scanForResult(
      () -> {
        try {
          return xrplClient.accountChannels(AccountChannelsRequestParams.builder()
            .account(sourceWallet.classicAddress())
            .ledgerSpecifier(LedgerSpecifier.CURRENT)
            .build());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      channels -> channels.channels().stream()
        .anyMatch(channel -> channel.destinationAccount().equals(destinationWallet.classicAddress()))
    );

    assertThat(accountChannelsResult.ledgerHash()).isNull();
    assertThat(accountChannelsResult.ledgerIndex()).isNull();
    assertThat(accountChannelsResult.ledgerCurrentIndex()).isNotEmpty();
  }
}
