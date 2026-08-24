package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
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
import org.xrpl.xrpl4j.crypto.confidential.model.SecretBlindingFactor;
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
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptClawback;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptMergeInbox;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutableConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ImmutableConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base class for Confidential MPT (XLS-0096) integration tests, extending {@link AbstractMptIT} with the confidential
 * setup every confidential IT shares: an issuance with registered issuer/auditor keys, holder key registration and
 * funding, and proof-bound Convert / MergeInbox / Send / ConvertBack / Clawback construction plus balance decryption.
 * A {@code build*} method returns a signed, sequence-bound transaction (a proof commits to the account sequence it was
 * generated for); the wrappers also submit and wait, while negative tests use {@code build*} directly to assert a
 * {@code tec*} result.
 */
public abstract class AbstractConfidentialMptIT extends AbstractMptIT {

  /**
   * A decrypt upper bound comfortably above any balance these tests establish.
   */
  protected static final UnsignedLong DECRYPT_BOUND = UnsignedLong.valueOf(1_000_000);

  private final ConfidentialMptConvertService convertService = new ConfidentialMptConvertService();
  private final ConfidentialMptSendService sendService = new ConfidentialMptSendService();
  private final ConfidentialMptConvertBackService convertBackService = new ConfidentialMptConvertBackService();
  private final ConfidentialMptClawbackService clawbackService = new ConfidentialMptClawbackService();
  private final BlindingFactorGenerator blindingFactorGenerator = new JnaBlindingFactorGenerator();
  private final MptAmountEncryptor encryptor = new JnaMptAmountEncryptor();
  private final MptAmountDecryptor decryptor = new JnaMptAmountDecryptor();

  /**
   * A fresh ElGamal (secp256k1) keypair used for confidential encryption.
   */
  protected KeyPair elGamalKeyPair() {
    return Seed.elGamalSecp256k1Seed().deriveKeyPair();
  }

  /**
   * The recommended fee for a single-signed confidential MPT transaction (carries rippled's fee multiplier).
   */
  protected XrpCurrencyAmount confidentialFee() throws JsonRpcClientErrorException {
    return FeeUtils.computeConfidentialMptNetworkFees(xrplClient.fee(), UnsignedInteger.ZERO).recommendedFee();
  }

  // ===========================================================================
  // Issuance + holder setup
  // ===========================================================================

  /**
   * Create a confidential issuance (transfer + clawback + lock + confidential) with registered issuer/auditor keys.
   */
  protected ConfidentialIssuance createConfidentialIssuance() throws Exception {
    return createConfidentialIssuance(false);
  }

  /**
   * Create a confidential issuance, optionally requiring issuer authorization of holders.
   *
   * @param requireAuth Whether the issuance is created with {@code tfMPTRequireAuth} (allow-listing).
   *
   * @return A {@link ConfidentialIssuance} bundling the issuer account, its issuer/auditor ElGamal keys, and the id.
   */
  protected ConfidentialIssuance createConfidentialIssuance(final boolean requireAuth) throws Exception {
    final KeyPair issuer = createRandomAccountEd25519();
    final MpTokenIssuanceId issuanceId = createMptIssuance(issuer, MpTokenIssuanceCreateFlags.builder()
      .tfMptCanTransfer(true)
      .tfMptCanClawback(true)
      .tfMptCanLock(true)
      .tfMptCanHoldConfidentialBalance(true)
      .tfMptRequireAuth(requireAuth)
      .build());
    return registerConfidentialKeys(issuer, issuanceId);
  }

