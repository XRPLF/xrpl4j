package org.xrpl.xrpl4j.crypto.confidential;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
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

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialIssuanceInfo;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialTokenState;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.ImmutableConfidentialTokenState;
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
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialCiphertextArithmetic;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialCiphertextArithmetic;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptClawback;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptMergeInbox;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.ImmutableBatch;
import org.xrpl.xrpl4j.model.transactions.ImmutableConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ImmutableConfidentialMptConvertBack;
import org.xrpl.xrpl4j.model.transactions.ImmutableConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Assembles a Batch (XLS-56) of Confidential MPT (XLS-0096) inner transactions from pre-fetched ledger state.
 *
 * <p>This assembler is <em>client-free</em>: it performs no I/O. The caller fetches the ledger state (per-account
 * sequences, confidential balances, issuance keys) and supplies it via {@link ConfidentialBatchRequest}; the assembler
 * assigns each inner its position-derived sequence (a confidential proof binds its sequence, so it must be built with
 * the final value), builds each inner's zero-knowledge proof, shapes every inner as an inner-batch transaction
 * ({@code tfInnerBatchTxn}, {@code Fee: 0}, empty {@code SigningPubKey}), and returns an unsigned {@link Batch}.</p>
 *
 * <p>When several balance-mutating operations share a Batch for the same {@code (account, token)}, each proof must bind
 * the balance the previous inner leaves behind, not the stale on-ledger value. The assembler threads a predicted
 * {@link ConfidentialTokenState} per {@code (account, token)}, advancing it homomorphically after each inner (via
 * {@link ConfidentialCiphertextArithmetic}) so the predicted ciphertext equals what the ledger will store. Values that
 * an in-batch MergeInbox or Clawback resets to a canonical encrypted zero become unavailable; a later inner that reads
 * one fails rather than emit a proof the ledger would reject.</p>
 *
 * <p>Signing stays with the caller: for combinations whose inners are submitted by accounts other than the outer
 * account, add a {@code BatchSigner} for each via {@code signInner}/{@code multiSignInner}, then sign the outer
 * Batch.</p>
 */
public class ConfidentialMptBatchAssembler {

  // rippled bounds a Batch to 2-8 inner transactions; fail fast before building any proof.
  private static final int MIN_BATCH_INNERS = 2;
  private static final int MAX_BATCH_INNERS = 8;
  private static final XrpCurrencyAmount ZERO_FEE = XrpCurrencyAmount.ofDrops(0);
  // A send proof's first 32 bytes are the re-randomization challenge rippled reuses to deterministically re-blind the
  // destination's inbox credit; reproduce it to predict the recipient's post-send inbox.
  private static final int CHALLENGE_SIZE = 32;

  private final ConfidentialMptConvertService convertService;
  private final ConfidentialMptSendService sendService;
  private final ConfidentialMptConvertBackService convertBackService;
  private final ConfidentialMptClawbackService clawbackService;
  private final MptAmountEncryptor encryptor;
  private final MptAmountDecryptor decryptor;
  private final BlindingFactorGenerator blindingFactorGenerator;
  private final ConfidentialCiphertextArithmetic ciphertextArithmetic;

