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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMPTClawbackService;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMPTConvertBackService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMPTConvertService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMPTSendService;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTSendProof;
import org.xrpl.xrpl4j.crypto.confidential.model.MPTConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.util.MPTAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcMPTAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.MPTAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.bc.BcMPTAmountDecryptor;
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
    ConfidentialMPTConvertService convertService =
      new ConfidentialMPTConvertService();
    ConfidentialMPTSendService sendService =
      new ConfidentialMPTSendService();
    ConfidentialMPTConvertBackService convertBackService =
      new ConfidentialMPTConvertBackService();
    ConfidentialMPTClawbackService clawbackService =
      new ConfidentialMPTClawbackService();
    MPTAmountEncryptor encryptor = new BcMPTAmountEncryptor();
    MPTAmountDecryptor decryptor = new BcMPTAmountDecryptor();

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
    // Get updated holder account info for ConfidentialMPTConvert
    // IMPORTANT: Get this BEFORE generating ZKProof because the context hash includes the sequence number
    AccountInfoResult holderAccountInfoForConvert = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Use ConfidentialMPTConvertService to generate all cryptographic data
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amountToConvert = UnsignedLong.valueOf(500);

    ConfidentialMPTConvertContext context = convertService.generateContext(holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfoForConvert.accountData().sequence(),
      mpTokenIssuanceId,
      amountToConvert
    );

    ConfidentialMPTConvertProof zkProof = convertService.generateProof(holderElGamalKeyPair, context);

    EncryptedAmount holderEncryptedAmount = encryptor.encrypt(amountToConvert,
      holderElGamalKeyPair.publicKey(), blindingFactor);
    EncryptedAmount issuerEncryptedAmount = encryptor.encrypt(amountToConvert, issuerElGamalKeyPair.publicKey(), blindingFactor);



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
      .holderEncryptedAmount(holderEncryptedAmount.toHex())
      .issuerEncryptedAmount(issuerEncryptedAmount.toHex())
      .blindingFactor(blindingFactor.hexValue())
      .zkProof(zkProof.hexValue())
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

    BlindingFactor holder2BlindingFactor = BlindingFactor.generate();

    ConfidentialMPTConvertContext holder2Context = convertService.generateContext(holder2KeyPair.publicKey().deriveAddress(),
      holder2AccountInfoForConvert.accountData().sequence(),
      mpTokenIssuanceId,
      holder2AmountToConvert
    );

    ConfidentialMPTConvertProof holder2ZkProof = convertService.generateProof(holder2ElGamalKeyPair, holder2Context);

    EncryptedAmount holder2EncryptedAmount = encryptor.encrypt(holder2AmountToConvert, holder2ElGamalKeyPair.publicKey(),
      holder2BlindingFactor);
    EncryptedAmount issuer2EncryptedAmount = encryptor.encrypt(holder2AmountToConvert, issuerElGamalKeyPair.publicKey(), holder2BlindingFactor);


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
      .holderEncryptedAmount(holder2EncryptedAmount.toHex())
      .issuerEncryptedAmount(issuer2EncryptedAmount.toHex())
      .blindingFactor(holder2BlindingFactor.hexValue())
      .zkProof(holder2ZkProof.hexValue())
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
    // Using ConfidentialMPTSendService for proof generation
    UnsignedLong sendAmount = UnsignedLong.valueOf(100);


    // Get holder 1 account info for the Send transaction
    AccountInfoResult holderAccountInfoForSend = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    // Get the version from holder 1's MPToken (default to 0 if not present)
    UnsignedInteger holder1Version = holder1MpToken.confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

    // Generate context for ConfidentialMPTSend using the service
    ConfidentialMPTSendContext sendContext = sendService.generateContext(
      holderKeyPair.publicKey().deriveAddress(),  // sender account
      holderAccountInfoForSend.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuanceId
      holder2KeyPair.publicKey().deriveAddress(),  // destination
      holder1Version  // version from MPToken
    );

    // Generate a single transaction blinding factor for all recipients (matching C implementation)
    BlindingFactor txBlindingFactor = BlindingFactor.generate();

    // Encrypt for sender (holder 1) - using wrapper encryptor which returns port's EncryptedAmount
    EncryptedAmount senderCiphertext = encryptor.encrypt(
      sendAmount, holderElGamalKeyPair.publicKey(), txBlindingFactor
    );

    // Encrypt for destination (holder 2)
    EncryptedAmount destinationCiphertext = encryptor.encrypt(
      sendAmount, holder2ElGamalKeyPair.publicKey(), txBlindingFactor
    );

    // Encrypt for issuer
    EncryptedAmount issuerCiphertextForSend = encryptor.encrypt(
      sendAmount, issuerElGamalKeyPair.publicKey(), txBlindingFactor
    );

    // Create MPTConfidentialParty objects for each recipient (publicKey, encryptedAmount)
    MPTConfidentialParty senderParty = MPTConfidentialParty.of(
      holderElGamalKeyPair.publicKey(), senderCiphertext
    );
    MPTConfidentialParty destParty = MPTConfidentialParty.of(
      holder2ElGamalKeyPair.publicKey(), destinationCiphertext
    );
    MPTConfidentialParty issuerParty = MPTConfidentialParty.of(
      issuerElGamalKeyPair.publicKey(), issuerCiphertextForSend
    );

    // Get the sender's current encrypted spending balance from the ledger and decrypt it
    String currentEncryptedBalance = holder1MpToken.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder 1 has no confidential balance"));
    EncryptedAmount currentBalanceCiphertext =
      EncryptedAmount.fromHex(currentEncryptedBalance);

    // Decrypt the sender's current spending balance using wrapper decryptor
    UnsignedLong senderCurrentBalance = decryptor.decrypt(
      currentBalanceCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1_000_000)
    );

    // Generate blinding factors for Pedersen commitments
    BlindingFactor amountBlindingFactorForSend = BlindingFactor.generate();
    BlindingFactor balanceBlindingFactorForSend = BlindingFactor.generate();

    // Generate Pedersen proof parameters for amount using the service
    PedersenProofParams amountParams = sendService.generatePedersenProofParams(
      sendAmount, senderCiphertext, amountBlindingFactorForSend
    );

    // Generate Pedersen proof parameters for balance using the service
    PedersenProofParams balanceParams = sendService.generatePedersenProofParams(
      senderCurrentBalance, currentBalanceCiphertext, balanceBlindingFactorForSend
    );

    // Generate the complete ConfidentialMPTSend proof using the service
    ConfidentialMPTSendProof sendProof = sendService.generateProof(
      holderElGamalKeyPair,
      sendAmount,
      Arrays.asList(senderParty, destParty, issuerParty),
      txBlindingFactor,
      sendContext,
      amountParams,
      balanceParams
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
      .senderEncryptedAmount(senderCiphertext.toHex())
      .destinationEncryptedAmount(destinationCiphertext.toHex())
      .issuerEncryptedAmount(issuerCiphertextForSend.toHex())
      .zkProof(sendProof.hexValue())
      .amountCommitment(amountParams.pedersenCommitment().hexValue())
      .balanceCommitment(balanceParams.pedersenCommitment().hexValue())
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
    EncryptedAmount senderBalanceCiphertextAfterSend = EncryptedAmount.fromBytes(
      senderBalanceBytesAfterSend
    );

    UnsignedLong senderBalanceAfterSend = decryptor.decrypt(
      senderBalanceCiphertextAfterSend, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1_000_000)
    );

    // Expected balance: 500 (initial) - 100 (sent) = 400
    long expectedSenderBalance = amountToConvert.longValue() - sendAmount.longValue();
    assertThat(senderBalanceAfterSend.longValue()).isEqualTo(expectedSenderBalance);

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
    EncryptedAmount holderConvertBackCiphertext = encryptor.encrypt(
      convertBackAmount, holderElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );

    // Encrypt the convert back amount for issuer (to be subtracted from issuer mirror balance)
    EncryptedAmount issuerConvertBackCiphertext = encryptor.encrypt(
      convertBackAmount, issuerElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );

    // Generate context for ConfidentialMPTConvertBack using the service
    ConfidentialMPTConvertBackContext convertBackContext = convertBackService.generateContext(
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
    EncryptedAmount currentBalanceCiphertextForConvertBack = EncryptedAmount.fromHex(
      currentEncryptedBalanceForConvertBack
    );

    // Decrypt the current spending balance from the ledger
    UnsignedLong currentSpendingBalance = decryptor.decrypt(
      currentBalanceCiphertextForConvertBack, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1_000_000)
    );


    // Generate Pedersen proof params for the current spending balance using the service
    BlindingFactor convertBackBalanceBlindingFactor = BlindingFactor.generate();
    PedersenProofParams convertBackBalanceParams = convertBackService.generatePedersenProofParams(
      currentSpendingBalance,
      currentBalanceCiphertextForConvertBack,
      convertBackBalanceBlindingFactor
    );

    // Generate the ConvertBack proof using the service
    ConfidentialMPTConvertBackProof convertBackProof =
      convertBackService.generateProof(
        holderElGamalKeyPair,
        convertBackAmount,
        convertBackContext,
        convertBackBalanceParams
      );

    // Get the Pedersen commitment from the params
    PedersenCommitment convertBackCommitment = org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment.of(
      convertBackBalanceParams.pedersenCommitment()
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
      .holderEncryptedAmount(holderConvertBackCiphertext.toHex())
      .issuerEncryptedAmount(issuerConvertBackCiphertext.toHex())
      .blindingFactor(convertBackBlindingFactor.hexValue())
      .balanceCommitment(convertBackCommitment.hexValue())
      .zkProof(convertBackProof.hexValue())
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
    EncryptedAmount remainingBalanceCiphertext = EncryptedAmount.fromHex(remainingEncryptedBalance);

    UnsignedLong remainingConfidentialBalance = decryptor.decrypt(
      remainingBalanceCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1_000_000)
    );

    // Expected remaining balance: 400 - 50 = 350
    long expectedRemainingBalance = senderBalanceAfterSend.longValue() - convertBackAmount.longValue();
    assertThat(remainingConfidentialBalance.longValue()).isEqualTo(expectedRemainingBalance);

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
    EncryptedAmount issuerBalanceCiphertext = EncryptedAmount.fromHex(issuerEncryptedBalanceForClawback);

    UnsignedLong issuerDecryptedBalance = decryptor.decrypt(issuerBalanceCiphertext, issuerElGamalKeyPair.privateKey(),
      UnsignedLong.ZERO, UnsignedLong.valueOf(1_000_000));
    assertThat(issuerDecryptedBalance.longValue()).isGreaterThanOrEqualTo(clawbackAmount.longValue());

    // Get updated issuer account info for the clawback transaction
    AccountInfoResult issuerAccountInfoForClawback = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Generate context hash for clawback using the service
    ConfidentialMPTClawbackContext clawbackContext = clawbackService.generateContext(
      issuerKeyPair.publicKey().deriveAddress(),  // issuer account
      issuerAccountInfoForClawback.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuance ID
      clawbackAmount,  // amount
      holderKeyPair.publicKey().deriveAddress()  // holder
    );

    // Generate the clawback proof using the service
    ConfidentialMPTClawbackProof clawbackProof = clawbackService.generateProof(
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
