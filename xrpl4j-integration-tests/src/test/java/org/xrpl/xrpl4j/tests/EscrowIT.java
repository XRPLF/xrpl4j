package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import static org.assertj.core.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.EscrowLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.ledger.EscrowObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.flags.MpTokenAuthorizeFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransferFee;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.Duration;
import java.util.List;

/**
 * Integration test to validate creation, cancellation, and execution of escrow transactions.
 */
public class EscrowIT extends AbstractIT {

  @Test
  public void createAndFinishTimeBasedEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random sender and receiver accounts
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      senderKeyPair.publicKey().deriveAddress(),
      escrowCreate.sequence()
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
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverKeyPair.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      finishResult.transactionResult().transaction().transactionType(),
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
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        receiverAccountInfo.accountData().balance()
          .plus(escrowCreate.amount()
            .map(
              xrpCurrencyAmount -> xrpCurrencyAmount,
              issuedCurrencyAmount -> fail("Shouldn't be issued currency amount"),
              mptCurrencyAmount -> fail("Shouldn't be MPT currency amount")
            )
            .minus(feeResult.drops().openLedgerFee())
          )
      )
    );

  }

  @Test
  public void createAndCancelTimeBasedEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random sender and receiver accounts
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    scanForResult(() -> getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress()));
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(10))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    final TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    this.scanForResult(
      () -> this.getValidatedAccountObjects(senderKeyPair.publicKey().deriveAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          EscrowObject.class.isAssignableFrom(object.getClass()) &&
            ((EscrowObject) object).destination().equals(receiverKeyPair.publicKey().deriveAddress())
        )
    );

    assertEntryEqualsObjectFromAccountObjects(
      senderKeyPair.publicKey().deriveAddress(),
      escrowCreate.sequence()
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
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCancel> signedEscrowCancel = signatureService.sign(
      senderKeyPair.privateKey(), escrowCancel
    );
    SubmitResult<EscrowCancel> cancelResult = xrplClient.submit(signedEscrowCancel);
    assertThat(cancelResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      cancelResult.transactionResult().transaction().transactionType(),
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(() -> this.getValidatedTransaction(cancelResult.transactionResult().hash(), EscrowCancel.class));

    //////////////////////
    // Ensure that the funds were released to the sender.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress()),
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
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Create Secret Escrow CryptoCondition/Fulfillment Pair.
    final byte[] secret = "shh".getBytes();
    final PreimageSha256Fulfillment executeEscrowFulfillment = PreimageSha256Fulfillment.from(secret);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .signingPublicKey(senderKeyPair.publicKey())
      // With the fix1571 amendment enabled, you must supply FinishAfter, Condition, or both.
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .condition(executeEscrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      senderKeyPair.publicKey().deriveAddress(),
      escrowCreate.sequence()
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
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    final XrpCurrencyAmount feeForFulfillment = EscrowFinish
      .computeFee(feeResult.drops().openLedgerFee(), executeEscrowFulfillment);
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      // V-- Be sure to add more fee to process the Fulfillment
      .fee(EscrowFinish.computeFee(feeResult.drops().openLedgerFee(), executeEscrowFulfillment))
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .condition(executeEscrowFulfillment.getDerivedCondition()) // <-- condition and fulfillment are required.
      .fulfillment(executeEscrowFulfillment) // <-- condition and fulfillment are required to finish an escrow
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverKeyPair.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      finishResult.transactionResult().transaction().transactionType(),
      finishResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(() -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class));

    //////////////////////
    // Ensure that the funds were released to the receiver.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        receiverAccountInfo.accountData().balance()
          .plus(escrowCreate.amount()
            .map(
              xrpCurrencyAmount -> xrpCurrencyAmount,
              issuedCurrencyAmount -> fail("Shouldn't be issued currency amount"),
              mptCurrencyAmount -> fail("Shouldn't be MPT currency amount")
            )
          )
          .minus(feeForFulfillment)
      )
    );

  }

  @Test
  public void createAndCancelCryptoConditionBasedEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random sender and receiver accounts
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Create Secret Escrow CryptoCondition/Fulfillment Pair.
    final byte[] secret = "shh".getBytes();
    final PreimageSha256Fulfillment escrowFulfillment = PreimageSha256Fulfillment.from(secret);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(10))))
      .condition(escrowFulfillment.getDerivedCondition()) // <-- Only the fulfillment holder can execute this.
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      senderKeyPair.publicKey().deriveAddress(),
      escrowCreate.sequence()
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
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCancel> signedEscrowCancel = signatureService.sign(
      senderKeyPair.privateKey(), escrowCancel
    );
    SubmitResult<EscrowCancel> cancelResult = xrplClient.submit(signedEscrowCancel);
    assertThat(cancelResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      cancelResult.transactionResult().transaction().transactionType(),
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Wait until the transaction enters a validated ledger
    this.scanForResult(() -> this.getValidatedTransaction(cancelResult.transactionResult().hash(), EscrowCancel.class));

    //////////////////////
    // Ensure that the funds were released to the sender.
    this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        senderAccountInfo.accountData().balance()
          .minus(feeResult.drops().openLedgerFee().times(XrpCurrencyAmount.of(UnsignedLong.valueOf(2))))
      )
    );

  }

  @Test
  public void createAndFinishTimeBasedEscrowWithPreAuthorization()
    throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Create random sender, receiver and issuer accounts
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();
    KeyPair issuerKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Create and accept credentials.
    CredentialType[] goodCredentials =
      {CredentialType.ofPlainText("driver licence"), CredentialType.ofPlainText("voting card")};

    createAndAcceptCredentials(issuerKeyPair, senderKeyPair, goodCredentials);

    // Receiver enables Deposit Authorization account setting.
    enableDepositAuthorization(receiverKeyPair);

    // Receiver pre-authorizes goodCredentials from the issuer.
    preAuthorizeCredentials(issuerKeyPair, receiverKeyPair, goodCredentials);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    final TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      senderKeyPair.publicKey().deriveAddress(),
      escrowCreate.sequence()
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
    // Escrow creator submits EscrowFinish transaction with CredentialIds.
    senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    final AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    List<Hash256> credentialObjectIds = getCredentialObjectIds(issuerKeyPair, senderKeyPair, goodCredentials);

    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .credentialIds(credentialObjectIds)
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      senderKeyPair.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      finishResult.transactionResult().transaction().transactionType(),
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
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().balance().equals(
        receiverAccountInfo.accountData().balance()
          .plus(escrowCreate.amount().map(
              xrpCurrencyAmount -> xrpCurrencyAmount,
              issuedCurrencyAmount -> fail("Shouldn't be issued currency amount"),
              mptCurrencyAmount -> fail("Shouldn't be MPT currency amount")
            )
          )
      )
    );
  }

  @Test
  public void createAndFinishTimeBasedEscrowWithPreAuthorizationFailure()
    throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Create random sender, receiver and issuer accounts
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();
    KeyPair issuerKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Create and accept credentials.
    CredentialType[] goodCredentials =
      {CredentialType.ofPlainText("driver licence"), CredentialType.ofPlainText("voting card")};

    createAndAcceptCredentials(issuerKeyPair, senderKeyPair, goodCredentials);

    // Receiver enables Deposit Authorization account setting.
    enableDepositAuthorization(receiverKeyPair);

    // Receiver pre-authorizes goodCredentials from the issuer.
    preAuthorizeCredentials(issuerKeyPair, receiverKeyPair, goodCredentials);

    //////////////////////
    // Sender account creates an Escrow with the receiver account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(XrpCurrencyAmount.ofDrops(123456))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    //////////////////////
    // Submit the EscrowCreate transaction and validate that it was successful
    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    //////////////////////
    // Then wait until the transaction gets committed to a validated ledger
    final TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      senderKeyPair.publicKey().deriveAddress(),
      escrowCreate.sequence()
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
    // Escrow creator submits EscrowFinish transaction with some missing CredentialIds.
    senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    List<Hash256> credentialObjectIds = getCredentialObjectIds(issuerKeyPair, senderKeyPair, goodCredentials);

    // Not passing all the credentialIds.
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .credentialIds(credentialObjectIds.subList(0, 1))
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      senderKeyPair.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tecNO_PERMISSION");
  }

  private void assertEntryEqualsObjectFromAccountObjects(
    Address escrowOwner,
    UnsignedInteger createSequence
  ) throws JsonRpcClientErrorException {
    EscrowObject escrowObject = (EscrowObject) this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.ESCROW)
            .account(escrowOwner)
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.size() == 1
    ).get(0);

    LedgerEntryResult<EscrowObject> escrowEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.escrow(
        EscrowLedgerEntryParams.builder()
          .owner(escrowOwner)
          .seq(createSequence)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(escrowEntry.node()).isEqualTo(escrowObject);

    LedgerEntryResult<EscrowObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(escrowObject.index(), EscrowObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(escrowEntry.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(escrowObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());
  }

  @Test
  public void createAndFinishIssuedCurrencyEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random issuer, sender and receiver accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Enable ALLOW_TRUSTLINE_LOCKING on issuer account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet enableLocking = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_LOCKING)
      .build();

    SingleSignedTransaction<AccountSet> signedEnableLocking = signatureService.sign(
      issuerKeyPair.privateKey(), enableLocking
    );
    SubmitResult<AccountSet> enableLockingResult = xrplClient.submit(signedEnableLocking);
    assertThat(enableLockingResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(
      () -> this.getValidatedTransaction(enableLockingResult.transactionResult().hash(), AccountSet.class));

    //////////////////////
    // Set a transfer rate on the issuer account (1% = 1,010,000,000)
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet setTransferRate = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .transferRate(UnsignedInteger.valueOf(1010000000L)) // 1% transfer fee
      .build();

    SingleSignedTransaction<AccountSet> signedSetTransferRate = signatureService.sign(
      issuerKeyPair.privateKey(), setTransferRate
    );
    SubmitResult<AccountSet> setTransferRateResult = xrplClient.submit(signedSetTransferRate);
    assertThat(setTransferRateResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(
      () -> this.getValidatedTransaction(setTransferRateResult.transactionResult().hash(), AccountSet.class));

    //////////////////////
    // Create trustlines from sender and receiver to issuer
    String currency = "USD";
    IssuedCurrencyAmount trustLimit = IssuedCurrencyAmount.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(currency)
      .value("10000")
      .build();

    TrustLine senderTrustLine = createTrustLine(
      senderKeyPair,
      trustLimit,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );
    logger.info("Sender trustline created: {}", senderTrustLine);

    TrustLine receiverTrustLine = createTrustLine(
      receiverKeyPair,
      trustLimit,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );
    logger.info("Receiver trustline created: {}", receiverTrustLine);

    //////////////////////
    // Issue 1000 USD to sender
    sendIssuedCurrency(
      issuerKeyPair,
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency(currency)
        .value("1000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Sender creates an IOU Escrow with the receiver
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    IssuedCurrencyAmount escrowAmount = IssuedCurrencyAmount.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(currency)
      .value("100")
      .build();

    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(escrowAmount)
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    //////////////////////
    // Verify the escrow object was created with transferRate field locked at 1%
    assertEntryEqualsObjectFromAccountObjects(
      senderKeyPair.publicKey().deriveAddress(),
      escrowCreate.sequence()
    );

    // Get the escrow object and verify the transfer rate is locked
    EscrowObject escrowObject = (EscrowObject) this.scanForResult(
        () -> this.getValidatedAccountObjects(senderKeyPair.publicKey().deriveAddress()),
        objectsResult -> objectsResult.accountObjects().stream()
          .anyMatch(object -> EscrowObject.class.isAssignableFrom(object.getClass()))
      ).accountObjects().stream()
      .filter(object -> EscrowObject.class.isAssignableFrom(object.getClass()))
      .map(object -> (EscrowObject) object)
      .filter(escrow -> escrow.previousTransactionId()
        .map(txId -> txId.equals(createResult.transactionResult().hash()))
        .orElse(false))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Escrow object not found"));

    assertThat(escrowObject.transferRate()).isPresent();
    assertThat(escrowObject.transferRate().orElse(UnsignedInteger.ZERO)).isEqualTo(
      UnsignedInteger.valueOf(1010000000L)
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
    // Receiver finishes the escrow
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverKeyPair.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      finishResult.transactionResult().transaction().transactionType(),
      finishResult.transactionResult().hash()
    );

    this.scanForResult(() -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class));
  }

  @Test
  public void iouEscrowLocksTransferRateAtCreation() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random issuer, sender and receiver accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Enable ALLOW_TRUSTLINE_LOCKING and set initial transfer rate on issuer account
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Enable locking and set 1% transfer rate in one transaction
    AccountSet enableLockingAndSetRate = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_LOCKING)
      .transferRate(UnsignedInteger.valueOf(1010000000L)) // 1% transfer fee
      .build();

    SingleSignedTransaction<AccountSet> signedEnableLockingAndSetRate = signatureService.sign(
      issuerKeyPair.privateKey(), enableLockingAndSetRate
    );
    SubmitResult<AccountSet> enableLockingAndSetRateResult = xrplClient.submit(signedEnableLockingAndSetRate);
    assertThat(enableLockingAndSetRateResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(
      () -> this.getValidatedTransaction(enableLockingAndSetRateResult.transactionResult().hash(), AccountSet.class));

    //////////////////////
    // Create trustlines from sender and receiver to issuer
    String currency = "USD";
    IssuedCurrencyAmount trustLimit = IssuedCurrencyAmount.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(currency)
      .value("10000")
      .build();

    createTrustLine(senderKeyPair, trustLimit, FeeUtils.computeNetworkFees(feeResult).recommendedFee());
    createTrustLine(receiverKeyPair, trustLimit, FeeUtils.computeNetworkFees(feeResult).recommendedFee());

    //////////////////////
    // Issue 1000 USD to sender
    sendIssuedCurrency(
      issuerKeyPair,
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency(currency)
        .value("1000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Sender creates an IOU Escrow with the receiver
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    IssuedCurrencyAmount escrowAmount = IssuedCurrencyAmount.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .currency(currency)
      .value("100")
      .build();

    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(escrowAmount)
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

    TransactionResult<EscrowCreate> result = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class)
    );

    //////////////////////
    // Verify the escrow object has the transfer rate locked at 1%
    EscrowObject escrowObjectBefore = (EscrowObject) this.scanForResult(
        () -> this.getValidatedAccountObjects(senderKeyPair.publicKey().deriveAddress()),
        objectsResult -> objectsResult.accountObjects().stream()
          .anyMatch(object -> EscrowObject.class.isAssignableFrom(object.getClass()))
      ).accountObjects().stream()
      .filter(object -> EscrowObject.class.isAssignableFrom(object.getClass()))
      .map(object -> (EscrowObject) object)
      .filter(escrow -> escrow.previousTransactionId()
        .map(txId -> txId.equals(createResult.transactionResult().hash()))
        .orElse(false))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Escrow object not found"));

    assertThat(escrowObjectBefore.transferRate()).isPresent();
    assertThat(escrowObjectBefore.transferRate().orElse(UnsignedInteger.ZERO)).isEqualTo(
      UnsignedInteger.valueOf(1010000000L)
    );

    //////////////////////
    // Change the issuer's transfer rate to 2% AFTER the escrow was created
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet changeTransferRate = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .transferRate(UnsignedInteger.valueOf(1020000000L)) // Change to 2% transfer fee
      .build();

    SingleSignedTransaction<AccountSet> signedChangeTransferRate = signatureService.sign(
      issuerKeyPair.privateKey(), changeTransferRate
    );
    SubmitResult<AccountSet> changeTransferRateResult = xrplClient.submit(signedChangeTransferRate);
    assertThat(changeTransferRateResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(
      () -> this.getValidatedTransaction(changeTransferRateResult.transactionResult().hash(), AccountSet.class));

    //////////////////////
    // Verify the escrow object STILL has the transfer rate locked at 1% (not 2%)
    EscrowObject escrowObjectAfter = (EscrowObject) this.scanForResult(
        () -> this.getValidatedAccountObjects(senderKeyPair.publicKey().deriveAddress()),
        objectsResult -> objectsResult.accountObjects().stream()
          .anyMatch(object -> EscrowObject.class.isAssignableFrom(object.getClass()))
      ).accountObjects().stream()
      .filter(object -> EscrowObject.class.isAssignableFrom(object.getClass()))
      .map(object -> (EscrowObject) object)
      .filter(escrow -> escrow.previousTransactionId()
        .map(txId -> txId.equals(createResult.transactionResult().hash()))
        .orElse(false))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Escrow object not found"));

    assertThat(escrowObjectAfter.transferRate()).isPresent();
    // The transfer rate should STILL be 1%, not 2%, proving it was locked at escrow creation
    assertThat(escrowObjectAfter.transferRate().orElse(UnsignedInteger.ZERO)).isEqualTo(
      UnsignedInteger.valueOf(1010000000L)
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
    // Receiver finishes the escrow - the locked 1% rate should be applied, not the current 2% rate
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverKeyPair.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      finishResult.transactionResult().transaction().transactionType(),
      finishResult.transactionResult().hash()
    );

    this.scanForResult(() -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class));

    // TODO: Verify the receiver received the correct amount with the locked 1% transfer rate applied
    // This would require checking the receiver's balance and calculating the expected amount after the 1% fee
  }

  @Test
  public void createAndFinishMptEscrow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random issuer, sender and receiver accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Create MPT issuance
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.builder()
      .tfMptCanEscrow(true)
      .tfMptCanTransfer(true)
      .build();

    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(100)))
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .mpTokenMetadata(MpTokenMetadata.of("ABCD"))
      .flags(flags)
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(),
      issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo("tesSUCCESS");

    this.scanForResult(() -> this.getValidatedTransaction(
      issuanceCreateResult.transactionResult().hash(),
      MpTokenIssuanceCreate.class
    ));

    MpTokenIssuanceId mpTokenIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    //////////////////////
    // Sender and receiver authorize the MPT
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize senderAuthorize = MpTokenAuthorize.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(senderKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedSenderAuthorize = signatureService.sign(
      senderKeyPair.privateKey(),
      senderAuthorize
    );
    SubmitResult<MpTokenAuthorize> senderAuthorizeResult = xrplClient.submit(signedSenderAuthorize);
    assertThat(senderAuthorizeResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(
      senderAuthorizeResult.transactionResult().hash(),
      MpTokenAuthorize.class
    ));

    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize receiverAuthorize = MpTokenAuthorize.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .sequence(receiverAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(receiverKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedReceiverAuthorize = signatureService.sign(
      receiverKeyPair.privateKey(),
      receiverAuthorize
    );
    SubmitResult<MpTokenAuthorize> receiverAuthorizeResult = xrplClient.submit(signedReceiverAuthorize);
    assertThat(receiverAuthorizeResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(
      receiverAuthorizeResult.transactionResult().hash(),
      MpTokenAuthorize.class
    ));

    //////////////////////
    // Mint MPT tokens to sender
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("100000")
      .build();

    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(senderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(issuerKeyPair.privateKey(), mint);
    SubmitResult<Payment> mintResult = xrplClient.submit(signedMint);
    assertThat(mintResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(mintResult.transactionResult().hash(), Payment.class));

    //////////////////////
    // Sender creates an MPT Escrow with the receiver
    senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount escrowAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("10000")
      .build();

    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(escrowAmount)
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100))))
      .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5))))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      createResult.transactionResult().transaction().transactionType(),
      createResult.transactionResult().hash()
    );

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
    // Receiver finishes the escrow
    receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(result.transaction().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverKeyPair.privateKey(), escrowFinish
    );
    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logInfo(
      finishResult.transactionResult().transaction().transactionType(),
      finishResult.transactionResult().hash()
    );

    this.scanForResult(() -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class));
  }

  @Test
  void iouEscrowFailsWithoutAllowTrustlineLockingFlag() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create issuer, sender, and receiver accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    //////////////////////
    // NOTE: We intentionally DO NOT set the ALLOW_TRUSTLINE_LOCKING flag on the issuer
    // This should cause the EscrowCreate to fail with tecNO_PERMISSION

    //////////////////////
    // Create trustlines for sender and receiver
    createTrustLine(
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("10000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    createTrustLine(
      receiverKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("10000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Send some IOU tokens to the sender
    sendIssuedCurrency(
      issuerKeyPair,
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("1000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Try to create an escrow with IOU tokens WITHOUT the ALLOW_TRUSTLINE_LOCKING flag
    // This should fail with tecNO_PERMISSION
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    UnsignedLong finishAfter = UnsignedLong.valueOf(System.currentTimeMillis() / 1000 + 5);
    UnsignedLong cancelAfter = finishAfter.plus(UnsignedLong.valueOf(95));

    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("100")
        .build())
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(cancelAfter)
      .finishAfter(finishAfter)
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );

    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);

    //////////////////////
    // Verify the transaction failed with tecNO_PERMISSION
    assertThat(createResult.engineResult()).isEqualTo("tecNO_PERMISSION");
    logger.info("EscrowCreate correctly failed with tecNO_PERMISSION when ALLOW_TRUSTLINE_LOCKING flag is not set");
  }

  @Test
  void iouEscrowFailsWithFrozenTrustline() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create issuer, sender, and receiver accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    //////////////////////
    // Set ALLOW_TRUSTLINE_LOCKING flag on issuer
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet enableLocking = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_LOCKING)
      .build();

    SingleSignedTransaction<AccountSet> signedEnableLocking = signatureService.sign(
      issuerKeyPair.privateKey(), enableLocking
    );
    xrplClient.submit(signedEnableLocking);
    this.scanForResult(() -> this.getValidatedTransaction(signedEnableLocking.hash(), AccountSet.class));

    //////////////////////
    // Create trustlines for sender and receiver
    createTrustLine(
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("10000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    createTrustLine(
      receiverKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("10000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Send some IOU tokens to the sender
    sendIssuedCurrency(
      issuerKeyPair,
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("1000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Freeze the sender's trustline
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    TrustSet freezeTrustline = TrustSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .issuer(senderKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("0")
        .build())
      .flags(TrustSetFlags.builder()
        .tfSetFreeze()
        .build())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedFreeze = signatureService.sign(
      issuerKeyPair.privateKey(), freezeTrustline
    );
    xrplClient.submit(signedFreeze);
    this.scanForResult(() -> this.getValidatedTransaction(signedFreeze.hash(), TrustSet.class));

    //////////////////////
    // Try to create an escrow with frozen IOU tokens
    // This should fail with tecFROZEN
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    UnsignedLong finishAfter = UnsignedLong.valueOf(System.currentTimeMillis() / 1000 + 5);
    UnsignedLong cancelAfter = finishAfter.plus(UnsignedLong.valueOf(95));

    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("100")
        .build())
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(cancelAfter)
      .finishAfter(finishAfter)
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );

    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);

    //////////////////////
    // Verify the transaction failed with tecFROZEN
    assertThat(createResult.engineResult()).isEqualTo("tecFROZEN");
    logger.info("EscrowCreate correctly failed with tecFROZEN when trustline is frozen");
  }

  @Test
  void iouEscrowAutoCreatesTrustlineOnFinish() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // This test verifies that when an IOU escrow is finished, if the receiver doesn't have a trustline,
    // it will be automatically created (assuming the receiver has enough XRP reserve).
    // Note: Auto-creation only works if the issuer doesn't require authorization.

    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    //////////////////////
    // Set ALLOW_TRUSTLINE_LOCKING flag on issuer
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet enableLocking = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_LOCKING)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedEnableLocking = signatureService.sign(
      issuerKeyPair.privateKey(), enableLocking
    );
    xrplClient.submit(signedEnableLocking);
    this.scanForResult(() -> this.getValidatedTransaction(signedEnableLocking.hash(), AccountSet.class));

    //////////////////////
    // Create trustline for sender ONLY (not for receiver - it will be auto-created)
    createTrustLine(
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("10000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Send some IOU tokens to the sender
    sendIssuedCurrency(
      issuerKeyPair,
      senderKeyPair,
      IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("1000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    //////////////////////
    // Create escrow with IOU tokens
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    UnsignedLong cancelAfter = instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100)));
    UnsignedLong finishAfter = instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5)));

    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .currency("USD")
        .value("100")
        .build())
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(cancelAfter)
      .finishAfter(finishAfter)
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );

    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info("EscrowCreate transaction successful: " + createResult.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class));

    //////////////////////
    // Wait until the close time on the current validated ledger is after the finishAfter time on the Escrow
    this.scanForResult(
      this::getValidatedLedger,
      ledgerResult ->
        FluentCompareTo.is(ledgerResult.ledger().closeTime().orElse(UnsignedLong.ZERO))
          .greaterThan(
            createResult.transactionResult().transaction().finishAfter()
              .map(finishAfter_ -> finishAfter_.plus(UnsignedLong.valueOf(5)))
              .orElse(UnsignedLong.MAX_VALUE)
          )
    );

    //////////////////////
    // Finish the escrow - this should auto-create the trustline for the receiver
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverKeyPair.privateKey(), escrowFinish
    );

    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info("EscrowFinish transaction successful - trustline was auto-created for receiver: " +
      finishResult.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class));

    logger.info("Verified: Trustline was automatically created for receiver during EscrowFinish");
  }

  @Test
  void mptEscrowAutoCreatesMpTokenOnFinish() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // This test verifies that when an MPT escrow is finished, if the receiver doesn't have an MPToken,
    // it will be automatically created (assuming the receiver has enough XRP reserve).

    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    //////////////////////
    // Create MPT issuance
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.builder()
      .tfMptCanEscrow(true)
      .tfMptCanTransfer(true)
      .build();

    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(100)))
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .mpTokenMetadata(MpTokenMetadata.of("ABCD"))
      .flags(flags)
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(),
      issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo("tesSUCCESS");

    this.scanForResult(() -> this.getValidatedTransaction(
      issuanceCreateResult.transactionResult().hash(),
      MpTokenIssuanceCreate.class
    ));

    MpTokenIssuanceId mpTokenIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    //////////////////////
    // Sender authorizes the MPT ONLY (not receiver - it will be auto-created)
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize senderAuthorize = MpTokenAuthorize.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(senderKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedSenderAuthorize = signatureService.sign(
      senderKeyPair.privateKey(),
      senderAuthorize
    );
    SubmitResult<MpTokenAuthorize> senderAuthorizeResult = xrplClient.submit(signedSenderAuthorize);
    assertThat(senderAuthorizeResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(
      senderAuthorizeResult.transactionResult().hash(),
      MpTokenAuthorize.class
    ));

    //////////////////////
    // Mint MPT tokens to sender
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("100000")
      .build();

    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(senderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(issuerKeyPair.privateKey(), mint);
    SubmitResult<Payment> mintResult = xrplClient.submit(signedMint);
    assertThat(mintResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(mintResult.transactionResult().hash(), Payment.class));

    //////////////////////
    // Sender creates an MPT Escrow with the receiver
    senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount escrowAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("10000")
      .build();

    UnsignedLong cancelAfter = instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(100)));
    UnsignedLong finishAfter = instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(5)));

    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .sequence(senderAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .amount(escrowAmount)
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .cancelAfter(cancelAfter)
      .finishAfter(finishAfter)
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
      senderKeyPair.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> createResult = xrplClient.submit(signedEscrowCreate);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info("EscrowCreate transaction successful: " + createResult.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(createResult.transactionResult().hash(), EscrowCreate.class));

    //////////////////////
    // Wait until the close time on the current validated ledger is after the finishAfter time on the Escrow
    this.scanForResult(
      this::getValidatedLedger,
      ledgerResult ->
        FluentCompareTo.is(ledgerResult.ledger().closeTime().orElse(UnsignedLong.ZERO))
          .greaterThan(
            createResult.transactionResult().transaction().finishAfter()
              .map(finishAfter_ -> finishAfter_.plus(UnsignedLong.valueOf(5)))
              .orElse(UnsignedLong.MAX_VALUE)
          )
    );

    //////////////////////
    // Finish the escrow - this should auto-create the MPToken for the receiver
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress())
    );

    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .owner(senderKeyPair.publicKey().deriveAddress())
      .offerSequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .build();

    SingleSignedTransaction<EscrowFinish> signedEscrowFinish = signatureService.sign(
      receiverKeyPair.privateKey(), escrowFinish
    );

    SubmitResult<EscrowFinish> finishResult = xrplClient.submit(signedEscrowFinish);
    assertThat(finishResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info("EscrowFinish transaction successful - MPToken was auto-created for receiver: " +
      finishResult.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(finishResult.transactionResult().hash(), EscrowFinish.class));

    logger.info("Verified: MPToken was automatically created for receiver during EscrowFinish");
  }

}