  /**
   * Register fresh issuer/auditor ElGamal keys on an already-confidential-capable issuance, returning the bundled
   * {@link ConfidentialIssuance}. Split out from {@link #createConfidentialIssuance(boolean)} so a test can enable the
   * confidential capability on a non-confidentially-created issuance (via {@code tfMPTSetCanHoldConfidentialBalance})
   * and then register keys on it.
   *
   * @param issuer     The issuer account keypair (already funded, already owning {@code issuanceId}).
   * @param issuanceId An issuance whose {@code lsfMPTCanHoldConfidentialBalance} is already set.
   *
   * @return A {@link ConfidentialIssuance} bundling the issuer account, its issuer/auditor ElGamal keys, and the id.
   */
  protected ConfidentialIssuance registerConfidentialKeys(final KeyPair issuer, final MpTokenIssuanceId issuanceId)
    throws Exception {
    final KeyPair issuerElGamal = elGamalKeyPair();
    final KeyPair auditorElGamal = elGamalKeyPair();

    final MpTokenIssuanceSet registerKeys = MpTokenIssuanceSet.builder()
      .account(issuer.publicKey().deriveAddress())
      .sequence(currentSequence(issuer.publicKey().deriveAddress()))
      .fee(networkFee())
      .signingPublicKey(issuer.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .issuerEncryptionKey(issuerElGamal.publicKey())
      .auditorEncryptionKey(auditorElGamal.publicKey())
      .build();
    submitAndWait(signatureService.sign(issuer.privateKey(), registerKeys), MpTokenIssuanceSet.class);

    // Assert the issuance actually carries the confidential capability flag and both registered keys. A regression in
    // MPTokenIssuanceSet, or in MpTokenIssuanceObject's deserialization of these two fields, would otherwise be
    // invisible to every test below (they all hold their own copies of the keys).
    final MpTokenIssuanceObject issuanceObject = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(issuanceId, LedgerSpecifier.VALIDATED)
    ).node();
    assertThat(issuanceObject.flags().lsfMptCanHoldConfidentialBalance()).isTrue();
    assertThat(issuanceObject.issuerEncryptionKey()).contains(issuerElGamal.publicKey());
    assertThat(issuanceObject.auditorEncryptionKey()).contains(auditorElGamal.publicKey());

    return new ConfidentialIssuance(issuer, issuerElGamal, auditorElGamal, issuanceId);
  }

  /**
   * A fresh, funded holder that has authorized (opted into) the issuance but not yet registered its ElGamal key.
   */
  protected ConfidentialHolder setupHolder(final ConfidentialIssuance issuance) throws Exception {
    final ConfidentialHolder holder = new ConfidentialHolder(createRandomAccountEd25519(), elGamalKeyPair());
    authorizeHolder(holder.account, issuance.issuanceId);
    return holder;
  }

  /**
   * A holder that has registered its ElGamal key via a zero-amount Convert (a valid Send destination).
   */
  protected ConfidentialHolder registerHolderKey(final ConfidentialIssuance issuance) throws Exception {
    final ConfidentialHolder holder = setupHolder(issuance);
    submitAndWait(buildConvert(holder, issuance, 0L, true), ConfidentialMptConvert.class);
    return holder;
  }

  /**
   * A holder with {@code amount} spendable confidential balance (pay public MPT, convert, merge).
   */
  protected ConfidentialHolder holderWithBalance(final ConfidentialIssuance issuance, final long amount)
    throws Exception {
    final ConfidentialHolder holder = setupHolder(issuance);
    payMpt(issuance.issuer, holder.address(), issuance.issuanceId, amount);
    submitAndWait(buildConvert(holder, issuance, amount, true), ConfidentialMptConvert.class);
    submitAndWait(buildMergeInbox(holder, issuance), ConfidentialMptMergeInbox.class);
    return holder;
  }

  // ===========================================================================
  // Convert
  // ===========================================================================

  /**
   * Submit a Convert (moving {@code amount} public MPT into the confidential inbox) and wait for validation.
   */
  protected void convert(final ConfidentialHolder holder, final ConfidentialIssuance issuance, final long amount,
    final boolean registerKey) throws Exception {
    submitAndWait(buildConvert(holder, issuance, amount, registerKey), ConfidentialMptConvert.class);
  }

