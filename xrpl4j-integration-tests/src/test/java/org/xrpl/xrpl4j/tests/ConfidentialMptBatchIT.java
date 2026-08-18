package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialBatchInner;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialBatchRequest;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialClawbackOp;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptBatchAssembler;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptConvertService;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialSendOp;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialIssuanceInfo;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialTokenState;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.util.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSigner;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptMergeInbox;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * End-to-end integration test for {@link ConfidentialMptBatchAssembler} against a real rippled node: it assembles a
 * Batch of chained Confidential MPT sends (combo 1 — same account, same token), submits it, and asserts the decrypted
 * on-ledger balances. The atomic all-or-nothing Batch is accepted only if every chained proof was built against
 * correctly predicted balance/version state, so a passing balance assertion is a strong end-to-end statement.
 */
@DisabledIf(
  value = "shouldNotRun",
  disabledReason = "ConfidentialMptBatchIT only runs on a local rippled node or Devnet."
)
public class ConfidentialMptBatchIT extends AbstractIT {

  private static ConfidentialMptConvertService convertService;
  private static BlindingFactorGenerator blindingFactorGenerator;
  private static MptAmountEncryptor encryptor;
  private static MptAmountDecryptor decryptor;
  private static ConfidentialMptBatchAssembler assembler;