  /**
   * Constructs an assembler wiring the default native (JNA) implementations of every collaborator.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public ConfidentialMptBatchAssembler() {
    this(
      new ConfidentialMptConvertService(),
      new ConfidentialMptSendService(),
      new ConfidentialMptConvertBackService(),
      new ConfidentialMptClawbackService(),
      new JnaMptAmountEncryptor(),
      new JnaMptAmountDecryptor(),
      new JnaBlindingFactorGenerator(),
      new JnaConfidentialCiphertextArithmetic()
    );
  }

  /**
   * Constructs an assembler with the supplied collaborators.
   *
   * @param convertService          The convert proof service.
   * @param sendService             The send proof service.
   * @param convertBackService      The convert-back proof service.
   * @param clawbackService         The clawback proof service.
   * @param encryptor               The amount encryptor.
   * @param decryptor               The amount decryptor.
   * @param blindingFactorGenerator The blinding factor generator.
   * @param ciphertextArithmetic    The homomorphic ciphertext arithmetic used to predict chained balances.
   */
  public ConfidentialMptBatchAssembler(
    final ConfidentialMptConvertService convertService,
    final ConfidentialMptSendService sendService,
    final ConfidentialMptConvertBackService convertBackService,
    final ConfidentialMptClawbackService clawbackService,
    final MptAmountEncryptor encryptor,
    final MptAmountDecryptor decryptor,
    final BlindingFactorGenerator blindingFactorGenerator,
    final ConfidentialCiphertextArithmetic ciphertextArithmetic
  ) {
    this.convertService = Objects.requireNonNull(convertService, "convertService must not be null");
    this.sendService = Objects.requireNonNull(sendService, "sendService must not be null");
    this.convertBackService = Objects.requireNonNull(convertBackService, "convertBackService must not be null");
    this.clawbackService = Objects.requireNonNull(clawbackService, "clawbackService must not be null");
    this.encryptor = Objects.requireNonNull(encryptor, "encryptor must not be null");
    this.decryptor = Objects.requireNonNull(decryptor, "decryptor must not be null");
    this.blindingFactorGenerator =
      Objects.requireNonNull(blindingFactorGenerator, "blindingFactorGenerator must not be null");
    this.ciphertextArithmetic =
      Objects.requireNonNull(ciphertextArithmetic, "ciphertextArithmetic must not be null");
  }

  /**
   * Assembles an unsigned {@link Batch} from the request's ordered confidential operations and pre-fetched state.
   *
   * @param request The batch inputs.
   *
   * @return The assembled, unsigned {@link Batch}.
   *
   * @throws IllegalArgumentException if the request has fewer than 2 or more than 8 inners, or a needed sequence,
   *                                  state, or issuance entry is missing.
   * @throws IllegalStateException    if a proof cannot be built against the (possibly predicted) state — for example,
   *                                  a balance an earlier inner reset to a canonical encrypted zero.
   */
  public Batch assemble(final ConfidentialBatchRequest request) {
    Objects.requireNonNull(request, "request must not be null");
    final int innerCount = request.inners().size();
    Preconditions.checkArgument(
      innerCount >= MIN_BATCH_INNERS && innerCount <= MAX_BATCH_INNERS,
      "a Batch requires between %s and %s inner transactions, but got %s",
      MIN_BATCH_INNERS, MAX_BATCH_INNERS, innerCount
    );

    final Address outerAccount = request.accountPublicKey().deriveAddress();
    // Per-account next-sequence counter. The outer Batch consumes the outer account's current sequence, so the outer
    // account's inners start one later; every other account's inners start at its own current sequence.
    final Map<Address, UnsignedInteger> nextSequence = new HashMap<>(request.accountSequences());
    final UnsignedInteger outerSequence =
      requireSequence(nextSequence, outerAccount, "sequence for outer account " + outerAccount);
    nextSequence.put(outerAccount, outerSequence.plus(UnsignedInteger.ONE));

    // Predicted (account, token) state, threaded inner-to-inner. Seeded from the caller's fetched state.
    final Map<String, ConfidentialTokenState> predicted = new HashMap<>(request.states());

    // The most an issuance's confidential outstanding can rise within this Batch, per token; threaded into each spend's
    // decrypt bound so a balance topped up by an in-batch Convert stays decryptable against the pre-batch total.
    final Map<MpTokenIssuanceId, UnsignedLong> convertTotals = sumConvertsByToken(request.inners());

    final List<RawTransactionWrapper> rawTransactions = new ArrayList<>();
    for (final ConfidentialBatchInner inner : request.inners()) {
      if (inner.operation().isPresent()) {
        final ConfidentialMptOp op = inner.operation().get();
        final Address account = op.account();
        final UnsignedInteger sequence = requireSequence(nextSequence, account, "sequence for account " + account);
        nextSequence.put(account, sequence.plus(UnsignedInteger.ONE));

        final BuiltInner built = buildInner(request, predicted, convertTotals, op, sequence);
        for (final Map.Entry<String, ConfidentialTokenState> update : built.updates) {
          predicted.put(update.getKey(), update.getValue());
        }
        rawTransactions.add(RawTransactionWrapper.of(built.transaction));
      } else {
        // A pre-built plain inner is passed through untouched (the caller shaped and sequenced it); the
        // RawTransactionWrapper/Batch validation rejects a malformed one.
        final Transaction plain = inner.plainTransaction().get();
        rawTransactions.add(RawTransactionWrapper.of(plain));
        // Advance the submitter's counter past a plain inner that consumed a regular sequence, so a later confidential
        // inner for the same account is numbered correctly. A ticketed plain inner consumes no sequence.
        if (!plain.ticketSequence().isPresent()) {
          nextSequence.put(plain.account(), plain.sequence().plus(UnsignedInteger.ONE));
        }
      }
    }

    final ImmutableBatch.Builder batch = Batch.builder()
      .account(outerAccount)
      .fee(request.outerFee())
      .sequence(outerSequence)
      .signingPublicKey(request.accountPublicKey())
      .rawTransactions(rawTransactions);
    request.batchFlags().ifPresent(batch::flags);
    return batch.build();
  }

