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
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

/**
 * Integration tests for ripple_path_find and path_find RPC methods with MPT (Multi-Purpose Token) support.
 * Tests path finding functionality for MPT as source, destination, and intermediate assets.
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "PathFindIT only runs on local rippled node or devnet.")
public class PathFindIT extends AbstractIT {

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  /**
   * Tests ripple_path_find with MPT as the destination amount.
   * Creates an MPT issuance, authorizes a holder, and finds paths to deliver MPT from issuer to holder.
   *
   * <p><strong>Note:</strong> This test is currently disabled because MPT support in path finding RPCs
   * is not yet fully implemented in the rippled version being used ({@code rippleci/rippled:develop}).
   * The test fails with "internal (Internal error.)" when calling ripple_path_find with MPT destination amount.
   * Once MPT path finding support is available, remove the {@code @Disabled} annotation to enable this test.</p>
   */
  @Test
  @Disabled("MPT in ripple_path_find not yet supported in rippled")
  void ripplePathFindWithMptDestination() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair holderKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult holderAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance with tfMptCanTrade to allow trading
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
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

    // Get the MPT issuance ID from transaction metadata
    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Authorize holder to hold this MPT
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(holderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mptIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(
      holderKeyPair.privateKey(), authorize
    );
    SubmitResult<MpTokenAuthorize> authorizeResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAuthorize.hash(),
      authorizeResult.validatedLedgerIndex(),
      authorize.lastLedgerSequence().get(),
      authorize.sequence(),
      holderKeyPair.publicKey().deriveAddress()
    );

    // Use ripple_path_find to find paths to deliver MPT from issuer to holder
    MptCurrencyAmount destinationAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("100")
      .build();



    RipplePathFindRequestParams pathFindParams = RipplePathFindRequestParams.builder()
      .sourceAccount(issuerKeyPair.publicKey().deriveAddress())
      .destinationAccount(holderKeyPair.publicKey().deriveAddress())
      .destinationAmount(destinationAmount)
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();

    RipplePathFindResult pathFindResult = xrplClient.ripplePathFind(pathFindParams);

    // Verify that ripple_path_find returns at least one alternative path
    assertThat(pathFindResult.alternatives()).isNotEmpty();
    assertThat(pathFindResult.destinationAccount()).isEqualTo(holderKeyPair.publicKey().deriveAddress());

    // Verify that a payment using the found path succeeds
    AccountInfoResult issuerInfoBeforePayment = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    Payment payment = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(destinationAmount)
      .sequence(issuerInfoBeforePayment.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerInfoBeforePayment.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(
      issuerKeyPair.privateKey(), payment
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedPayment);
    assertThat(paymentResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedPayment.hash(),
      paymentResult.validatedLedgerIndex(),
      payment.lastLedgerSequence().get(),
      payment.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    logger.info("Successfully found path and delivered MPT from issuer to holder");
  }

  /**
   * Tests ripple_path_find with MPT as source currency.
   * Creates an MPT/XRP scenario where the source wants to spend MPT to deliver XRP.
   *
   * <p><strong>Note:</strong> This test is currently disabled because MPT support in path finding RPCs
   * is not yet fully implemented in the rippled version being used ({@code rippleci/rippled:develop}).
   * The test fails with "dstAmtMalformed" when calling ripple_path_find with MPT in sendMax.
   * Once MPT path finding support is available, remove the {@code @Disabled} annotation to enable this test.</p>
   */
  @Test
  @Disabled("MPT in ripple_path_find not yet supported in rippled")
  void ripplePathFindWithMptSource() throws JsonRpcClientErrorException, JsonProcessingException {
    final KeyPair issuerKeyPair = createRandomAccountEd25519();
    final KeyPair holderKeyPair = createRandomAccountEd25519();
    final KeyPair recipientKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    final AccountInfoResult issuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    scanForResult(
      () -> this.getValidatedAccountInfo(recipientKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
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

    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Authorize holder
    final AccountInfoResult holderAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(holderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mptIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(
      holderKeyPair.privateKey(), authorize
    );
    SubmitResult<MpTokenAuthorize> authorizeResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAuthorize.hash(),
      authorizeResult.validatedLedgerIndex(),
      authorize.lastLedgerSequence().get(),
      authorize.sequence(),
      holderKeyPair.publicKey().deriveAddress()
    );

    // Mint MPT to holder
    AccountInfoResult issuerInfoBeforeMint = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("10000")
      .build();

    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .sequence(issuerInfoBeforeMint.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerInfoBeforeMint.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(
      issuerKeyPair.privateKey(), mint
    );
    SubmitResult<Payment> mintResult = xrplClient.submit(signedMint);
    assertThat(mintResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedMint.hash(),
      mintResult.validatedLedgerIndex(),
      mint.lastLedgerSequence().get(),
      mint.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Use ripple_path_find to find paths where holder spends MPT to deliver XRP to recipient
    // Note: This tests the scenario where MPT is the source currency
    XrpCurrencyAmount xrpDestinationAmount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5));

    RipplePathFindRequestParams pathFindParams = RipplePathFindRequestParams.builder()
      .sourceAccount(holderKeyPair.publicKey().deriveAddress())
      .destinationAccount(recipientKeyPair.publicKey().deriveAddress())
      .destinationAmount(xrpDestinationAmount)
      .sendMax(MptCurrencyAmount.builder()
        .mptIssuanceId(mptIssuanceId)
        .value("1000")
        .build())
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();

    RipplePathFindResult pathFindResult = xrplClient.ripplePathFind(pathFindParams);

    // The path finding may or may not find a path depending on liquidity
    // For this test, we're primarily verifying that the RPC call succeeds with MPT as sendMax
    assertThat(pathFindResult).isNotNull();
    assertThat(pathFindResult.destinationAccount()).isEqualTo(recipientKeyPair.publicKey().deriveAddress());

    logger.info("Successfully queried ripple_path_find with MPT as source currency (sendMax)");
  }
}
