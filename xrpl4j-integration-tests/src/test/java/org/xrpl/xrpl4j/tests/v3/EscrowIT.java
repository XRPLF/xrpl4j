package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
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

import java.time.Duration;
import java.time.Instant;

/**
 * Integration test to validate creation, cancellation, and execution of escrow transactions.
 */
public class EscrowIT extends AbstractIT {

  @Test
  public void createAndFinishTimeBasedEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccountEd25519();
    Wallet receiverWallet = createRandomAccountEd25519();

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.address())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.address())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSingedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderWallet.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    //////////////////////
    // Wait until the close time on the current validated ledger is after the finishAfter time on the Escrow
    this.scanForResult(
      this::getValidatedLedger,
      ledgerResult ->
        FluentCompareTo.is(ledgerResult.ledger().closeTime().orElse(UnsignedLong.ZERO))
          .greaterThan(
            createResult.transactionResult().transaction().finishAfter()
              .map(finishAfter -> finishAfter.plus(UnsignedLong.valueOf(5)))
              .orElse(UnsignedLong.MAX_VALUE)
          )
    );

    //////////////////////
    // Receiver submits an EscrowFinish transaction to release the Escrow funds
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.address())
    );
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverWallet.address())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderWallet.address())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverWallet.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowFinish transaction successful: https://testnet.xrpl.org/transactions/{}",
      finishResult.transactionResult().hash()
    );

    //////////////////////
    // Wait for the EscrowFinish to get applied to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class)
    );

    /////////////////////
    // Ensure that the funds were released to the receiver.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.address()),
      infoResult -> infoResult.accountData().balance().equals(
        receiverAccountInfo.accountData().balance()
          .plus(escrowCreate.amount())
          .minus(feeResult.drops().openLedgerFee())
      )
    );

  }

  @Test
  public void createAndCancelTimeBasedEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccountEd25519();
    Wallet receiverWallet = createRandomAccountEd25519();

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address())
    );

    scanForResult(() -> getValidatedAccountInfo(receiverWallet.address()));
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.address())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.address())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(1))))
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSingedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderWallet.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    this.scanForResult(
      () -> this.getValidatedAccountObjects(senderWallet.address()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          EscrowObject.class.isAssignableFrom(object.getClass()) &&
            ((EscrowObject) object).destination().equals(receiverWallet.address())
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
      .account(senderWallet.address())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .owner(senderWallet.address())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<EscrowCancel> signedEscrowCancel = signatureService.sign(
      senderWallet.privateKey(), escrowCancel
    );
    SubmitResult<EscrowCancel> cancelResult = xrplClient.submit(signedEscrowCancel);
    assertThat(cancelResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(() -> this.getValidatedTransaction(cancelResult.transactionResult().hash(), EscrowCancel.class));

    //////////////////////
    // Ensure that the funds were released to the sender.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address()),
      infoResult -> infoResult.accountData().balance().equals(
        senderAccountInfo.accountData().balance()
          .minus(feeResult.drops().openLedgerFee().times(XrpCurrencyAmount.of(UnsignedLong.valueOf(2))))
      )
    );
  }

  @Test
  public void createAndFinishCryptoConditionBasedEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccountEd25519();
    Wallet receiverWallet = createRandomAccountEd25519();

    //////////////////////
    // Create Secret Escrow CryptoCondition/Fulfillment Pair.
    final byte[] secret = "shh".getBytes();
    final PreimageSha256Fulfillment executeEscrowFulfillment = PreimageSha256Fulfillment.from(secret);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.address())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.address())
      .signingPublicKey(senderWallet.publicKey().base16Value())
      // With the fix1571 amendment enabled, you must supply FinishAfter, Condition, or both.
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .condition(executeEscrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSingedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderWallet.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    //////////////////////
    // Wait until the close time on the current validated ledger is after the finishAfter time on the Escrow
    this.scanForResult(
      this::getValidatedLedger,
      ledgerResult ->
        FluentCompareTo.is(ledgerResult.ledger().closeTime().orElse(UnsignedLong.ZERO))
          .greaterThan(
            createResult.transactionResult().transaction().finishAfter()
              .map(cancelAfter -> cancelAfter.plus(UnsignedLong.valueOf(5)))
              .orElse(UnsignedLong.MAX_VALUE)
          )
    );

    //////////////////////
    // Execute the escrow using the secret fulfillment known only to the appropriate party.
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.address())
    );

    final XrpCurrencyAmount feeForFulfillment = EscrowFinish
      .computeFee(feeResult.drops().openLedgerFee(), executeEscrowFulfillment);
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverWallet.address())
      // V-- Be sure to add more fee to process the Fulfillment
      .fee(EscrowFinish.computeFee(feeResult.drops().openLedgerFee(), executeEscrowFulfillment))
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderWallet.address())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverWallet.publicKey().base16Value())
      .condition(executeEscrowFulfillment.getDerivedCondition()) // <-- condition and fulfillment are required.
      .fulfillment(executeEscrowFulfillment) // <-- condition and fulfillment are required to finish an escrow
      .build();

    SingleSingedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverWallet.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowFinish transaction successful: https://testnet.xrpl.org/transactions/{}",
      finishResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(() -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class));

    //////////////////////
    // Ensure that the funds were released to the receiver.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverWallet.address()),
      infoResult -> infoResult.accountData().balance().equals(
        receiverAccountInfo.accountData().balance()
          .plus(escrowCreate.amount())
          .minus(feeForFulfillment)
      )
    );

  }

  @Test
  public void createAndCancelCryptoConditionBasedEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random sender and receiver accounts
    Wallet senderWallet = createRandomAccountEd25519();
    Wallet receiverWallet = createRandomAccountEd25519();

    //////////////////////
    // Create Secret Escrow CryptoCondition/Fulfillment Pair.
    final byte[] secret = "shh".getBytes();
    final PreimageSha256Fulfillment escrowFulfillment = PreimageSha256Fulfillment.from(secret);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderWallet.address())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverWallet.address())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .condition(escrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSingedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderWallet.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
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
      .account(senderWallet.address())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .owner(senderWallet.address())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<EscrowCancel> signedEscrowCancel = signatureService.sign(
      senderWallet.privateKey(), escrowCancel
    );
    SubmitResult<EscrowCancel> cancelResult = xrplClient.submit(signedEscrowCancel);
    assertThat(cancelResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "EscrowCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(() -> this.getValidatedTransaction(cancelResult.transactionResult().hash(), EscrowCancel.class));

    //////////////////////
    // Ensure that the funds were released to the sender.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address()),
      infoResult -> infoResult.accountData().balance().equals(
        senderAccountInfo.accountData().balance()
          .minus(feeResult.drops().openLedgerFee().times(XrpCurrencyAmount.of(UnsignedLong.valueOf(2))))
      )
    );

  }

  /**
   * Returns the minimum time that can be used for escrow expirations. The ledger will not accept an expiration time
   * that is earlier than the last ledger close time, so we must use the latter of current time or ledger close time
   * (which for unexplained reasons can sometimes be later than now).
   *
   * @return An {@link Instant}.
   */
  private Instant getMinExpirationTime() {
    LedgerResult result = getValidatedLedger();
    Instant closeTime = xrpTimestampToInstant(
      result.ledger().closeTime()
        .orElseThrow(() ->
          new RuntimeException("Ledger close time must be present to calculate a minimum expiration time.")
        )
    );

    Instant now = Instant.now();
    return closeTime.isBefore(now) ? now : closeTime;
  }

}