  private BuiltInner buildInner(
    final ConfidentialBatchRequest request,
    final Map<String, ConfidentialTokenState> predicted,
    final Map<MpTokenIssuanceId, UnsignedLong> convertTotals,
    final ConfidentialMptOp op,
    final UnsignedInteger sequence
  ) {
    return op.map(
      send -> buildSend(request, predicted, convertTotals, send, sequence),
      convert -> buildConvert(request, predicted, convert, sequence),
      convertBack -> buildConvertBack(request, predicted, convertTotals, convertBack, sequence),
      mergeInbox -> buildMergeInbox(predicted, mergeInbox, sequence),
      clawback -> buildClawback(predicted, clawback, sequence)
    );
  }

  private BuiltInner buildSend(
    final ConfidentialBatchRequest request,
    final Map<String, ConfidentialTokenState> predicted,
    final Map<MpTokenIssuanceId, UnsignedLong> convertTotals,
    final ConfidentialSendOp op,
    final UnsignedInteger sequence
  ) {
    final String senderKey = ConfidentialBatchRequest.stateKey(op.account(), op.mpTokenIssuanceId());
    final String destinationKey = ConfidentialBatchRequest.stateKey(op.destination(), op.mpTokenIssuanceId());
    final ConfidentialTokenState senderState = requireState(predicted, senderKey, "sender");
    final ConfidentialTokenState destState = requireState(predicted, destinationKey, "destination");
    final ConfidentialIssuanceInfo issuance = requireIssuance(request, op.mpTokenIssuanceId());

    final EncryptedAmount spending = requireBalance(senderState.spending(), "sender spending balance");
    final PublicKey destPublicKey = destState.holderKey().orElseThrow(() -> new IllegalStateException(
      "ConfidentialMptBatchAssembler: destination " + op.destination() + " has no registered holder encryption key"
    ));

    final ConfidentialMptSendContext context = sendService.generateContext(
      op.account(), sequence, op.mpTokenIssuanceId(), op.destination(), senderState.version()
    );

    final BlindingFactor txBlinding = blindingFactorGenerator.generate();
    final EncryptedAmount senderCiphertext = encryptor.encrypt(op.amount(), op.senderKeyPair().publicKey(), txBlinding);
    final EncryptedAmount destCiphertext = encryptor.encrypt(op.amount(), destPublicKey, txBlinding);
    final EncryptedAmount issuerCiphertext =
      encryptor.encrypt(op.amount(), issuance.issuerEncryptionKey(), txBlinding);
    final Optional<EncryptedAmount> auditorCiphertext =
      issuance.auditorEncryptionKey().map(key -> encryptor.encrypt(op.amount(), key, txBlinding));

    // Proof participants, in order: sender, destination, issuer, [auditor].
    final List<MptConfidentialParty> participants = new ArrayList<>();
    participants.add(MptConfidentialParty.of(op.senderKeyPair().publicKey(), senderCiphertext));
    participants.add(MptConfidentialParty.of(destPublicKey, destCiphertext));
    participants.add(MptConfidentialParty.of(issuance.issuerEncryptionKey(), issuerCiphertext));
    issuance.auditorEncryptionKey().ifPresent(key ->
      participants.add(MptConfidentialParty.of(key, auditorCiphertext.get())));

    final UnsignedLong currentBalance = decryptor.decrypt(
      spending, op.senderKeyPair().privateKey(), UnsignedLong.ZERO,
      decryptBound(issuance, convertTotals, op.mpTokenIssuanceId())
    );
    final Commitment amountCommitment = sendService.generatePedersenCommitment(op.amount(), txBlinding);
    final BlindingFactor balanceBlinding = blindingFactorGenerator.generate();
    final PedersenProofParams balanceParams =
      sendService.generatePedersenProofParams(currentBalance, spending, balanceBlinding);
    final ConfidentialMptSendProof proof = sendService.generateProof(
      op.senderKeyPair(), op.amount(), participants, txBlinding, context, amountCommitment, balanceParams
    );

    final ImmutableConfidentialMptSend.Builder builder = ConfidentialMptSend.builder()
      .account(op.account())
      .fee(ZERO_FEE)
      .sequence(sequence)
      .flags(TransactionFlags.INNER_BATCH_TXN)
      .destination(op.destination())
      .mpTokenIssuanceId(op.mpTokenIssuanceId())
      .senderEncryptedAmount(senderCiphertext)
      .destinationEncryptedAmount(destCiphertext)
      .issuerEncryptedAmount(issuerCiphertext)
      .zkProof(proof)
      .amountCommitment(amountCommitment)
      .balanceCommitment(Commitment.of(balanceParams.pedersenCommitment()));
    auditorCiphertext.ifPresent(builder::auditorEncryptedAmount);

    // Predicted state after the send: debit the sender, credit the destination's balances (re-blinded as rippled will).
    final ConfidentialTokenState debitedSender =
      applyDebit(senderState, senderCiphertext, issuerCiphertext, auditorCiphertext);
    final ConfidentialTokenState creditedDest = applyRecipientCredit(
      destState, proof, destPublicKey, destCiphertext, issuance, issuerCiphertext, auditorCiphertext
    );
    return new BuiltInner(builder.build(), update(senderKey, debitedSender), update(destinationKey, creditedDest));
  }