  /**
   * Build a signed {@link ConfidentialMptConvert}. When {@code registerKey} is true it also registers the holder's
   * ElGamal key (holder key + Schnorr proof); a top-up on an already-registered holder omits both.
   */
  protected SingleSignedTransaction<ConfidentialMptConvert> buildConvert(
    final ConfidentialHolder holder, final ConfidentialIssuance issuance, final long amount, final boolean registerKey
  ) throws Exception {
    final UnsignedInteger sequence = currentSequence(holder.address());
    final UnsignedLong value = UnsignedLong.valueOf(amount);
    final BlindingFactor blinding = blindingFactorGenerator.generate();

    final ImmutableConfidentialMptConvert.Builder builder = ConfidentialMptConvert.builder()
      .account(holder.address())
      .fee(confidentialFee())
      .sequence(sequence)
      .signingPublicKey(holder.account.publicKey())
      .mpTokenIssuanceId(issuance.issuanceId)
      .mptAmount(MpTokenNumericAmount.of(value))
      .holderEncryptedAmount(encryptor.encrypt(value, holder.elGamal.publicKey(), blinding))
      .issuerEncryptedAmount(encryptor.encrypt(value, issuance.issuerElGamal.publicKey(), blinding))
      .auditorEncryptedAmount(encryptor.encrypt(value, issuance.auditorElGamal.publicKey(), blinding))
      .blindingFactor(blinding);

    if (registerKey) {
      final ConfidentialMptConvertContext context =
        convertService.generateContext(holder.address(), sequence, issuance.issuanceId);
      final ConfidentialMptConvertProof proof = convertService.generateProof(holder.elGamal, context);
      // The library must accept its own proof. rippled accepting the transaction only exercises the generator; this
      // is the only thing that exercises the verifier.
      assertThat(convertService.verifyProof(proof, holder.elGamal.publicKey(), context)).isTrue();
      builder.holderEncryptionKey(holder.elGamal.publicKey()).zkProof(proof);
    }

    return signatureService.sign(holder.account.privateKey(), builder.build());
  }

  // ===========================================================================
  // MergeInbox
  // ===========================================================================

  /**
   * Submit a MergeInbox (folding the confidential inbox into the spending balance) and wait for validation.
   */
  protected void mergeInbox(final ConfidentialHolder holder, final ConfidentialIssuance issuance) throws Exception {
    submitAndWait(buildMergeInbox(holder, issuance), ConfidentialMptMergeInbox.class);
  }

  /**
   * Build a signed {@link ConfidentialMptMergeInbox}.
   */
  protected SingleSignedTransaction<ConfidentialMptMergeInbox> buildMergeInbox(
    final ConfidentialHolder holder, final ConfidentialIssuance issuance
  ) throws Exception {
    final ConfidentialMptMergeInbox merge = ConfidentialMptMergeInbox.builder()
      .account(holder.address())
      .fee(confidentialFee())
      .sequence(currentSequence(holder.address()))
      .signingPublicKey(holder.account.publicKey())
      .mpTokenIssuanceId(issuance.issuanceId)
      .build();
    return signatureService.sign(holder.account.privateKey(), merge);
  }

  // ===========================================================================
  // Send
  // ===========================================================================

  /**
   * Submit a confidential Send of {@code amount} from {@code sender} to {@code destination} and wait.
   */
  protected void send(final ConfidentialHolder sender, final ConfidentialHolder destination,
    final ConfidentialIssuance issuance, final long amount) throws Exception {
    submitAndWait(buildSend(sender, destination, issuance, amount, Collections.emptyList()), ConfidentialMptSend.class);
  }

  /**
   * Build a signed {@link ConfidentialMptSend}. The amount is encrypted for all four parties (sender, destination,
   * issuer, auditor) under a shared blinding factor, with a range proof over the sender's current spending balance.
   */
  protected SingleSignedTransaction<ConfidentialMptSend> buildSend(
    final ConfidentialHolder sender, final ConfidentialHolder destination, final ConfidentialIssuance issuance,
    final long amount, final List<Hash256> credentialIds
  ) throws Exception {
    final UnsignedInteger sequence = currentSequence(sender.address());
    final UnsignedLong value = UnsignedLong.valueOf(amount);
    final MpTokenObject senderToken = getMpToken(sender.address(), issuance.issuanceId);

    final ConfidentialMptSendContext context = sendService.generateContext(
      sender.address(), sequence, issuance.issuanceId, destination.address(), senderToken.confidentialBalanceVersion()
    );

    final SecretBlindingFactor blinding = blindingFactorGenerator.generateSecretBlindingFactor();
    final EncryptedAmount senderCiphertext = encryptor.encrypt(value, sender.elGamal.publicKey(), blinding);
    final EncryptedAmount destCiphertext = encryptor.encrypt(value, destination.elGamal.publicKey(), blinding);
    final EncryptedAmount issuerCiphertext = encryptor.encrypt(value, issuance.issuerElGamal.publicKey(), blinding);
    final EncryptedAmount auditorCiphertext = encryptor.encrypt(value, issuance.auditorElGamal.publicKey(), blinding);

    final EncryptedAmount senderBalance = senderToken.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("sender has no confidential spending balance"));
    final UnsignedLong senderCurrentBalance =
      decryptor.decrypt(senderBalance, sender.elGamal.privateKey(), UnsignedLong.ZERO, DECRYPT_BOUND);

