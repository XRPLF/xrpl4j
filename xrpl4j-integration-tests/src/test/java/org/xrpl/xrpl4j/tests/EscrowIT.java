package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import com.ripple.cryptoconditions.der.DerEncodingException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.ledger.EscrowObject;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

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
        .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(10))))
        .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
        .signingPublicKey(senderWallet.publicKey())
        .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
    assertThat(createResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
        "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/" + createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
        () -> this.getValidatedTransaction(
            createResult.transactionResult().hash(),
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
                .orElse(UnsignedLong.ZERO)
                .compareTo(createResult.transactionResult().transaction().finishAfter().orElse(UnsignedLong.MAX_VALUE)) > 0
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
        "EscrowFinish transaction successful: https://testnet.xrpl.org/transactions/" + finishResult.transactionResult().hash()
    );

    //////////////////////
    // Wait for the EscrowFinish to get applied to a validated ledger
    this.scanForResult(
        () -> this.getValidatedTransaction(
            finishResult.transactionResult().hash(),
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
        .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
        .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(1))))
        .signingPublicKey(senderWallet.publicKey())
        .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
    assertThat(createResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
        "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/" + createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
        () -> this.getValidatedTransaction(
            createResult.transactionResult().hash(),
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
            FluentCompareTo.is(ledgerResult.ledger().closeTime().orElse(UnsignedLong.ZERO))
                .greaterThan(
                    createResult.transactionResult().transaction().cancelAfter()
                        .map(cancelAfter -> cancelAfter.plus(UnsignedLong.valueOf(5)))
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
        "EscrowCancel transaction successful: https://testnet.xrpl.org/transactions/" + cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(
        () -> this.getValidatedTransaction(
            cancelResult.transactionResult().hash(),
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
    final byte[] secret = "shh".getBytes();
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
        .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
        .condition(executeEscrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
        .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
    assertThat(createResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
        "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/" + createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
        () -> this.getValidatedTransaction(
            createResult.transactionResult().hash(),
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
            .orElse(UnsignedLong.ZERO)
            .compareTo(createResult.transactionResult().transaction().finishAfter().orElse(UnsignedLong.MAX_VALUE)) > 0
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
        "EscrowFinish transaction successful: https://testnet.xrpl.org/transactions/" + finishResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(
        () -> this.getValidatedTransaction(
            finishResult.transactionResult().hash(),
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
    final byte[] secret = "shh".getBytes();
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
        .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
        .condition(escrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
        .signingPublicKey(senderWallet.publicKey())
        .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(senderWallet, escrowCreate);
    assertThat(createResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
        "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/" + createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
        () -> this.getValidatedTransaction(
            createResult.transactionResult().hash(),
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
                .orElse(UnsignedLong.ZERO)
                .compareTo(
                    createResult.transactionResult().transaction().cancelAfter()
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
        "EscrowCancel transaction successful: https://testnet.xrpl.org/transactions/" + cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(
        () -> this.getValidatedTransaction(
            cancelResult.transactionResult().hash(),
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

  /**
   * Returns the minimum time that can be used for escrow expirations. The ledger will not
   * accept an expiration time that is earlier than the last ledger close time so we must use the latter of
   * current time or ledger close time (which for unexplained reasons can sometimes be later than now).
   * @return
   */
  private Instant getMinExpirationTime() {
    LedgerResult result = getValidatedLedger();
    Instant closeTime =  xrpTimestampToInstant(
        result.ledger().closeTime()
            .orElseThrow(() ->
                new RuntimeException("Ledger close time must be present to calculate a minimum expiration time.")
            )
    );

    Instant now = Instant.now();
    return closeTime.isBefore(now) ? now : closeTime;
  }

}