  private BuiltInner buildConvert(
    final ConfidentialBatchRequest request,
    final Map<String, ConfidentialTokenState> predicted,
    final ConfidentialConvertOp op,
    final UnsignedInteger sequence
  ) {
    final ConfidentialIssuanceInfo issuance = requireIssuance(request, op.mpTokenIssuanceId());
    final String key = ConfidentialBatchRequest.stateKey(op.account(), op.mpTokenIssuanceId());
    final ConfidentialTokenState state = predicted.getOrDefault(key, ConfidentialTokenState.builder().build());

    final BlindingFactor blinding = blindingFactorGenerator.generate();
    final EncryptedAmount holderCiphertext =
      encryptor.encrypt(op.amount(), op.holderKeyPair().publicKey(), blinding);
    final EncryptedAmount issuerCiphertext =
      encryptor.encrypt(op.amount(), issuance.issuerEncryptionKey(), blinding);
    final Optional<EncryptedAmount> auditorCiphertext =
      issuance.auditorEncryptionKey().map(auditorKey -> encryptor.encrypt(op.amount(), auditorKey, blinding));

    final ImmutableConfidentialMptConvert.Builder builder = ConfidentialMptConvert.builder()
      .account(op.account())
      .fee(ZERO_FEE)
      .sequence(sequence)
      .flags(TransactionFlags.INNER_BATCH_TXN)
      .mpTokenIssuanceId(op.mpTokenIssuanceId())
      .mptAmount(MpTokenNumericAmount.of(op.amount()))
      .holderEncryptedAmount(holderCiphertext)
      .issuerEncryptedAmount(issuerCiphertext)
      .blindingFactor(blinding);
    auditorCiphertext.ifPresent(builder::auditorEncryptedAmount);

    // rippled requires HolderEncryptionKey and ZKProof to be set-or-omitted together: attach both only when
    // registering the holder key (its first Convert). A top-up Convert omits them (rippled rejects a
    // re-registration as tecDUPLICATE), so its Schnorr proof — an expensive native call — is never generated.
    final Optional<PublicKey> registeredKey;
    if (op.registerKey()) {
      final ConfidentialMptConvertContext context =
        convertService.generateContext(op.account(), sequence, op.mpTokenIssuanceId());
      builder
        .holderEncryptionKey(op.holderKeyPair().publicKey())
        .zkProof(convertService.generateProof(op.holderKeyPair(), context));
      registeredKey = Optional.of(op.holderKeyPair().publicKey());
    } else {
      registeredKey = Optional.empty();
    }

    // Predicted state after the convert: credit inbox/issuer/auditor, and register the holder key only when this
    // Convert actually did (a top-up leaves the previously-registered key in place).
    final ConfidentialTokenState credited = applyConvertCredit(
      state, holderCiphertext, issuerCiphertext, auditorCiphertext, registeredKey
    );
    return new BuiltInner(builder.build(), update(key, credited));
  }

