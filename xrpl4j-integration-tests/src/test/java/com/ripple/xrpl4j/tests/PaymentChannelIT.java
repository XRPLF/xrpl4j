package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.model.client.accounts.AccountInfoResult;
import com.ripple.xrpl4j.model.client.accounts.PaymentChannelResultObject;
import com.ripple.xrpl4j.model.client.channels.ChannelVerifyResult;
import com.ripple.xrpl4j.model.client.channels.UnsignedClaim;
import com.ripple.xrpl4j.model.client.fees.FeeResult;
import com.ripple.xrpl4j.model.ledger.PayChannelObject;
import com.ripple.xrpl4j.model.client.transactions.SubmitResult;
import com.ripple.xrpl4j.client.JsonRpcClientErrorException;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.keypairs.DefaultKeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.PaymentChannelClaim;
import com.ripple.xrpl4j.model.transactions.PaymentChannelCreate;
import com.ripple.xrpl4j.model.transactions.PaymentChannelFund;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public class PaymentChannelIT extends AbstractIT {

  private final XrplBinaryCodec binaryCodec;
  private final KeyPairService keyPairService;
  private final ObjectMapper objectMapper;

  public PaymentChannelIT() {
    this.binaryCodec = new XrplBinaryCodec();
    this.keyPairService = DefaultKeyPairService.getInstance();
    this.objectMapper = ObjectMapperFactory.create();
  }

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

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.classicAddress()));

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
    ChannelVerifyResult channelVerifyResult = xrplClient.channelVerify(
      paymentChannel.channelId(),
      unsignedClaim.amount(),
      signature,
      sourceWallet.publicKey()
    );
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
    assertThat(claimResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("PaymentChannelClaim transaction successful. https://testnet.xrpl.org/transactions/{}",
      claimResult.transaction().hash().orElse("n/a")
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
    AccountInfoResult senderAccountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.classicAddress()));

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
    assertThat(fundResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("PaymentChannelFund transaction successful. https://testnet.xrpl.org/transactions/{}",
      fundResult.transaction().hash().orElse("n/a")
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
    assertThat(expiryResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("PaymentChannelFund transaction successful. https://testnet.xrpl.org/transactions/{}",
      expiryResult.transaction().hash().orElse("n/a")
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
}