    final Commitment amountCommitment = sendService.generatePedersenCommitment(value, blinding);
    final SecretBlindingFactor balanceBlinding = blindingFactorGenerator.generateSecretBlindingFactor();
    final PedersenProofParams balanceParams =
      sendService.generatePedersenProofParams(senderCurrentBalance, senderBalance, balanceBlinding);

    final List<MptConfidentialParty> parties = Arrays.asList(
      MptConfidentialParty.of(sender.elGamal.publicKey(), senderCiphertext),
      MptConfidentialParty.of(destination.elGamal.publicKey(), destCiphertext),
      MptConfidentialParty.of(issuance.issuerElGamal.publicKey(), issuerCiphertext),
      MptConfidentialParty.of(issuance.auditorElGamal.publicKey(), auditorCiphertext)
    );
    final ConfidentialMptSendProof proof = sendService.generateProof(
      sender.elGamal, value, parties, blinding, context, amountCommitment, balanceParams
    );
    // Self-verify: exercises JnaConfidentialMptSendProofVerifier, including participant ordering.
    assertThat(sendService.verifyProof(
      proof, parties, senderBalance, context, amountCommitment, Commitment.of(balanceParams.pedersenCommitment())
    )).isTrue();

    final ImmutableConfidentialMptSend.Builder builder = ConfidentialMptSend.builder()
      .account(sender.address())
      .fee(confidentialFee())
      .sequence(sequence)
      .signingPublicKey(sender.account.publicKey())
      .destination(destination.address())
      .mpTokenIssuanceId(issuance.issuanceId)
      .senderEncryptedAmount(senderCiphertext)
      .destinationEncryptedAmount(destCiphertext)
      .issuerEncryptedAmount(issuerCiphertext)
      .auditorEncryptedAmount(auditorCiphertext)
      .zkProof(proof)
      .amountCommitment(Commitment.of(amountCommitment.hexValue()))
      .balanceCommitment(Commitment.of(balanceParams.pedersenCommitment().hexValue()));
    if (!credentialIds.isEmpty()) {
      builder.credentialIds(credentialIds);
    }