  private BuiltInner buildConvertBack(
    final ConfidentialBatchRequest request,
    final Map<String, ConfidentialTokenState> predicted,
    final Map<MpTokenIssuanceId, UnsignedLong> convertTotals,
    final ConfidentialConvertBackOp op,
    final UnsignedInteger sequence
  ) {
    final String key = ConfidentialBatchRequest.stateKey(op.account(), op.mpTokenIssuanceId());
    final ConfidentialTokenState state = requireState(predicted, key, "holder");
    final ConfidentialIssuanceInfo issuance = requireIssuance(request, op.mpTokenIssuanceId());
    final EncryptedAmount spending = requireBalance(state.spending(), "holder spending balance");

    final BlindingFactor blinding = blindingFactorGenerator.generate();
    final EncryptedAmount holderCiphertext =
      encryptor.encrypt(op.amount(), op.holderKeyPair().publicKey(), blinding);
    final EncryptedAmount issuerCiphertext =
      encryptor.encrypt(op.amount(), issuance.issuerEncryptionKey(), blinding);
    final Optional<EncryptedAmount> auditorCiphertext =
      issuance.auditorEncryptionKey().map(auditorKey -> encryptor.encrypt(op.amount(), auditorKey, blinding));

    final ConfidentialMptConvertBackContext context = convertBackService.generateContext(
      op.account(), sequence, op.mpTokenIssuanceId(), state.version()
    );
    final UnsignedLong currentBalance = decryptor.decrypt(
      spending, op.holderKeyPair().privateKey(), UnsignedLong.ZERO,
      decryptBound(issuance, convertTotals, op.mpTokenIssuanceId())
    );
    final BlindingFactor balanceBlinding = blindingFactorGenerator.generate();
    final PedersenProofParams balanceParams =
      convertBackService.generatePedersenProofParams(currentBalance, spending, balanceBlinding);
    final ConfidentialMptConvertBackProof proof =
      convertBackService.generateProof(op.holderKeyPair(), op.amount(), context, balanceParams);

    final ImmutableConfidentialMptConvertBack.Builder builder = ConfidentialMptConvertBack.builder()
      .account(op.account())
      .fee(ZERO_FEE)
      .sequence(sequence)
      .flags(TransactionFlags.INNER_BATCH_TXN)
      .mpTokenIssuanceId(op.mpTokenIssuanceId())
      .mptAmount(MpTokenNumericAmount.of(op.amount()))
      .holderEncryptedAmount(holderCiphertext)
      .issuerEncryptedAmount(issuerCiphertext)
      .blindingFactor(blinding)
      .balanceCommitment(Commitment.of(balanceParams.pedersenCommitment()))
      .zkProof(proof);
    auditorCiphertext.ifPresent(builder::auditorEncryptedAmount);

    // Predicted state after the convert-back: debit spending/issuer/auditor.
    final ConfidentialTokenState debited =
      applyDebit(state, holderCiphertext, issuerCiphertext, auditorCiphertext);
    return new BuiltInner(builder.build(), update(key, debited));
  }

  private BuiltInner buildMergeInbox(
    final Map<String, ConfidentialTokenState> predicted,
    final ConfidentialMergeInboxOp op,
    final UnsignedInteger sequence
  ) {
    final String key = ConfidentialBatchRequest.stateKey(op.account(), op.mpTokenIssuanceId());
    final ConfidentialTokenState state = requireState(predicted, key, "holder");

    final Transaction tx = ConfidentialMptMergeInbox.builder()
      .account(op.account())
      .fee(ZERO_FEE)
      .sequence(sequence)
      .flags(TransactionFlags.INNER_BATCH_TXN)
      .mpTokenIssuanceId(op.mpTokenIssuanceId())
      .build();

    return new BuiltInner(tx, update(key, applyMerge(state)));
  }

