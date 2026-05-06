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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.ledger.CheckObject;
import org.xrpl.xrpl4j.model.ledger.EscrowObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.function.Predicate;

/**
 * Integration tests to validate submission of Check transactions.
 */
@SuppressWarnings( {"OptionalGetWithoutIsPresent"})
public class CheckIT extends AbstractIT {

  @Test
  public void createXrpCheckAndCash() throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Generate and fund source and destination accounts
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Create a Check with an InvoiceID for easy identification
    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .sendMax(XrpCurrencyAmount.ofDrops(12345))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      sourceKeyPair.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> response = xrplClient.submit(signedCheckCreate);
    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source wallet's account objects, and validate that the created Check makes
    // it into the ledger
    CheckObject checkObject = (CheckObject) this
      .scanForResult(
        () -> this.getValidatedAccountObjects(sourceKeyPair.publicKey().deriveAddress()),
        result -> result.accountObjects().stream().anyMatch(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
      .findFirst().get();

    assertEntryEqualsObjectFromAccountObjects(checkObject);

    //////////////////////
    // Destination wallet cashes the Check
    feeResult = xrplClient.fee();
    AccountInfoResult destinationAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    CheckCash checkCash = CheckCash.builder()
      .account(destinationKeyPair.publicKey().deriveAddress())
      .amount(checkObject.sendMax())
      .sequence(destinationAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(destinationKeyPair.publicKey())
      .build();
    SingleSignedTransaction<CheckCash> signedCheckCash = signatureService.sign(
      destinationKeyPair.privateKey(), checkCash
    );
    SubmitResult<CheckCash> cashResponse = xrplClient.submit(signedCheckCash);
    assertThat(cashResponse.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCash transaction successful: https://testnet.xrpl.org/transactions/{}",
      cashResponse.transactionResult().hash()
    );

    //////////////////////
    // Validate that the destination account balance increases by the check amount minus fees
    this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress()),
      result -> {
        logger.info("AccountInfoResult after CheckCash balance: {}", result.accountData().balance().value());
        return result.accountData().balance().equals(
          destinationAccountInfo.accountData().balance()
            .plus((XrpCurrencyAmount) checkObject.sendMax())
            .minus(checkCash.fee()));
      });

    //////////////////////
    // Validate that the Check object was deleted
    this.scanForResult(
      () -> this.getValidatedAccountObjects(sourceKeyPair.publicKey().deriveAddress()),
      result -> result.accountObjects().stream().noneMatch(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
    );
  }

  @Test
  public void createCheckAndSourceCancels() throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Generate and fund source and destination accounts
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Create a Check with an InvoiceID for easy identification
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .sendMax(XrpCurrencyAmount.ofDrops(12345))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      sourceKeyPair.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> response = xrplClient.submit(signedCheckCreate);
    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source wallet's account objects, and validate that the created Check makes
    // it into the ledger
    CheckObject checkObject = (CheckObject) this
      .scanForResult(() -> this.getValidatedAccountObjects(sourceKeyPair.publicKey().deriveAddress()),
        result -> result.accountObjects().stream().anyMatch(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
      .findFirst().get();

    assertEntryEqualsObjectFromAccountObjects(checkObject);

    //////////////////////
    // Source account cancels the Check
    feeResult = xrplClient.fee();
    CheckCancel checkCancel = CheckCancel.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCancel> signedCheckCancel = signatureService.sign(
      sourceKeyPair.privateKey(), checkCancel
    );
    SubmitResult<CheckCancel> cancelResult = xrplClient.submit(signedCheckCancel);
    assertThat(cancelResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Validate that the Check does not exist after cancelling
    this.scanForResult(
      () -> this.getValidatedAccountObjects(sourceKeyPair.publicKey().deriveAddress()),
      result -> result.accountObjects().stream().noneMatch(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
    );
  }

  @Test
  public void createCheckAndDestinationCancels() throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Generate and fund source and destination accounts
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Create a Check with an InvoiceID for easy identification
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .sendMax(XrpCurrencyAmount.ofDrops(12345))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    //////////////////////
    // Poll the ledger for the source wallet's account objects, and validate that the created Check makes
    // it into the ledger
    SingleSignedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      sourceKeyPair.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> response = xrplClient.submit(signedCheckCreate);
    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    CheckObject checkObject = (CheckObject) this
      .scanForResult(
        () -> this.getValidatedAccountObjects(sourceKeyPair.publicKey().deriveAddress()),
        result -> result.accountObjects().stream().anyMatch(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(sourceKeyPair, destinationKeyPair, invoiceId))
      .findFirst().get();

    assertEntryEqualsObjectFromAccountObjects(checkObject);

    //////////////////////
    // Destination account cancels the Check
    feeResult = xrplClient.fee();
    AccountInfoResult destinationAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    CheckCancel checkCancel = CheckCancel.builder()
      .account(destinationKeyPair.publicKey().deriveAddress())
      .sequence(destinationAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(destinationKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCancel> signedCheckCancel = signatureService.sign(
      destinationKeyPair.privateKey(), checkCancel
    );
    SubmitResult<CheckCancel> cancelResult = xrplClient.submit(signedCheckCancel);
    assertThat(cancelResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Validate that the Check does not exist after cancelling
    this.scanForResult(
      () -> this.getValidatedAccountObjects(sourceKeyPair.publicKey().deriveAddress()),
      result -> result.accountObjects().stream().noneMatch(findCheck(sourceKeyPair, destinationKeyPair, invoiceId)));
  }

  // =============================================
  // MPT Check Integration Tests
  // =============================================

  /**
   * Creates an MPT issuance, authorizes a holder, mints tokens to the holder, then the issuer creates a
   * {@link CheckCreate} with MPT as {@code SendMax} to send MPT to the holder. The holder cashes the check
   * using {@link CheckCash} with an MPT {@code Amount}. Verifies the check is on ledger and that MPT balances
   * change correctly after cashing.
   *
   * @see <a href="https://github.com/XRPLF/XRPL-Standards/discussions/177">XLS-82d: MPT DEX Integration</a>
   */
  @DisabledIf(value = "shouldNotRunMptChecks",
    disabledReason = "MPT Checks require MPTokensV2 which is only available on the develop rippled image.")
  @Test
  public void createMptCheckAndCashWithAmount() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Generate and fund issuer and holder accounts
    final KeyPair issuerKeyPair = createRandomAccountEd25519();
    final KeyPair holderKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Create MPT issuance with tfMptCanTransfer to allow payments
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(
        issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true)
        .tfMptCanEscrow(true)
        .tfMptCanTrade(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Extract MPT issuance ID from transaction metadata
    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    logger.info("Created MPT issuance: {}", mptIssuanceId.value());

    //////////////////////
    // Holder authorizes the MPT
    feeResult = xrplClient.fee();
    AccountInfoResult holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holderKeyPair.publicKey())
      .mpTokenIssuanceId(mptIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(
      holderKeyPair.privateKey(), authorize
    );
    SubmitResult<MpTokenAuthorize> authorizeResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    //////////////////////
    // Issuer mints MPT to holder via Payment
    feeResult = xrplClient.fee();
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("100000")
      .build();

    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(issuerKeyPair.privateKey(), mint);
    SubmitResult<Payment> mintResult = xrplClient.submit(signedMint);
    assertThat(mintResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    //////////////////////
    // Issuer creates a Check with MPT SendMax to send MPT to holder
    feeResult = xrplClient.fee();
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("MPT Check test".getBytes()).toString());
    MptCurrencyAmount checkAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("50000")
      .build();

    CheckCreate checkCreate = CheckCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .sendMax(checkAmount)
      .invoiceId(invoiceId)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      issuerKeyPair.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> checkCreateResponse = xrplClient.submit(signedCheckCreate);
    assertThat(checkCreateResponse.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("CheckCreate with MPT transaction successful: {}", checkCreateResponse.transactionResult().hash());

    //////////////////////
    // Poll the ledger for the check object
    CheckObject checkObject = (CheckObject) this
      .scanForResult(
        () -> this.getValidatedAccountObjects(issuerKeyPair.publicKey().deriveAddress()),
        result -> result.accountObjects().stream().anyMatch(findCheck(issuerKeyPair, holderKeyPair, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(issuerKeyPair, holderKeyPair, invoiceId))
      .findFirst().get();

    assertThat(checkObject.sendMax()).isEqualTo(checkAmount);
    assertEntryEqualsObjectFromAccountObjects(checkObject);

    //////////////////////
    // Holder cashes the Check with MPT Amount
    feeResult = xrplClient.fee();
    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    CheckCash checkCash = CheckCash.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .amount(checkAmount)
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(holderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCash> signedCheckCash = signatureService.sign(
      holderKeyPair.privateKey(), checkCash
    );
    SubmitResult<CheckCash> cashResponse = xrplClient.submit(signedCheckCash);
    assertThat(cashResponse.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("CheckCash with MPT transaction successful: {}", cashResponse.transactionResult().hash());

    //////////////////////
    // Validate that the Check object was deleted
    this.scanForResult(
      () -> this.getValidatedAccountObjects(issuerKeyPair.publicKey().deriveAddress()),
      result -> result.accountObjects().stream().noneMatch(findCheck(issuerKeyPair, holderKeyPair, invoiceId))
    );

    logger.info("MPT Check create and cash test completed successfully");
  }

  /**
   * Creates an MPT issuance, authorizes a holder, mints tokens to the holder, then the issuer creates a
   * {@link CheckCreate} with MPT as {@code SendMax} to send MPT to the holder. The holder cashes the check
   * using {@link CheckCash} with an MPT {@code DeliverMin}. Verifies the check is on ledger and that the check
   * can be cashed for at least the minimum amount.
   *
   * @see <a href="https://github.com/XRPLF/XRPL-Standards/discussions/177">XLS-82d: MPT DEX Integration</a>
   */
  @DisabledIf(value = "shouldNotRunMptChecks",
    disabledReason = "MPT Checks require MPTokensV2 which is only available on the develop rippled image.")
  @Test
  public void createMptCheckAndCashWithDeliverMin() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Generate and fund issuer and holder accounts
    final KeyPair issuerKeyPair = createRandomAccountEd25519();
    final KeyPair holderKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Create MPT issuance with tfMptCanTransfer to allow payments
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(
        issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true)
        .tfMptCanEscrow(true)
        .tfMptCanTrade(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Extract MPT issuance ID from transaction metadata
    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    logger.info("Created MPT issuance: {}", mptIssuanceId.value());

    //////////////////////
    // Holder authorizes the MPT
    feeResult = xrplClient.fee();
    AccountInfoResult holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holderKeyPair.publicKey())
      .mpTokenIssuanceId(mptIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(
      holderKeyPair.privateKey(), authorize
    );
    SubmitResult<MpTokenAuthorize> authorizeResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    //////////////////////
    // Issuer mints MPT to holder via Payment
    feeResult = xrplClient.fee();
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("100000")
      .build();

    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(issuerKeyPair.privateKey(), mint);
    SubmitResult<Payment> mintResult = xrplClient.submit(signedMint);
    assertThat(mintResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    //////////////////////
    // Issuer creates a Check with MPT SendMax to send MPT to holder
    feeResult = xrplClient.fee();
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("MPT Check DeliverMin test".getBytes()).toString());
    MptCurrencyAmount checkSendMax = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("75000")
      .build();

    CheckCreate checkCreate = CheckCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .sendMax(checkSendMax)
      .invoiceId(invoiceId)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      issuerKeyPair.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> checkCreateResponse = xrplClient.submit(signedCheckCreate);
    assertThat(checkCreateResponse.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("CheckCreate with MPT transaction successful: {}", checkCreateResponse.transactionResult().hash());

    //////////////////////
    // Poll the ledger for the check object
    CheckObject checkObject = (CheckObject) this
      .scanForResult(
        () -> this.getValidatedAccountObjects(issuerKeyPair.publicKey().deriveAddress()),
        result -> result.accountObjects().stream().anyMatch(findCheck(issuerKeyPair, holderKeyPair, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(issuerKeyPair, holderKeyPair, invoiceId))
      .findFirst().get();

    assertThat(checkObject.sendMax()).isEqualTo(checkSendMax);
    assertEntryEqualsObjectFromAccountObjects(checkObject);

    //////////////////////
    // Holder cashes the Check with MPT DeliverMin (requesting at least 50000, but SendMax is 75000)
    feeResult = xrplClient.fee();
    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount deliverMin = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("50000")
      .build();

    CheckCash checkCash = CheckCash.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .deliverMin(deliverMin)
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(holderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CheckCash> signedCheckCash = signatureService.sign(
      holderKeyPair.privateKey(), checkCash
    );
    SubmitResult<CheckCash> cashResponse = xrplClient.submit(signedCheckCash);
    assertThat(cashResponse.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("CheckCash with MPT DeliverMin transaction successful: {}", cashResponse.transactionResult().hash());

    //////////////////////
    // Validate that the Check object was deleted
    this.scanForResult(
      () -> this.getValidatedAccountObjects(issuerKeyPair.publicKey().deriveAddress()),
      result -> result.accountObjects().stream().noneMatch(findCheck(issuerKeyPair, holderKeyPair, invoiceId))
    );

    logger.info("MPT Check create and cash with DeliverMin test completed successfully");
  }


  static boolean shouldNotRunMptChecks() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null ||
      System.getProperty("useDevnet") != null;
  }

  private Predicate<LedgerObject> findCheck(KeyPair sourceKeyPair, KeyPair destinationKeyPair, Hash256 invoiceId) {
    return object ->
      CheckObject.class.isAssignableFrom(object.getClass()) &&
        ((CheckObject) object).invoiceId().map(id -> id.equals(invoiceId)).orElse(false) &&
        ((CheckObject) object).account().equals(sourceKeyPair.publicKey().deriveAddress()) &&
        ((CheckObject) object).destination().equals(destinationKeyPair.publicKey().deriveAddress());
  }


  private void assertEntryEqualsObjectFromAccountObjects(CheckObject checkObject) throws JsonRpcClientErrorException {
    LedgerEntryResult<CheckObject> checkEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.check(checkObject.index(), LedgerSpecifier.CURRENT));

    assertThat(checkEntry.node()).isEqualTo(checkObject);

    LedgerEntryResult<CheckObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(checkObject.index(), CheckObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(checkEntry.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(checkObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());
  }
}
