package org.xrpl.xrpl4j.crypto.confidential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialIssuanceInfo;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialTokenState;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
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
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptClawback;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Unit tests for {@link ConfidentialMptBatchAssembler} exercising the orchestration (sequence pinning, inner shaping,
 * bounds, per-op dispatch, predicted-state threading, and reset handling) with mocked crypto collaborators. Proof
 * correctness is covered by the integration test against a real rippled node.
 */
class ConfidentialMptBatchAssemblerTest {

  private static final KeyPair ALICE = Seed.secp256k1SeedFromPassphrase(Passphrase.of("alice")).deriveKeyPair();
  private static final KeyPair BOB = Seed.secp256k1SeedFromPassphrase(Passphrase.of("bob")).deriveKeyPair();
  private static final KeyPair CAROL = Seed.secp256k1SeedFromPassphrase(Passphrase.of("carol")).deriveKeyPair();
  private static final KeyPair ALICE_EG =
    Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("a-eg")).deriveKeyPair();
  private static final KeyPair BOB_EG = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("b-eg")).deriveKeyPair();
  private static final KeyPair ISSUER_EG =
    Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("i-eg")).deriveKeyPair();

  private static final Address ALICE_ADDR = ALICE.publicKey().deriveAddress();
  private static final Address BOB_ADDR = BOB.publicKey().deriveAddress();
  private static final Address CAROL_ADDR = CAROL.publicKey().deriveAddress();
  private static final MpTokenIssuanceId TOKEN = MpTokenIssuanceId.of(Strings.repeat("0", 48));
  private static final XrpCurrencyAmount OUTER_FEE = XrpCurrencyAmount.ofDrops(1000);

  private static final BlindingFactor DUMMY_BF = BlindingFactor.of(Strings.repeat("11", 32));
  private static final EncryptedAmount DUMMY_CT = EncryptedAmount.of(Strings.repeat("02", 66));
  private static final Commitment DUMMY_COMMITMENT = Commitment.of(Strings.repeat("03", 33));
  private static final ConfidentialMptSendContext DUMMY_SEND_CTX =
    ConfidentialMptSendContext.fromHex(Strings.repeat("AB", 32));
  private static final ConfidentialMptSendProof DUMMY_SEND_PROOF =
    ConfidentialMptSendProof.fromHex(Strings.repeat("CD", 946));
  private static final ConfidentialMptConvertContext DUMMY_CONVERT_CTX =
    ConfidentialMptConvertContext.fromHex(Strings.repeat("AB", 32));
  private static final ConfidentialMptConvertProof DUMMY_CONVERT_PROOF =
    ConfidentialMptConvertProof.fromHex(Strings.repeat("CD", 64));
  private static final ConfidentialMptConvertBackContext DUMMY_CONVERT_BACK_CTX =
    ConfidentialMptConvertBackContext.fromHex(Strings.repeat("AB", 32));
  private static final ConfidentialMptConvertBackProof DUMMY_CONVERT_BACK_PROOF =
    ConfidentialMptConvertBackProof.fromHex(Strings.repeat("CD", 816));
  private static final ConfidentialMptClawbackContext DUMMY_CLAWBACK_CTX =
    ConfidentialMptClawbackContext.fromHex(Strings.repeat("AB", 32));
  private static final ConfidentialMptClawbackProof DUMMY_CLAWBACK_PROOF =
    ConfidentialMptClawbackProof.fromHex(Strings.repeat("CD", 64));
  private static final PedersenProofParams DUMMY_PARAMS = PedersenProofParams.builder()
    .pedersenCommitment(UnsignedByteArray.fromHex(Strings.repeat("02", 33)))
    .amount(UnsignedLong.valueOf(70))
    .encryptedAmount(DUMMY_CT)
    .blindingFactor(DUMMY_BF)
    .build();

  private ConfidentialMptConvertService convertService;
  private ConfidentialMptSendService sendService;
  private ConfidentialMptConvertBackService convertBackService;
  private ConfidentialMptClawbackService clawbackService;
  private MptAmountEncryptor encryptor;
  private MptAmountDecryptor decryptor;
  private BlindingFactorGenerator blindingFactorGenerator;
  private ConfidentialCiphertextArithmetic ciphertextArithmetic;
  private ConfidentialMptBatchAssembler assembler;

  @BeforeEach
  void setUp() {
    convertService = mock(ConfidentialMptConvertService.class);
    sendService = mock(ConfidentialMptSendService.class);
    convertBackService = mock(ConfidentialMptConvertBackService.class);
    clawbackService = mock(ConfidentialMptClawbackService.class);
    encryptor = mock(MptAmountEncryptor.class);
    decryptor = mock(MptAmountDecryptor.class);
    blindingFactorGenerator = mock(BlindingFactorGenerator.class);
    ciphertextArithmetic = mock(ConfidentialCiphertextArithmetic.class);
    assembler = new ConfidentialMptBatchAssembler(
      convertService, sendService, convertBackService, clawbackService, encryptor, decryptor, blindingFactorGenerator,
      ciphertextArithmetic
    );
  }

  private void stubCommonCrypto() {
    when(blindingFactorGenerator.generate()).thenReturn(DUMMY_BF);
    when(encryptor.encrypt(any(), any(), any())).thenReturn(DUMMY_CT);
    when(decryptor.decrypt(any(), any(), any(), any())).thenReturn(UnsignedLong.valueOf(70));
    when(ciphertextArithmetic.add(any(), any())).thenReturn(DUMMY_CT);
    when(ciphertextArithmetic.subtract(any(), any())).thenReturn(DUMMY_CT);
  }

  private void stubSendCrypto() {
    stubCommonCrypto();
    when(sendService.generateContext(any(), any(), any(), any(), any())).thenReturn(DUMMY_SEND_CTX);
    when(sendService.generatePedersenCommitment(any(), any())).thenReturn(DUMMY_COMMITMENT);
    when(sendService.generatePedersenProofParams(any(), any(), any())).thenReturn(DUMMY_PARAMS);
    when(sendService.generateProof(any(), any(), any(), any(), any(), any(), any())).thenReturn(DUMMY_SEND_PROOF);
  }

  private void stubConvertCrypto() {
    stubCommonCrypto();
    when(convertService.generateContext(any(), any(), any())).thenReturn(DUMMY_CONVERT_CTX);
    when(convertService.generateProof(any(), any())).thenReturn(DUMMY_CONVERT_PROOF);
  }

  private void stubConvertBackCrypto() {
    stubCommonCrypto();
    when(convertBackService.generateContext(any(), any(), any(), any())).thenReturn(DUMMY_CONVERT_BACK_CTX);
    when(convertBackService.generatePedersenProofParams(any(), any(), any())).thenReturn(DUMMY_PARAMS);
    when(convertBackService.generateProof(any(), any(), any(), any())).thenReturn(DUMMY_CONVERT_BACK_PROOF);
  }

  private void stubClawbackCrypto() {
    when(clawbackService.generateContext(any(), any(), any(), any())).thenReturn(DUMMY_CLAWBACK_CTX);
    when(clawbackService.generateProof(any(), any(), any(), any(), any())).thenReturn(DUMMY_CLAWBACK_PROOF);
  }

  private ConfidentialConvertOp convert(final Address account) {
    return ConfidentialConvertOp.builder()
      .account(account).amount(UnsignedLong.valueOf(10)).holderKeyPair(ALICE_EG).mpTokenIssuanceId(TOKEN).build();
  }

  private ConfidentialConvertBackOp convertBack(final Address account) {
    return ConfidentialConvertBackOp.builder()
      .account(account).amount(UnsignedLong.valueOf(20)).holderKeyPair(ALICE_EG).mpTokenIssuanceId(TOKEN).build();
  }

  private ConfidentialClawbackOp clawback(final Address issuer, final Address holder) {
    return ConfidentialClawbackOp.builder()
      .account(issuer).holder(holder).amount(UnsignedLong.valueOf(40)).issuerKeyPair(ISSUER_EG)
      .mpTokenIssuanceId(TOKEN).build();
  }

  /** A holder state with a spendable + issuer-encrypted balance, enough for a debit or clawback to read. */
  private ConfidentialTokenState fundedState() {
    return ConfidentialTokenState.builder()
      .spending(DUMMY_CT).issuerEncrypted(DUMMY_CT).version(UnsignedInteger.valueOf(3)).build();
  }

  private Map<MpTokenIssuanceId, ConfidentialIssuanceInfo> defaultIssuances() {
    Map<MpTokenIssuanceId, ConfidentialIssuanceInfo> issuances = new HashMap<>();
    issuances.put(TOKEN, ConfidentialIssuanceInfo.builder()
      .issuerEncryptionKey(ISSUER_EG.publicKey()).outstandingAmount(UnsignedLong.valueOf(500)).build());
    return issuances;
  }

  private ImmutableConfidentialBatchRequest.Builder baseRequest(final List<ConfidentialMptOp> inners) {
    final Map<Address, UnsignedInteger> sequences = new HashMap<>();
    sequences.put(ALICE_ADDR, UnsignedInteger.valueOf(10));
    sequences.put(BOB_ADDR, UnsignedInteger.valueOf(20));
    return ConfidentialBatchRequest.builder()
      .accountPublicKey(ALICE.publicKey())
      .inners(inners.stream().map(ConfidentialBatchInner::of).collect(Collectors.toList()))
      .accountSequences(sequences)
      .issuances(defaultIssuances())
      .outerFee(OUTER_FEE);
  }

  @Test
  void rejectsFewerThanTwoInners() {
    // The count check fires before any inner is built.
    assertThatThrownBy(() -> assembler.assemble(baseRequest(Arrays.asList(convert(ALICE_ADDR))).build()))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 2 and 8");
  }

  @Test
  void rejectsMoreThanEightInners() {
    final List<ConfidentialMptOp> nine = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      nine.add(convert(ALICE_ADDR));
    }
    assertThatThrownBy(() -> assembler.assemble(baseRequest(nine).build()))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 2 and 8");
  }

  @Test
  void pinsOuterAccountSequencesAndShapesInners() {
    stubConvertCrypto();
    // Two converts by the outer account: outer Batch consumes seq 10, so inners get 11 and 12.
    Batch batch = assembler.assemble(baseRequest(Arrays.asList(convert(ALICE_ADDR), convert(ALICE_ADDR))).build());

    assertThat(batch.account()).isEqualTo(ALICE_ADDR);
    assertThat(batch.sequence()).isEqualTo(UnsignedInteger.valueOf(10));
    assertThat(batch.fee()).isEqualTo(OUTER_FEE);
    assertThat(batch.signingPublicKey()).isEqualTo(ALICE.publicKey());
    assertThat(batch.rawTransactions()).hasSize(2);

    ConfidentialMptConvert first = (ConfidentialMptConvert) batch.rawTransactions().get(0).rawTransaction();
    ConfidentialMptConvert second = (ConfidentialMptConvert) batch.rawTransactions().get(1).rawTransaction();
    assertThat(first.sequence()).isEqualTo(UnsignedInteger.valueOf(11));
    assertThat(second.sequence()).isEqualTo(UnsignedInteger.valueOf(12));
    for (ConfidentialMptConvert inner : Arrays.asList(first, second)) {
      assertThat(inner.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(0));
      assertThat(inner.flags().tfInnerBatchTxn()).isTrue();
      assertThat(inner.signingPublicKey()).isEqualTo(PublicKey.MULTI_SIGN_PUBLIC_KEY);
      assertThat(inner.mpTokenIssuanceId()).isEqualTo(TOKEN);
    }
  }

  @Test
  void pinsPerAccountSequencesAcrossAccounts() {
    stubConvertCrypto();
    // Alice is the outer account (inner starts at 11); Bob's inner starts at his own current sequence 20.
    Batch batch = assembler.assemble(baseRequest(Arrays.asList(convert(ALICE_ADDR), convert(BOB_ADDR))).build());

    assertThat(batch.rawTransactions().get(0).rawTransaction().sequence()).isEqualTo(UnsignedInteger.valueOf(11));
    assertThat(batch.rawTransactions().get(1).rawTransaction().sequence()).isEqualTo(UnsignedInteger.valueOf(20));
  }

  @Test
  void throwsOnMissingSequence() {
    stubConvertCrypto();
    // Bob has an inner but no sequence entry; the first inner (Alice) builds, the second trips the missing sequence.
    Map<Address, UnsignedInteger> sequences = new HashMap<>();
    sequences.put(ALICE_ADDR, UnsignedInteger.valueOf(10));
    assertThatThrownBy(() -> assembler.assemble(
      ConfidentialBatchRequest.builder()
        .accountPublicKey(ALICE.publicKey())
        .inners(Arrays.asList(
          ConfidentialBatchInner.of(convert(ALICE_ADDR)), ConfidentialBatchInner.of(convert(BOB_ADDR))))
        .accountSequences(sequences)
        .issuances(defaultIssuances())
        .outerFee(OUTER_FEE)
        .build()
    )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("sequence for account");
  }

  @Test
  void throwsWhenSenderStateMissing() {
    ConfidentialSendOp send = ConfidentialSendOp.builder()
      .account(ALICE_ADDR).destination(BOB_ADDR).amount(UnsignedLong.valueOf(30))
      .senderKeyPair(ALICE_EG).mpTokenIssuanceId(TOKEN).build();
    // No token states provided: building the send fails looking up the sender's state.
    assertThatThrownBy(() -> assembler.assemble(baseRequest(Arrays.asList(send, convert(BOB_ADDR))).build()))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("missing sender state");
  }

  @Test
  void chainsPredictedStateForSameAccountSameToken() {
    // Combo 1: Alice sends to Bob, then to Carol, both from the same (account, token). The second send's proof must
    // bind the balance/version the first send leaves behind — so the assembler advances the predicted version and
    // subtracts the first debit homomorphically before building the second send.
    stubSendCrypto();

    ConfidentialSendOp toBob = ConfidentialSendOp.builder()
      .account(ALICE_ADDR).destination(BOB_ADDR).amount(UnsignedLong.valueOf(30))
      .senderKeyPair(ALICE_EG).mpTokenIssuanceId(TOKEN).build();
    ConfidentialSendOp toCarol = ConfidentialSendOp.builder()
      .account(ALICE_ADDR).destination(CAROL_ADDR).amount(UnsignedLong.valueOf(20))
      .senderKeyPair(ALICE_EG).mpTokenIssuanceId(TOKEN).build();

    ConfidentialTokenState senderState = ConfidentialTokenState.builder()
      .spending(DUMMY_CT).issuerEncrypted(DUMMY_CT).version(UnsignedInteger.valueOf(5)).build();
    ConfidentialTokenState bobState = ConfidentialTokenState.builder()
      .holderKey(BOB_EG.publicKey()).inbox(DUMMY_CT).issuerEncrypted(DUMMY_CT).build();
    ConfidentialTokenState carolState = ConfidentialTokenState.builder()
      .holderKey(BOB_EG.publicKey()).inbox(DUMMY_CT).issuerEncrypted(DUMMY_CT).build();

    Map<String, ConfidentialTokenState> states = new HashMap<>();
    states.put(ConfidentialBatchRequest.stateKey(ALICE_ADDR, TOKEN), senderState);
    states.put(ConfidentialBatchRequest.stateKey(BOB_ADDR, TOKEN), bobState);
    states.put(ConfidentialBatchRequest.stateKey(CAROL_ADDR, TOKEN), carolState);

    Batch batch = assembler.assemble(baseRequest(Arrays.asList(toBob, toCarol)).states(states).build());

    assertThat(batch.rawTransactions()).hasSize(2);
    ConfidentialMptSend firstSend = (ConfidentialMptSend) batch.rawTransactions().get(0).rawTransaction();
    // Shaping + proof binding on the first send.
    assertThat(firstSend.sequence()).isEqualTo(UnsignedInteger.valueOf(11));
    assertThat(firstSend.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(0));
    assertThat(firstSend.flags().tfInnerBatchTxn()).isTrue();
    assertThat(firstSend.signingPublicKey()).isEqualTo(PublicKey.MULTI_SIGN_PUBLIC_KEY);
    assertThat(firstSend.destination()).isEqualTo(BOB_ADDR);
    assertThat(firstSend.zkProof()).isEqualTo(DUMMY_SEND_PROOF);
    assertThat(firstSend.auditorEncryptedAmount()).isEmpty(); // issuance registered no auditor key
    // The two sends get consecutive sequences 11, 12 (Alice is the outer account, so inners start at 11).
    assertThat(batch.rawTransactions().get(1).rawTransaction().sequence()).isEqualTo(UnsignedInteger.valueOf(12));

    // The predicted version advances: the first send binds version 5, the second binds 6.
    ArgumentCaptor<UnsignedInteger> versions = ArgumentCaptor.forClass(UnsignedInteger.class);
    verify(sendService, atLeastOnce())
      .generateContext(eq(ALICE_ADDR), any(), eq(TOKEN), any(), versions.capture());
    assertThat(versions.getAllValues()).containsExactly(UnsignedInteger.valueOf(5), UnsignedInteger.valueOf(6));
    // The balance decrypt is bounded by the issuance's outstanding amount.
    verify(decryptor, atLeastOnce()).decrypt(any(), any(), eq(UnsignedLong.ZERO), eq(UnsignedLong.valueOf(500)));
    // The sender's spending balance was threaded homomorphically (debited) between the two sends.
    verify(ciphertextArithmetic, atLeastOnce()).subtract(any(), any());
  }

  @Test
  void buildsConvertBackInnerAndChainsVersion() {
    stubConvertBackCrypto();
    Map<String, ConfidentialTokenState> states = new HashMap<>();
    states.put(ConfidentialBatchRequest.stateKey(ALICE_ADDR, TOKEN), fundedState());

    Batch batch = assembler.assemble(
      baseRequest(Arrays.asList(convertBack(ALICE_ADDR), convertBack(ALICE_ADDR))).states(states).build()
    );

    ConfidentialMptConvertBack inner = (ConfidentialMptConvertBack) batch.rawTransactions().get(0).rawTransaction();
    assertThat(inner.sequence()).isEqualTo(UnsignedInteger.valueOf(11));
    assertThat(inner.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(0));
    assertThat(inner.flags().tfInnerBatchTxn()).isTrue();
    assertThat(inner.signingPublicKey()).isEqualTo(PublicKey.MULTI_SIGN_PUBLIC_KEY);
    assertThat(inner.zkProof()).isEqualTo(DUMMY_CONVERT_BACK_PROOF);

    // The second convert-back binds the version the first left behind (3 -> 4), threaded via the debit.
    ArgumentCaptor<UnsignedInteger> versions = ArgumentCaptor.forClass(UnsignedInteger.class);
    verify(convertBackService, atLeastOnce()).generateContext(eq(ALICE_ADDR), any(), eq(TOKEN), versions.capture());
    assertThat(versions.getAllValues()).containsExactly(UnsignedInteger.valueOf(3), UnsignedInteger.valueOf(4));
    verify(ciphertextArithmetic, atLeastOnce()).subtract(any(), any());
  }

  @Test
  void buildsClawbackInner() {
    stubClawbackCrypto();
    // Alice (as the issuer submitter of the outer Batch) claws back from Bob then Carol — distinct holders.
    Map<String, ConfidentialTokenState> states = new HashMap<>();
    states.put(ConfidentialBatchRequest.stateKey(BOB_ADDR, TOKEN), fundedState());
    states.put(ConfidentialBatchRequest.stateKey(CAROL_ADDR, TOKEN), fundedState());

    Batch batch = assembler.assemble(baseRequest(
      Arrays.asList(clawback(ALICE_ADDR, BOB_ADDR), clawback(ALICE_ADDR, CAROL_ADDR))
    ).states(states).build());

    ConfidentialMptClawback inner = (ConfidentialMptClawback) batch.rawTransactions().get(0).rawTransaction();
    assertThat(inner.sequence()).isEqualTo(UnsignedInteger.valueOf(11));
    assertThat(inner.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(0));
    assertThat(inner.flags().tfInnerBatchTxn()).isTrue();
    assertThat(inner.signingPublicKey()).isEqualTo(PublicKey.MULTI_SIGN_PUBLIC_KEY);
    assertThat(inner.holder()).isEqualTo(BOB_ADDR);
    assertThat(inner.zkProof()).isEqualTo(DUMMY_CLAWBACK_PROOF);
  }

  @Test
  void throwsWhenReadingBalanceResetByEarlierMerge() {
    // Two merges on the same (account, token): the first resets the inbox to an uncomputable value; the second cannot
    // read it and must fail loudly rather than emit a doomed proof.
    stubCommonCrypto();
    ConfidentialMergeInboxOp merge1 =
      ConfidentialMergeInboxOp.builder().account(ALICE_ADDR).mpTokenIssuanceId(TOKEN).build();
    ConfidentialMergeInboxOp merge2 =
      ConfidentialMergeInboxOp.builder().account(ALICE_ADDR).mpTokenIssuanceId(TOKEN).build();

    ConfidentialTokenState aliceState = ConfidentialTokenState.builder()
      .spending(DUMMY_CT).inbox(DUMMY_CT).version(UnsignedInteger.ZERO).build();
    Map<String, ConfidentialTokenState> states = new HashMap<>();
    states.put(ConfidentialBatchRequest.stateKey(ALICE_ADDR, TOKEN), aliceState);

    assertThatThrownBy(() -> assembler.assemble(baseRequest(Arrays.asList(merge1, merge2)).states(states).build()))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("inbox balance");
  }

  @Test
  void passesThroughPlainInnerAndSequencesAroundIt() {
    // A pre-built, already-shaped plain Payment is interleaved with a confidential Convert (both by the outer account).
    // The plain inner is passed through untouched; the confidential inner after it is numbered past the plain's
    // sequence (11 -> 12).
    stubConvertCrypto();

    Payment plain = Payment.builder()
      .account(ALICE_ADDR)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(UnsignedInteger.valueOf(11))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .destination(BOB_ADDR)
      .amount(XrpCurrencyAmount.ofDrops(1))
      .build();

    Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(ALICE.publicKey())
      .inners(Arrays.asList(ConfidentialBatchInner.ofPlain(plain), ConfidentialBatchInner.of(convert(ALICE_ADDR))))
      .accountSequences(Collections.singletonMap(ALICE_ADDR, UnsignedInteger.valueOf(10)))
      .issuances(defaultIssuances())
      .outerFee(OUTER_FEE)
      .build());

    assertThat(batch.rawTransactions()).hasSize(2);
    assertThat(batch.rawTransactions().get(0).rawTransaction()).isInstanceOf(Payment.class);
    assertThat(batch.rawTransactions().get(0).rawTransaction().sequence()).isEqualTo(UnsignedInteger.valueOf(11));
    // The Convert follows the plain inner (which consumed sequence 11), so it is numbered 12.
    ConfidentialMptConvert convertInner = (ConfidentialMptConvert) batch.rawTransactions().get(1).rawTransaction();
    assertThat(convertInner.sequence()).isEqualTo(UnsignedInteger.valueOf(12));
  }
}
