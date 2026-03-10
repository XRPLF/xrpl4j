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

/**
 * Integration tests for ripple_path_find and path_find RPC methods with MPT (Multi-Purpose Token) support.
 * Tests path finding functionality for MPT as destination asset.
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "PathFindIT only runs on local rippled node or devnet.")
public class PathFindIT extends AbstractIT {

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  /**
   * Tests ripple_path_find with MPT as the destination amount.
   * Creates an MPT issuance, authorizes a holder, and attempts to find paths to deliver MPT.
   *
   * <p>This test validates that the ripple_path_find RPC accepts MPT amounts in the destination_amount field.
   * Since MPT pathfinding may return empty alternatives when no liquidity exists, the test focuses on
   * verifying that the RPC call succeeds and then validates MPT payment functionality directly.</p>
   */
  @Test
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

    // Create MPT issuance with tfMptCanTrade and tfMptCanTransfer to allow trading and transfers
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

    // Call ripple_path_find - it may return empty alternatives if no liquidity paths exist
    RipplePathFindResult pathFindResult = xrplClient.ripplePathFind(pathFindParams);
    assertThat(pathFindResult).isNotNull();
    assertThat(pathFindResult.destinationAccount()).isEqualTo(holderKeyPair.publicKey().deriveAddress());

    // Verify that a direct MPT payment works (issuer to holder)
    // This validates that even if pathfinding returns empty alternatives, direct payments work
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

    logger.info("Successfully called ripple_path_find with MPT destination and delivered MPT payment");
  }
}
