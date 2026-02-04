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
import org.xrpl.xrpl4j.crypto.mpt.SchnorrProofOfKnowledge;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.java.JavaElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.java.JavaElGamalBalanceEncryptor;
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

    System.out.println("MpTokenIssuanceId: " + mpTokenIssuanceId.value());

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
      issuerElGamalKeyPair.publicKey().uncompressedValue().hexValue()
    );

    System.out.println("=== Issuer ElGamal Key Pair ===");
    System.out.println(
      "Private Key (32 bytes natural): " + issuerElGamalKeyPair.privateKey().naturalBytes().hexValue());
    System.out.println("Public Key (64 bytes, uncompressed): " + issuerElGamalPublicKey.value());

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

    System.out.println("MpTokenIssuanceSet submitted: " + signedIssuanceSet.hash());

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

    System.out.println("MpTokenIssuanceSet validated successfully!");

    //////////////////////
    // Create holder account
    KeyPair holderKeyPair = createRandomAccountEd25519();
    System.out.println("Holder Address: " + holderKeyPair.publicKey().deriveAddress());

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
    System.out.println("Holder MPT Balance: " + holderMpToken.mptAmount().value());

    //////////////////////
    // Generate Holder ElGamal key pair
    KeyPair holderElGamalKeyPair = Seed.secp256k1Seed().deriveKeyPair();
    ElGamalPublicKey holderElGamalPublicKey = ElGamalPublicKey.of(
      holderElGamalKeyPair.publicKey().uncompressedValue().hexValue()
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
    System.out.println("\n=== Encryption Verification ===");
    boolean holderEncryptionValid = encryptor.verifyEncryption(
      holderCiphertext, holderElGamalEcPoint, amountToConvert, blindingFactorBytes
    );
    System.out.println("Holder encryption verification: " + holderEncryptionValid);

    boolean issuerEncryptionValid = encryptor.verifyEncryption(
      issuerCiphertext, issuerElGamalEcPoint, amountToConvert, blindingFactorBytes
    );
    System.out.println("Issuer encryption verification: " + issuerEncryptionValid);

    //////////////////////
    // Decrypt ciphertexts to verify they contain the correct amount (500)
    System.out.println("\n=== Decryption Verification ===");
    JavaElGamalBalanceDecryptor decryptor = new JavaElGamalBalanceDecryptor(secp256k1);

    // Decrypt holder ciphertext using holder's private key
    byte[] holderPrivateKeyForDecrypt = holderElGamalKeyPair.privateKey().naturalBytes().toByteArray();
    long holderDecryptedAmount = decryptor.decrypt(holderCiphertext, holderPrivateKeyForDecrypt);
    System.out.println(
      "Holder ciphertext decrypted amount: " + holderDecryptedAmount + " (expected: " + amountToConvert + ")");

    // Decrypt issuer ciphertext using issuer's private key
    byte[] issuerPrivateKeyForDecrypt = issuerElGamalKeyPair.privateKey().naturalBytes().toByteArray();
    long issuerDecryptedAmount = decryptor.decrypt(issuerCiphertext, issuerPrivateKeyForDecrypt);
    System.out.println(
      "Issuer ciphertext decrypted amount: " + issuerDecryptedAmount + " (expected: " + amountToConvert + ")");

    // Verify both decrypt to the same amount
    assertThat(holderDecryptedAmount).isEqualTo(amountToConvert.longValue());
    assertThat(issuerDecryptedAmount).isEqualTo(amountToConvert.longValue());
    System.out.println("Both ciphertexts decrypt to the correct amount: " + amountToConvert);

    //////////////////////
    // Generate ZKProof (Schnorr Proof of Knowledge)
    // Try WITHOUT contextId first (as the C implementation allows null context_id)
    byte[] holderPrivateKeyBytes = holderElGamalKeyPair.privateKey().naturalBytes().toByteArray();

    // Debug: Print private key info
    System.out.println("\n=== ZKProof Debug ===");
    System.out.println("MPTokenIssuanceId: " + mpTokenIssuanceId.value());
    System.out.println("Holder Private Key length: " + holderPrivateKeyBytes.length);
    System.out.println("Holder Private Key (hex): " + BaseEncoding.base16().encode(holderPrivateKeyBytes));

    byte[] holderPkCompressed = secp256k1.serializeCompressed(holderElGamalEcPoint);
    System.out.println("Holder Public Key (compressed, hex): " + BaseEncoding.base16().encode(holderPkCompressed));
    System.out.println("Holder ElGamal Public Key (uncompressed 64 bytes): " + holderElGamalPublicKey.value());

    SchnorrProofOfKnowledge schnorrPok = new SchnorrProofOfKnowledge(secp256k1, secureRandom);

    // Generate proof WITHOUT contextId (null)
    byte[] zkProofBytes = schnorrPok.generate(
      holderPrivateKeyBytes,
      holderElGamalEcPoint,
      null  // No contextId
    );
    String zkProof = BaseEncoding.base16().encode(zkProofBytes);

    // Verify the proof locally before submitting
    boolean localVerify = schnorrPok.verify(zkProofBytes, holderElGamalEcPoint, null);
    System.out.println("Local proof verification (no contextId): " + localVerify);
    System.out.println("ZKProof length: " + zkProofBytes.length);
    System.out.println("ZKProof (hex): " + zkProof);

    //////////////////////
    // Get updated holder account info for ConfidentialMPTConvert
    AccountInfoResult holderAccountInfoForConvert = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
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

    System.out.println("ConfidentialMPTConvert submitted: " + signedConfidentialConvert.hash());
    System.out.println("ConfidentialMPTConvert result: " + confidentialConvertResult.engineResult());

    //////////////////////
    // Print everything for tracking
    System.out.println("\n========== SUMMARY ==========");
    System.out.println("=== Issuer ===");
    System.out.println("Issuer Address: " + issuerKeyPair.publicKey().deriveAddress());
    System.out.println("Issuer ElGamal Private Key: " + issuerElGamalKeyPair.privateKey().naturalBytes().hexValue());
    System.out.println("Issuer ElGamal Public Key: " + issuerElGamalPublicKey.value());

    System.out.println("\n=== Holder ===");
    System.out.println("Holder Address: " + holderKeyPair.publicKey().deriveAddress());
    System.out.println("Holder ElGamal Private Key: " + holderElGamalKeyPair.privateKey().naturalBytes().hexValue());
    System.out.println("Holder ElGamal Public Key: " + holderElGamalPublicKey.value());

    System.out.println("\n=== MPToken Issuance ===");
    System.out.println("MPTokenIssuanceId: " + mpTokenIssuanceId.value());

    System.out.println("\n=== Confidential Conversion ===");
    System.out.println("Amount to Convert: " + amountToConvert);
    System.out.println("Blinding Factor: " + blindingFactor.value());
    System.out.println("Holder Encrypted Amount: " + holderEncryptedAmount.value());
    System.out.println("Issuer Encrypted Amount: " + issuerEncryptedAmount.value());
    System.out.println("ZK Proof: " + zkProof);

    System.out.println("\n=== Transaction Result ===");
    System.out.println("Transaction Hash: " + signedConfidentialConvert.hash());
    System.out.println("Engine Result: " + confidentialConvertResult.engineResult());
    System.out.println("Engine Result Message: " + confidentialConvertResult.engineResultMessage());
  }
}