    return signatureService.sign(sender.account.privateKey(), builder.build());
  }

  // ===========================================================================
  // ConvertBack
  // ===========================================================================

  /**
   * Submit a ConvertBack (revealing {@code amount} of confidential balance to public) and wait.
   */
  protected void convertBack(final ConfidentialHolder holder, final ConfidentialIssuance issuance, final long amount)
    throws Exception {
    submitAndWait(buildConvertBack(holder, issuance, amount), ConfidentialMptConvertBack.class);
  }

  /**
   * Build a signed {@link ConfidentialMptConvertBack} with a range proof over the holder's spending balance.
   */
  protected SingleSignedTransaction<ConfidentialMptConvertBack> buildConvertBack(
    final ConfidentialHolder holder, final ConfidentialIssuance issuance, final long amount
  ) throws Exception {
    final UnsignedInteger sequence = currentSequence(holder.address());
    final UnsignedLong value = UnsignedLong.valueOf(amount);
    final MpTokenObject token = getMpToken(holder.address(), issuance.issuanceId);
    final BlindingFactor blinding = blindingFactorGenerator.generate();

    final ConfidentialMptConvertBackContext context = convertBackService.generateContext(
      holder.address(), sequence, issuance.issuanceId, token.confidentialBalanceVersion()
    );

    final EncryptedAmount currentBalance = token.confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("holder has no confidential spending balance"));
    final UnsignedLong currentPlain =
      decryptor.decrypt(currentBalance, holder.elGamal.privateKey(), UnsignedLong.ZERO, DECRYPT_BOUND);

    final SecretBlindingFactor balanceBlinding = blindingFactorGenerator.generateSecretBlindingFactor();
    final PedersenProofParams balanceParams =
      convertBackService.generatePedersenProofParams(currentPlain, currentBalance, balanceBlinding);
    final ConfidentialMptConvertBackProof proof =
      convertBackService.generateProof(holder.elGamal, value, context, balanceParams);
    // Self-verify: exercises JnaConfidentialMptConvertBackProofVerifier.
    assertThat(convertBackService.verifyProof(
      proof, holder.elGamal.publicKey(), currentBalance, Commitment.of(balanceParams.pedersenCommitment()), value,
      context
    )).isTrue();

    final ConfidentialMptConvertBack convertBack = ConfidentialMptConvertBack.builder()
      .account(holder.address())
      .fee(confidentialFee())
      .sequence(sequence)
      .signingPublicKey(holder.account.publicKey())
      .mpTokenIssuanceId(issuance.issuanceId)
      .mptAmount(MpTokenNumericAmount.of(value))
      .holderEncryptedAmount(encryptor.encrypt(value, holder.elGamal.publicKey(), blinding))
      .issuerEncryptedAmount(encryptor.encrypt(value, issuance.issuerElGamal.publicKey(), blinding))
      .auditorEncryptedAmount(encryptor.encrypt(value, issuance.auditorElGamal.publicKey(), blinding))
      .blindingFactor(blinding)
      .balanceCommitment(Commitment.of(balanceParams.pedersenCommitment().hexValue()))
      .zkProof(proof)
      .build();
    return signatureService.sign(holder.account.privateKey(), convertBack);
  }

  // ===========================================================================
  // Clawback
  // ===========================================================================

  /**
   * Submit a full Clawback of {@code holder}'s entire confidential balance and wait.
   */
  protected void clawback(final ConfidentialIssuance issuance, final ConfidentialHolder holder) throws Exception {
    final UnsignedLong full = issuerBalanceOf(issuance, holder);
    submitAndWait(buildClawback(issuance, holder, full.longValue()), ConfidentialMptClawback.class);
  }

  /**
   * Build a signed {@link ConfidentialMptClawback} for a specific {@code amount}. Passing an amount other than the
   * holder's full balance yields a proof rippled rejects with {@code tecBAD_PROOF}.
   */
  protected SingleSignedTransaction<ConfidentialMptClawback> buildClawback(
    final ConfidentialIssuance issuance, final ConfidentialHolder holder, final long amount
  ) throws Exception {
    final UnsignedInteger sequence = currentSequence(issuance.issuer.publicKey().deriveAddress());
    final UnsignedLong value = UnsignedLong.valueOf(amount);
    final EncryptedAmount issuerBalance = getMpToken(holder.address(), issuance.issuanceId).issuerEncryptedBalance()
      .orElseThrow(() -> new RuntimeException("holder has no issuer-encrypted balance"));

    final ConfidentialMptClawbackContext context = clawbackService.generateContext(
      issuance.issuer.publicKey().deriveAddress(), sequence, issuance.issuanceId, holder.address()
    );
    final ConfidentialMptClawbackProof proof = clawbackService.generateProof(
      issuerBalance, issuance.issuerElGamal.publicKey(), value, issuance.issuerElGamal.privateKey(), context
    );
    // Self-verify, but only for a full-balance clawback: the proof asserts the issuer-encrypted balance equals the
    // stated amount, so rejectsClawbackWithWrongAmount deliberately builds one that cannot verify.
    if (value.equals(issuerBalanceOf(issuance, holder))) {
      assertThat(clawbackService.verifyProof(
        proof, issuerBalance, issuance.issuerElGamal.publicKey(), value, context
      )).isTrue();
    }

    final ConfidentialMptClawback clawback = ConfidentialMptClawback.builder()
      .account(issuance.issuer.publicKey().deriveAddress())
      .fee(confidentialFee())
      .sequence(sequence)
      .signingPublicKey(issuance.issuer.publicKey())
      .mpTokenIssuanceId(issuance.issuanceId)
      .holder(holder.address())
      .mptAmount(MpTokenNumericAmount.of(value))
      .zkProof(proof)
      .build();
    return signatureService.sign(issuance.issuer.privateKey(), clawback);
  }

  // ===========================================================================
  // Balance reads
  // ===========================================================================

  /**
   * Decrypt {@code holder}'s spendable confidential balance with the holder's own key.
   */
  protected UnsignedLong spendable(final ConfidentialHolder holder, final ConfidentialIssuance issuance)
    throws Exception {
    final EncryptedAmount spending = getMpToken(holder.address(), issuance.issuanceId).confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("holder has no confidential spending balance"));
    return decryptor.decrypt(spending, holder.elGamal.privateKey(), UnsignedLong.ZERO, DECRYPT_BOUND);
  }

  /**
   * Decrypt {@code holder}'s confidential inbox balance with the holder's own key.
   */
  protected UnsignedLong inboxBalance(final ConfidentialHolder holder, final ConfidentialIssuance issuance)
    throws Exception {
    final EncryptedAmount inbox = getMpToken(holder.address(), issuance.issuanceId).confidentialBalanceInbox()
      .orElseThrow(() -> new RuntimeException("holder has no confidential inbox balance"));
    return decryptor.decrypt(inbox, holder.elGamal.privateKey(), UnsignedLong.ZERO, DECRYPT_BOUND);
  }

  /**
   * Auditor selective disclosure: decrypt {@code holder}'s balance with the auditor's key.
   */
  protected UnsignedLong auditorReads(final ConfidentialHolder holder, final ConfidentialIssuance issuance)
    throws Exception {
    final EncryptedAmount auditorBalance = getMpToken(holder.address(), issuance.issuanceId).auditorEncryptedBalance()
      .orElseThrow(() -> new RuntimeException("holder has no auditor-encrypted balance"));
    return decryptor.decrypt(auditorBalance, issuance.auditorElGamal.privateKey(), UnsignedLong.ZERO, DECRYPT_BOUND);
  }

  /**
   * The issuer's encrypted mirror of {@code holder}'s balance, decrypted with the issuer's key.
   */
  private UnsignedLong issuerBalanceOf(final ConfidentialIssuance issuance, final ConfidentialHolder holder)
    throws Exception {
    final EncryptedAmount issuerBalance = getMpToken(holder.address(), issuance.issuanceId).issuerEncryptedBalance()
      .orElseThrow(() -> new RuntimeException("holder has no issuer-encrypted balance"));
    return decryptor.decrypt(issuerBalance, issuance.issuerElGamal.privateKey(), UnsignedLong.ZERO, DECRYPT_BOUND);
  }

  // ===========================================================================
  // Value types
  // ===========================================================================

  /**
   * A confidential issuance: the issuer account, its issuer/auditor ElGamal keys, and the issuance id.
   */
  protected static final class ConfidentialIssuance {
    protected final KeyPair issuer;
    protected final KeyPair issuerElGamal;
    protected final KeyPair auditorElGamal;
    protected final MpTokenIssuanceId issuanceId;

    protected ConfidentialIssuance(final KeyPair issuer, final KeyPair issuerElGamal, final KeyPair auditorElGamal,
      final MpTokenIssuanceId issuanceId) {
      this.issuer = issuer;
      this.issuerElGamal = issuerElGamal;
      this.auditorElGamal = auditorElGamal;
      this.issuanceId = issuanceId;
    }
  }

  /**
   * A confidential holder: its XRPL account keypair and its ElGamal keypair.
   */
  protected static final class ConfidentialHolder {
    protected final KeyPair account;
    protected final KeyPair elGamal;

    protected ConfidentialHolder(final KeyPair account, final KeyPair elGamal) {
      this.account = account;
      this.elGamal = elGamal;
    }

    protected Address address() {
      return account.publicKey().deriveAddress();
    }
  }
}
