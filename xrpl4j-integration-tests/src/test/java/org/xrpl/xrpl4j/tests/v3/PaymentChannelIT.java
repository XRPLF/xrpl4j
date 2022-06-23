package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.calculateFeeDynamically;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
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
import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.Duration;
import java.time.Instant;

/**
 * An Integration Test to validate submission of PaymentChannel transactions.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class PaymentChannelIT extends AbstractIT {

  @Test
  public void createPaymentChannel() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(sourceWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(destinationWallet.address())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey().base16Value())
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();
    SingleSingedTransaction<PaymentChannelCreate> signedPaymentChannelCreate = signatureService.sign(
      sourceWallet.privateKey(), paymentChannelCreate
    );
    //////////////////////////
    // Validate that the transaction was submitted successfully
    SubmitResult<PaymentChannelCreate> createResult = xrplClient.submit(signedPaymentChannelCreate);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////////
    // Wait for the payment channel to exist in a validated ledger
    // and validate its fields
    PaymentChannelResultObject paymentChannel = scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.address()),
      channels -> channels.channels().stream()
        .anyMatch(channel -> channel.destinationAccount().equals(destinationWallet.address()))
    )
      .channels().stream()
      .filter(channel -> channel.destinationAccount().equals(destinationWallet.address()))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Could not find payment channel for destination address."));

    assertThat(paymentChannel.amount()).isEqualTo(paymentChannelCreate.amount());
    assertThat(paymentChannel.settleDelay()).isEqualTo(paymentChannelCreate.settleDelay());
    assertThat(paymentChannel.publicKeyHex()).isNotEmpty().get().isEqualTo(paymentChannelCreate.publicKey());
    assertThat(paymentChannel.cancelAfter()).isNotEmpty().get().isEqualTo(paymentChannelCreate.cancelAfter().get());

    //////////////////////////
    // Also validate that the channel exists in the account's objects
    scanForResult(
      () -> getValidatedAccountObjects(sourceWallet.address()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          PayChannelObject.class.isAssignableFrom(object.getClass()) &&
            ((PayChannelObject) object).destination().equals(destinationWallet.address())
        )
    );

    //////////////////////////
    // Validate that the amount of the payment channel was deducted from the source
    // accounts XRP balance
    AccountInfoResult senderAccountInfoAfterCreate = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address()),
      accountInfo -> accountInfo.ledgerIndex()
        .orElseThrow(() -> new RuntimeException("Ledger index was not present."))
        .equals(senderAccountInfo.ledgerIndex()
          .orElseThrow(() -> new RuntimeException("Ledger index was not present."))
          .plus(UnsignedInteger.ONE))
    );

    assertThat(senderAccountInfoAfterCreate.accountData().balance())
      .isEqualTo(senderAccountInfo.accountData().balance()
        .minus(paymentChannelCreate.amount())
        .minus(paymentChannelCreate.fee())
      );
  }

  @Test
  void createAndClaimPaymentChannel() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(sourceWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000000))
      .destination(destinationWallet.address())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey().base16Value())
      .cancelAfter(this.instantToXrpTimestamp(Instant.now().plus(Duration.ofMinutes(1))))
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SingleSingedTransaction<PaymentChannelCreate> signedPaymentChannelCreate = signatureService.sign(
      sourceWallet.privateKey(), paymentChannelCreate
    );
    SubmitResult<PaymentChannelCreate> createResult = xrplClient.submit(signedPaymentChannelCreate);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////////
    // Wait for the payment channel to exist in a validated ledger
    // and validate its fields
    PaymentChannelResultObject paymentChannel = scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.address()),
      channels -> channels.channels().stream()
        .anyMatch(channel -> channel.destinationAccount().equals(destinationWallet.address()))
    )
      .channels().stream()
      .filter(channel -> channel.destinationAccount().equals(destinationWallet.address()))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Could not find payment channel for destination address."));

    assertThat(paymentChannel.amount()).isEqualTo(paymentChannelCreate.amount());
    assertThat(paymentChannel.settleDelay()).isEqualTo(paymentChannelCreate.settleDelay());
    assertThat(paymentChannel.publicKeyHex()).isNotEmpty().get().isEqualTo(paymentChannelCreate.publicKey());
    assertThat(paymentChannel.cancelAfter()).isNotEmpty().get().isEqualTo(paymentChannelCreate.cancelAfter().get());

    AccountInfoResult destinationAccountInfo = scanForResult(
      () -> getValidatedAccountInfo(destinationWallet.address())
    );

    //////////////////////////
    // Source account signs a claim
    UnsignedClaim unsignedClaim = UnsignedClaim.builder()
      .channel(paymentChannel.channelId())
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .build();

    Signature signedClaimSignature = signatureService.sign(sourceWallet.privateKey(), unsignedClaim);

    //////////////////////////
    // Destination account verifies the claim signature
    ChannelVerifyResult channelVerifyResult = xrplClient.channelVerify(
      ChannelVerifyRequestParams.builder()
        .channelId(paymentChannel.channelId())
        .amount(unsignedClaim.amount())
        .signature(signedClaimSignature.base16Value())
        .publicKey(sourceWallet.publicKey().base16Value())
        .build()
    );
    assertThat(channelVerifyResult.signatureVerified()).isTrue();

    //////////////////////////
    // Destination account submits the signed claim to the ledger to get their XRP
    PaymentChannelClaim paymentChannelClaim = PaymentChannelClaim.builder()
      .account(destinationWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(destinationAccountInfo.accountData().sequence())
      .channel(paymentChannel.channelId())
      .balance(paymentChannel.balance().plus(unsignedClaim.amount()))
      .amount(unsignedClaim.amount())
      .signature(signedClaimSignature.base16Value())
      .publicKey(sourceWallet.publicKey().base16Value())
      .signingPublicKey(destinationWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<PaymentChannelClaim> signedPaymentChannelClaim = signatureService.sign(
      destinationWallet.privateKey(), paymentChannelClaim
    );
    SubmitResult<PaymentChannelClaim> claimResult = xrplClient.submit(signedPaymentChannelClaim);
    assertThat(claimResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "PaymentChannelClaim transaction successful. https://testnet.xrpl.org/transactions/{}",
      claimResult.transactionResult().hash()
    );

    //////////////////////////
    // Validate that the destination account balance has gone up by the claim amount
    this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationWallet.address()),
      infoResult -> infoResult.accountData().balance().equals(
        destinationAccountInfo.accountData().balance()
          .minus(paymentChannelClaim.fee())
          .plus(paymentChannelClaim.balance().get())
      )
    );
  }

  @Test
  void createAddFundsAndSetExpirationToPaymentChannel() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(sourceWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000000))
      .destination(destinationWallet.address())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey().base16Value())
      .cancelAfter(this.instantToXrpTimestamp(Instant.now().plus(Duration.ofMinutes(1))))
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SingleSingedTransaction<PaymentChannelCreate> signedPaymentChannelCreate = signatureService.sign(
      sourceWallet.privateKey(), paymentChannelCreate
    );
    SubmitResult<PaymentChannelCreate> createResult = xrplClient.submit(signedPaymentChannelCreate);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////////
    // Wait for the payment channel to exist in a validated ledger
    // and validate its fields
    PaymentChannelResultObject paymentChannel = scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.address()),
      channels -> channels.channels().stream()
        .anyMatch(channel -> channel.destinationAccount().equals(destinationWallet.address()))
    )
      .channels().stream()
      .filter(channel -> channel.destinationAccount().equals(destinationWallet.address()))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Could not find payment channel for destination address."));

    assertThat(paymentChannel.amount()).isEqualTo(paymentChannelCreate.amount());
    assertThat(paymentChannel.settleDelay()).isEqualTo(paymentChannelCreate.settleDelay());
    assertThat(paymentChannel.publicKeyHex()).isNotEmpty().get().isEqualTo(paymentChannelCreate.publicKey());
    assertThat(paymentChannel.cancelAfter()).isNotEmpty().get().isEqualTo(paymentChannelCreate.cancelAfter().get());

    PaymentChannelFund paymentChannelFund = PaymentChannelFund.builder()
      .account(sourceWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .channel(paymentChannel.channelId())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SingleSingedTransaction<PaymentChannelFund> signedPaymentChannelFund = signatureService.sign(
      sourceWallet.privateKey(), paymentChannelFund
    );
    SubmitResult<PaymentChannelFund> fundResult = xrplClient.submit(signedPaymentChannelFund);
    assertThat(fundResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "PaymentChannelFund transaction successful. https://testnet.xrpl.org/transactions/{}",
      fundResult.transactionResult().hash()
    );

    //////////////////////////
    // Validate that the amount in the channel increased by the fund amount
    scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.address()),
      channelsResult -> channelsResult.channels().stream()
        .anyMatch(
          channel ->
            channel.channelId().equals(paymentChannel.channelId()) &&
              channel.amount().equals(paymentChannel.amount().plus(paymentChannelFund.amount()))
        )
    );

    //////////////////////////
    // Then set a new expiry on the channel by submitting a PaymentChannelFund
    // transaction with an expiration and 1 drop of XRP in the amount field
    UnsignedLong newExpiry = instantToXrpTimestamp(Instant.now())
      .plus(UnsignedLong.valueOf(paymentChannel.settleDelay().longValue()))
      .plus(UnsignedLong.valueOf(30));

    PaymentChannelFund paymentChannelFundWithNewExpiry = PaymentChannelFund.builder()
      .account(sourceWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .channel(paymentChannel.channelId())
      .amount(XrpCurrencyAmount.ofDrops(1))
      .expiration(newExpiry)
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SingleSingedTransaction<PaymentChannelFund> signedPaymentChannelFundWithExpiry = signatureService.sign(
      sourceWallet.privateKey(), paymentChannelFundWithNewExpiry
    );
    SubmitResult<PaymentChannelFund> expiryResult = xrplClient.submit(signedPaymentChannelFundWithExpiry);
    assertThat(expiryResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "PaymentChannelFund transaction successful. https://testnet.xrpl.org/transactions/{}",
      expiryResult.transactionResult().hash()
    );

    //////////////////////////
    // Validate that the expiration was set properly
    scanForResult(
      () -> getValidatedAccountChannels(sourceWallet.address()),
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
  void testCurrentAccountChannels() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////////
    // Create source and destination accounts on ledger
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );

    //////////////////////////
    // Submit a PaymentChannelCreate transaction to create a payment channel between
    // the source and destination accounts
    PaymentChannelCreate createPaymentChannel = PaymentChannelCreate.builder()
      .account(sourceWallet.address())
      .fee(calculateFeeDynamically(feeResult))
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(destinationWallet.address())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey(sourceWallet.publicKey().base16Value())
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    //////////////////////////
    // Validate that the transaction was submitted successfully
    SingleSingedTransaction<PaymentChannelCreate> signedCreatePaymentChannel = signatureService.sign(
      sourceWallet.privateKey(), createPaymentChannel
    );
    SubmitResult<PaymentChannelCreate> createResult = xrplClient.submit(signedCreatePaymentChannel);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "PaymentChannelCreate transaction successful. https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////////
    // Wait for the payment channel to exist in a validated ledger
    // and validate its fields
    AccountChannelsResult accountChannelsResult = scanForResult(
      () -> {
        try {
          return xrplClient.accountChannels(AccountChannelsRequestParams.builder()
            .account(sourceWallet.address())
            .ledgerSpecifier(LedgerSpecifier.CURRENT)
            .build());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      channels -> channels.channels().stream()
        .anyMatch(channel -> channel.destinationAccount().equals(destinationWallet.address()))
    );

    // TODO
    assertThat(accountChannelsResult.ledgerHash()).isNull();
    assertThat(accountChannelsResult.ledgerIndex()).isNull();
    assertThat(accountChannelsResult.ledgerCurrentIndex()).isNotEmpty();
  }
}
