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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptClawbackService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptConvertBackService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptConvertService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptSendService;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.MpTokenLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptClawback;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptMergeInbox;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;

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
@DisabledIf(value = "shouldNotRun", disabledReason = "ConfidentialTransfersIT only runs on local rippled node.")
public class ConfidentialTransfersIT extends AbstractIT {

  // Upper bound for brute-force decryption search range
  private static final UnsignedLong DECRYPT_MAX = UnsignedLong.valueOf(1_000_000);

  private static ConfidentialMptConvertService convertService;
  private static ConfidentialMptSendService sendService;
  private static ConfidentialMptConvertBackService convertBackService;
  private static ConfidentialMptClawbackService clawbackService;
  private static BlindingFactorGenerator blindingFactorGenerator;
  private static MptAmountEncryptor encryptor;
  private static MptAmountDecryptor decryptor;

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null ||
      System.getProperty("useDevnet") != null;
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

    // =====================================================================
    // 1. Create MPTokenIssuance with transfer, clawback, and privacy flags
    // =====================================================================
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(fee)
      .lastLedgerSequence(lastLedgerSeq(issuerAccountInfo))
      .signingPublicKey(issuerKeyPair.publicKey())
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true)
        .tfMptCanClawback(true)
        .tfMptCanConfidentialAmount(true)
        .build())
      .build();

    TransactionResult<MpTokenIssuanceCreate> issuanceCreateResult =
      signSubmitAndWait(issuanceCreate, issuerKeyPair, MpTokenIssuanceCreate.class);

    MpTokenIssuanceId mpTokenIssuanceId = issuanceCreateResult.metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("Metadata did not contain issuance ID"));

    // Verify the issuance was created with the correct flags
    MpTokenIssuanceObject issuance = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(mpTokenIssuanceId, LedgerSpecifier.VALIDATED)
    ).node();
    assertThat(issuance.flags().lsfMptCanTransfer()).isTrue();
    assertThat(issuance.flags().lsfMptCanClawback()).isTrue();
    assertThat(issuance.flags().lsfMptCanConfidentialAmount()).isTrue();

    // =====================================================================
    // 2. Register the issuer's ElGamal public key via MpTokenIssuanceSet.
    //    The issuer needs an ElGamal key pair so that encrypted amounts can
    //    be created for the issuer's mirror balance on each holder's MPToken.
    // =====================================================================
    KeyPair issuerElGamalKeyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(issuerAccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .issuerEncryptionKey(issuerElGamalKeyPair.publicKey().base16Value())
      .build();

    signSubmitAndWait(issuanceSet, issuerKeyPair, MpTokenIssuanceSet.class);

    // =====================================================================
    // 3. Create Holder 1, authorize the MPToken, and transfer 1000 public MPTs
    // =====================================================================
    KeyPair holderKeyPair = createRandomAccountEd25519();
    AccountInfoResult holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize holderAuthorize = MpTokenAuthorize.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .sequence(holderAccountInfo.accountData().sequence())
      .fee(fee)
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(holderAccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    signSubmitAndWait(holderAuthorize, holderKeyPair, MpTokenAuthorize.class);

    // Transfer 1000 public MPTs from issuer to Holder 1
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
      .lastLedgerSequence(lastLedgerSeq(issuerAccountInfo))
      .build();

    signSubmitAndWait(paymentToHolder, issuerKeyPair, Payment.class);

    // Verify Holder 1 has 1000 public MPTs
    MpTokenObject holderMpToken = getMpToken(holderKeyPair, mpTokenIssuanceId);
    assertThat(holderMpToken.mptAmount()).isEqualTo(MpTokenNumericAmount.of(1000L));

    // =====================================================================
    // 4. ConfidentialMptConvert: Convert 500 of Holder 1's public MPTs to
    //    confidential balance. This also registers the holder's ElGamal key.
    //    The amount is encrypted for both holder and issuer using the same
    //    blinding factor, and a ZK proof of ElGamal key ownership is included.
    //    IMPORTANT: Get account info BEFORE generating the ZK proof because
    //    the context hash includes the sequence number.
    // =====================================================================
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

    BlindingFactor convertBlindingFactor = blindingFactorGenerator.generate();
    EncryptedAmount holderEncryptedForConvert = encryptor.encrypt(
      amountToConvert, holderElGamalKeyPair.publicKey(), convertBlindingFactor
    );
    EncryptedAmount issuerEncryptedForConvert = encryptor.encrypt(
      amountToConvert, issuerElGamalKeyPair.publicKey(), convertBlindingFactor
    );

    ConfidentialMptConvert confidentialConvert = ConfidentialMptConvert.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(holderAccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(amountToConvert))
      .holderEncryptionKey(holderElGamalKeyPair.publicKey().base16Value())
      .holderEncryptedAmount(holderEncryptedForConvert.toHex())
      .issuerEncryptedAmount(issuerEncryptedForConvert.toHex())
      .blindingFactor(convertBlindingFactor.hexValue())
      .zkProof(convertZkProof.hexValue())
      .build();

    TransactionResult<ConfidentialMptConvert> convertResult =
      signSubmitAndWait(confidentialConvert, holderKeyPair, ConfidentialMptConvert.class);
    final Hash256 convertHash = convertResult.hash();

    // =====================================================================
    // 5. MergeInbox: After ConfidentialMptConvert, tokens land in the
    //    holder's inbox (CB_IN). MergeInbox moves them into the spending
    //    balance (CB_S) so they can be used in ConfidentialMptSend.
    // =====================================================================
    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    ConfidentialMptMergeInbox mergeInbox = ConfidentialMptMergeInbox.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(holderAccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    TransactionResult<ConfidentialMptMergeInbox> mergeResult =
      signSubmitAndWait(mergeInbox, holderKeyPair, ConfidentialMptMergeInbox.class);
    final Hash256 mergeHash = mergeResult.hash();

    // =====================================================================
    // 6. Set up Holder 2: create account, authorize the MPToken, and register
    //    their ElGamal key via a zero-amount ConfidentialMptConvert. A zero-amount
    //    convert registers the key without moving any tokens.
    // =====================================================================
    KeyPair holder2KeyPair = createRandomAccountEd25519();
    AccountInfoResult holder2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder2KeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize holder2Authorize = MpTokenAuthorize.builder()
      .account(holder2KeyPair.publicKey().deriveAddress())
      .sequence(holder2AccountInfo.accountData().sequence())
      .fee(fee)
      .signingPublicKey(holder2KeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(holder2AccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    signSubmitAndWait(holder2Authorize, holder2KeyPair, MpTokenAuthorize.class);

    // Register Holder 2's ElGamal key via zero-amount ConfidentialMptConvert
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

    BlindingFactor holder2ConvertBlindingFactor = blindingFactorGenerator.generate();
    EncryptedAmount holder2EncryptedForConvert = encryptor.encrypt(
      UnsignedLong.ZERO, holder2ElGamalKeyPair.publicKey(), holder2ConvertBlindingFactor
    );
    EncryptedAmount issuerEncryptedForHolder2Convert = encryptor.encrypt(
      UnsignedLong.ZERO, issuerElGamalKeyPair.publicKey(), holder2ConvertBlindingFactor
    );

    ConfidentialMptConvert holder2ConfidentialConvert = ConfidentialMptConvert.builder()
      .account(holder2KeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(holder2AccountInfo.accountData().sequence())
      .signingPublicKey(holder2KeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(holder2AccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(UnsignedLong.ZERO))
      .holderEncryptionKey(holder2ElGamalKeyPair.publicKey().base16Value())
      .holderEncryptedAmount(holder2EncryptedForConvert.toHex())
      .issuerEncryptedAmount(issuerEncryptedForHolder2Convert.toHex())
      .blindingFactor(holder2ConvertBlindingFactor.hexValue())
      .zkProof(holder2ConvertProof.hexValue())
      .build();

    TransactionResult<ConfidentialMptConvert> holder2ConvertResult =
      signSubmitAndWait(holder2ConfidentialConvert, holder2KeyPair, ConfidentialMptConvert.class);
    final Hash256 holder2ConvertHash = holder2ConvertResult.hash();

    // =====================================================================
    // 7. ConfidentialMptSend: Holder 1 sends 100 confidential MPTs to Holder 2.
    //    The send amount is encrypted for all three parties (sender, destination,
    //    issuer) using the same blinding factor. Range proofs and Pedersen
    //    commitments prove the amount is valid and the sender has sufficient
    //    balance, without revealing the actual values.
    // =====================================================================
    UnsignedLong sendAmount = UnsignedLong.valueOf(100);

    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    holderMpToken = getMpToken(holderKeyPair, mpTokenIssuanceId);
    UnsignedInteger holder1Version = holderMpToken.confidentialBalanceVersion()
      .orElse(UnsignedInteger.ZERO);

    // Generate context hash incorporating sender, sequence, issuance, destination, and version
    ConfidentialMptSendContext sendContext = sendService.generateContext(
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfo.accountData().sequence(),
      mpTokenIssuanceId,
      holder2KeyPair.publicKey().deriveAddress(),
      holder1Version
    );

    // Encrypt the send amount for all three parties using the same blinding factor
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

    // Decrypt the sender's current spending balance to use in Pedersen proof params
    EncryptedAmount senderBalanceCiphertext = EncryptedAmount.fromHex(
      holderMpToken.confidentialBalanceSpending()
        .orElseThrow(() -> new RuntimeException("Sender has no confidential balance"))
    );
    UnsignedLong senderCurrentBalance = decryptor.decrypt(
      senderBalanceCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
    );

    // Generate Pedersen commitments for the amount and remaining balance (for range proofs)
    BlindingFactor amountBlindingFactor = blindingFactorGenerator.generate();
    BlindingFactor balanceBlindingFactor = blindingFactorGenerator.generate();
    PedersenProofParams amountParams = sendService.generatePedersenProofParams(
      sendAmount, senderCiphertext, amountBlindingFactor
    );
    PedersenProofParams balanceParams = sendService.generatePedersenProofParams(
      senderCurrentBalance, senderBalanceCiphertext, balanceBlindingFactor
    );

    // Generate the combined ZK proof (range proofs + plaintext equality proofs)
    ConfidentialMptSendProof sendProof = sendService.generateProof(
      holderElGamalKeyPair,
      sendAmount,
      Arrays.asList(
        MptConfidentialParty.of(holderElGamalKeyPair.publicKey(), senderCiphertext),
        MptConfidentialParty.of(holder2ElGamalKeyPair.publicKey(), destCiphertext),
        MptConfidentialParty.of(issuerElGamalKeyPair.publicKey(), issuerCiphertextForSend)
      ),
      sendBlindingFactor,
      sendContext,
      amountParams,
      balanceParams
    );

    ConfidentialMptSend confidentialSend = ConfidentialMptSend.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(holderAccountInfo))
      .destination(holder2KeyPair.publicKey().deriveAddress())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .senderEncryptedAmount(senderCiphertext.toHex())
      .destinationEncryptedAmount(destCiphertext.toHex())
      .issuerEncryptedAmount(issuerCiphertextForSend.toHex())
      .zkProof(sendProof.hexValue())
      .amountCommitment(amountParams.pedersenCommitment().hexValue())
      .balanceCommitment(balanceParams.pedersenCommitment().hexValue())
      .build();

    TransactionResult<ConfidentialMptSend> sendResult =
      signSubmitAndWait(confidentialSend, holderKeyPair, ConfidentialMptSend.class);
    final Hash256 sendHash = sendResult.hash();

    // Verify sender's confidential balance was reduced: 500 - 100 = 400
    MpTokenObject senderMpTokenAfterSend = getMpToken(holderKeyPair, mpTokenIssuanceId);
    EncryptedAmount senderBalanceAfterSendCiphertext = EncryptedAmount.fromHex(
      senderMpTokenAfterSend.confidentialBalanceSpending()
        .orElseThrow(() -> new RuntimeException("Sender has no confidential balance after send"))
    );
    UnsignedLong senderBalanceAfterSend = decryptor.decrypt(
      senderBalanceAfterSendCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
    );
    assertThat(senderBalanceAfterSend.longValue()).isEqualTo(400L);

    // =====================================================================
    // 8. ConfidentialMptConvertBack: Holder 1 converts 50 confidential MPTs
    //    back to public balance. This subtracts from both the holder's and
    //    issuer's encrypted balances.
    // =====================================================================
    UnsignedLong convertBackAmount = UnsignedLong.valueOf(50);

    holderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holderKeyPair.publicKey().deriveAddress())
    );

    MpTokenObject holderMpTokenForConvertBack = getMpToken(holderKeyPair, mpTokenIssuanceId);
    UnsignedInteger holderVersionForConvertBack = holderMpTokenForConvertBack
      .confidentialBalanceVersion().orElse(UnsignedInteger.ZERO);

    // Encrypt the convert-back amount for holder and issuer
    BlindingFactor convertBackBlindingFactor = blindingFactorGenerator.generate();
    EncryptedAmount holderEncryptedForConvertBack = encryptor.encrypt(
      convertBackAmount, holderElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );
    EncryptedAmount issuerEncryptedForConvertBack = encryptor.encrypt(
      convertBackAmount, issuerElGamalKeyPair.publicKey(), convertBackBlindingFactor
    );

    // Generate context hash for the convert-back transaction
    ConfidentialMptConvertBackContext convertBackContext = convertBackService.generateContext(
      holderKeyPair.publicKey().deriveAddress(),
      holderAccountInfo.accountData().sequence(),
      mpTokenIssuanceId,
      holderVersionForConvertBack
    );

    // Decrypt current spending balance to generate a Pedersen commitment proving sufficient funds
    EncryptedAmount currentBalanceForConvertBack = EncryptedAmount.fromHex(
      holderMpTokenForConvertBack.confidentialBalanceSpending()
        .orElseThrow(() -> new RuntimeException("Holder has no confidential balance"))
    );
    UnsignedLong currentSpendingBalance = decryptor.decrypt(
      currentBalanceForConvertBack, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
    );

    BlindingFactor convertBackBalanceBlindingFactor = blindingFactorGenerator.generate();
    PedersenProofParams convertBackBalanceParams = convertBackService.generatePedersenProofParams(
      currentSpendingBalance, currentBalanceForConvertBack, convertBackBalanceBlindingFactor
    );

    ConfidentialMptConvertBackProof convertBackProof = convertBackService.generateProof(
      holderElGamalKeyPair, convertBackAmount, convertBackContext, convertBackBalanceParams
    );

    PedersenCommitment convertBackCommitment = PedersenCommitment.of(
      convertBackBalanceParams.pedersenCommitment()
    );

    ConfidentialMptConvertBack convertBack = ConfidentialMptConvertBack.builder()
      .account(holderKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(holderAccountInfo.accountData().sequence())
      .signingPublicKey(holderKeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(holderAccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .mptAmount(MpTokenNumericAmount.of(convertBackAmount))
      .holderEncryptedAmount(holderEncryptedForConvertBack.toHex())
      .issuerEncryptedAmount(issuerEncryptedForConvertBack.toHex())
      .blindingFactor(convertBackBlindingFactor.hexValue())
      .balanceCommitment(convertBackCommitment.hexValue())
      .zkProof(convertBackProof.hexValue())
      .build();

    TransactionResult<ConfidentialMptConvertBack> convertBackResult =
      signSubmitAndWait(convertBack, holderKeyPair, ConfidentialMptConvertBack.class);
    final Hash256 convertBackHash = convertBackResult.hash();

    // Verify remaining confidential balance: 400 - 50 = 350
    MpTokenObject holderMpTokenAfterConvertBack = getMpToken(holderKeyPair, mpTokenIssuanceId);
    EncryptedAmount remainingBalanceCiphertext = EncryptedAmount.fromHex(
      holderMpTokenAfterConvertBack.confidentialBalanceSpending()
        .orElseThrow(() -> new RuntimeException("No confidential balance after convert back"))
    );
    UnsignedLong remainingConfidentialBalance = decryptor.decrypt(
      remainingBalanceCiphertext, holderElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
    );
    assertThat(remainingConfidentialBalance.longValue()).isEqualTo(350L);

    // =====================================================================
    // 9. ConfidentialMptClawback: Issuer claws back 350 confidential MPTs
    //    from Holder 1. The issuer decrypts their mirror of the holder's
    //    balance (IssuerEncryptedBalance on the holder's MPToken) and
    //    generates a proof that the clawback amount does not exceed it.
    // =====================================================================
    UnsignedLong clawbackAmount = UnsignedLong.valueOf(350);

    // Read the issuer's encrypted mirror of the holder's balance from the ledger
    MpTokenObject holderMpTokenForClawback = getMpToken(holderKeyPair, mpTokenIssuanceId);
    EncryptedAmount issuerBalanceCiphertext = EncryptedAmount.fromHex(
      holderMpTokenForClawback.issuerEncryptedBalance()
        .orElseThrow(() -> new RuntimeException("No issuer encrypted balance found"))
    );

    // Verify issuer can decrypt and the balance is sufficient for the clawback
    UnsignedLong issuerDecryptedBalance = decryptor.decrypt(
      issuerBalanceCiphertext, issuerElGamalKeyPair.privateKey(), UnsignedLong.ZERO, DECRYPT_MAX
    );
    assertThat(issuerDecryptedBalance.longValue()).isGreaterThanOrEqualTo(clawbackAmount.longValue());

    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Generate context hash and clawback proof
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

    ConfidentialMptClawback clawback = ConfidentialMptClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(lastLedgerSeq(issuerAccountInfo))
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .holder(holderKeyPair.publicKey().deriveAddress())
      .mptAmount(MpTokenNumericAmount.of(clawbackAmount))
      .zkProof(clawbackProof.hexValue())
      .build();

    TransactionResult<ConfidentialMptClawback> clawbackResult =
      signSubmitAndWait(clawback, issuerKeyPair, ConfidentialMptClawback.class);
    final Hash256 clawbackHash = clawbackResult.hash();

    // Verify the holder's MPToken still exists after clawback (balances zeroed out)
    MpTokenObject holderMpTokenAfterClawback = getMpToken(holderKeyPair, mpTokenIssuanceId);
    assertThat(holderMpTokenAfterClawback).isNotNull();

    // =====================================================================
    // Print all Confidential MPT transaction hashes for debugging
    // =====================================================================
    System.out.println("\n========== CONFIDENTIAL MPT TRANSACTION HASHES ==========");
    System.out.println("ConfidentialMptConvert (Holder 1):     " + convertHash);
    System.out.println("ConfidentialMptMergeInbox (Holder 1):  " + mergeHash);
    System.out.println("ConfidentialMptConvert (Holder 2):     " + holder2ConvertHash);
    System.out.println("ConfidentialMptSend:                   " + sendHash);
    System.out.println("ConfidentialMptConvertBack:            " + convertBackHash);
    System.out.println("ConfidentialMptClawback:               " + clawbackHash);
    System.out.println("==========================================================\n");
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

  private UnsignedInteger lastLedgerSeq(AccountInfoResult accountInfo) {
    return accountInfo.ledgerIndexSafe()
      .plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue();
  }
}
