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
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.ZKProofUtils;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextMultiProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextParticipant;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.LinkageProofType;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaPedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SecretKeyProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaElGamalPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertBackContext;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaEqualityPlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.EqualityPlaintextProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaSamePlaintextMultiProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaSecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.java.JavaElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.java.JavaElGamalBalanceEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalKeyPair;
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

import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ConfidentialTransfersIT extends AbstractIT {

  private static final String SUCCESS_STATUS = "tesSUCCESS";

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
    ElGamalKeyPair issuerElGamalKeyPair = ElGamalKeyPair.generate();
    String issuerElGamalPublicKey = issuerElGamalKeyPair.publicKey()
      .toReversedHex64();  // 64 bytes, reversed for C compatibility

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
      .issuerElGamalPublicKey(issuerElGamalPublicKey)
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
    ElGamalKeyPair holderElGamalKeyPair = ElGamalKeyPair.generate();
    String holderElGamalPublicKey = holderElGamalKeyPair.publicKey()
      .toReversedHex64();  // 64 bytes, reversed for C compatibility

    //////////////////////
    // Prepare encryption utilities
    JavaElGamalBalanceEncryptor encryptor = new JavaElGamalBalanceEncryptor();

    //////////////////////
    // Generate blinding factor (same for both holder and issuer encryption)
    BlindingFactor blindingFactor = BlindingFactor.generate();

    //////////////////////
    // Encrypt 500 MPT for holder
    UnsignedLong amountToConvert = UnsignedLong.valueOf(500);
    ElGamalCiphertext holderCiphertext = encryptor.encrypt(
      amountToConvert, holderElGamalKeyPair.publicKey(), blindingFactor
    );
    String holderEncryptedAmount = holderCiphertext.hexValue();

    //////////////////////
    // Encrypt 500 MPT for issuer (using same blinding factor)
    ElGamalCiphertext issuerCiphertext = encryptor.encrypt(
      amountToConvert, issuerElGamalKeyPair.publicKey(), blindingFactor
    );
    String issuerEncryptedAmount = issuerCiphertext.hexValue();

    //////////////////////
    // Verify encryption locally before submitting
    boolean holderEncryptionValid = encryptor.verifyEncryption(
      holderCiphertext, holderElGamalKeyPair.publicKey(), amountToConvert, blindingFactor
    );
    assertThat(holderEncryptionValid).isTrue();

    boolean issuerEncryptionValid = encryptor.verifyEncryption(
      issuerCiphertext, issuerElGamalKeyPair.publicKey(), amountToConvert, blindingFactor
    );
    assertThat(issuerEncryptionValid).isTrue();

    //////////////////////
    // Decrypt ciphertexts to verify they contain the correct amount (500)
    JavaElGamalBalanceDecryptor decryptor = new JavaElGamalBalanceDecryptor();

    // Decrypt holder ciphertext using holder's private key
    long holderDecryptedAmount = decryptor.decrypt(holderCiphertext, holderElGamalKeyPair.privateKey());

    // Decrypt issuer ciphertext using issuer's private key
    long issuerDecryptedAmount = decryptor.decrypt(issuerCiphertext, issuerElGamalKeyPair.privateKey());

    // Verify both decrypt to the same amount
    assertThat(holderDecryptedAmount).isEqualTo(amountToConvert.longValue());
    assertThat(issuerDecryptedAmount).isEqualTo(amountToConvert.longValue());

    //////////////////////
    // Get updated holder account info for ConfidentialMPTConvert
    // IMPORTANT: Get this BEFORE generating ZKProof because the context hash includes the sequence number
    AccountInfoResult holderAccountInfoForConvert = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Generate ZKProof (Schnorr Proof of Knowledge) with context hash

    // Create proof generator
    JavaSecretKeyProofGenerator proofGenerator = new JavaSecretKeyProofGenerator();

    // Generate context hash for ConfidentialMPTConvert transaction
    // Context = SHA512Half(txType || account || sequence || issuanceId || amount)
    ConfidentialMPTConvertContext context = ConfidentialMPTConvertContext.generate(
      holderKeyPair.publicKey().deriveAddress(),  // account
      holderAccountInfoForConvert.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuanceId
      amountToConvert  // amount
    );

    // Generate nonce for proof
    BlindingFactor nonce = BlindingFactor.generate();
    SecretKeyProof zkProof = proofGenerator.generateProof(holderElGamalKeyPair.privateKey(), context, nonce);
    String zkProofHex = zkProof.hexValue();

    // Verify the proof locally before submitting
    boolean localVerify = proofGenerator.verifyProof(zkProof, holderElGamalKeyPair.publicKey(), context);
    assertThat(localVerify).isTrue();

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
      .holderElGamalPublicKey(holderElGamalPublicKey)
      .holderEncryptedAmount(holderEncryptedAmount)
      .issuerEncryptedAmount(issuerEncryptedAmount)
      .blindingFactor(blindingFactor.hexValue())
      .zkProof(zkProofHex)
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
    ElGamalKeyPair holder2ElGamalKeyPair = ElGamalKeyPair.generate();
    String holder2ElGamalPublicKey = holder2ElGamalKeyPair.publicKey().toReversedHex64();

    //////////////////////
    // Prepare encryption for Holder 2 (0 amount conversion to register public key)
    UnsignedLong holder2AmountToConvert = UnsignedLong.ZERO;

    // Generate blinding factor for holder 2
    BlindingFactor holder2BlindingFactor = BlindingFactor.generate();

    // Encrypt 0 MPT for holder 2
    ElGamalCiphertext holder2Ciphertext = encryptor.encrypt(
      holder2AmountToConvert, holder2ElGamalKeyPair.publicKey(), holder2BlindingFactor
    );
    String holder2EncryptedAmount = holder2Ciphertext.hexValue();

    // Encrypt 0 MPT for issuer (using holder 2's blinding factor)
    ElGamalCiphertext issuerCiphertextForHolder2 = encryptor.encrypt(
      holder2AmountToConvert, issuerElGamalKeyPair.publicKey(), holder2BlindingFactor
    );
    String issuerEncryptedAmountForHolder2 = issuerCiphertextForHolder2.hexValue();

    //////////////////////
    // Get updated holder 2 account info for ConfidentialMPTConvert
    AccountInfoResult holder2AccountInfoForConvert = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder2KeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Generate ZKProof for Holder 2

    ConfidentialMPTConvertContext holder2Context = ConfidentialMPTConvertContext.generate(
      holder2KeyPair.publicKey().deriveAddress(),
      holder2AccountInfoForConvert.accountData().sequence(),
      mpTokenIssuanceId,
      holder2AmountToConvert
    );

    BlindingFactor holder2Nonce = BlindingFactor.generate();
    SecretKeyProof holder2ZkProof = proofGenerator.generateProof(holder2ElGamalKeyPair.privateKey(), holder2Context,
      holder2Nonce);
    String holder2ZkProofHex = holder2ZkProof.hexValue();

    // Verify the proof locally
    boolean holder2LocalVerify = proofGenerator.verifyProof(holder2ZkProof, holder2ElGamalKeyPair.publicKey(),
      holder2Context);
    assertThat(holder2LocalVerify).isTrue();

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
      .holderElGamalPublicKey(holder2ElGamalPublicKey)
      .holderEncryptedAmount(holder2EncryptedAmount)
      .issuerEncryptedAmount(issuerEncryptedAmountForHolder2)
      .blindingFactor(holder2BlindingFactor.hexValue())
      .zkProof(holder2ZkProofHex)
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
    String senderEncryptedAmount = senderCiphertext.hexValue();

    // Encrypt for destination (holder 2)
    ElGamalCiphertext destinationCiphertext = encryptor.encrypt(
      sendAmount, holder2ElGamalKeyPair.publicKey(), sendBlindingFactorHolder2
    );
    String destinationEncryptedAmount = destinationCiphertext.hexValue();

    // Encrypt for issuer
    ElGamalCiphertext issuerCiphertextForSend = encryptor.encrypt(
      sendAmount, issuerElGamalKeyPair.publicKey(), sendBlindingFactorIssuer
    );
    String issuerEncryptedAmountForSend = issuerCiphertextForSend.hexValue();

    //////////////////////
    // Get holder 1 account info for the Send transaction
    AccountInfoResult holderAccountInfoForSend = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Generate SamePlaintextMultiProof (proves all 3 ciphertexts encrypt the same amount)
    JavaSamePlaintextMultiProofGenerator samePlaintextProofGenerator = new JavaSamePlaintextMultiProofGenerator();

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

    // Generate nonces for the proof (one for amount, one for each of 3 participants)
    BlindingFactor nonceKm = BlindingFactor.generate();
    BlindingFactor nonceKrSender = BlindingFactor.generate();
    BlindingFactor nonceKrDest = BlindingFactor.generate();
    BlindingFactor nonceKrIssuer = BlindingFactor.generate();

    // Create participants for proof generation
    SamePlaintextParticipant senderParticipant = SamePlaintextParticipant.forProofGeneration(
      senderCiphertext, holderElGamalKeyPair.publicKey(), sendBlindingFactorSender, nonceKrSender
    );
    SamePlaintextParticipant destParticipant = SamePlaintextParticipant.forProofGeneration(
      destinationCiphertext, holder2ElGamalKeyPair.publicKey(), sendBlindingFactorHolder2, nonceKrDest
    );
    SamePlaintextParticipant issuerParticipant = SamePlaintextParticipant.forProofGeneration(
      issuerCiphertextForSend, issuerElGamalKeyPair.publicKey(), sendBlindingFactorIssuer, nonceKrIssuer
    );

    // Generate the SamePlaintextMultiProof for all 3 participants: sender, destination, issuer
    SamePlaintextMultiProof samePlaintextProof = samePlaintextProofGenerator.generateProof(
      sendAmount,
      senderParticipant,
      destParticipant,
      issuerParticipant,
      Optional.empty(),
      sendContext,
      nonceKm
    );

    // Create participants for verification (only need ciphertext and publicKey)
    SamePlaintextParticipant senderVerify = SamePlaintextParticipant.forVerification(
      senderCiphertext, holderElGamalKeyPair.publicKey()
    );
    SamePlaintextParticipant destVerify = SamePlaintextParticipant.forVerification(
      destinationCiphertext, holder2ElGamalKeyPair.publicKey()
    );
    SamePlaintextParticipant issuerVerify = SamePlaintextParticipant.forVerification(
      issuerCiphertextForSend, issuerElGamalKeyPair.publicKey()
    );

    // Verify the proof locally
    boolean sendProofValid = samePlaintextProofGenerator.verify(
      samePlaintextProof,
      senderVerify,
      destVerify,
      issuerVerify,
      Optional.empty(),
      sendContext
    );
    assertThat(sendProofValid).isTrue();

    //////////////////////
    // Generate Pedersen Commitments and Linkage Proofs
    PedersenCommitmentGenerator pedersenGen = new JavaPedersenCommitmentGenerator();
    JavaElGamalPedersenLinkProofGenerator linkProofGenerator = new JavaElGamalPedersenLinkProofGenerator();

    // Generate blinding factors for Pedersen commitments
    BlindingFactor amountPedersenRho = BlindingFactor.generate();
    BlindingFactor balancePedersenRho = BlindingFactor.generate();

    // Generate Amount Pedersen Commitment: PCm = sendAmount * G + amountPedersenRho * H
    PedersenCommitment amountCommitment = pedersenGen.generateCommitment(sendAmount, amountPedersenRho);

    // For balance commitment, we need the sender's current balance (500)
    // The balance commitment proves the sender has sufficient funds
    UnsignedLong senderCurrentBalance = UnsignedLong.valueOf(500);
    PedersenCommitment balanceCommitment = pedersenGen.generateCommitment(senderCurrentBalance, balancePedersenRho);

    //////////////////////
    // Generate Amount Linkage Proof
    // Proves: sender's encrypted amount (senderCiphertext) links to amountCommitment
    BlindingFactor amountNonceKm = BlindingFactor.generate();
    BlindingFactor amountNonceKr = BlindingFactor.generate();
    BlindingFactor amountNonceKrho = BlindingFactor.generate();

    ElGamalPedersenLinkProof amountLinkageProof = linkProofGenerator.generateProof(
      LinkageProofType.AMOUNT_COMMITMENT,
      senderCiphertext,
      holderElGamalKeyPair.publicKey(),
      amountCommitment,
      sendAmount,
      sendBlindingFactorSender,
      amountPedersenRho,
      amountNonceKm,
      amountNonceKr,
      amountNonceKrho,
      sendContext
    );

    // Verify the amount linkage proof locally
    boolean amountLinkageValid = linkProofGenerator.verify(
      LinkageProofType.AMOUNT_COMMITMENT,
      amountLinkageProof,
      senderCiphertext,
      holderElGamalKeyPair.publicKey(),
      amountCommitment,
      sendContext
    );
    assertThat(amountLinkageValid).isTrue();

    //////////////////////
    // Generate Balance Linkage Proof
    // Proves: sender's current encrypted balance links to balanceCommitment
    // Uses the sender's private key as the "r" parameter (swapped parameters vs amount linkage)

    // Get the sender's current encrypted balance from the MPToken object
    String currentEncryptedBalance = holder1MpToken.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder 1 has no confidential balance"));
    byte[] currentBalanceBytes = BaseEncoding.base16().decode(currentEncryptedBalance);
    ElGamalCiphertext currentBalanceCiphertext = ElGamalCiphertext.fromBytes(currentBalanceBytes);

    // Get the holder's ElGamal private key as a BlindingFactor
    BlindingFactor holderPrivateKeyAsBlindingFactor = BlindingFactor.fromBytes(
      holderElGamalKeyPair.privateKey().naturalBytes().toByteArray()
    );

    // Generate Balance Linkage Proof nonces
    BlindingFactor balanceNonceKm = BlindingFactor.generate();
    BlindingFactor balanceNonceKr = BlindingFactor.generate();
    BlindingFactor balanceNonceKrho = BlindingFactor.generate();

    ElGamalPedersenLinkProof balanceLinkageProof = linkProofGenerator.generateProof(
      LinkageProofType.BALANCE_COMMITMENT,
      currentBalanceCiphertext,
      holderElGamalKeyPair.publicKey(),
      balanceCommitment,
      senderCurrentBalance,
      holderPrivateKeyAsBlindingFactor,
      balancePedersenRho,
      balanceNonceKm,
      balanceNonceKr,
      balanceNonceKrho,
      sendContext
    );

    //////////////////////
    // Combine all proofs into full ZKProof for ConfidentialMPTSend
    // SamePlaintextMultiProof (359 bytes) + Amount Linkage (195 bytes) + Balance Linkage (195 bytes) = 749 bytes
    String fullZkProofHex = ZKProofUtils.combineSendProofsHex(
      samePlaintextProof, amountLinkageProof, balanceLinkageProof
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
      .senderEncryptedAmount(senderEncryptedAmount)
      .destinationEncryptedAmount(destinationEncryptedAmount)
      .issuerEncryptedAmount(issuerEncryptedAmountForSend)
      .zkProof(fullZkProofHex)
      .amountCommitment(amountCommitment.toReversedHex64())
      .balanceCommitment(balanceCommitment.toReversedHex64())
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

    JavaElGamalBalanceDecryptor balanceDecryptor = new JavaElGamalBalanceDecryptor();
    long senderBalanceAfterSend = balanceDecryptor.decrypt(
      senderBalanceCiphertextAfterSend, holderElGamalKeyPair.privateKey()
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
    String holderConvertBackEncryptedAmount = holderConvertBackCiphertext.hexValue();

    // Encrypt the convert back amount for issuer (to be subtracted from issuer mirror balance)
    ElGamalCiphertext issuerConvertBackCiphertext = encryptor.encrypt(
      convertBackAmount, issuerElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );
    String issuerConvertBackEncryptedAmount = issuerConvertBackCiphertext.hexValue();

    // Generate context for ConfidentialMPTConvertBack
    ConfidentialMPTConvertBackContext convertBackContext = ConfidentialMPTConvertBackContext.generate(
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfoForConvertBack.accountData().sequence(),
      mpTokenIssuanceId,
      convertBackAmount,
      holder1VersionForConvertBack
    );

    // Generate Pedersen commitment for the current spending balance (400)
    UnsignedLong currentSpendingBalance = UnsignedLong.valueOf(senderBalanceAfterSend);
    BlindingFactor convertBackPedersenRho = BlindingFactor.generate();
    PedersenCommitment convertBackCommitment = pedersenGen.generateCommitment(currentSpendingBalance,
      convertBackPedersenRho);

    // Get the current encrypted balance from the ledger for the balance linkage proof
    String currentEncryptedBalanceForConvertBack = holder1MpTokenForConvertBack.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder 1 has no confidential balance"));
    byte[] currentBalanceBytesForConvertBack = BaseEncoding.base16().decode(
      currentEncryptedBalanceForConvertBack
    );
    ElGamalCiphertext currentBalanceCiphertextForConvertBack = ElGamalCiphertext.fromBytes(
      currentBalanceBytesForConvertBack
    );

    // Generate Balance Linkage Proof
    // This proves the Pedersen commitment matches the on-ledger encrypted balance
    BlindingFactor convertBackNonceKm = BlindingFactor.generate();
    BlindingFactor convertBackNonceKr = BlindingFactor.generate();
    BlindingFactor convertBackNonceKrho = BlindingFactor.generate();

    ElGamalPedersenLinkProof convertBackBalanceLinkageProof = linkProofGenerator.generateProof(
      LinkageProofType.BALANCE_COMMITMENT,
      currentBalanceCiphertextForConvertBack,
      holderElGamalKeyPair.publicKey(),
      convertBackCommitment,
      currentSpendingBalance,
      holderPrivateKeyAsBlindingFactor,
      convertBackPedersenRho,
      convertBackNonceKm,
      convertBackNonceKr,
      convertBackNonceKrho,
      convertBackContext
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
      .holderEncryptedAmount(holderConvertBackEncryptedAmount)
      .issuerEncryptedAmount(issuerConvertBackEncryptedAmount)
      .blindingFactor(convertBackBlindingFactor.hexValue())
      .balanceCommitment(convertBackCommitment.toReversedHex64())
      .zkProof(convertBackBalanceLinkageProof.hexValue())
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
      remainingBalanceCiphertext, holderElGamalKeyPair.privateKey()
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

    // Verify the issuer can decrypt this balance to 350
    long issuerDecryptedBalance = balanceDecryptor.decrypt(issuerBalanceCiphertext, issuerElGamalKeyPair.privateKey());
    assertThat(issuerDecryptedBalance).isEqualTo(clawbackAmount.longValue());

    // Get updated issuer account info for the clawback transaction
    AccountInfoResult issuerAccountInfoForClawback = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Generate the Equality Plaintext Proof
    // For clawback, the ciphertext is constructed as: c1=issuer's pk, c2=balance.c2
    // And the publicKey is the balance.c1
    // The issuer uses their private key as the "randomness" parameter
    JavaEqualityPlaintextProofGenerator equalityProofGenerator = new JavaEqualityPlaintextProofGenerator();

    // Generate context hash for clawback
    ConfidentialMPTClawbackContext clawbackContext = ConfidentialMPTClawbackContext.generate(
      issuerKeyPair.publicKey().deriveAddress(),  // issuer account
      issuerAccountInfoForClawback.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuance ID
      clawbackAmount,  // amount
      holderKeyPair.publicKey().deriveAddress()  // holder
    );

    // Generate the Equality Plaintext Proof (parameter swapping handled internally)
    BlindingFactor issuerPrivateKeyAsBlindingFactor = BlindingFactor.fromBytes(
      issuerElGamalKeyPair.privateKey().naturalBytes().toByteArray()
    );
    BlindingFactor clawbackNonce = BlindingFactor.generate();

    EqualityPlaintextProof clawbackProof = equalityProofGenerator.generateProof(
      issuerBalanceCiphertext,  // IssuerEncryptedBalance ciphertext
      issuerElGamalKeyPair.publicKey(),  // issuer's ElGamal public key
      clawbackAmount,
      issuerPrivateKeyAsBlindingFactor,  // issuer's private key as "randomness"
      clawbackNonce,  // random nonce for commitment
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