  private BuiltInner buildClawback(
    final Map<String, ConfidentialTokenState> predicted,
    final ConfidentialClawbackOp op,
    final UnsignedInteger sequence
  ) {
    final String holderKey = ConfidentialBatchRequest.stateKey(op.holder(), op.mpTokenIssuanceId());
    final ConfidentialTokenState holderState = requireState(predicted, holderKey, "holder");
    final EncryptedAmount issuerBalance =
      requireBalance(holderState.issuerEncrypted(), "holder issuer-encrypted balance");

    final ConfidentialMptClawbackContext context = clawbackService.generateContext(
      op.account(), sequence, op.mpTokenIssuanceId(), op.holder()
    );
    final ConfidentialMptClawbackProof proof = clawbackService.generateProof(
      issuerBalance, op.issuerKeyPair().publicKey(), op.amount(), op.issuerKeyPair().privateKey(), context
    );

    final Transaction tx = ConfidentialMptClawback.builder()
      .account(op.account())
      .fee(ZERO_FEE)
      .sequence(sequence)
      .flags(TransactionFlags.INNER_BATCH_TXN)
      .mpTokenIssuanceId(op.mpTokenIssuanceId())
      .holder(op.holder())
      .mptAmount(MpTokenNumericAmount.of(op.amount()))
      .zkProof(proof)
      .build();

    return new BuiltInner(tx, update(holderKey, applyClawback(holderState)));
  }

  // =========================================================================
  // Predicted-state transitions (homomorphic)
  // =========================================================================

  /**
   * Debit a spender's balances (Send/ConvertBack): subtract the encrypted amounts from spending, issuer-encrypted, and
   * (when present) auditor-encrypted balances, and bump the version.
   */
  private ConfidentialTokenState applyDebit(
    final ConfidentialTokenState state,
    final EncryptedAmount spendCiphertext,
    final EncryptedAmount issuerCiphertext,
    final Optional<EncryptedAmount> auditorCiphertext
  ) {
    final EncryptedAmount spending =
      ciphertextArithmetic.subtract(requireBalance(state.spending(), "spending balance"), spendCiphertext);
    final EncryptedAmount issuerEncrypted =
      ciphertextArithmetic.subtract(requireBalance(state.issuerEncrypted(), "issuer-encrypted balance"),
        issuerCiphertext);
    final Optional<EncryptedAmount> auditorEncrypted = auditorCiphertext.isPresent() ?
      Optional.of(ciphertextArithmetic.subtract(
        requireBalance(state.auditorEncrypted(), "auditor-encrypted balance"), auditorCiphertext.get())) :
      state.auditorEncrypted();
    return ConfidentialTokenState.builder()
      .from(state)
      .spending(spending)
      .issuerEncrypted(issuerEncrypted)
      .auditorEncrypted(auditorEncrypted)
      .version(state.version().plus(UnsignedInteger.ONE))
      .build();
  }

  /**
   * Credit a holder's pending balances after a Convert (rippled adds the tx ciphertexts straight in; a first-ever
   * convert initializes them). Spending is untouched. The holder key is (re-)registered only when {@code registeredKey}
   * is present — a top-up Convert that does not register leaves the previously-registered key in place.
   */
  private ConfidentialTokenState applyConvertCredit(
    final ConfidentialTokenState state,
    final EncryptedAmount holderCiphertext,
    final EncryptedAmount issuerCiphertext,
    final Optional<EncryptedAmount> auditorCiphertext,
    final Optional<PublicKey> registeredKey
  ) {
    final EncryptedAmount inbox = state.inbox().isPresent() ?
      ciphertextArithmetic.add(state.inbox().get(), holderCiphertext) :
      holderCiphertext;
    final EncryptedAmount issuerEncrypted = state.issuerEncrypted().isPresent() ?
      ciphertextArithmetic.add(state.issuerEncrypted().get(), issuerCiphertext) :
      issuerCiphertext;
    Optional<EncryptedAmount> auditorEncrypted = state.auditorEncrypted();
    if (auditorCiphertext.isPresent()) {
      auditorEncrypted = auditorEncrypted.isPresent() ?
        Optional.of(ciphertextArithmetic.add(auditorEncrypted.get(), auditorCiphertext.get())) :
        auditorCiphertext;
    }
    final ImmutableConfidentialTokenState.Builder builder = ConfidentialTokenState.builder()
      .from(state)
      .inbox(inbox)
      .issuerEncrypted(issuerEncrypted)
      .auditorEncrypted(auditorEncrypted);
    registeredKey.ifPresent(builder::holderKey);
    return builder.build();
  }

