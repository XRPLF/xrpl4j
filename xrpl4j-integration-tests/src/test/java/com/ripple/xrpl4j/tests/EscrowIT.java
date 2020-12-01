package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import com.ripple.cryptoconditions.der.DerEncodingException;
import com.ripple.xrpl4j.model.client.accounts.AccountInfoResult;
import com.ripple.xrpl4j.model.client.fees.FeeResult;
import com.ripple.xrpl4j.model.ledger.EscrowObject;
import com.ripple.xrpl4j.model.client.transactions.SubmitResult;
import com.ripple.xrpl4j.model.client.transactions.TransactionResult;
import com.ripple.xrpl4j.client.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.EscrowCancel;
import com.ripple.xrpl4j.model.transactions.EscrowCreate;
import com.ripple.xrpl4j.model.transactions.EscrowFinish;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.immutables.FluentCompareTo;
import com.ripple.xrpl4j.wallet.Wallet;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

/**
 * Integration test to validate creation, cancellation, and execution of escrow transactions.
 */
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
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.classicAddress())
      .cancelAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(20))))
      .finishAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(10))))
      .signingPublicKey(senderWallet.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
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
      ledgerResult ->
        ledgerResult
          .ledger()
          .closeTime()
          .compareTo(createResult.transaction().finishAfter().orElse(UnsignedLong.MAX_VALUE)) > 0
    );

    //////////////////////
    // Receiver submits an EscrowFinish transaction to release the Escrow funds
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.classicAddress())
    );
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderWallet.classicAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverWallet.publicKey())
      .build();

    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(receiverWallet, escrowFinish);
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

    /////////////////////
    // Ensure that the funds were released to the receiver.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.classicAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        receiverAccountInfo.accountData().balance()
          .plus(escrowCreate.amount())
          .minus(feeResult.drops().openLedgerFee())
      )
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

    scanForResult(() -> getValidatedAccountInfo(receiverWallet.classicAddress()));
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.classicAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.classicAddress())
      .cancelAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(10))))
      .finishAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(1))))
      .signingPublicKey(senderWallet.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
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

    this.scanForResult(
      () -> this.getValidatedAccountObjects(senderWallet.classicAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
      .anyMatch(object ->
        EscrowObject.class.isAssignableFrom(object.getClass()) &&
          ((EscrowObject) object).destination().equals(receiverWallet.classicAddress())
        )
    );

    //////////////////////
    // Wait until the close time on the current validated ledger is after the cancelAfter time on the Escrow
    this.scanForResult(
      this::getValidatedLedger,
      ledgerResult ->
        FluentCompareTo.is(ledgerResult.ledger().closeTime())
          .greaterThan(
            createResult.transaction().cancelAfter()
              .map(cancelAfter -> cancelAfter.plus(UnsignedLong.valueOf(10)))
              .orElse(UnsignedLong.MAX_VALUE)
          )
    );

    //////////////////////
    // Sender account cancels the Escrow
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(senderWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .owner(senderWallet.classicAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderWallet.publicKey())
      .build();

    SubmitResult<EscrowCancel> cancelResult = xrplClient.submit(senderWallet, escrowCancel);
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

    //////////////////////
    // Ensure that the funds were released to the sender.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        senderAccountInfo.accountData().balance()
          .minus(feeResult.drops().openLedgerFee().times(XrpCurrencyAmount.of(UnsignedLong.valueOf(2))))
      )
    );
  }

  @Test
  public void createAndFinishCryptoConditionBasedEscrow() throws JsonRpcClientErrorException, DerEncodingException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccount();
    Wallet receiverWallet = createRandomAccount();

    //////////////////////
    // Create Secret Escrow CryptoCondition/Fulfillment Pair.
    final byte[] secret = "shh" .getBytes();
    final PreimageSha256Fulfillment executeEscrowFulfillment = PreimageSha256Fulfillment.from(secret);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.classicAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.classicAddress())
      .signingPublicKey(senderWallet.publicKey())
      // With the fix1571 amendment enabled, you must supply FinishAfter, Condition, or both.
      .finishAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(10))))
      .condition(executeEscrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
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
      ledgerResult -> ledgerResult
        .ledger()
        .closeTime()
        .compareTo(createResult.transaction().finishAfter().orElse(UnsignedLong.MAX_VALUE)) > 0
    );

    //////////////////////
    // Execute the escrow using the secret fulfillment known only to the appropriate party.
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.classicAddress())
    );

    final XrpCurrencyAmount feeForFulfillment = EscrowFinish
      .computeFee(feeResult.drops().openLedgerFee(), executeEscrowFulfillment);
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverWallet.classicAddress())
      // V-- Be sure to add more fee to process the Fulfillment
      .fee(EscrowFinish.computeFee(feeResult.drops().openLedgerFee(), executeEscrowFulfillment))
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderWallet.classicAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverWallet.publicKey())
      .condition(executeEscrowFulfillment.getDerivedCondition()) // <-- condition and fulfillment are required.
      .fulfillment(executeEscrowFulfillment) // <-- condition and fulfillment are required to finish an escrow
      .build();

    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(receiverWallet, escrowFinish);
    assertThat(finishResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowFinish transaction successful: https://testnet.xrpl.org/transactions/" + finishResult.transaction().hash()
        .orElse("n/a")
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        finishResult.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Cannot look up transaction because no hash was returned.")),
        EscrowFinish.class
      )
    );

    //////////////////////
    // Ensure that the funds were released to the receiver.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.classicAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        receiverAccountInfo.accountData().balance()
          .plus(escrowCreate.amount())
          .minus(feeForFulfillment)
      )
    );

  }

  @Test
  public void createAndCancelCryptoConditionBasedEscrow() throws JsonRpcClientErrorException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccount();
    Wallet receiverWallet = createRandomAccount();

    //////////////////////
    // Create Secret Escrow CryptoCondition/Fulfillment Pair.
    final byte[] secret = "shh" .getBytes();
    final PreimageSha256Fulfillment escrowFulfillment = PreimageSha256Fulfillment.from(secret);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.classicAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.classicAddress())
      .cancelAfter(instantToXrpTimestamp(Instant.now().plus(Duration.ofSeconds(10))))
      .condition(escrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
      .signingPublicKey(senderWallet.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
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
      ledgerResult ->
        ledgerResult
          .ledger()
          .closeTime()
          .compareTo(
            createResult.transaction().cancelAfter()
              .map(cancelAfter -> cancelAfter.plus(UnsignedLong.valueOf(5)))
              .orElse(UnsignedLong.MAX_VALUE)
          ) > 0
    );

    //////////////////////
    // Sender account cancels the Escrow
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(senderWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .owner(senderWallet.classicAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderWallet.publicKey())
      .build();

    SubmitResult<EscrowCancel> cancelResult = xrplClient.submit(senderWallet, escrowCancel);
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

    //////////////////////
    // Ensure that the funds were released to the sender.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.classicAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        senderAccountInfo.accountData().balance()
          .minus(feeResult.drops().openLedgerFee().times(XrpCurrencyAmount.of(UnsignedLong.valueOf(2))))
      )
    );

  }

}
