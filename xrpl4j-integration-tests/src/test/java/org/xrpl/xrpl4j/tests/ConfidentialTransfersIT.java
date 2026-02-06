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
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
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
    boolean holder2LocalVerify = proofGenerator.verifyProof(holder2ZkProofBytes, holder2ElGamalEcPoint, holder2ContextId);
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
  }
}
