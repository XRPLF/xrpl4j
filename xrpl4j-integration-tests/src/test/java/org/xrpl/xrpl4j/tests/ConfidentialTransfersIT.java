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
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptClawbackService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptConvertBackService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptConvertService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptSendService;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.MpTokenLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptClawback;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptMergeInbox;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;
import java.util.List;

/**
 * Integration test for the full Confidential MPT (Multi-Purpose Token) transfer lifecycle.
 *
 * <p>This test exercises the following end-to-end flow:
 * <ol>
 *   <li>Create an MPTokenIssuance with privacy, transfer, and clawback capabilities</li>
 *   <li>Register the issuer's ElGamal public key via MpTokenIssuanceSet</li>
 *   <li>Create and authorize a holder, then transfer public MPTs to the holder</li>
 *   <li>Convert public MPTs to confidential (encrypted) balance via ConfidentialMptConvert</li>
 *   <li>Merge the holder's inbox into the spending balance via ConfidentialMptMergeInbox</li>
 *   <li>Set up a second holder and register their ElGamal key</li>
 *   <li>Send confidential MPTs between holders via ConfidentialMptSend</li>
 *   <li>Convert confidential MPTs back to public balance via ConfidentialMptConvertBack</li>
 *   <li>Issuer claws back remaining confidential MPTs via ConfidentialMptClawback</li>
 * </ol>
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "ConfidentialTransfersIT only runs on a local rippled node or Devnet.")
public class ConfidentialTransfersIT extends AbstractIT {

  private static ConfidentialMptConvertService convertService;
  private static ConfidentialMptSendService sendService;
  private static ConfidentialMptConvertBackService convertBackService;
  private static ConfidentialMptClawbackService clawbackService;
  private static BlindingFactorGenerator blindingFactorGenerator;
  private static MptAmountEncryptor encryptor;
  private static MptAmountDecryptor decryptor;

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  @BeforeAll
  static void initServices() {
    convertService = new ConfidentialMptConvertService();
    sendService = new ConfidentialMptSendService();
    convertBackService = new ConfidentialMptConvertBackService();
    clawbackService = new ConfidentialMptClawbackService();
    blindingFactorGenerator = new JnaBlindingFactorGenerator();
    encryptor = new JnaMptAmountEncryptor();
    decryptor = new JnaMptAmountDecryptor();
  }

  @Test
  public void testEntireFlow() throws JsonRpcClientErrorException, JsonProcessingException {
    FeeResult feeResult = xrplClient.fee();
    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();
    // Confidential MPT transactions carry a base-fee multiplier (kConfidentialFeeMultiplier = 9) in rippled.
    final XrpCurrencyAmount confidentialFee = FeeUtils
      .computeConfidentialMptNetworkFees(feeResult, UnsignedInteger.ZERO)
      .recommendedFee();

    // 1. Create MPTokenIssuance with transfer, clawback, and privacy flags.
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(fee)
      .signingPublicKey(issuerKeyPair.publicKey())
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true)
        .tfMptCanClawback(true)
        .tfMptCanHoldConfidentialBalance(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(),
      issuanceCreate
    );
    assertThat(xrplClient.submit(signedIssuanceCreate).engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<MpTokenIssuanceCreate> issuanceCreateResult = this.scanForResult(
      () -> this.getValidatedTransaction(signedIssuanceCreate.hash(), MpTokenIssuanceCreate.class)
    );

    MpTokenIssuanceId mpTokenIssuanceId = issuanceCreateResult.metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("Metadata did not contain issuance ID"));

    MpTokenIssuanceObject issuance = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(mpTokenIssuanceId, LedgerSpecifier.VALIDATED)
    ).node();
    assertThat(issuance.flags().lsfMptCanTransfer()).isTrue();
    assertThat(issuance.flags().lsfMptCanClawback()).isTrue();
    assertThat(issuance.flags().lsfMptCanHoldConfidentialBalance()).isTrue();

    // 2. Register the issuer's and auditor's ElGamal public keys via MpTokenIssuanceSet.
    KeyPair issuerElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    KeyPair auditorElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .issuerEncryptionKey(issuerElGamalKeyPair.publicKey())
      .auditorEncryptionKey(auditorElGamalKeyPair.publicKey())
      .build();

    SingleSignedTransaction<MpTokenIssuanceSet> signedIssuanceSet = signatureService.sign(
      issuerKeyPair.privateKey(),
      issuanceSet
    );
    assertThat(xrplClient.submit(signedIssuanceSet).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedIssuanceSet.hash(), MpTokenIssuanceSet.class)
    );

    MpTokenIssuanceObject issuanceObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(mpTokenIssuanceId, LedgerSpecifier.VALIDATED)
    ).node();
    assertThat(issuanceObject.issuerEncryptionKey().get()).isEqualTo(
      PublicKey.fromBase16EncodedPublicKey(issuerElGamalKeyPair.publicKey().base16Value())
    );
    assertThat(issuanceObject.auditorEncryptionKey().get()).isEqualTo(
      PublicKey.fromBase16EncodedPublicKey(auditorElGamalKeyPair.publicKey().base16Value())
    );

    assertMpTokenIssuanceEntryEqualsObjectFromAccountObjects(issuanceObject, mpTokenIssuanceId);

    // 3. Create Holder 1, authorize the MPToken, and transfer 1000 public MPTs.
    KeyPair holderKeyPair = createRandomAccountEd25519();
    AccountInfoResult holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize holderAuthorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(fee)
      .signingPublicKey(holderKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedHolderAuthorize = signatureService.sign(
      holderKeyPair.privateKey(),
      holderAuthorize
    );
    assertThat(xrplClient.submit(signedHolderAuthorize).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedHolderAuthorize.hash(), MpTokenAuthorize.class)
    );

    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    Payment paymentToHolder = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(holderKeyPair.publicKey().deriveAddress())
      .amount(MptCurrencyAmount.builder()
        .mptIssuanceId(mpTokenIssuanceId)
        .value("1000")
        .build())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentToHolder = signatureService.sign(
      issuerKeyPair.privateKey(),
      paymentToHolder
    );
    assertThat(xrplClient.submit(signedPaymentToHolder).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedPaymentToHolder.hash(), Payment.class)
    );

    MpTokenObject holderMpToken = getMpToken(holderKeyPair, mpTokenIssuanceId);
    assertThat(holderMpToken.mptAmount()).isEqualTo(MpTokenNumericAmount.of(1000L));

    // 4. ConfidentialMptConvert: convert 500 of Holder 1's public MPTs to confidential balance, registering the
    //    holder's ElGamal key. Account info must be fetched before proof generation, as the context hash includes
    //    the sequence number.
    KeyPair holderElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    UnsignedLong amountToConvert = UnsignedLong.valueOf(500);

    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    ConfidentialMptConvertContext convertContext = convertService.generateContext(
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfo.accountData().sequence(),
      mpTokenIssuanceId
    );
    ConfidentialMptConvertProof convertZkProof = convertService.generateProof(
      holderElGamalKeyPair, convertContext
    );
    assertThat(convertService.verifyProof(convertZkProof, holderElGamalKeyPair.publicKey(), convertContext)).isTrue();

    BlindingFactor convertBlindingFactor = blindingFactorGenerator.generate();
    EncryptedAmount holderEncryptedForConvert = encryptor.encrypt(
      amountToConvert, holderElGamalKeyPair.publicKey(), convertBlindingFactor
    );
    EncryptedAmount issuerEncryptedForConvert = encryptor.encrypt(
      amountToConvert, issuerElGamalKeyPair.publicKey(), convertBlindingFactor
    );
    EncryptedAmount auditorEncryptedForConvert = encryptor.encrypt(
      amountToConvert, auditorElGamalKeyPair.publicKey(), convertBlindingFactor
    );

    ConfidentialMptConvert confidentialConvert = ConfidentialMptConvert.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(confidentialFee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(amountToConvert))
      .holderEncryptionKey(holderElGamalKeyPair.publicKey())
      .holderEncryptedAmount(holderEncryptedForConvert)
      .issuerEncryptedAmount(issuerEncryptedForConvert)
      .auditorEncryptedAmount(auditorEncryptedForConvert)
      .blindingFactor(convertBlindingFactor)
      .zkProof(convertZkProof)
      .build();

    SingleSignedTransaction<ConfidentialMptConvert> signedConfidentialConvert = signatureService.sign(
      holderKeyPair.privateKey(),
      confidentialConvert
    );
    assertThat(xrplClient.submit(signedConfidentialConvert).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedConfidentialConvert.hash(), ConfidentialMptConvert.class)
    );

    // 5. MergeInbox: move converted tokens from the holder's inbox (CB_IN) into the spending balance (CB_S).
    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    ConfidentialMptMergeInbox mergeInbox = ConfidentialMptMergeInbox.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(confidentialFee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<ConfidentialMptMergeInbox> signedMergeInbox = signatureService.sign(
      holderKeyPair.privateKey(),
      mergeInbox
    );
    assertThat(xrplClient.submit(signedMergeInbox).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedMergeInbox.hash(), ConfidentialMptMergeInbox.class)
    );

    // 6. Set up Holder 2: create account, authorize the MPToken, and register their ElGamal key via a zero-amount
    //    ConfidentialMptConvert (registers the key without moving tokens).
    KeyPair holder2KeyPair = createRandomAccountEd25519();
    AccountInfoResult holder2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder2KeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize holder2Authorize = MpTokenAuthorize.builder()
      .account(holder2KeyPair.publicKey().deriveAddress())
      .sequence(holder2AccountInfo.accountData().sequence())
      .fee(fee)
      .signingPublicKey(holder2KeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedHolder2Authorize = signatureService.sign(
      holder2KeyPair.privateKey(),
      holder2Authorize
    );
    assertThat(xrplClient.submit(signedHolder2Authorize).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedHolder2Authorize.hash(), MpTokenAuthorize.class)
    );

    KeyPair holder2ElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();

    holder2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder2KeyPair.publicKey().deriveAddress())
    );

    ConfidentialMptConvertContext holder2ConvertContext = convertService.generateContext(
      holder2KeyPair.publicKey().deriveAddress(),
      holder2AccountInfo.accountData().sequence(),
      mpTokenIssuanceId
    );
    ConfidentialMptConvertProof holder2ConvertProof = convertService.generateProof(
      holder2ElGamalKeyPair, holder2ConvertContext
    );
    assertThat(convertService.verifyProof(
      holder2ConvertProof, holder2ElGamalKeyPair.publicKey(), holder2ConvertContext
    )).isTrue();

    BlindingFactor holder2ConvertBlindingFactor = blindingFactorGenerator.generate();
    EncryptedAmount holder2EncryptedForConvert = encryptor.encrypt(
      UnsignedLong.ZERO, holder2ElGamalKeyPair.publicKey(), holder2ConvertBlindingFactor
    );
    EncryptedAmount issuerEncryptedForHolder2Convert = encryptor.encrypt(
      UnsignedLong.ZERO, issuerElGamalKeyPair.publicKey(), holder2ConvertBlindingFactor
    );
    EncryptedAmount auditorEncryptedForHolder2Convert = encryptor.encrypt(
      UnsignedLong.ZERO, auditorElGamalKeyPair.publicKey(), holder2ConvertBlindingFactor
    );

    ConfidentialMptConvert holder2ConfidentialConvert = ConfidentialMptConvert.builder()
      .account(holder2KeyPair.publicKey().deriveAddress())
      .fee(confidentialFee)
      .sequence(holder2AccountInfo.accountData().sequence())
      .signingPublicKey(holder2KeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(UnsignedLong.ZERO))
      .holderEncryptionKey(holder2ElGamalKeyPair.publicKey())
      .holderEncryptedAmount(holder2EncryptedForConvert)
      .issuerEncryptedAmount(issuerEncryptedForHolder2Convert)
      .auditorEncryptedAmount(auditorEncryptedForHolder2Convert)
      .blindingFactor(holder2ConvertBlindingFactor)
      .zkProof(holder2ConvertProof)
      .build();

    SingleSignedTransaction<ConfidentialMptConvert> signedHolder2ConfidentialConvert = signatureService.sign(
      holder2KeyPair.privateKey(),
      holder2ConfidentialConvert
    );
    assertThat(xrplClient.submit(signedHolder2ConfidentialConvert).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedHolder2ConfidentialConvert.hash(), ConfidentialMptConvert.class)
    );

    // 7. ConfidentialMptSend: Holder 1 sends 100 confidential MPTs to Holder 2, encrypted for all four parties
    //    (sender, destination, issuer, auditor) under a shared blinding factor.
    UnsignedLong sendAmount = UnsignedLong.valueOf(100);

    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    holderMpToken = getMpToken(holderKeyPair, mpTokenIssuanceId);
    UnsignedInteger holder1Version = holderMpToken.confidentialBalanceVersion();

    ConfidentialMptSendContext sendContext = sendService.generateContext(
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfo.accountData().sequence(),
      mpTokenIssuanceId,
      holder2KeyPair.publicKey().deriveAddress(),
      holder1Version
    );

    BlindingFactor sendBlindingFactor = blindingFactorGenerator.generate();
    EncryptedAmount senderCiphertext = encryptor.encrypt(
      sendAmount, holderElGamalKeyPair.publicKey(), sendBlindingFactor
    );
    EncryptedAmount destCiphertext = encryptor.encrypt(
      sendAmount, holder2ElGamalKeyPair.publicKey(), sendBlindingFactor
    );
    EncryptedAmount issuerCiphertextForSend = encryptor.encrypt(
      sendAmount, issuerElGamalKeyPair.publicKey(), sendBlindingFactor
    );
    EncryptedAmount auditorCiphertextForSend = encryptor.encrypt(
      sendAmount, auditorElGamalKeyPair.publicKey(), sendBlindingFactor
    );

    EncryptedAmount senderBalanceCiphertext = holderMpToken.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Sender has no confidential balance"));
    // The holder's confidential balance can never exceed the 500 originally converted, so decrypt within that range.
    UnsignedLong senderCurrentBalance = decryptor.decrypt(
      senderBalanceCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, amountToConvert
    );

    Commitment amountCommitment = sendService.generatePedersenCommitment(
      sendAmount, sendBlindingFactor
    );

    BlindingFactor balanceBlindingFactor = blindingFactorGenerator.generate();
    PedersenProofParams balanceParams = sendService.generatePedersenProofParams(
      senderCurrentBalance, senderBalanceCiphertext, balanceBlindingFactor
    );

    ConfidentialMptSendProof sendProof = sendService.generateProof(
      holderElGamalKeyPair,
      sendAmount,
      Arrays.asList(
        MptConfidentialParty.of(holderElGamalKeyPair.publicKey(), senderCiphertext),
        MptConfidentialParty.of(holder2ElGamalKeyPair.publicKey(), destCiphertext),
        MptConfidentialParty.of(issuerElGamalKeyPair.publicKey(), issuerCiphertextForSend),
        MptConfidentialParty.of(auditorElGamalKeyPair.publicKey(), auditorCiphertextForSend)
      ),
      sendBlindingFactor,
      sendContext,
      amountCommitment,
      balanceParams
    );
    assertThat(sendService.verifyProof(
      sendProof,
      Arrays.asList(
        MptConfidentialParty.of(holderElGamalKeyPair.publicKey(), senderCiphertext),
        MptConfidentialParty.of(holder2ElGamalKeyPair.publicKey(), destCiphertext),
        MptConfidentialParty.of(issuerElGamalKeyPair.publicKey(), issuerCiphertextForSend),
        MptConfidentialParty.of(auditorElGamalKeyPair.publicKey(), auditorCiphertextForSend)
      ),
      senderBalanceCiphertext,
      sendContext,
      amountCommitment,
      Commitment.of(balanceParams.pedersenCommitment())
    )).isTrue();

    ConfidentialMptSend confidentialSend = ConfidentialMptSend.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(confidentialFee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .destination(holder2KeyPair.publicKey().deriveAddress())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .senderEncryptedAmount(senderCiphertext)
      .destinationEncryptedAmount(destCiphertext)
      .issuerEncryptedAmount(issuerCiphertextForSend)
      .auditorEncryptedAmount(auditorCiphertextForSend)
      .zkProof(sendProof)
      .amountCommitment(Commitment.of(amountCommitment.hexValue()))
      .balanceCommitment(Commitment.of(balanceParams.pedersenCommitment().hexValue()))
      .build();

    SingleSignedTransaction<ConfidentialMptSend> signedConfidentialSend = signatureService.sign(
      holderKeyPair.privateKey(),
      confidentialSend
    );
    assertThat(xrplClient.submit(signedConfidentialSend).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedConfidentialSend.hash(), ConfidentialMptSend.class)
    );

    // Verify sender's confidential balance was reduced: 500 - 100 = 400
    MpTokenObject senderMpTokenAfterSend = getMpToken(holderKeyPair, mpTokenIssuanceId);
    EncryptedAmount senderBalanceAfterSendCiphertext = senderMpTokenAfterSend.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Sender has no confidential balance after send"));
    UnsignedLong senderBalanceAfterSend = decryptor.decrypt(
      senderBalanceAfterSendCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, amountToConvert
    );
    assertThat(senderBalanceAfterSend.longValue()).isEqualTo(400L);

    // 8. ConfidentialMptConvertBack: Holder 1 converts 50 confidential MPTs back to public balance.
    UnsignedLong convertBackAmount = UnsignedLong.valueOf(50);

    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MpTokenObject holderMpTokenForConvertBack = getMpToken(holderKeyPair, mpTokenIssuanceId);
    UnsignedInteger holderVersionForConvertBack = holderMpTokenForConvertBack
      .confidentialBalanceVersion();

    BlindingFactor convertBackBlindingFactor = blindingFactorGenerator.generate();
    EncryptedAmount holderEncryptedForConvertBack = encryptor.encrypt(
      convertBackAmount, holderElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );
    EncryptedAmount issuerEncryptedForConvertBack = encryptor.encrypt(
      convertBackAmount, issuerElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );
    EncryptedAmount auditorEncryptedForConvertBack = encryptor.encrypt(
      convertBackAmount, auditorElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );

    ConfidentialMptConvertBackContext convertBackContext = convertBackService.generateContext(
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfo.accountData().sequence(),
      mpTokenIssuanceId,
      holderVersionForConvertBack
    );

    EncryptedAmount currentBalanceForConvertBack = holderMpTokenForConvertBack.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("Holder has no confidential balance"));
    UnsignedLong currentSpendingBalance = decryptor.decrypt(
      currentBalanceForConvertBack, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, amountToConvert
    );

    BlindingFactor convertBackBalanceBlindingFactor = blindingFactorGenerator.generate();
    PedersenProofParams convertBackBalanceParams = convertBackService.generatePedersenProofParams(
      currentSpendingBalance, currentBalanceForConvertBack, convertBackBalanceBlindingFactor
    );

    ConfidentialMptConvertBackProof convertBackProof = convertBackService.generateProof(
      holderElGamalKeyPair, convertBackAmount, convertBackContext, convertBackBalanceParams
    );
    assertThat(convertBackService.verifyProof(
      convertBackProof,
      holderElGamalKeyPair.publicKey(),
      currentBalanceForConvertBack,
      Commitment.of(convertBackBalanceParams.pedersenCommitment()),
      convertBackAmount,
      convertBackContext
    )).isTrue();

    Commitment convertBackCommitment = Commitment.of(
      convertBackBalanceParams.pedersenCommitment()
    );

    ConfidentialMptConvertBack convertBack = ConfidentialMptConvertBack.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(confidentialFee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(convertBackAmount))
      .holderEncryptedAmount(holderEncryptedForConvertBack)
      .issuerEncryptedAmount(issuerEncryptedForConvertBack)
      .auditorEncryptedAmount(auditorEncryptedForConvertBack)
      .blindingFactor(convertBackBlindingFactor)
      .balanceCommitment(Commitment.of(convertBackCommitment.hexValue()))
      .zkProof(convertBackProof)
      .build();

    SingleSignedTransaction<ConfidentialMptConvertBack> signedConvertBack = signatureService.sign(
      holderKeyPair.privateKey(),
      convertBack
    );
    assertThat(xrplClient.submit(signedConvertBack).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedConvertBack.hash(), ConfidentialMptConvertBack.class)
    );

    // Verify remaining confidential balance: 400 - 50 = 350
    MpTokenObject holderMpTokenAfterConvertBack = getMpToken(holderKeyPair, mpTokenIssuanceId);
    EncryptedAmount remainingBalanceCiphertext = holderMpTokenAfterConvertBack.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("No confidential balance after convert back"));
    UnsignedLong remainingConfidentialBalance = decryptor.decrypt(
      remainingBalanceCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, amountToConvert
    );
    assertThat(remainingConfidentialBalance.longValue()).isEqualTo(350L);

    // 9. ConfidentialMptClawback: Issuer claws back 350 confidential MPTs from Holder 1, using the issuer's
    //    encrypted mirror of the holder's balance (IssuerEncryptedBalance on the holder's MPToken).
    UnsignedLong clawbackAmount = UnsignedLong.valueOf(350);

    MpTokenObject holderMpTokenForClawback = getMpToken(holderKeyPair, mpTokenIssuanceId);
    EncryptedAmount issuerBalanceCiphertext = holderMpTokenForClawback.issuerEncryptedBalance()
      .orElseThrow(() -> new RuntimeException("No issuer encrypted balance found"));

    // The issuer's encrypted mirror tracks the holder's balance, so it is bounded by the same range.
    UnsignedLong issuerDecryptedBalance = decryptor.decrypt(
      issuerBalanceCiphertext, issuerElGamalKeyPair.privateKey(), UnsignedLong.ZERO, amountToConvert
    );
    assertThat(issuerDecryptedBalance.longValue()).isGreaterThanOrEqualTo(clawbackAmount.longValue());

    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    ConfidentialMptClawbackContext clawbackContext = clawbackService.generateContext(
      issuerKeyPair.publicKey().deriveAddress(),
      issuerAccountInfo.accountData().sequence(),
      mpTokenIssuanceId,
      holderKeyPair.publicKey().deriveAddress()
    );

    ConfidentialMptClawbackProof clawbackProof = clawbackService.generateProof(
      issuerBalanceCiphertext,
      issuerElGamalKeyPair.publicKey(),
      clawbackAmount,
      issuerElGamalKeyPair.privateKey(),
      clawbackContext
    );
    assertThat(clawbackService.verifyProof(
      clawbackProof,
      issuerBalanceCiphertext,
      issuerElGamalKeyPair.publicKey(),
      clawbackAmount,
      clawbackContext
    )).isTrue();

    ConfidentialMptClawback clawback = ConfidentialMptClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(confidentialFee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .holder(holderKeyPair.publicKey().deriveAddress())
      .mptAmount(MpTokenNumericAmount.of(clawbackAmount))
      .zkProof(clawbackProof)
      .build();

    SingleSignedTransaction<ConfidentialMptClawback> signedClawback = signatureService.sign(
      issuerKeyPair.privateKey(),
      clawback
    );
    assertThat(xrplClient.submit(signedClawback).engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedClawback.hash(), ConfidentialMptClawback.class)
    );

    MpTokenObject holderMpTokenAfterClawback = getMpToken(holderKeyPair, mpTokenIssuanceId);
    assertThat(holderMpTokenAfterClawback).isNotNull();
  }

  private MpTokenObject getMpToken(
    KeyPair holderKeyPair,
    MpTokenIssuanceId mpTokenIssuanceId
  ) throws JsonRpcClientErrorException {
    return xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holderKeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();
  }

  private void assertMpTokenIssuanceEntryEqualsObjectFromAccountObjects(
    MpTokenIssuanceObject issuanceObject,
    MpTokenIssuanceId mpTokenIssuanceId
  ) throws JsonRpcClientErrorException {
    LedgerEntryResult<MpTokenIssuanceObject> issuanceEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(mpTokenIssuanceId, LedgerSpecifier.VALIDATED)
    );

    assertThat(issuanceEntry.node()).isEqualTo(issuanceObject);

    LedgerEntryResult<MpTokenIssuanceObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(issuanceObject.index(), MpTokenIssuanceObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(issuanceEntry.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(issuanceObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndexUnTyped.node()).isEqualTo(issuanceEntry.node());

    List<LedgerObject> accountObjects = xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .type(AccountObjectType.MPT_ISSUANCE)
        .account(issuanceObject.issuer())
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    ).accountObjects();

    assertThat(accountObjects).contains(issuanceObject);
  }
}