  /**
   * Fold a holder's inbox into spending after a MergeInbox and reset the inbox (rippled resets it to a canonical
   * encrypted zero the client cannot reproduce, so it becomes unavailable).
   */
  private ConfidentialTokenState applyMerge(final ConfidentialTokenState state) {
    final EncryptedAmount spending = ciphertextArithmetic.add(
      requireBalance(state.spending(), "spending balance"), requireBalance(state.inbox(), "inbox balance")
    );
    return ConfidentialTokenState.builder()
      .from(state)
      .spending(spending)
      .inbox(Optional.empty())
      .version(state.version().plus(UnsignedInteger.ONE))
      .build();
  }

  /**
   * Reset a holder's balances after a Clawback burns their entire confidential holding (all balances become the
   * canonical encrypted zero the client cannot reproduce, so they become unavailable).
   */
  private ConfidentialTokenState applyClawback(final ConfidentialTokenState state) {
    return ConfidentialTokenState.builder()
      .from(state)
      .spending(Optional.empty())
      .inbox(Optional.empty())
      .issuerEncrypted(Optional.empty())
      .auditorEncrypted(Optional.empty())
      .version(state.version().plus(UnsignedInteger.ONE))
      .build();
  }

  /**
   * Predict a destination's balances after a Send credits it. rippled credits three of the recipient's balances —
   * inbox, issuer-encrypted, and (with an auditor) auditor-encrypted — each re-randomized with the proof challenge (an
   * added encryption of zero under the target key) and keyed on the recipient, issuer, and auditor respectively; so
   * reproduce all three. The recipient's version is left unchanged (rippled bumps only the sender's on a send).
   *
   * @param destState        The destination's current predicted state.
   * @param proof            The built send proof (its first 32 bytes are the re-randomization challenge).
   * @param destPublicKey    The recipient's ElGamal key (the inbox credit is re-randomized under it).
   * @param destCiphertext   The amount encrypted to the recipient.
   * @param issuance         The issuance's confidential keys (issuer mirror is under the issuer key, auditor under the
   *                         auditor key).
   * @param issuerCiphertext The amount encrypted to the issuer.
   * @param auditorCiphertext The amount encrypted to the auditor, if the issuance has one.
   *
   * @return The destination's state after the credits.
   */
  private ConfidentialTokenState applyRecipientCredit(
    final ConfidentialTokenState destState,
    final ConfidentialMptSendProof proof,
    final PublicKey destPublicKey,
    final EncryptedAmount destCiphertext,
    final ConfidentialIssuanceInfo issuance,
    final EncryptedAmount issuerCiphertext,
    final Optional<EncryptedAmount> auditorCiphertext
  ) {
    final byte[] proofBytes = proof.value().toByteArray();
    final BlindingFactor challenge = BlindingFactor.fromBytes(Arrays.copyOfRange(proofBytes, 0, CHALLENGE_SIZE));

    final EncryptedAmount inbox = ciphertextArithmetic.add(
      requireBalance(destState.inbox(), "destination inbox balance"),
      rerandomize(destCiphertext, destPublicKey, challenge)
    );
    final EncryptedAmount issuerEncrypted = ciphertextArithmetic.add(
      requireBalance(destState.issuerEncrypted(), "destination issuer-encrypted balance"),
      rerandomize(issuerCiphertext, issuance.issuerEncryptionKey(), challenge)
    );
    Optional<EncryptedAmount> auditorEncrypted = destState.auditorEncrypted();
    if (auditorCiphertext.isPresent() && issuance.auditorEncryptionKey().isPresent()) {
      auditorEncrypted = Optional.of(ciphertextArithmetic.add(
        requireBalance(auditorEncrypted, "destination auditor-encrypted balance"),
        rerandomize(auditorCiphertext.get(), issuance.auditorEncryptionKey().get(), challenge)
      ));
    }
    return ConfidentialTokenState.builder()
      .from(destState)
      .inbox(inbox)
      .issuerEncrypted(issuerEncrypted)
      .auditorEncrypted(auditorEncrypted)
      .build();
  }

