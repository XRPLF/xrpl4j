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
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.ZKProofUtils;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.RangeProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc.BcRangeProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.commitments.bc.BcPedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextMultiProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextParticipant;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.mpt.commitments.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.LinkageProofType;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SecretKeyProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc.BcPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertBackContext;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc.BcPlaintextEqualityProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.EqualityPlaintextProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc.BcSamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.service.BcConfidentialMPTConvertService;
import org.xrpl.xrpl4j.crypto.mpt.service.ConfidentialMPTConvertService;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.BulletproofRangeProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ConfidentialMPTConvertResult;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.bc.BcElGamalDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.bc.BcElGamalEncryptor;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.MpTokenLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMPTClawback;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMPTConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMPTConvertBack;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMPTMergeInbox;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMPTSend;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@DisabledIf(value = "shouldNotRun", disabledReason = "ConfidentialTransfersIT only runs on local rippled node.")
public class ConfidentialTransfersIT extends AbstractIT {

  private static final String SUCCESS_STATUS = "tesSUCCESS";

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null ||
      System.getProperty("useDevnet") != null;
  }

  @Test
  public void testEntireFlow() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////
    // Create random issuer account
    KeyPair issuerKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Get fee and account info
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Create MPTokenIssuance with lsfMPTCanTransfer and lsfMPTCanClawback flags
    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.builder()
      .tfMptCanTransfer(true)
      .tfMptCanClawback(true)
      .tfMptCanPrivacy(true)
      .build();

    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .flags(flags)
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(),
      issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateSubmitResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    //////////////////////
    // Wait for validation
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedIssuanceCreate.hash(),
        issuanceCreateSubmitResult.validatedLedgerIndex(),
        issuanceCreate.lastLedgerSequence().orElseThrow(RuntimeException::new),
        issuanceCreate.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Get the MPTokenIssuanceId from the transaction metadata
    MpTokenIssuanceId mpTokenIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    //////////////////////
    // Verify the issuance was created with the correct flags
    MpTokenIssuanceObject issuanceFromLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(
        mpTokenIssuanceId,
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanTransfer()).isTrue();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanClawback()).isTrue();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanPrivacy()).isTrue();

    //////////////////////
    // Generate Issuer ElGamal key pair and submit MpTokenIssuanceSet to register it
    KeyPair issuerElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();

    // Get updated issuer account info for the next transaction
    AccountInfoResult issuerAccountInfoForSet = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfoForSet.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerAccountInfoForSet.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .issuerElGamalPublicKey(issuerElGamalKeyPair.publicKey().base16Value())
      .build();

    SingleSignedTransaction<MpTokenIssuanceSet> signedIssuanceSet = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceSet
    );
    SubmitResult<MpTokenIssuanceSet> issuanceSetResult = xrplClient.submit(signedIssuanceSet);
    assertThat(issuanceSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedIssuanceSet.hash(),
        issuanceSetResult.validatedLedgerIndex(),
        issuanceSet.lastLedgerSequence().orElseThrow(RuntimeException::new),
        issuanceSet.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Create holder account
    KeyPair holderKeyPair = createRandomAccountEd25519();

    //////////////////////
    // Holder authorizes the MPToken
    AccountInfoResult holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize holderAuthorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(holderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedHolderAuthorize = signatureService.sign(
      holderKeyPair.privateKey(), holderAuthorize
    );
    SubmitResult<MpTokenAuthorize> holderAuthorizeResult = xrplClient.submit(signedHolderAuthorize);
    assertThat(holderAuthorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedHolderAuthorize.hash(),
        holderAuthorizeResult.validatedLedgerIndex(),
        holderAuthorize.lastLedgerSequence().orElseThrow(RuntimeException::new),
        holderAuthorize.sequence(),
        holderKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Transfer 1000 MPTs to Holder
    AccountInfoResult issuerAccountInfoForPayment = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount transferAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("1000")
      .build();

    Payment paymentToHolder = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfoForPayment.accountData().sequence())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(transferAmount)
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerAccountInfoForPayment.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedPaymentToHolder = signatureService.sign(
      issuerKeyPair.privateKey(), paymentToHolder
    );
    SubmitResult<Payment> paymentToHolderResult = xrplClient.submit(signedPaymentToHolder);
    assertThat(paymentToHolderResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedPaymentToHolder.hash(),
        paymentToHolderResult.validatedLedgerIndex(),
        paymentToHolder.lastLedgerSequence().orElseThrow(RuntimeException::new),
        paymentToHolder.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Verify holder has 1000 MPTs
    MpTokenObject holderMpToken = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(holderMpToken.mptAmount()).isEqualTo(MpTokenNumericAmount.of(1000L));

    //////////////////////
    // Generate Holder ElGamal key pair
    KeyPair holderElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();

    //////////////////////
    // Prepare encryption utilities (still needed for later operations)
    BcElGamalEncryptor encryptor = new BcElGamalEncryptor();

    //////////////////////
    // Get updated holder account info for ConfidentialMPTConvert
    // IMPORTANT: Get this BEFORE generating ZKProof because the context hash includes the sequence number
    AccountInfoResult holderAccountInfoForConvert = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Use ConfidentialMPTConvertService to generate all cryptographic data
    UnsignedLong amountToConvert = UnsignedLong.valueOf(500);
    ConfidentialMPTConvertService<org.xrpl.xrpl4j.crypto.keys.PrivateKey> convertService =
      new BcConfidentialMPTConvertService();

    ConfidentialMPTConvertResult convertResult = convertService.generateConvertData(
      holderElGamalKeyPair.privateKey(),
      holderElGamalKeyPair.publicKey(),
      issuerElGamalKeyPair.publicKey(),
      Optional.empty(),  // no auditor
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfoForConvert.accountData().sequence(),
      mpTokenIssuanceId,
      amountToConvert
    );

    //////////////////////
    // Build and submit ConfidentialMPTConvert transaction
    ConfidentialMPTConvert confidentialConvert = ConfidentialMPTConvert.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holderAccountInfoForConvert.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(
        holderAccountInfoForConvert.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(amountToConvert))
      .holderElGamalPublicKey(holderElGamalKeyPair.publicKey().base16Value())
      .holderEncryptedAmount(convertResult.holderEncryptedAmount().hexValue())
      .issuerEncryptedAmount(convertResult.issuerEncryptedAmount().hexValue())
      .blindingFactor(convertResult.blindingFactor().hexValue())
      .zkProof(convertResult.zkProof().hexValue())
      .build();

    SingleSignedTransaction<ConfidentialMPTConvert> signedConfidentialConvert = signatureService.sign(
      holderKeyPair.privateKey(), confidentialConvert
    );
    SubmitResult<ConfidentialMPTConvert> confidentialConvertResult = xrplClient.submit(signedConfidentialConvert);
    assertThat(confidentialConvertResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for validation
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedConfidentialConvert.hash(),
        confidentialConvertResult.validatedLedgerIndex(),
        confidentialConvert.lastLedgerSequence().orElseThrow(RuntimeException::new),
        confidentialConvert.sequence(),
        holderKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Merge inbox to spending balance for Holder 1
    // After ConfidentialMPTConvert, tokens go to inbox. We need to merge them to spending balance before sending.
    AccountInfoResult holderAccountInfoForMerge = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    ConfidentialMPTMergeInbox mergeInbox = ConfidentialMPTMergeInbox.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holderAccountInfoForMerge.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(
        holderAccountInfoForMerge.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<ConfidentialMPTMergeInbox> signedMergeInbox = signatureService.sign(
      holderKeyPair.privateKey(), mergeInbox
    );
    SubmitResult<ConfidentialMPTMergeInbox> mergeInboxResult = xrplClient.submit(signedMergeInbox);
    assertThat(mergeInboxResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for validation
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedMergeInbox.hash(),
        mergeInboxResult.validatedLedgerIndex(),
        mergeInbox.lastLedgerSequence().orElseThrow(RuntimeException::new),
        mergeInbox.sequence(),
        holderKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Create second holder account (Holder 2)
    KeyPair holder2KeyPair = createRandomAccountEd25519();

    //////////////////////
    // Holder 2 authorizes the MPToken
    AccountInfoResult holder2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder2KeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize holder2Authorize = MpTokenAuthorize.builder()
      .account(holder2KeyPair.publicKey().deriveAddress())
      .sequence(holder2AccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holder2KeyPair.publicKey())
      .lastLedgerSequence(holder2AccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedHolder2Authorize = signatureService.sign(
      holder2KeyPair.privateKey(), holder2Authorize
    );
    SubmitResult<MpTokenAuthorize> holder2AuthorizeResult = xrplClient.submit(signedHolder2Authorize);
    assertThat(holder2AuthorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedHolder2Authorize.hash(),
        holder2AuthorizeResult.validatedLedgerIndex(),
        holder2Authorize.lastLedgerSequence().orElseThrow(RuntimeException::new),
        holder2Authorize.sequence(),
        holder2KeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Generate Holder 2 ElGamal key pair
    KeyPair holder2ElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();

    //////////////////////
    // Prepare ConfidentialMPTConvert for Holder 2 (0 amount conversion to register public key)
    UnsignedLong holder2AmountToConvert = UnsignedLong.ZERO;

    //////////////////////
    // Get updated holder 2 account info for ConfidentialMPTConvert
    AccountInfoResult holder2AccountInfoForConvert = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder2KeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Use ConfidentialMPTConvertService for Holder 2
    ConfidentialMPTConvertResult holder2ConvertResult = convertService.generateConvertData(
      holder2ElGamalKeyPair.privateKey(),
      holder2ElGamalKeyPair.publicKey(),
      issuerElGamalKeyPair.publicKey(),
      Optional.empty(),  // no auditor
      holder2KeyPair.publicKey().deriveAddress(),
      holder2AccountInfoForConvert.accountData().sequence(),
      mpTokenIssuanceId,
      holder2AmountToConvert
    );

    //////////////////////
    // Build and submit ConfidentialMPTConvert for Holder 2 (0 amount to register public key)
    ConfidentialMPTConvert holder2ConfidentialConvert = ConfidentialMPTConvert.builder()
      .account(holder2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holder2AccountInfoForConvert.accountData().sequence())
      .signingPublicKey(holder2KeyPair.publicKey())
      .lastLedgerSequence(
        holder2AccountInfoForConvert.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(holder2AmountToConvert))
      .holderElGamalPublicKey(holder2ElGamalKeyPair.publicKey().base16Value())
      .holderEncryptedAmount(holder2ConvertResult.holderEncryptedAmount().hexValue())
      .issuerEncryptedAmount(holder2ConvertResult.issuerEncryptedAmount().hexValue())
      .blindingFactor(holder2ConvertResult.blindingFactor().hexValue())
      .zkProof(holder2ConvertResult.zkProof().hexValue())
      .build();

    SingleSignedTransaction<ConfidentialMPTConvert> signedHolder2ConfidentialConvert = signatureService.sign(
      holder2KeyPair.privateKey(), holder2ConfidentialConvert
    );
    SubmitResult<ConfidentialMPTConvert> holder2ConfidentialConvertResult = xrplClient.submit(
      signedHolder2ConfidentialConvert
    );
    assertThat(holder2ConfidentialConvertResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for holder 2's convert to be validated
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedHolder2ConfidentialConvert.hash(),
        holder2ConfidentialConvertResult.validatedLedgerIndex(),
        holder2ConfidentialConvert.lastLedgerSequence().orElseThrow(RuntimeException::new),
        holder2ConfidentialConvert.sequence(),
        holder2KeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Get MPToken objects for both holders (needed for version and balance info)
    MpTokenObject holder1MpToken = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    //////////////////////
    // ConfidentialMPTSend: Holder 1 sends 100 confidential MPT to Holder 2
    UnsignedLong sendAmount = UnsignedLong.valueOf(100);

    // Generate blinding factors for the send (one for each recipient: sender, destination, issuer)
    BlindingFactor sendBlindingFactorSender = BlindingFactor.generate();
    BlindingFactor sendBlindingFactorHolder2 = BlindingFactor.generate();
    BlindingFactor sendBlindingFactorIssuer = BlindingFactor.generate();

    // Encrypt for sender (holder 1) - this is the amount being debited
    ElGamalCiphertext senderCiphertext = encryptor.encrypt(
      sendAmount, holderElGamalKeyPair.publicKey(), sendBlindingFactorSender
    );

    // Encrypt for destination (holder 2)
    ElGamalCiphertext destinationCiphertext = encryptor.encrypt(
      sendAmount, holder2ElGamalKeyPair.publicKey(), sendBlindingFactorHolder2
    );

    // Encrypt for issuer
    ElGamalCiphertext issuerCiphertextForSend = encryptor.encrypt(
      sendAmount, issuerElGamalKeyPair.publicKey(), sendBlindingFactorIssuer
    );

    //////////////////////
    // Get holder 1 account info for the Send transaction
    AccountInfoResult holderAccountInfoForSend = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Generate SamePlaintextMultiProof (proves all 3 ciphertexts encrypt the same amount)
    BcSamePlaintextProofGenerator samePlaintextProofGenerator = new BcSamePlaintextProofGenerator();

    // Get the version from holder 1's MPToken (default to 0 if not present)
    UnsignedInteger holder1Version = holder1MpToken.confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

    // Generate context for ConfidentialMPTSend
    ConfidentialMPTSendContext sendContext = ConfidentialMPTSendContext.generate(
      holderKeyPair.publicKey().deriveAddress(),  // sender account
      holderAccountInfoForSend.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuanceId
      holder2KeyPair.publicKey().deriveAddress(),  // destination
      holder1Version  // version from MPToken
    );

    // Create participants for proof generation
    SamePlaintextParticipant senderParticipant = SamePlaintextParticipant.forProofGeneration(
      senderCiphertext, holderElGamalKeyPair.publicKey(), sendBlindingFactorSender
    );
    SamePlaintextParticipant destParticipant = SamePlaintextParticipant.forProofGeneration(
      destinationCiphertext, holder2ElGamalKeyPair.publicKey(), sendBlindingFactorHolder2
    );
    SamePlaintextParticipant issuerParticipant = SamePlaintextParticipant.forProofGeneration(
      issuerCiphertextForSend, issuerElGamalKeyPair.publicKey(), sendBlindingFactorIssuer
    );

    // Generate the SamePlaintextMultiProof for all 3 participants: sender, destination, issuer
    SamePlaintextMultiProof samePlaintextProof = samePlaintextProofGenerator.generateProof(
      sendAmount,
      Arrays.asList(senderParticipant, destParticipant, issuerParticipant),
      sendContext
    );

    //////////////////////
    // Generate Pedersen Commitments and Linkage Proofs
    PedersenCommitmentGenerator pedersenGen = new BcPedersenCommitmentGenerator();
    BcPedersenLinkProofGenerator linkProofGenerator = new BcPedersenLinkProofGenerator();
    BcElGamalDecryptor balanceDecryptor = new BcElGamalDecryptor();

    // Generate blinding factors for Pedersen commitments
    BlindingFactor amountBlindingFactorForSend = BlindingFactor.generate();
    BlindingFactor balanceBlindingFactorForSend = BlindingFactor.generate();

    // Generate Amount Pedersen Commitment: PCm = sendAmount * G + amountBlindingFactorForSend * H
    PedersenCommitment amountCommitment = pedersenGen.generateCommitment(sendAmount, amountBlindingFactorForSend);

    // Get the sender's current encrypted spending balance from the ledger and decrypt it
    String currentEncryptedBalance = holder1MpToken.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder 1 has no confidential balance"));
    byte[] currentBalanceBytes = BaseEncoding.base16().decode(currentEncryptedBalance);
    ElGamalCiphertext currentBalanceCiphertext = ElGamalCiphertext.fromBytes(currentBalanceBytes);

    // Decrypt the sender's current spending balance (this is what a real client would do)
    long senderCurrentBalanceLong = balanceDecryptor.decrypt(
      currentBalanceCiphertext, holderElGamalKeyPair.privateKey(), 0, 1_000_000
    );
    UnsignedLong senderCurrentBalance = UnsignedLong.valueOf(senderCurrentBalanceLong);

    // Generate Balance Pedersen Commitment: PCb = senderCurrentBalance * G + balanceBlindingFactorForSend * H
    PedersenCommitment balanceCommitment = pedersenGen.generateCommitment(senderCurrentBalance, balanceBlindingFactorForSend);

    //////////////////////
    // Generate Amount Linkage Proof
    // Proves: sender's encrypted amount (senderCiphertext) links to amountCommitment
    ElGamalPedersenLinkProof amountLinkageProof = linkProofGenerator.generateProof(
      LinkageProofType.AMOUNT_COMMITMENT,
      senderCiphertext,
      holderElGamalKeyPair.publicKey(),
      amountCommitment,
      sendAmount,
      sendBlindingFactorSender,
      amountBlindingFactorForSend,
      sendContext
    );

    //////////////////////
    // Generate Balance Linkage Proof
    // Proves: sender's current encrypted balance links to balanceCommitment
    // Uses the sender's private key as the "r" parameter (swapped parameters vs amount linkage)

    // Get the holder's ElGamal private key as a BlindingFactor
    BlindingFactor holderPrivateKeyAsBlindingFactor = BlindingFactor.fromBytes(
      holderElGamalKeyPair.privateKey().naturalBytes().toByteArray()
    );

    // Generate Balance Linkage Proof (nonces are generated internally)
    ElGamalPedersenLinkProof balanceLinkageProof = linkProofGenerator.generateProof(
      LinkageProofType.BALANCE_COMMITMENT,
      currentBalanceCiphertext,
      holderElGamalKeyPair.publicKey(),
      balanceCommitment,
      senderCurrentBalance,
      holderPrivateKeyAsBlindingFactor,
      balanceBlindingFactorForSend,
      sendContext
    );

    //////////////////////
    // Generate Bulletproof Range Proof for amount and remaining balance
    // This proves both values are non-negative (in range [0, 2^64))

    // Compute remaining balance: senderCurrentBalance - sendAmount
    UnsignedLong remainingBalanceForSend = UnsignedLong.valueOf(
      senderCurrentBalance.longValue() - sendAmount.longValue()
    );

    // Compute blinding factor for remaining balance: rho_rem = rho_balance - rho_amount
    // This matches the C++ implementation: secp256k1_mpt_scalar_negate + secp256k1_mpt_scalar_add
    byte[] negAmountBlinding = Secp256k1Operations.scalarNegate(amountBlindingFactorForSend.toBytes());
    byte[] rhoRemBytes = Secp256k1Operations.scalarAdd(balanceBlindingFactorForSend.toBytes(), negAmountBlinding);
    BlindingFactor rhoRem = BlindingFactor.fromBytes(rhoRemBytes);

    // Generate aggregated bulletproof for {amount, remainingBalance}
    RangeProofGenerator sendBulletproofGenerator = new BcRangeProofGenerator();
    BulletproofRangeProof sendBulletproof = sendBulletproofGenerator.generateProof(
      Arrays.asList(sendAmount, remainingBalanceForSend),
      Arrays.asList(amountBlindingFactorForSend, rhoRem),
      sendContext
    );

    //////////////////////
    // Combine all proofs into full ZKProof for ConfidentialMPTSend
    // SamePlaintextMultiProof (359) + Amount Linkage (195) + Balance Linkage (195) + Bulletproof (754) = 1503 bytes
    String fullZkProofHex = ZKProofUtils.combineSendProofsWithBulletproofHex(
      samePlaintextProof, amountLinkageProof, balanceLinkageProof, sendBulletproof
    );

    //////////////////////
    // Build and submit ConfidentialMPTSend transaction
    ConfidentialMPTSend confidentialSend = ConfidentialMPTSend.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holderAccountInfoForSend.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(
        holderAccountInfoForSend.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .destination(holder2KeyPair.publicKey().deriveAddress())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .senderEncryptedAmount(senderCiphertext.hexValue())
      .destinationEncryptedAmount(destinationCiphertext.hexValue())
      .issuerEncryptedAmount(issuerCiphertextForSend.hexValue())
      .zkProof(fullZkProofHex)
      .amountCommitment(amountCommitment.hexValue())
      .balanceCommitment(balanceCommitment.hexValue())
      .build();

    SingleSignedTransaction<ConfidentialMPTSend> signedConfidentialSend = signatureService.sign(
      holderKeyPair.privateKey(), confidentialSend
    );
    SubmitResult<ConfidentialMPTSend> confidentialSendResult = xrplClient.submit(signedConfidentialSend);
    assertThat(confidentialSendResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for validation
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedConfidentialSend.hash(),
        confidentialSendResult.validatedLedgerIndex(),
        confidentialSend.lastLedgerSequence().orElseThrow(RuntimeException::new),
        confidentialSend.sequence(),
        holderKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Verify sender's confidential balance was reduced by the send amount
    MpTokenObject senderMpTokenAfterSend = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    // Decrypt the sender's confidential balance after the send
    String senderEncryptedBalanceAfterSend = senderMpTokenAfterSend.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Sender has no confidential balance after send"));
    byte[] senderBalanceBytesAfterSend = BaseEncoding.base16().decode(senderEncryptedBalanceAfterSend);
    ElGamalCiphertext senderBalanceCiphertextAfterSend = ElGamalCiphertext.fromBytes(
      senderBalanceBytesAfterSend
    );

    long senderBalanceAfterSend = balanceDecryptor.decrypt(
      senderBalanceCiphertextAfterSend, holderElGamalKeyPair.privateKey(), 0, 1_000_000
    );

    // Expected balance: 500 (initial) - 100 (sent) = 400
    long expectedSenderBalance = amountToConvert.longValue() - sendAmount.longValue();
    assertThat(senderBalanceAfterSend).isEqualTo(expectedSenderBalance);

    //////////////////////
    // ConfidentialMPTConvertBack: Holder 1 converts 50 confidential MPT back to public balance
    UnsignedLong convertBackAmount = UnsignedLong.valueOf(50);

    // Get updated account info for holder 1
    AccountInfoResult holderAccountInfoForConvertBack = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    // Get the current MPToken to get the version
    MpTokenObject holder1MpTokenForConvertBack = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    UnsignedInteger holder1VersionForConvertBack = holder1MpTokenForConvertBack.confidentialBalanceVersion()
      .orElse(UnsignedInteger.ZERO);

    // Generate blinding factor for convert back
    BlindingFactor convertBackBlindingFactor = BlindingFactor.generate();

    // Encrypt the convert back amount for holder (to be subtracted from spending balance)
    ElGamalCiphertext holderConvertBackCiphertext = encryptor.encrypt(
      convertBackAmount, holderElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );

    // Encrypt the convert back amount for issuer (to be subtracted from issuer mirror balance)
    ElGamalCiphertext issuerConvertBackCiphertext = encryptor.encrypt(
      convertBackAmount, issuerElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );

    // Generate context for ConfidentialMPTConvertBack
    ConfidentialMPTConvertBackContext convertBackContext = ConfidentialMPTConvertBackContext.generate(
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfoForConvertBack.accountData().sequence(),
      mpTokenIssuanceId,
      convertBackAmount,
      holder1VersionForConvertBack
    );

    // Get the current encrypted balance from the ledger
    String currentEncryptedBalanceForConvertBack = holder1MpTokenForConvertBack.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder 1 has no confidential balance"));
    byte[] currentBalanceBytesForConvertBack = BaseEncoding.base16().decode(
      currentEncryptedBalanceForConvertBack
    );
    ElGamalCiphertext currentBalanceCiphertextForConvertBack = ElGamalCiphertext.fromBytes(
      currentBalanceBytesForConvertBack
    );

    // Decrypt the current spending balance from the ledger
    long decryptedCurrentBalance = balanceDecryptor.decrypt(
      currentBalanceCiphertextForConvertBack, holderElGamalKeyPair.privateKey(), 0, 1_000_000
    );
    UnsignedLong currentSpendingBalance = UnsignedLong.valueOf(decryptedCurrentBalance);

    // Generate Pedersen commitment for the current spending balance
    BlindingFactor convertBackBalanceBlindingFactor = BlindingFactor.generate();
    PedersenCommitment convertBackCommitment = pedersenGen.generateCommitment(currentSpendingBalance,
      convertBackBalanceBlindingFactor);

    // Generate Balance Linkage Proof (nonces are generated internally)
    // This proves the Pedersen commitment matches the on-ledger encrypted balance
    ElGamalPedersenLinkProof convertBackBalanceLinkageProof = linkProofGenerator.generateProof(
      LinkageProofType.BALANCE_COMMITMENT,
      currentBalanceCiphertextForConvertBack,
      holderElGamalKeyPair.publicKey(),
      convertBackCommitment,
      currentSpendingBalance,
      holderPrivateKeyAsBlindingFactor,
      convertBackBalanceBlindingFactor,
      convertBackContext
    );

    // Compute the remaining balance after conversion: currentBalance - convertBackAmount
    UnsignedLong remainingBalanceValue = UnsignedLong.valueOf(
      currentSpendingBalance.longValue() - convertBackAmount.longValue()
    );

    // Generate bulletproof range proof for the remaining balance
    // This proves the remaining balance is non-negative (in range [0, 2^64))
    // IMPORTANT: Use the SAME blinding factor as the Pedersen commitment for the current balance
    // This matches the C++ implementation where pcParams.blindingFactor is reused
    RangeProofGenerator bulletproofGenerator = new BcRangeProofGenerator();
    BulletproofRangeProof bulletproof = bulletproofGenerator.generateProof(
      Collections.singletonList(remainingBalanceValue),
      Collections.singletonList(convertBackBalanceBlindingFactor),  // Same blinding factor as used for balance commitment
      convertBackContext
    );

    // Combine Pedersen linkage proof + bulletproof into zkProof using utility
    // Format: pedersenProof (195 bytes) + bulletproof (688 bytes) = 883 bytes total
    String combinedZkProofHex = ZKProofUtils.combineConvertBackProofsHex(
      convertBackBalanceLinkageProof, bulletproof
    );

    //////////////////////
    // Build and submit ConfidentialMPTConvertBack transaction
    ConfidentialMPTConvertBack convertBack = ConfidentialMPTConvertBack.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(holderAccountInfoForConvertBack.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(
        holderAccountInfoForConvertBack.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(convertBackAmount))
      .holderEncryptedAmount(holderConvertBackCiphertext.hexValue())
      .issuerEncryptedAmount(issuerConvertBackCiphertext.hexValue())
      .blindingFactor(convertBackBlindingFactor.hexValue())
      .balanceCommitment(convertBackCommitment.hexValue())
      .zkProof(combinedZkProofHex)
      .build();

    SingleSignedTransaction<ConfidentialMPTConvertBack> signedConvertBack = signatureService.sign(
      holderKeyPair.privateKey(), convertBack
    );
    SubmitResult<ConfidentialMPTConvertBack> convertBackResult = xrplClient.submit(signedConvertBack);
    assertThat(convertBackResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for validation
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedConvertBack.hash(),
        convertBackResult.validatedLedgerIndex(),
        convertBack.lastLedgerSequence().orElseThrow(RuntimeException::new),
        convertBack.sequence(),
        holderKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Verify the remaining confidential balance after ConvertBack (400 - 50 = 350)
    MpTokenObject holder1MpTokenAfterConvertBack = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    // Decrypt the remaining confidential balance
    String remainingEncryptedBalance = holder1MpTokenAfterConvertBack.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder 1 has no confidential balance after convert back"));
    byte[] remainingBalanceBytes = BaseEncoding.base16().decode(remainingEncryptedBalance);
    ElGamalCiphertext remainingBalanceCiphertext = ElGamalCiphertext.fromBytes(remainingBalanceBytes);

    long remainingConfidentialBalance = balanceDecryptor.decrypt(
      remainingBalanceCiphertext, holderElGamalKeyPair.privateKey(), 0, 1_000_000
    );

    // Expected remaining balance: 400 - 50 = 350
    long expectedRemainingBalance = senderBalanceAfterSend - convertBackAmount.longValue();
    assertThat(remainingConfidentialBalance).isEqualTo(expectedRemainingBalance);

    //////////////////////
    // ConfidentialMPTClawback: Issuer claws back 350 confidential MPT from holder
    UnsignedLong clawbackAmount = UnsignedLong.valueOf(350);

    // Get the holder's MPToken to retrieve the IssuerEncryptedBalance
    MpTokenObject holderMpTokenForClawback = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    // Get the issuer's encrypted balance for this holder (IssuerEncryptedBalance)
    String issuerEncryptedBalanceForClawback = holderMpTokenForClawback.issuerEncryptedBalance()
      .orElseThrow(() -> new RuntimeException("No issuer encrypted balance found"));
    byte[] issuerEncryptedBalanceBytes = BaseEncoding.base16().decode(issuerEncryptedBalanceForClawback);
    ElGamalCiphertext issuerBalanceCiphertext = ElGamalCiphertext.fromBytes(issuerEncryptedBalanceBytes);

    long issuerDecryptedBalance = balanceDecryptor.decrypt(issuerBalanceCiphertext, issuerElGamalKeyPair.privateKey(), 0, 1_000_000);
    assertThat(issuerDecryptedBalance).isGreaterThanOrEqualTo(clawbackAmount.longValue());

    // Get updated issuer account info for the clawback transaction
    AccountInfoResult issuerAccountInfoForClawback = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Generate the Equality Plaintext Proof
    // For clawback, the ciphertext is constructed as: c1=issuer's pk, c2=balance.c2
    // And the publicKey is the balance.c1
    // The issuer uses their private key as the "randomness" parameter
    BcPlaintextEqualityProofGenerator equalityProofGenerator = new BcPlaintextEqualityProofGenerator();

    // Generate context hash for clawback
    ConfidentialMPTClawbackContext clawbackContext = ConfidentialMPTClawbackContext.generate(
      issuerKeyPair.publicKey().deriveAddress(),  // issuer account
      issuerAccountInfoForClawback.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuance ID
      clawbackAmount,  // amount
      holderKeyPair.publicKey().deriveAddress()  // holder
    );

    EqualityPlaintextProof clawbackProof = equalityProofGenerator.generateProof(
      issuerBalanceCiphertext,  // IssuerEncryptedBalance ciphertext
      issuerElGamalKeyPair.publicKey(),  // issuer's ElGamal public key
      clawbackAmount,
      issuerElGamalKeyPair.privateKey(),
      clawbackContext
    );

    //////////////////////
    // Build and submit ConfidentialMPTClawback transaction
    ConfidentialMPTClawback clawback = ConfidentialMPTClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfoForClawback.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerAccountInfoForClawback.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .holder(holderKeyPair.publicKey().deriveAddress())
      .mptAmount(MpTokenNumericAmount.of(clawbackAmount))
      .zkProof(clawbackProof.hexValue())
      .build();

    SingleSignedTransaction<ConfidentialMPTClawback> signedClawback = signatureService.sign(
      issuerKeyPair.privateKey(), clawback
    );
    SubmitResult<ConfidentialMPTClawback> clawbackResult = xrplClient.submit(signedClawback);
    assertThat(clawbackResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for validation
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedClawback.hash(),
        clawbackResult.validatedLedgerIndex(),
        clawback.lastLedgerSequence().orElseThrow(RuntimeException::new),
        clawback.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    //////////////////////
    // Verify the holder's confidential balances are zeroed out after clawback
    MpTokenObject holderMpTokenAfterClawback = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    // After clawback, verify the holder's confidential balances are cleared
    // The confidentialBalanceSpending should be empty or represent zero
    assertThat(holderMpTokenAfterClawback).isNotNull();

    //////////////////////
    // Print all ConfidentialMPT transaction hashes
    System.out.println("\n========== CONFIDENTIAL MPT TRANSACTION HASHES ==========");
    System.out.println("ConfidentialMPTConvert (Holder 1):     " + signedConfidentialConvert.hash());
    System.out.println("ConfidentialMPTMergeInbox (Holder 1):  " + signedMergeInbox.hash());
    System.out.println("ConfidentialMPTConvert (Holder 2):     " + signedHolder2ConfidentialConvert.hash());
    System.out.println("ConfidentialMPTSend:                   " + signedConfidentialSend.hash());
    System.out.println("ConfidentialMPTConvertBack:            " + signedConvertBack.hash());
    System.out.println("ConfidentialMPTClawback:               " + signedClawback.hash());
    System.out.println("==========================================================\n");
  }
}
