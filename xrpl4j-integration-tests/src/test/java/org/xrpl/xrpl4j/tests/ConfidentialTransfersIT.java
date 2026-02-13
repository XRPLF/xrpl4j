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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaElGamalPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaSamePlaintextMultiProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java.JavaSecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.java.JavaElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.java.JavaElGamalBalanceEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;
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
import org.xrpl.xrpl4j.model.transactions.BlindingFactor;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMPTConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMPTSend;
import org.xrpl.xrpl4j.model.transactions.ElGamalPublicKey;
import org.xrpl.xrpl4j.model.transactions.EncryptedAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;

import java.security.SecureRandom;
import java.util.Arrays;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ConfidentialTransfersIT extends AbstractIT {

  private static final UnsignedInteger NETWORK_ID = UnsignedInteger.valueOf(85449);

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
    KeyPair issuerElGamalKeyPair = Seed.secp256k1Seed().deriveKeyPair();
    ElGamalPublicKey issuerElGamalPublicKey = ElGamalPublicKey.of(
      issuerElGamalKeyPair.publicKey().uncompressedValueReversed().hexValue()  // 64 bytes, reversed for C compatibility
    );

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
    KeyPair holderElGamalKeyPair = Seed.secp256k1Seed().deriveKeyPair();
    ElGamalPublicKey holderElGamalPublicKey = ElGamalPublicKey.of(
      holderElGamalKeyPair.publicKey().uncompressedValueReversed().hexValue()  // 64 bytes, reversed for C compatibility
    );

    //////////////////////
    // Prepare encryption utilities
    Secp256k1Operations secp256k1 = new Secp256k1Operations();
    SecureRandom secureRandom = new SecureRandom();
    JavaElGamalBalanceEncryptor encryptor = new JavaElGamalBalanceEncryptor(secp256k1);

    //////////////////////
    // Generate blinding factor (same for both holder and issuer encryption)
    byte[] blindingFactorBytes = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BlindingFactor blindingFactor = BlindingFactor.of(BaseEncoding.base16().encode(blindingFactorBytes));

    //////////////////////
    // Encrypt 500 MPT for holder
    UnsignedLong amountToConvert = UnsignedLong.valueOf(500);
    ECPoint holderElGamalEcPoint = BcKeyUtils.toEcPublicKeyParameters(
      holderElGamalKeyPair.publicKey()
    ).getQ();
    ElGamalCiphertext holderCiphertext = encryptor.encrypt(amountToConvert, holderElGamalEcPoint, blindingFactorBytes);
    EncryptedAmount holderEncryptedAmount = EncryptedAmount.of(
      BaseEncoding.base16().encode(holderCiphertext.toBytes())
    );

    //////////////////////
    // Encrypt 500 MPT for issuer (using same blinding factor)
    ECPoint issuerElGamalEcPoint = BcKeyUtils.toEcPublicKeyParameters(
      issuerElGamalKeyPair.publicKey()
    ).getQ();
    ElGamalCiphertext issuerCiphertext = encryptor.encrypt(amountToConvert, issuerElGamalEcPoint, blindingFactorBytes);
    EncryptedAmount issuerEncryptedAmount = EncryptedAmount.of(
      BaseEncoding.base16().encode(issuerCiphertext.toBytes())
    );

    //////////////////////
    // Verify encryption locally before submitting
    boolean holderEncryptionValid = encryptor.verifyEncryption(
      holderCiphertext, holderElGamalEcPoint, amountToConvert, blindingFactorBytes
    );
    assertThat(holderEncryptionValid).isTrue();

    boolean issuerEncryptionValid = encryptor.verifyEncryption(
      issuerCiphertext, issuerElGamalEcPoint, amountToConvert, blindingFactorBytes
    );
    assertThat(issuerEncryptionValid).isTrue();

    //////////////////////
    // Decrypt ciphertexts to verify they contain the correct amount (500)
    JavaElGamalBalanceDecryptor decryptor = new JavaElGamalBalanceDecryptor(secp256k1);

    // Decrypt holder ciphertext using holder's private key
    ElGamalPrivateKey holderPrivateKeyForDecrypt = ElGamalPrivateKey.of(
      holderElGamalKeyPair.privateKey().naturalBytes()
    );
    long holderDecryptedAmount = decryptor.decrypt(holderCiphertext, holderPrivateKeyForDecrypt);

    // Decrypt issuer ciphertext using issuer's private key
    ElGamalPrivateKey issuerPrivateKeyForDecrypt = ElGamalPrivateKey.of(
      issuerElGamalKeyPair.privateKey().naturalBytes()
    );
    long issuerDecryptedAmount = decryptor.decrypt(issuerCiphertext, issuerPrivateKeyForDecrypt);

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
    byte[] holderPrivateKeyBytes = holderElGamalKeyPair.privateKey().naturalBytes().toByteArray();

    // Create proof generator and generate context hash
    JavaSecretKeyProofGenerator proofGenerator = new JavaSecretKeyProofGenerator(secp256k1);

    // Generate context hash for ConfidentialMPTConvert transaction
    // Context = SHA512Half(txType || account || sequence || issuanceId || amount)
    byte[] contextId = proofGenerator.generateConvertContext(
      holderKeyPair.publicKey().deriveAddress(),  // account
      holderAccountInfoForConvert.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuanceId
      amountToConvert  // amount
    );

    // Generate proof with context (nonce = null for random)
    byte[] zkProofBytes = proofGenerator.generateProof(holderPrivateKeyBytes, contextId, null);
    String zkProof = BaseEncoding.base16().encode(zkProofBytes);

    // Verify the proof locally before submitting
    boolean localVerify = proofGenerator.verifyProof(zkProofBytes, holderElGamalEcPoint, contextId);
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
      .blindingFactor(blindingFactor)
      .zkProof(zkProof)
      .build();

    SingleSignedTransaction<ConfidentialMPTConvert> signedConfidentialConvert = signatureService.sign(
      holderKeyPair.privateKey(), confidentialConvert
    );
    SubmitResult<ConfidentialMPTConvert> confidentialConvertResult = xrplClient.submit(signedConfidentialConvert);
    assertThat(confidentialConvertResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    System.out.println("Holder 1 ConfidentialMPTConvert tx hash: " + signedConfidentialConvert.hash());

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
    // Create second holder account
    KeyPair holder2KeyPair = createRandomAccountEd25519();
    System.out.println("Holder 2 Address: " + holder2KeyPair.publicKey().deriveAddress());

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
    KeyPair holder2ElGamalKeyPair = Seed.secp256k1Seed().deriveKeyPair();
    ElGamalPublicKey holder2ElGamalPublicKey = ElGamalPublicKey.of(
      holder2ElGamalKeyPair.publicKey().uncompressedValueReversed().hexValue()
    );

    //////////////////////
    // Prepare encryption for Holder 2 (0 amount conversion to register public key)
    UnsignedLong holder2AmountToConvert = UnsignedLong.ZERO;

    // Generate blinding factor for holder 2
    byte[] holder2BlindingFactorBytes = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BlindingFactor holder2BlindingFactor = BlindingFactor.of(BaseEncoding.base16().encode(holder2BlindingFactorBytes));

    // Encrypt 0 MPT for holder 2
    ECPoint holder2ElGamalEcPoint = BcKeyUtils.toEcPublicKeyParameters(
      holder2ElGamalKeyPair.publicKey()
    ).getQ();
    ElGamalCiphertext holder2Ciphertext = encryptor.encrypt(
      holder2AmountToConvert, holder2ElGamalEcPoint, holder2BlindingFactorBytes
    );
    EncryptedAmount holder2EncryptedAmount = EncryptedAmount.of(
      BaseEncoding.base16().encode(holder2Ciphertext.toBytes())
    );

    // Encrypt 0 MPT for issuer (using holder 2's blinding factor)
    ElGamalCiphertext issuerCiphertextForHolder2 = encryptor.encrypt(
      holder2AmountToConvert, issuerElGamalEcPoint, holder2BlindingFactorBytes
    );
    EncryptedAmount issuerEncryptedAmountForHolder2 = EncryptedAmount.of(
      BaseEncoding.base16().encode(issuerCiphertextForHolder2.toBytes())
    );

    //////////////////////
    // Get updated holder 2 account info for ConfidentialMPTConvert
    AccountInfoResult holder2AccountInfoForConvert = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder2KeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Generate ZKProof for Holder 2
    byte[] holder2PrivateKeyBytes = holder2ElGamalKeyPair.privateKey().naturalBytes().toByteArray();

    byte[] holder2ContextId = proofGenerator.generateConvertContext(
      holder2KeyPair.publicKey().deriveAddress(),
      holder2AccountInfoForConvert.accountData().sequence(),
      mpTokenIssuanceId,
      holder2AmountToConvert
    );

    byte[] holder2ZkProofBytes = proofGenerator.generateProof(holder2PrivateKeyBytes, holder2ContextId, null);
    String holder2ZkProof = BaseEncoding.base16().encode(holder2ZkProofBytes);

    // Verify the proof locally
    boolean holder2LocalVerify = proofGenerator.verifyProof(holder2ZkProofBytes, holder2ElGamalEcPoint,
      holder2ContextId);
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
      .blindingFactor(holder2BlindingFactor)
      .zkProof(holder2ZkProof)
      .build();

    SingleSignedTransaction<ConfidentialMPTConvert> signedHolder2ConfidentialConvert = signatureService.sign(
      holder2KeyPair.privateKey(), holder2ConfidentialConvert
    );
    SubmitResult<ConfidentialMPTConvert> holder2ConfidentialConvertResult = xrplClient.submit(
      signedHolder2ConfidentialConvert
    );

    System.out.println("\n========== HOLDER 2 REGISTRATION ==========");
    System.out.println("Holder 2 Address: " + holder2KeyPair.publicKey().deriveAddress());
    System.out.println("Holder 2 ConfidentialMPTConvert tx hash: " + signedHolder2ConfidentialConvert.hash());
    System.out.println("Holder 2 ConfidentialMPTConvert result: " + holder2ConfidentialConvertResult.engineResult());

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
    // Get and print MPToken objects for both holders
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    MpTokenObject holder1MpToken = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    System.out.println("\n========== HOLDER 1 MPTOKEN OBJECT ==========");
    System.out.println(holder1MpToken.toString());

    MpTokenObject holder2MpToken = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holder2KeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    System.out.println("\n========== HOLDER 2 MPTOKEN OBJECT ==========");
    System.out.println(holder2MpToken.toString());

    //////////////////////
    // ConfidentialMPTSend: Holder 1 sends 100 MPT to Holder 2
    UnsignedLong sendAmount = UnsignedLong.valueOf(100);

    // Generate blinding factors for the send (one for each recipient: sender, destination, issuer)
    byte[] sendBlindingFactorSender = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] sendBlindingFactorHolder2 = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] sendBlindingFactorIssuer = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);

    // Encrypt for sender (holder 1) - this is the amount being debited
    ElGamalCiphertext senderCiphertext = encryptor.encrypt(sendAmount, holderElGamalEcPoint, sendBlindingFactorSender);
    EncryptedAmount senderEncryptedAmount = EncryptedAmount.of(
      BaseEncoding.base16().encode(senderCiphertext.toBytes())
    );

    // Encrypt for destination (holder 2)
    ElGamalCiphertext destinationCiphertext = encryptor.encrypt(
      sendAmount, holder2ElGamalEcPoint, sendBlindingFactorHolder2
    );
    EncryptedAmount destinationEncryptedAmount = EncryptedAmount.of(
      BaseEncoding.base16().encode(destinationCiphertext.toBytes())
    );

    // Encrypt for issuer
    ElGamalCiphertext issuerCiphertextForSend = encryptor.encrypt(
      sendAmount, issuerElGamalEcPoint, sendBlindingFactorIssuer
    );
    EncryptedAmount issuerEncryptedAmountForSend = EncryptedAmount.of(
      BaseEncoding.base16().encode(issuerCiphertextForSend.toBytes())
    );

    //////////////////////
    // Get holder 1 account info for the Send transaction
    AccountInfoResult holderAccountInfoForSend = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    //////////////////////
    // Generate SamePlaintextMultiProof
    JavaSamePlaintextMultiProofGenerator samePlaintextProofGenerator = new JavaSamePlaintextMultiProofGenerator(
      secp256k1);

    // Get the version from holder 1's MPToken (default to 0 if not present)
    UnsignedInteger holder1Version = holder1MpToken.confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

    // Generate context hash for ConfidentialMPTSend
    byte[] sendContextHash = samePlaintextProofGenerator.generateSendContext(
      holderKeyPair.publicKey().deriveAddress(),  // sender account
      holderAccountInfoForSend.accountData().sequence(),  // sequence
      mpTokenIssuanceId,  // issuanceId
      holder2KeyPair.publicKey().deriveAddress(),  // destination
      holder1Version  // version from MPToken
    );

    System.out.println("\n========== SEND CONTEXT ==========");
    System.out.println("Sender: " + holderKeyPair.publicKey().deriveAddress());
    System.out.println("Destination: " + holder2KeyPair.publicKey().deriveAddress());
    System.out.println("Sequence: " + holderAccountInfoForSend.accountData().sequence());
    System.out.println("Version: " + holder1Version);
    System.out.println("Context Hash: " + BaseEncoding.base16().encode(sendContextHash));

    // Generate nonces for the proof (one for amount, one for each of 3 recipients)
    byte[] nonceKm = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] nonceKrSender = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] nonceKrDest = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] nonceKrIssuer = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);

    // Generate the SamePlaintextMultiProof for all 3 ciphertexts: sender, destination, issuer
    byte[] samePlaintextProof = samePlaintextProofGenerator.generateProof(
      sendAmount,
      Arrays.asList(senderCiphertext, destinationCiphertext, issuerCiphertextForSend),
      Arrays.asList(holderElGamalEcPoint, holder2ElGamalEcPoint, issuerElGamalEcPoint),
      Arrays.asList(sendBlindingFactorSender, sendBlindingFactorHolder2, sendBlindingFactorIssuer),
      sendContextHash,
      nonceKm,
      Arrays.asList(nonceKrSender, nonceKrDest, nonceKrIssuer)
    );

    System.out.println(
      "SamePlaintextMultiProof size: " + samePlaintextProof.length + " bytes (expected 359 for 3 recipients)");
    System.out.println("SamePlaintextMultiProof: " + BaseEncoding.base16().encode(samePlaintextProof));

    // Verify the proof locally
    boolean sendProofValid = samePlaintextProofGenerator.verify(
      samePlaintextProof,
      Arrays.asList(senderCiphertext, destinationCiphertext, issuerCiphertextForSend),
      Arrays.asList(holderElGamalEcPoint, holder2ElGamalEcPoint, issuerElGamalEcPoint),
      sendContextHash
    );
    System.out.println("SamePlaintextMultiProof valid locally: " + sendProofValid);
    assertThat(sendProofValid).isTrue();

    //////////////////////
    // Generate Pedersen Commitments and Linkage Proofs
    PedersenCommitmentGenerator pedersenGen = new PedersenCommitmentGenerator(secp256k1);
    JavaElGamalPedersenLinkProofGenerator linkProofGenerator = new JavaElGamalPedersenLinkProofGenerator(secp256k1);

    // Generate blinding factors for Pedersen commitments
    byte[] amountPedersenRho = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] balancePedersenRho = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);

    // Generate Amount Pedersen Commitment: PCm = sendAmount * G + amountPedersenRho * H
    byte[] amountCommitmentBytes = pedersenGen.generateCommitment(sendAmount, amountPedersenRho);
    ECPoint amountCommitmentPoint = secp256k1.deserialize(amountCommitmentBytes);

    // For balance commitment, we need the sender's new balance after the send
    // Current balance = 500 (from ConfidentialMPTConvert), sending 100, so new balance = 400
    UnsignedLong senderNewBalance = UnsignedLong.valueOf(400);
    byte[] balanceCommitmentBytes = pedersenGen.generateCommitment(senderNewBalance, balancePedersenRho);
    ECPoint balanceCommitmentPoint = secp256k1.deserialize(balanceCommitmentBytes);

    // Convert commitments to uncompressed format (64 bytes) for the transaction
    // Remove the 04 prefix from uncompressed encoding
    byte[] amountCommitmentUncompressed = amountCommitmentPoint.getEncoded(false);
    byte[] balanceCommitmentUncompressed = balanceCommitmentPoint.getEncoded(false);
    // Skip the 04 prefix byte to get 64 bytes (X, Y)
    byte[] amountCommitment64 = new byte[64];
    byte[] balanceCommitment64 = new byte[64];
    System.arraycopy(amountCommitmentUncompressed, 1, amountCommitment64, 0, 64);
    System.arraycopy(balanceCommitmentUncompressed, 1, balanceCommitment64, 0, 64);

    // Reverse X and Y coordinates to match the format used for public keys on the ledger
    // This is required because rippled does memcpy directly into secp256k1_pubkey.data
    // and expects the same reversed format as public keys
    byte[] amountCommitment64Reversed = new byte[64];
    byte[] balanceCommitment64Reversed = new byte[64];
    // Reverse X coordinate (first 32 bytes)
    for (int i = 0; i < 32; i++) {
      amountCommitment64Reversed[i] = amountCommitment64[31 - i];
      balanceCommitment64Reversed[i] = balanceCommitment64[31 - i];
    }
    // Reverse Y coordinate (last 32 bytes)
    for (int i = 0; i < 32; i++) {
      amountCommitment64Reversed[32 + i] = amountCommitment64[63 - i];
      balanceCommitment64Reversed[32 + i] = balanceCommitment64[63 - i];
    }

    String amountCommitment = BaseEncoding.base16().encode(amountCommitment64Reversed);
    String balanceCommitment = BaseEncoding.base16().encode(balanceCommitment64Reversed);

    System.out.println("Amount Commitment: " + amountCommitment);
    System.out.println("Balance Commitment: " + balanceCommitment);

    //////////////////////
    // Generate Amount Linkage Proof using helper method
    // Proves: sender's encrypted amount (senderCiphertext) links to amountCommitment
    byte[] amountLinkageProof = generateAmountLinkageProof(
      linkProofGenerator,
      senderCiphertext,
      holderElGamalEcPoint,
      amountCommitmentPoint,
      sendAmount,
      sendBlindingFactorSender,
      amountPedersenRho,
      sendContextHash,
      secureRandom,
      secp256k1
    );

    System.out.println("Amount Linkage Proof size: " + amountLinkageProof.length + " bytes (expected 195)");
    System.out.println("Amount Linkage Proof: " + BaseEncoding.base16().encode(amountLinkageProof));

    // Print values for C code verification
    System.out.println("\n========== VALUES FOR C CODE VERIFICATION (Amount Linkage) ==========");
    System.out.println("// Public Key Pk (33 bytes compressed - used in challenge hash)");
    System.out.println(
      "const char* pk_compressed_hex = \"" + BaseEncoding.base16().encode(holderElGamalEcPoint.getEncoded(true)) +
        "\";");
    System.out.println("// Public Key Pk (65 bytes with 04 prefix - standard format)");
    System.out.println(
      "const char* pk_hex = \"" + BaseEncoding.base16().encode(holderElGamalEcPoint.getEncoded(false)) + "\";");
    System.out.println("// Public Key Pk (64 bytes reversed - as stored on ledger)");
    System.out.println(
      "const char* pk_reversed_hex = \"" + holderElGamalKeyPair.publicKey().uncompressedValueReversed().hexValue() +
        "\";");
    System.out.println("// ElGamal ciphertext C1 = r*G (33 bytes compressed)");
    System.out.println(
      "const char* c1_compressed_hex = \"" + BaseEncoding.base16().encode(senderCiphertext.c1().getEncoded(true)) +
        "\";");
    System.out.println("// ElGamal ciphertext C1 = r*G (65 bytes with 04 prefix)");
    System.out.println(
      "const char* c1_hex = \"" + BaseEncoding.base16().encode(senderCiphertext.c1().getEncoded(false)) + "\";");
    System.out.println("// ElGamal ciphertext C2 = m*G + r*Pk (33 bytes compressed)");
    System.out.println(
      "const char* c2_compressed_hex = \"" + BaseEncoding.base16().encode(senderCiphertext.c2().getEncoded(true)) +
        "\";");
    System.out.println("// ElGamal ciphertext C2 = m*G + r*Pk (65 bytes with 04 prefix)");
    System.out.println(
      "const char* c2_hex = \"" + BaseEncoding.base16().encode(senderCiphertext.c2().getEncoded(false)) + "\";");
    System.out.println("// Pedersen commitment PCm = m*G + rho*H (33 bytes compressed)");
    System.out.println(
      "const char* pcm_compressed_hex = \"" + BaseEncoding.base16().encode(amountCommitmentPoint.getEncoded(true)) +
        "\";");
    System.out.println("// Pedersen commitment PCm = m*G + rho*H (65 bytes with 04 prefix)");
    System.out.println(
      "const char* pcm_hex = \"" + BaseEncoding.base16().encode(amountCommitmentPoint.getEncoded(false)) + "\";");
    System.out.println("// Pedersen commitment PCm (64 bytes without prefix - as sent in tx)");
    System.out.println("const char* pcm_64_hex = \"" + amountCommitment + "\";");
    System.out.println("// Context ID (32 bytes)");
    System.out.println("const char* context_id_hex = \"" + BaseEncoding.base16().encode(sendContextHash) + "\";");
    System.out.println("// Proof from Java (195 bytes = 390 hex chars)");
    System.out.println("const char* proof_hex = \"" + BaseEncoding.base16().encode(amountLinkageProof) + "\";");
    System.out.println("// Amount (plaintext value)");
    System.out.println("uint64_t amount = " + sendAmount.longValue() + ";");
    System.out.println("// Sender Encrypted Amount (66 bytes - as sent in tx)");
    System.out.println("const char* sender_enc_amt_hex = \"" + senderEncryptedAmount.value() + "\";");
    System.out.println("=======================================================================\n");

    // Verify the amount linkage proof locally
    boolean amountLinkageValid = linkProofGenerator.verify(
      amountLinkageProof,
      senderCiphertext.c1(),
      senderCiphertext.c2(),
      holderElGamalEcPoint,
      amountCommitmentPoint,
      sendContextHash
    );
    System.out.println("Amount Linkage Proof valid locally: " + amountLinkageValid);
    assertThat(amountLinkageValid).isTrue();

    //////////////////////
    // Generate Balance Linkage Proof
    // Proves: sender's new encrypted balance links to balanceCommitment
    // The balance linkage proof uses the sender's current encrypted balance from the ledger
    // and the sender's private key as the "r" parameter (swapped parameters vs amount linkage)

    // Get the sender's current encrypted balance from the MPToken object
    EncryptedAmount currentEncryptedBalance = holder1MpToken.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder 1 has no confidential balance"));
    byte[] currentBalanceBytes = BaseEncoding.base16().decode(currentEncryptedBalance.value());
    ElGamalCiphertext currentBalanceCiphertext = ElGamalCiphertext.fromBytes(currentBalanceBytes, secp256k1);

    System.out.println(
      "Current Balance Ciphertext C1: " + BaseEncoding.base16().encode(currentBalanceCiphertext.c1().getEncoded(true)));
    System.out.println(
      "Current Balance Ciphertext C2: " + BaseEncoding.base16().encode(currentBalanceCiphertext.c2().getEncoded(true)));

    // Get the holder's ElGamal private key
    byte[] holderPrivateKey = holderElGamalKeyPair.privateKey().naturalBytes().toByteArray();
    // Ensure it's exactly 32 bytes (remove leading zero if present from BigInteger encoding)
    if (holderPrivateKey.length > 32) {
      byte[] trimmed = new byte[32];
      System.arraycopy(holderPrivateKey, holderPrivateKey.length - 32, trimmed, 0, 32);
      holderPrivateKey = trimmed;
    } else if (holderPrivateKey.length < 32) {
      byte[] padded = new byte[32];
      System.arraycopy(holderPrivateKey, 0, padded, 32 - holderPrivateKey.length, holderPrivateKey.length);
      holderPrivateKey = padded;
    }

    // Generate Balance Linkage Proof using helper method
    byte[] balanceLinkageProof = generateBalanceLinkageProof(
      linkProofGenerator,
      currentBalanceCiphertext,
      holderElGamalEcPoint,
      balanceCommitmentPoint,
      senderNewBalance,
      holderPrivateKey,
      balancePedersenRho,
      sendContextHash,
      secureRandom,
      secp256k1
    );

    System.out.println("Balance Linkage Proof size: " + balanceLinkageProof.length + " bytes (expected 195)");
    System.out.println("Balance Linkage Proof: " + BaseEncoding.base16().encode(balanceLinkageProof));

    // Verify the balance linkage proof locally
    // Note: For balance linkage, the parameters are swapped:
    // c1 = publicKey, c2 = ciphertext.c2, pk = ciphertext.c1
    boolean balanceLinkageValid = linkProofGenerator.verify(
      balanceLinkageProof,
      holderElGamalEcPoint,           // c1 = Pk = sk * G
      currentBalanceCiphertext.c2(),  // c2
      currentBalanceCiphertext.c1(),  // pk = original c1
      balanceCommitmentPoint,
      sendContextHash
    );
    System.out.println("Balance Linkage Proof valid locally: " + balanceLinkageValid);
    // assertThat(balanceLinkageValid).isTrue();

    // Combine SamePlaintextMultiProof (359 bytes) + Amount Linkage (195 bytes) + Balance Linkage (195 bytes) = 749 bytes
    byte[] fullZkProof = new byte[samePlaintextProof.length + amountLinkageProof.length + balanceLinkageProof.length];
    System.arraycopy(samePlaintextProof, 0, fullZkProof, 0, samePlaintextProof.length);
    System.arraycopy(amountLinkageProof, 0, fullZkProof, samePlaintextProof.length, amountLinkageProof.length);
    System.arraycopy(balanceLinkageProof, 0, fullZkProof, samePlaintextProof.length + amountLinkageProof.length,
      balanceLinkageProof.length);

    System.out.println("Full ZKProof size: " + fullZkProof.length + " bytes (expected 749)");

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
      .zkProof(BaseEncoding.base16().encode(fullZkProof))
      .amountCommitment(amountCommitment)
      .balanceCommitment(balanceCommitment)
      .build();

    SingleSignedTransaction<ConfidentialMPTSend> signedConfidentialSend = signatureService.sign(
      holderKeyPair.privateKey(), confidentialSend
    );

    System.out.println("\n========== CONFIDENTIAL MPT SEND ==========");
    System.out.println("Tx Hash: " + signedConfidentialSend.hash());

    SubmitResult<ConfidentialMPTSend> confidentialSendResult = xrplClient.submit(signedConfidentialSend);

    System.out.println("ConfidentialMPTSend result: " + confidentialSendResult.engineResult());
    System.out.println("ConfidentialMPTSend result message: " + confidentialSendResult.engineResultMessage());

    // Note: We expect this to fail because we're using placeholder commitments
    // The goal is to see if the SamePlaintextMultiProof is accepted by rippled
    System.out.println("\n========== TEST COMPLETE ==========");
  }

  /**
   * Generates an Amount Linkage Proof proving that an ElGamal ciphertext and Pedersen commitment encode the same
   * amount.
   *
   * <p>This corresponds to getAmountLinkageProof in rippled's MPTTester.</p>
   *
   * @param linkProofGenerator     The proof generator instance.
   * @param ciphertext             The ElGamal ciphertext (encrypted amount).
   * @param publicKey              The ElGamal public key used for encryption.
   * @param commitment             The Pedersen commitment point.
   * @param amount                 The plaintext amount.
   * @param elGamalBlindingFactor  The ElGamal randomness (r) used to create the ciphertext.
   * @param pedersenBlindingFactor The Pedersen blinding factor (rho) used to create the commitment.
   * @param contextHash            The context hash for domain separation.
   * @param secureRandom           SecureRandom instance for nonce generation.
   * @param secp256k1              Secp256k1Operations instance.
   *
   * @return The 195-byte linkage proof.
   */
  private byte[] generateAmountLinkageProof(
    JavaElGamalPedersenLinkProofGenerator linkProofGenerator,
    ElGamalCiphertext ciphertext,
    ECPoint publicKey,
    ECPoint commitment,
    UnsignedLong amount,
    byte[] elGamalBlindingFactor,
    byte[] pedersenBlindingFactor,
    byte[] contextHash,
    SecureRandom secureRandom,
    Secp256k1Operations secp256k1
  ) {
    // Generate random nonces for the proof
    byte[] nonceKm = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] nonceKr = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] nonceKrho = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);

    return linkProofGenerator.generateProof(
      ciphertext.c1(),      // c1 = r * G
      ciphertext.c2(),      // c2 = m * G + r * Pk
      publicKey,            // Pk
      commitment,           // PCm = m * G + rho * H
      amount,
      elGamalBlindingFactor,  // r
      pedersenBlindingFactor, // rho
      contextHash,
      nonceKm,
      nonceKr,
      nonceKrho
    );
  }

  /**
   * Generates a Balance Linkage Proof proving that an ElGamal ciphertext and Pedersen commitment encode the same
   * balance.
   *
   * <p>This corresponds to getBalanceLinkageProof in rippled's MPTTester. The key difference from
   * Amount Linkage is that this uses the private key as the "r" parameter, and swaps c1/pk positions.</p>
   *
   * <p>The proof structure is:
   * <ul>
   *   <li>c1 parameter = public key (sk * G)</li>
   *   <li>c2 parameter = ciphertext.c2</li>
   *   <li>publicKey parameter = ciphertext.c1</li>
   *   <li>r parameter = private key (sk)</li>
   * </ul>
   * </p>
   *
   * @param linkProofGenerator     The proof generator instance.
   * @param ciphertext             The ElGamal ciphertext (encrypted balance).
   * @param publicKey              The ElGamal public key.
   * @param commitment             The Pedersen commitment point for the balance.
   * @param balance                The plaintext balance amount.
   * @param privateKey             The ElGamal private key (used as "r" in the proof).
   * @param pedersenBlindingFactor The Pedersen blinding factor (rho) used to create the commitment.
   * @param contextHash            The context hash for domain separation.
   * @param secureRandom           SecureRandom instance for nonce generation.
   * @param secp256k1              Secp256k1Operations instance.
   *
   * @return The 195-byte linkage proof.
   */
  private byte[] generateBalanceLinkageProof(
    JavaElGamalPedersenLinkProofGenerator linkProofGenerator,
    ElGamalCiphertext ciphertext,
    ECPoint publicKey,
    ECPoint commitment,
    UnsignedLong balance,
    byte[] privateKey,
    byte[] pedersenBlindingFactor,
    byte[] contextHash,
    SecureRandom secureRandom,
    Secp256k1Operations secp256k1
  ) {
    // Generate random nonces for the proof
    byte[] nonceKm = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] nonceKr = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    byte[] nonceKrho = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);

    // Note: The parameters are swapped compared to Amount Linkage Proof
    // c1 = publicKey (sk * G), c2 = ciphertext.c2, pk = ciphertext.c1, r = privateKey
    return linkProofGenerator.generateProof(
      publicKey,            // c1 = Pk = sk * G
      ciphertext.c2(),      // c2 = m * G + r * Pk (from original encryption)
      ciphertext.c1(),      // pk = original c1 = r * G
      commitment,           // PCm = balance * G + rho * H
      balance,
      privateKey,           // r = private key (sk)
      pedersenBlindingFactor, // rho
      contextHash,
      nonceKm,
      nonceKr,
      nonceKrho
    );
  }
}