  // A decrypt upper bound comfortably above any balance used in this test.
  private static final UnsignedLong OUTSTANDING_BOUND = UnsignedLong.valueOf(100_000);
  // A generous outer-Batch fee; overpaying is harmless and avoids fee-estimation fragility on a standalone node.
  private static final XrpCurrencyAmount OUTER_FEE = XrpCurrencyAmount.ofDrops(1_000_000);
  // Confidential transactions carry a base-fee multiplier (kConfidentialFeeMultiplier = 9) in rippled; use a generous
  // flat fee for the standalone convert/merge setup steps rather than tracking the exact multiplier.
  private static final XrpCurrencyAmount CONFIDENTIAL_FEE = XrpCurrencyAmount.ofDrops(100_000);

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null || System.getProperty("useClioTestnet") != null;
  }

  @BeforeAll
  static void initServices() {
    convertService = new ConfidentialMptConvertService();
    blindingFactorGenerator = new JnaBlindingFactorGenerator();
    encryptor = new JnaMptAmountEncryptor();
    decryptor = new JnaMptAmountDecryptor();
    assembler = new ConfidentialMptBatchAssembler();
  }

  @Test
  public void chainedMultiSendInOneBatch() throws Exception {
    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = feeResult.drops().openLedgerFee();

    // Issuer + confidential issuance with issuer/auditor ElGamal keys.
    final KeyPair issuer = createRandomAccountEd25519();
    final MpTokenIssuanceId issuanceId = createConfidentialIssuance(issuer, fee);
    final KeyPair issuerElGamal = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    final KeyPair auditorElGamal = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    registerIssuanceKeys(issuer, issuanceId, issuerElGamal, auditorElGamal, fee);

    // Alice holds 500 spendable confidential MPT; Bob and Carol have only registered their keys (send destinations).
    final ConfidentialHolder alice = fundedHolder(issuer, issuanceId, issuerElGamal, auditorElGamal, 500L, fee);
    final ConfidentialHolder bob = registeredHolder(issuer, issuanceId, issuerElGamal, auditorElGamal, fee);
    final ConfidentialHolder carol = registeredHolder(issuer, issuanceId, issuerElGamal, auditorElGamal, fee);

    assertThat(spendable(alice, issuanceId)).isEqualTo(UnsignedLong.valueOf(500));

    // Assemble: Alice sends 30 to Bob then 20 to Carol (chained on Alice's spending balance) AND pays Carol 5 drops of
    // XRP with a plain Payment — all interleaved in one atomic Batch.
    final AccountInfoResult aliceInfo = accountInfo(alice.account);
    final UnsignedInteger aliceSequence = aliceInfo.accountData().sequence();
    final UnsignedLong carolXrpBefore = accountInfo(carol.account).accountData().balance().value();
    final Map<Address, UnsignedInteger> sequences = new HashMap<>();
    sequences.put(alice.address(), aliceSequence);

    // A plain (non-confidential) inner, caller-shaped as an inner-batch transaction. Its sequence follows the two
    // confidential sends the assembler assigns (aliceSequence + 1 and + 2), so it takes aliceSequence + 3.
    final Payment plainPayment = Payment.builder()
      .account(alice.address())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(aliceSequence.plus(UnsignedInteger.valueOf(3)))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .destination(carol.address())
      .amount(XrpCurrencyAmount.ofDrops(5))
      .build();

    final Map<String, ConfidentialTokenState> states = new HashMap<>();
    states.put(stateKey(alice, issuanceId), stateOf(alice, issuanceId));
    states.put(stateKey(bob, issuanceId), stateOf(bob, issuanceId));
    states.put(stateKey(carol, issuanceId), stateOf(carol, issuanceId));

    final Map<MpTokenIssuanceId, ConfidentialIssuanceInfo> issuances = new HashMap<>();
    issuances.put(issuanceId, ConfidentialIssuanceInfo.builder()
      .issuerEncryptionKey(issuerElGamal.publicKey())
      .auditorEncryptionKey(auditorElGamal.publicKey())
      .outstandingAmount(OUTSTANDING_BOUND)
      .build());

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(ConfidentialSendOp.builder().account(alice.address()).destination(bob.address())
          .amount(UnsignedLong.valueOf(30)).senderKeyPair(alice.elGamal).mpTokenIssuanceId(issuanceId).build()),
        ConfidentialBatchInner.of(ConfidentialSendOp.builder().account(alice.address()).destination(carol.address())
          .amount(UnsignedLong.valueOf(20)).senderKeyPair(alice.elGamal).mpTokenIssuanceId(issuanceId).build()),
        ConfidentialBatchInner.ofPlain(plainPayment))
      .accountSequences(sequences)
      .states(states)
      .issuances(issuances)
      .outerFee(OUTER_FEE)
      .build());

    // Alice owns and signs the outer Batch; all inners are hers, so no BatchSigners are needed.
    final SingleSignedTransaction<Batch> signedBatch = signatureService.sign(alice.account.privateKey(), batch);
    final SubmitResult<Batch> submitResult = xrplClient.submit(signedBatch);
    assertThat(submitResult.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signedBatch.hash(), Batch.class));

    // Both chained proofs validated iff Alice's spendable balance dropped by exactly 30 + 20.
    assertThat(spendable(alice, issuanceId)).isEqualTo(UnsignedLong.valueOf(450));
    // The plain Payment inner applied atomically alongside the confidential sends: Carol's XRP balance rose by 5 drops.
    final UnsignedLong carolXrpAfter = accountInfo(carol.account).accountData().balance().value();
    assertThat(carolXrpAfter.minus(carolXrpBefore)).isEqualTo(UnsignedLong.valueOf(5));
  }

  @Test
  public void clawsBackRecipientAfterSameBatchSend() throws Exception {
    final FeeResult feeResult = xrplClient.fee();
    final XrpCurrencyAmount fee = feeResult.drops().openLedgerFee();

    final KeyPair issuer = createRandomAccountEd25519();
    final MpTokenIssuanceId issuanceId = createConfidentialIssuance(issuer, fee);
    final KeyPair issuerElGamal = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    final KeyPair auditorElGamal = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    registerIssuanceKeys(issuer, issuanceId, issuerElGamal, auditorElGamal, fee);

    // Both hold spendable balances. Alice sends 30 to Bob (crediting Bob's inbox and issuer/auditor mirrors 50 -> 80),
    // then the issuer claws back Bob's entire post-send balance — all in one atomic Batch.
    final ConfidentialHolder alice = fundedHolder(issuer, issuanceId, issuerElGamal, auditorElGamal, 100L, fee);
    final ConfidentialHolder bob = fundedHolder(issuer, issuanceId, issuerElGamal, auditorElGamal, 50L, fee);

    // The issuer owns the outer Batch (it submits the clawback); Alice authorizes her send inner via a BatchSigner.
    final Map<Address, UnsignedInteger> sequences = new HashMap<>();
    sequences.put(issuer.publicKey().deriveAddress(), accountInfo(issuer).accountData().sequence());
    sequences.put(alice.address(), accountInfo(alice.account).accountData().sequence());

    final Map<String, ConfidentialTokenState> states = new HashMap<>();
    states.put(stateKey(alice, issuanceId), stateOf(alice, issuanceId));
    states.put(stateKey(bob, issuanceId), stateOf(bob, issuanceId));

    final Map<MpTokenIssuanceId, ConfidentialIssuanceInfo> issuances = new HashMap<>();
    issuances.put(issuanceId, ConfidentialIssuanceInfo.builder()
      .issuerEncryptionKey(issuerElGamal.publicKey())
      .auditorEncryptionKey(auditorElGamal.publicKey())
      .outstandingAmount(OUTSTANDING_BOUND)
      .build());

    final Batch unsigned = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(issuer.publicKey())
      .addInners(
        ConfidentialBatchInner.of(ConfidentialSendOp.builder().account(alice.address()).destination(bob.address())
          .amount(UnsignedLong.valueOf(30)).senderKeyPair(alice.elGamal).mpTokenIssuanceId(issuanceId).build()),
        ConfidentialBatchInner.of(ConfidentialClawbackOp.builder()
          .account(issuer.publicKey().deriveAddress())
          .holder(bob.address())
          .amount(UnsignedLong.valueOf(80))
          .issuerKeyPair(issuerElGamal)
          .mpTokenIssuanceId(issuanceId)
          .build()))
      .accountSequences(sequences)
      .states(states)
      .issuances(issuances)
      .outerFee(OUTER_FEE)
      .build());

    // Multi-account Batch: Alice signs her inner as a BatchSigner; the issuer signs the outer Batch.
    final Signature aliceInnerSignature =
      signatureService.signInner(alice.account.privateKey(), unsigned, alice.address());
    final Batch withSigners = Batch.builder().from(unsigned)
      .batchSigners(Collections.singletonList(BatchSignerWrapper.of(BatchSigner.builder()
        .account(alice.address())
        .signingPublicKey(alice.account.publicKey())
        .transactionSignature(aliceInnerSignature)
        .build())))
      .build();
    final SingleSignedTransaction<Batch> signed = signatureService.sign(issuer.privateKey(), withSigners);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), Batch.class));

    // The clawback proof binds Bob's post-send issuer-encrypted balance (50 -> 80); the atomic Batch succeeds only if
    // that recipient mirror was predicted correctly. It burns Bob's entire balance while Alice's send still applied.
    assertThat(spendable(bob, issuanceId)).isEqualTo(UnsignedLong.ZERO);
    assertThat(spendable(alice, issuanceId)).isEqualTo(UnsignedLong.valueOf(70));
  }

  // =========================================================================
  // Setup helpers
  // =========================================================================

  /** Fetch an account's validated info, retrying until the account appears (e.g. just after funding). */
  private AccountInfoResult accountInfo(final KeyPair account) {
    return this.scanForResult(() -> this.getValidatedAccountInfo(account.publicKey().deriveAddress()));
  }

  private MpTokenIssuanceId createConfidentialIssuance(final KeyPair issuer, final XrpCurrencyAmount fee)
    throws Exception {
    final AccountInfoResult info = accountInfo(issuer);
    final MpTokenIssuanceCreate create = MpTokenIssuanceCreate.builder()
      .account(issuer.publicKey().deriveAddress())
      .sequence(info.accountData().sequence())
      .fee(fee)
      .signingPublicKey(issuer.publicKey())
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTransfer(true).tfMptCanClawback(true).tfMptCanHoldConfidentialBalance(true).build())
      .build();
    final SingleSignedTransaction<MpTokenIssuanceCreate> signed = signatureService.sign(issuer.privateKey(), create);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo("tesSUCCESS");
    return this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), MpTokenIssuanceCreate.class))
      .metadata().orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId().orElseThrow(() -> new RuntimeException("no issuance id"));
  }

  private void registerIssuanceKeys(
    final KeyPair issuer, final MpTokenIssuanceId issuanceId,
    final KeyPair issuerElGamal, final KeyPair auditorElGamal, final XrpCurrencyAmount fee
  ) throws Exception {
    final AccountInfoResult info = accountInfo(issuer);
    final MpTokenIssuanceSet set = MpTokenIssuanceSet.builder()
      .account(issuer.publicKey().deriveAddress())
      .fee(fee)
      .sequence(info.accountData().sequence())
      .signingPublicKey(issuer.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .issuerEncryptionKey(issuerElGamal.publicKey())
      .auditorEncryptionKey(auditorElGamal.publicKey())
      .build();
    final SingleSignedTransaction<MpTokenIssuanceSet> signed = signatureService.sign(issuer.privateKey(), set);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), MpTokenIssuanceSet.class));
  }

  private void authorize(final KeyPair holder, final MpTokenIssuanceId issuanceId, final XrpCurrencyAmount fee)
    throws Exception {
    final AccountInfoResult info = accountInfo(holder);
    final MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holder.publicKey().deriveAddress())
      .sequence(info.accountData().sequence())
      .fee(fee)
      .signingPublicKey(holder.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .build();
    final SingleSignedTransaction<MpTokenAuthorize> signed = signatureService.sign(holder.privateKey(), authorize);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), MpTokenAuthorize.class));
  }

  /**
   * Build a Convert transaction that moves {@code amount} (possibly zero, to only register a key) of a holder's public
   * MPT into confidential form, standalone-signed and submitted.
   */
  private void convert(
    final KeyPair holder, final KeyPair holderElGamal, final MpTokenIssuanceId issuanceId,
    final KeyPair issuerElGamal, final KeyPair auditorElGamal, final UnsignedLong amount, final XrpCurrencyAmount fee
  ) throws Exception {
    final AccountInfoResult info = accountInfo(holder);
    final ConfidentialMptConvertContext context =
      convertService.generateContext(holder.publicKey().deriveAddress(), info.accountData().sequence(), issuanceId);
    final ConfidentialMptConvertProof proof = convertService.generateProof(holderElGamal, context);
    final BlindingFactor blinding = blindingFactorGenerator.generate();
    final ConfidentialMptConvert tx = ConfidentialMptConvert.builder()
      .account(holder.publicKey().deriveAddress())
      .fee(CONFIDENTIAL_FEE)
      .sequence(info.accountData().sequence())
      .signingPublicKey(holder.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .mptAmount(MpTokenNumericAmount.of(amount))
      .holderEncryptionKey(holderElGamal.publicKey())
      .holderEncryptedAmount(encryptor.encrypt(amount, holderElGamal.publicKey(), blinding))
      .issuerEncryptedAmount(encryptor.encrypt(amount, issuerElGamal.publicKey(), blinding))
      .auditorEncryptedAmount(encryptor.encrypt(amount, auditorElGamal.publicKey(), blinding))
      .blindingFactor(blinding)
      .zkProof(proof)
      .build();
    final SingleSignedTransaction<ConfidentialMptConvert> signed = signatureService.sign(holder.privateKey(), tx);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), ConfidentialMptConvert.class));
  }

  private void mergeInbox(final KeyPair holder, final MpTokenIssuanceId issuanceId, final XrpCurrencyAmount fee)
    throws Exception {
    final AccountInfoResult info = accountInfo(holder);
    final ConfidentialMptMergeInbox merge = ConfidentialMptMergeInbox.builder()
      .account(holder.publicKey().deriveAddress())
      .fee(CONFIDENTIAL_FEE)
      .sequence(info.accountData().sequence())
      .signingPublicKey(holder.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .build();
    final SingleSignedTransaction<ConfidentialMptMergeInbox> signed = signatureService.sign(holder.privateKey(), merge);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), ConfidentialMptMergeInbox.class));
  }

  /** A holder that has authorized, received {@code amount} public MPT, converted it, and merged it — fully spendable. */
  private ConfidentialHolder fundedHolder(
    final KeyPair issuer, final MpTokenIssuanceId issuanceId, final KeyPair issuerElGamal,
    final KeyPair auditorElGamal, final long amount, final XrpCurrencyAmount fee
  ) throws Exception {
    final KeyPair holder = createRandomAccountEd25519();
    final KeyPair holderElGamal = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    authorize(holder, issuanceId, fee);

    final AccountInfoResult issuerInfo = accountInfo(issuer);
    final Payment payment = Payment.builder()
      .account(issuer.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerInfo.accountData().sequence())
      .destination(holder.publicKey().deriveAddress())
      .amount(MptCurrencyAmount.builder().mptIssuanceId(issuanceId).value(Long.toString(amount)).build())
      .signingPublicKey(issuer.publicKey())
      .build();
    final SingleSignedTransaction<Payment> signedPayment = signatureService.sign(issuer.privateKey(), payment);
    assertThat(xrplClient.submit(signedPayment).engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signedPayment.hash(), Payment.class));

    convert(holder, holderElGamal, issuanceId, issuerElGamal, auditorElGamal, UnsignedLong.valueOf(amount), fee);
    mergeInbox(holder, issuanceId, fee);
    return new ConfidentialHolder(holder, holderElGamal);
  }

  /** A holder that has authorized and registered its ElGamal key (via a zero-amount convert) — a send destination. */
  private ConfidentialHolder registeredHolder(
    final KeyPair issuer, final MpTokenIssuanceId issuanceId, final KeyPair issuerElGamal,
    final KeyPair auditorElGamal, final XrpCurrencyAmount fee
  ) throws Exception {
    final KeyPair holder = createRandomAccountEd25519();
    final KeyPair holderElGamal = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    authorize(holder, issuanceId, fee);
    convert(holder, holderElGamal, issuanceId, issuerElGamal, auditorElGamal, UnsignedLong.ZERO, fee);
    return new ConfidentialHolder(holder, holderElGamal);
  }

  // =========================================================================
  // State + balance helpers
  // =========================================================================

  private String stateKey(final ConfidentialHolder holder, final MpTokenIssuanceId issuanceId) {
    return ConfidentialBatchRequest.stateKey(holder.address(), issuanceId);
  }

  private ConfidentialTokenState stateOf(final ConfidentialHolder holder, final MpTokenIssuanceId issuanceId)
    throws Exception {
    final MpTokenObject mpToken = getMpToken(holder.account, issuanceId);
    return ConfidentialTokenState.builder()
      .spending(mpToken.confidentialBalanceSpending())
      .inbox(mpToken.confidentialBalanceInbox())
      .issuerEncrypted(mpToken.issuerEncryptedBalance())
      .auditorEncrypted(mpToken.auditorEncryptedBalance())
      .version(mpToken.confidentialBalanceVersion())
      .holderKey(mpToken.holderEncryptionKey())
      .build();
  }

  private UnsignedLong spendable(final ConfidentialHolder holder, final MpTokenIssuanceId issuanceId) throws Exception {
    final EncryptedAmount spending = getMpToken(holder.account, issuanceId).confidentialBalanceSpending()
      .orElseThrow(() -> new RuntimeException("no spending balance"));
    return decryptor.decrypt(spending, holder.elGamal.privateKey(), UnsignedLong.ZERO, OUTSTANDING_BOUND);
  }

  private MpTokenObject getMpToken(final KeyPair holder, final MpTokenIssuanceId issuanceId) throws Exception {
    return xrplClient.ledgerEntry(LedgerEntryRequestParams.mpToken(
      org.xrpl.xrpl4j.model.client.ledger.MpTokenLedgerEntryParams.builder()
        .account(holder.publicKey().deriveAddress())
        .mpTokenIssuanceId(issuanceId)
        .build(),
      org.xrpl.xrpl4j.model.client.common.LedgerSpecifier.VALIDATED
    )).node();
  }

  /** A funded/registered confidential holder: its account keypair and its ElGamal keypair. */
  private static final class ConfidentialHolder {
    private final KeyPair account;
    private final KeyPair elGamal;

    private ConfidentialHolder(final KeyPair account, final KeyPair elGamal) {
      this.account = account;
      this.elGamal = elGamal;
    }

    private Address address() {
      return account.publicKey().deriveAddress();
    }
  }
}
