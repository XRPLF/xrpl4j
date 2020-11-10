package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.client.model.accounts.AccountInfoResult;
import com.ripple.xrpl4j.client.model.fees.FeeResult;
import com.ripple.xrpl4j.client.model.transactions.SubmissionResult;
import com.ripple.xrpl4j.client.model.transactions.TransactionResult;
import com.ripple.xrpl4j.client.rippled.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.EscrowCancel;
import com.ripple.xrpl4j.model.transactions.EscrowCreate;
import com.ripple.xrpl4j.model.transactions.EscrowFinish;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public class EscrowIT extends AbstractIT {

  @Test
  public void createAndFinishTimeBasedEscrow() throws JsonRpcClientErrorException, InterruptedException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccount();
    Wallet receiverWallet = createRandomAccount();

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.classicAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().minimumFee())
      .amount(XrpCurrencyAmount.of("123456"))
      .destination(receiverWallet.classicAddress())
      .cancelAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(20))))
      .finishAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(10))))
      .signingPublicKey(senderWallet.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmissionResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
    assertThat(createResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/" + createResult.transaction().hash()
        .orElse("n/a")
    );


    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(
        createResult.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Cannot look up transaction because no hash was returned.")),
        EscrowCreate.class
      )
    );

    //////////////////////
    // Wait until the close time on the current validated ledger is after the finishAfter time on the Escrow
    this.scanForResult(
      this::getValidatedLedger,
      ledgerResult -> {
        logger.info("Ledger close: {}. Escrow finishAfter: {}.", ledgerResult.ledger().closeTime(), createResult.transaction().finishAfter().orElse(UnsignedLong.ZERO));
        return ledgerResult
          .ledger()
          .closeTime()
          .compareTo(createResult.transaction().finishAfter().orElse(UnsignedLong.MAX_VALUE)) > 0;
      }
    );

    //////////////////////
    // Receiver submits an EscrowFinish transaction to release the Escrow funds
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.classicAddress())
    );
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderWallet.classicAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverWallet.publicKey())
      .build();

    SubmissionResult<EscrowFinish> finishResult = xrplClient.submit(receiverWallet, escrowFinish);
    assertThat(finishResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowFinish transaction successful: https://testnet.xrpl.org/transactions/" + finishResult.transaction().hash()
        .orElse("n/a")
    );

    //////////////////////
    // Wait for the EscrowFinish to get applied to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        finishResult.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Cannot look up transaction because no hash was returned.")),
        EscrowFinish.class
      )
    );

    AccountInfoResult receiverAccountInfoAfterFinish = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.classicAddress())
    );

    //////////////////////
    // Ensure that the funds were released to the receiver.
    assertThat(receiverAccountInfoAfterFinish.accountData().balance().value()).isEqualTo(
      UnsignedLong.valueOf(receiverAccountInfo.accountData().balance().value())
        .plus(UnsignedLong.valueOf(escrowCreate.amount().value()))
        .minus(UnsignedLong.valueOf(feeResult.drops().minimumFee().value())).toString()
    );
  }

  @Test
  public void createAndCancelTimeBasedEscrow() throws JsonRpcClientErrorException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccount();
    Wallet receiverWallet = createRandomAccount();

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.classicAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().minimumFee())
      .amount(XrpCurrencyAmount.of("123456"))
      .destination(receiverWallet.classicAddress())
      .cancelAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(10))))
      .finishAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(1))))
      .signingPublicKey(senderWallet.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmissionResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
    assertThat(createResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/" + createResult.transaction().hash()
        .orElse("n/a")
    );


    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(
        createResult.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Cannot look up transaction because no hash was returned.")),
        EscrowCreate.class
      )
    );

    //////////////////////
    // Wait until the close time on the current validated ledger is after the cancelAfter time on the Escrow
    this.scanForResult(
      this::getValidatedLedger,
      ledgerResult -> {
        logger.info("Ledger close: {}. Escrow cancelAfter: {}.",
          ledgerResult.ledger().closeTime(),
          createResult.transaction().cancelAfter().orElse(UnsignedLong.ZERO)
        );
        return ledgerResult
          .ledger()
          .closeTime()
          .compareTo(createResult.transaction().cancelAfter().orElse(UnsignedLong.MAX_VALUE)) > 0;
      }
    );

    //////////////////////
    // Sender account cancels the Escrow
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(senderWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .owner(senderWallet.classicAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderWallet.publicKey())
      .build();

    SubmissionResult<EscrowCancel> cancelResult = xrplClient.submit(senderWallet, escrowCancel);
    assertThat(cancelResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCancel transaction successful: https://testnet.xrpl.org/transactions/" + cancelResult.transaction().hash()
        .orElse("n/a")
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        cancelResult.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Cannot look up transaction because no hash was returned.")),
        EscrowFinish.class
      )
    );

    AccountInfoResult senderAccountInfoAfterCancel = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress())
    );

    //////////////////////
    // Ensure that the funds were released to the sender.
    assertThat(senderAccountInfoAfterCancel.accountData().balance().value()).isEqualTo(
      UnsignedLong.valueOf(senderAccountInfo.accountData().balance().value())
        .minus(UnsignedLong.valueOf(feeResult.drops().minimumFee().value()).times(UnsignedLong.valueOf(2))).toString()
    );
  }
}