  /**
   * Re-randomize a ciphertext with the send challenge, as rippled does before the homomorphic credit: add an encryption
   * of zero under the target key using the challenge as its blinding factor.
   */
  private EncryptedAmount rerandomize(
    final EncryptedAmount ciphertext, final PublicKey key, final BlindingFactor challenge
  ) {
    return ciphertextArithmetic.add(ciphertext, encryptor.encrypt(UnsignedLong.ZERO, key, challenge));
  }

  // =========================================================================
  // Lookups
  // =========================================================================

  private static UnsignedInteger requireSequence(
    final Map<Address, UnsignedInteger> sequences, final Address account, final String what
  ) {
    final UnsignedInteger sequence = sequences.get(account);
    if (sequence == null) {
      throw new IllegalArgumentException("ConfidentialMptBatchAssembler: missing " + what);
    }
    return sequence;
  }

  private static ConfidentialTokenState requireState(
    final Map<String, ConfidentialTokenState> states, final String key, final String role
  ) {
    final ConfidentialTokenState state = states.get(key);
    if (state == null) {
      throw new IllegalArgumentException("ConfidentialMptBatchAssembler: missing " + role + " state for " + key);
    }
    return state;
  }

  private static ConfidentialIssuanceInfo requireIssuance(
    final ConfidentialBatchRequest request, final MpTokenIssuanceId token
  ) {
    final ConfidentialIssuanceInfo issuance = request.issuances().get(token);
    if (issuance == null) {
      throw new IllegalArgumentException("ConfidentialMptBatchAssembler: missing issuance info for token " + token);
    }
    return issuance;
  }

  /**
   * The total amount converted into confidential form within this Batch, per token. A balance topped up by an in-batch
   * Convert can exceed the issuance's pre-batch outstanding, so this is added to each spend's bounded-decrypt range.
   *
   * @param inners The batch's confidential operations.
   *
   * @return A map from {@link MpTokenIssuanceId} to the total in-batch Convert amount.
   */
  private static Map<MpTokenIssuanceId, UnsignedLong> sumConvertsByToken(final List<ConfidentialBatchInner> inners) {
    final Map<MpTokenIssuanceId, UnsignedLong> totals = new HashMap<>();
    for (final ConfidentialBatchInner inner : inners) {
      if (inner.operation().isPresent() && inner.operation().get() instanceof ConfidentialConvertOp) {
        final ConfidentialConvertOp convert = (ConfidentialConvertOp) inner.operation().get();
        totals.merge(convert.mpTokenIssuanceId(), convert.amount(), UnsignedLong::plus);
      }
    }
    return totals;
  }

  /**
   * The upper bound for the bounded decryption of a spend's balance: the issuance's pre-batch confidential outstanding
   * plus any amount converted for the same token earlier in this Batch.
   *
   * @param issuance      The issuance's confidential parameters.
   * @param convertTotals The per-token in-batch Convert totals from {@link #sumConvertsByToken(List)}.
   * @param token         The token being spent.
   *
   * @return The decrypt upper bound.
   */
  private static UnsignedLong decryptBound(
    final ConfidentialIssuanceInfo issuance,
    final Map<MpTokenIssuanceId, UnsignedLong> convertTotals,
    final MpTokenIssuanceId token
  ) {
    return issuance.outstandingAmount().plus(convertTotals.getOrDefault(token, UnsignedLong.ZERO));
  }

  private static EncryptedAmount requireBalance(final Optional<EncryptedAmount> balance, final String what) {
    return balance.orElseThrow(() -> new IllegalStateException(
      "ConfidentialMptBatchAssembler: cannot read " + what + " — it is unavailable (absent on-ledger, or reset by an " +
        "earlier MergeInbox/Clawback in this Batch, a value the client cannot reproduce). Split these operations " +
        "across separate Batches."
    ));
  }

  private static Map.Entry<String, ConfidentialTokenState> update(
    final String key, final ConfidentialTokenState state
  ) {
    return new AbstractMap.SimpleImmutableEntry<>(key, state);
  }

  /**
   * A built inner transaction plus the predicted-state updates it implies, keyed by {@code (account, token)}.
   */
  private static final class BuiltInner {
    private final Transaction transaction;
    private final List<Map.Entry<String, ConfidentialTokenState>> updates;

    @SafeVarargs
    private BuiltInner(final Transaction transaction, final Map.Entry<String, ConfidentialTokenState>... updates) {
      this.transaction = transaction;
      this.updates = Arrays.asList(updates);
    }
  }
}
