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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialBatchInner;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialBatchRequest;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialClawbackOp;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialConvertBackOp;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialConvertOp;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMergeInboxOp;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptBatchAssembler;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialSendOp;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialIssuanceInfo;
import org.xrpl.xrpl4j.crypto.confidential.model.ConfidentialTokenState;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSigner;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Integration tests for {@link ConfidentialMptBatchAssembler} against a real rippled node, covering atomic XLS-56
 * Batches of Confidential MPT (XLS-0096) inners. Non-batch coverage lives in {@link ConfidentialMptIT}.
 */
@DisabledIf(
  value = "shouldNotRun",
  disabledReason = "ConfidentialMptBatchIT only runs on a local rippled node or Devnet."
)
public class ConfidentialMptBatchIT extends AbstractConfidentialMptIT {

  /** A generous outer-Batch fee; overpaying is harmless and avoids fee-estimation fragility on a standalone node. */
  private static final XrpCurrencyAmount OUTER_FEE = XrpCurrencyAmount.ofDrops(1_000_000);

  private final ConfidentialMptBatchAssembler assembler = new ConfidentialMptBatchAssembler();

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null || System.getProperty("useClioTestnet") != null;
  }

  // ===========================================================================
  // Batch composition scenarios
  // ===========================================================================

  @Test
  void chainsTwoSendsOnOneBalance() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    final ConfidentialHolder bob = registerHolderKey(issuance);
    final ConfidentialHolder carol = registerHolderKey(issuance);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, bob, 30, issuance)),
        ConfidentialBatchInner.of(sendOp(alice, carol, 20, issuance)))
      .accountSequences(sequences(alice))
      .states(states(pair(alice, issuance), pair(bob, issuance), pair(carol, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account);

    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(50));
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.valueOf(30));
    assertThat(inboxBalance(carol, issuance)).isEqualTo(UnsignedLong.valueOf(20));
  }

  @Test
  void sendsOnTwoTokensFromOneAccount() throws Exception {
    final ConfidentialIssuance tokenA = createConfidentialIssuance();
    final ConfidentialIssuance tokenB = createConfidentialIssuance();
    final ConfidentialHolder alice = new ConfidentialHolder(createRandomAccountEd25519(), elGamalKeyPair());
    fundHolderOnToken(alice, tokenA, 100);
    fundHolderOnToken(alice, tokenB, 100);
    final ConfidentialHolder recipientA = registerHolderKey(tokenA);
    final ConfidentialHolder recipientB = registerHolderKey(tokenB);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, recipientA, 30, tokenA)),
        ConfidentialBatchInner.of(sendOp(alice, recipientB, 40, tokenB)))
      .accountSequences(sequences(alice))
      .states(states(pair(alice, tokenA), pair(alice, tokenB), pair(recipientA, tokenA), pair(recipientB, tokenB)))
      .issuances(issuances(tokenA, tokenB))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account);

    assertThat(spendable(alice, tokenA)).isEqualTo(UnsignedLong.valueOf(70));
    assertThat(spendable(alice, tokenB)).isEqualTo(UnsignedLong.valueOf(60));
    assertThat(inboxBalance(recipientA, tokenA)).isEqualTo(UnsignedLong.valueOf(30));
    assertThat(inboxBalance(recipientB, tokenB)).isEqualTo(UnsignedLong.valueOf(40));
  }

  @Test
  void sendsOnOneTokenFromTwoAccounts() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    final ConfidentialHolder bob = holderWithBalance(issuance, 100);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, bob, 10, issuance)),
        ConfidentialBatchInner.of(sendOp(bob, alice, 20, issuance)))
      .accountSequences(sequences(alice, bob))
      .states(states(pair(alice, issuance), pair(bob, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account, bob);

    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(90));
    assertThat(spendable(bob, issuance)).isEqualTo(UnsignedLong.valueOf(80));
    // Each account also received the other's send into its inbox.
    assertThat(inboxBalance(alice, issuance)).isEqualTo(UnsignedLong.valueOf(20));
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.valueOf(10));
  }

  @Test
  void sendsOnTwoTokensFromTwoAccounts() throws Exception {
    final ConfidentialIssuance tokenA = createConfidentialIssuance();
    final ConfidentialIssuance tokenB = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(tokenA, 100);
    final ConfidentialHolder carol = registerHolderKey(tokenA);
    final ConfidentialHolder bob = holderWithBalance(tokenB, 100);
    final ConfidentialHolder dave = registerHolderKey(tokenB);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, carol, 30, tokenA)),
        ConfidentialBatchInner.of(sendOp(bob, dave, 40, tokenB)))
      .accountSequences(sequences(alice, bob))
      .states(states(pair(alice, tokenA), pair(carol, tokenA), pair(bob, tokenB), pair(dave, tokenB)))
      .issuances(issuances(tokenA, tokenB))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account, bob);

    assertThat(spendable(alice, tokenA)).isEqualTo(UnsignedLong.valueOf(70));
    assertThat(spendable(bob, tokenB)).isEqualTo(UnsignedLong.valueOf(60));
    assertThat(inboxBalance(carol, tokenA)).isEqualTo(UnsignedLong.valueOf(30));
    assertThat(inboxBalance(dave, tokenB)).isEqualTo(UnsignedLong.valueOf(40));
  }

  @Test
  void recipientRespendsReceivedFundsInSameBatch() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    // Bob needs an established balance for MergeInbox; a small nonzero start (a 0-MPT Payment is temBAD_AMOUNT).
    final ConfidentialHolder bob = holderWithBalance(issuance, 5);
    final ConfidentialHolder carol = registerHolderKey(issuance);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, bob, 40, issuance)),
        ConfidentialBatchInner.of(ConfidentialMergeInboxOp.builder()
          .account(bob.address()).mpTokenIssuanceId(issuance.issuanceId).build()),
        ConfidentialBatchInner.of(sendOp(bob, carol, 15, issuance)))
      .accountSequences(sequences(alice, bob))
      .states(states(pair(alice, issuance), pair(bob, issuance), pair(carol, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account, bob);

    // Bob spent 15 from (his 5 + the 40 he received and merged) in the same batch.
    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(60));
    assertThat(spendable(bob, issuance)).isEqualTo(UnsignedLong.valueOf(30));
    // Bob's inbox was folded into his spending balance by the mid-batch merge; carol holds the 15 he forwarded.
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.ZERO);
    assertThat(inboxBalance(carol, issuance)).isEqualTo(UnsignedLong.valueOf(15));
  }

  @Test
  void clawsBackSenderAfterSameBatchSend() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    final ConfidentialHolder bob = registerHolderKey(issuance);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      // The issuer owns the outer Batch; alice authorizes her send inner.
      .accountPublicKey(issuance.issuer.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, bob, 30, issuance)),
        ConfidentialBatchInner.of(clawbackOp(issuance, alice, 70)))
      .accountSequences(sequences(issuance.issuer, alice))
      .states(states(pair(alice, issuance), pair(bob, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, issuance.issuer, alice);

    // Alice sent 30 (balance -> 70), then the issuer clawed back her full post-send remainder.
    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.ZERO);
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.valueOf(30));
  }

  @Test
  void clawsBackRecipientAfterSameBatchSend() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    final ConfidentialHolder bob = holderWithBalance(issuance, 50);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(issuance.issuer.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, bob, 30, issuance)),
        ConfidentialBatchInner.of(clawbackOp(issuance, bob, 80)))
      .accountSequences(sequences(issuance.issuer, alice))
      .states(states(pair(alice, issuance), pair(bob, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, issuance.issuer, alice);

    // The send credits bob's issuer-encrypted mirror (50 -> 80); the clawback burns that predicted total,
    // clearing both his spending balance and the 30 that landed in his inbox.
    assertThat(spendable(bob, issuance)).isEqualTo(UnsignedLong.ZERO);
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.ZERO);
    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(70));
  }

  @Test
  void chainsSendAndConvertBackAsTwoDebits() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    final ConfidentialHolder bob = registerHolderKey(issuance);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, bob, 30, issuance)),
        ConfidentialBatchInner.of(ConfidentialConvertBackOp.builder()
          .account(alice.address()).amount(UnsignedLong.valueOf(20))
          .holderKeyPair(alice.elGamal).mpTokenIssuanceId(issuance.issuanceId).build()))
      .accountSequences(sequences(alice))
      .states(states(pair(alice, issuance), pair(bob, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account);

    // Two debits on one balance: 100 - 30 (send) - 20 (convert-back) = 50; the revealed 20 lands in public MPT.
    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(50));
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.valueOf(30));
    assertThat(getMpToken(alice.address(), issuance.issuanceId).mptAmount())
      .isEqualTo(MpTokenNumericAmount.of(UnsignedLong.valueOf(20)));
  }

  @Test
  void mixesPlainPaymentWithConfidentialSend() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    final ConfidentialHolder bob = registerHolderKey(issuance);
    final KeyPair carol = createRandomAccountEd25519();
    final UnsignedLong carolBefore = xrpBalance(carol);

    final UnsignedInteger aliceSequence = currentSequence(alice.address());
    // The outer Batch consumes aliceSequence and the confidential send inner takes +1, so this plain inner takes +2.
    final Payment plainPayment = Payment.builder()
      .account(alice.address())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(aliceSequence.plus(UnsignedInteger.valueOf(2)))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .destination(carol.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(1_000_000))
      .build();

    final Map<Address, UnsignedInteger> sequences = new HashMap<>();
    sequences.put(alice.address(), aliceSequence);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, bob, 30, issuance)),
        ConfidentialBatchInner.ofPlain(plainPayment))
      .accountSequences(sequences)
      .states(states(pair(alice, issuance), pair(bob, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account);

    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(70));
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.valueOf(30));
    assertThat(xrpBalance(carol).minus(carolBefore)).isEqualTo(UnsignedLong.valueOf(1_000_000));
  }

  @Test
  void registersDestinationKeyViaConvertThenSends() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    // Bob is authorized but has NOT registered his ElGamal key on-ledger yet.
    final ConfidentialHolder bob = setupHolder(issuance);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(ConfidentialConvertOp.builder()
          .account(bob.address()).amount(UnsignedLong.ZERO)
          .holderKeyPair(bob.elGamal).mpTokenIssuanceId(issuance.issuanceId).build()),
        ConfidentialBatchInner.of(sendOp(alice, bob, 30, issuance)))
      .accountSequences(sequences(alice, bob))
      .states(states(pair(alice, issuance), pair(bob, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account, bob);

    // The send applied only because it encrypted to bob's key, threaded from the in-batch Convert.
    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(70));
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.valueOf(30));
    assertThat(getMpToken(bob.address(), issuance.issuanceId).holderEncryptionKey()).isPresent();
  }

  @Test
  void convertsMergesThenSpendsToppedUpBalance() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);
    final ConfidentialHolder bob = registerHolderKey(issuance);
    // Fresh public MPT for alice to convert inside the Batch.
    payMpt(issuance.issuer, alice.address(), issuance.issuanceId, 100);

    final Batch batch = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(ConfidentialConvertOp.builder()
          .account(alice.address()).amount(UnsignedLong.valueOf(100))
          .holderKeyPair(alice.elGamal).mpTokenIssuanceId(issuance.issuanceId).registerKey(false).build()),
        ConfidentialBatchInner.of(ConfidentialMergeInboxOp.builder()
          .account(alice.address()).mpTokenIssuanceId(issuance.issuanceId).build()),
        ConfidentialBatchInner.of(sendOp(alice, bob, 150, issuance)))
      .accountSequences(sequences(alice))
      .states(states(pair(alice, issuance), pair(bob, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());
    submitBatch(batch, alice.account);

    // Convert (+100) and merge lift alice 100 -> 200, then she sends 150 -> 50.
    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(50));
    assertThat(inboxBalance(bob, issuance)).isEqualTo(UnsignedLong.valueOf(150));
  }

  @Test
  void sendsFromTwoMultiSignedAccounts() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder alice = holderWithBalance(issuance, 100);  // owns the outer Batch
    final ConfidentialHolder bob = holderWithBalance(issuance, 100);    // authorizes its own inner
    final ConfidentialHolder carol = registerHolderKey(issuance);

    // Give alice and bob each a 2-of-2 signer list; their master keys stay enabled, so the confidential
    // funding above (single-signed) was fine. Done last, so the sequences the assembler gets are post-setup.
    final KeyPair aliceSigner1 = createRandomAccountEd25519();
    final KeyPair aliceSigner2 = createRandomAccountEd25519();
    final KeyPair bobSigner1 = createRandomAccountEd25519();
    final KeyPair bobSigner2 = createRandomAccountEd25519();
    enableMultiSig(alice.account, aliceSigner1, aliceSigner2);
    enableMultiSig(bob.account, bobSigner1, bobSigner2);

    final Batch assembled = assembler.assemble(ConfidentialBatchRequest.builder()
      .accountPublicKey(alice.account.publicKey())
      .addInners(
        ConfidentialBatchInner.of(sendOp(alice, carol, 30, issuance)),
        ConfidentialBatchInner.of(sendOp(bob, carol, 40, issuance)))
      .accountSequences(sequences(alice, bob))
      .states(states(pair(alice, issuance), pair(bob, issuance), pair(carol, issuance)))
      .issuances(issuances(issuance))
      .outerFee(OUTER_FEE)
      .build());

    // The assembler sets the outer SigningPubKey for single-sig; multi-signing it requires the field empty.
    final Batch base = Batch.builder().from(assembled).signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY).build();

    // Inner multi-sign: bob's account authorizes its own inner (a multi-sig BatchSigner carries `signers`).
    final List<SignerWrapper> bobInnerSigners = Stream.of(bobSigner1, bobSigner2)
      .map(kp -> SignerWrapper.of(Signer.builder()
        .signingPublicKey(kp.publicKey())
        .transactionSignature(signatureService.multiSignInner(kp.privateKey(), base, bob.address()))
        .build()))
      .collect(Collectors.toList());
    final Batch withBobSigner = Batch.builder()
      .from(base)
      .addBatchSigners(BatchSignerWrapper.of(BatchSigner.builder()
        .account(bob.address())
        .signers(bobInnerSigners)
        .build()))
      .build();

    // Outer multi-sign: alice's signers sign the whole Batch, which now commits to bob's BatchSigner.
    final List<Signer> aliceOuterSigners = Stream.of(aliceSigner1, aliceSigner2)
      .map(kp -> Signer.builder()
        .signingPublicKey(kp.publicKey())
        .transactionSignature(signatureService.multiSign(kp.privateKey(), withBobSigner))
        .build())
      .collect(Collectors.toList());
    final MultiSignedTransaction<Batch> multiSigned = MultiSignedTransaction.<Batch>builder()
      .unsignedTransaction(withBobSigner)
      .signerSet(aliceOuterSigners)
      .build();

    final SubmitMultiSignedResult<Batch> result = xrplClient.submitMultisigned(multiSigned);
    assertThat(result.engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(result.transaction().hash(), Batch.class));

    assertThat(spendable(alice, issuance)).isEqualTo(UnsignedLong.valueOf(70));
    assertThat(spendable(bob, issuance)).isEqualTo(UnsignedLong.valueOf(60));
    assertThat(inboxBalance(carol, issuance)).isEqualTo(UnsignedLong.valueOf(70));
  }

  // ===========================================================================
  // Batch plumbing helpers
  // ===========================================================================

  private ConfidentialSendOp sendOp(final ConfidentialHolder from, final ConfidentialHolder to, final long amount,
    final ConfidentialIssuance issuance) {
    return ConfidentialSendOp.builder()
      .account(from.address())
      .destination(to.address())
      .amount(UnsignedLong.valueOf(amount))
      .senderKeyPair(from.elGamal)
      .mpTokenIssuanceId(issuance.issuanceId)
      .build();
  }

  private ConfidentialClawbackOp clawbackOp(final ConfidentialIssuance issuance, final ConfidentialHolder holder,
    final long amount) {
    return ConfidentialClawbackOp.builder()
      .account(issuance.issuer.publicKey().deriveAddress())
      .holder(holder.address())
      .amount(UnsignedLong.valueOf(amount))
      .issuerKeyPair(issuance.issuerElGamal)
      .mpTokenIssuanceId(issuance.issuanceId)
      .build();
  }

  /** Give an existing (already-created) holder a spendable balance on a token — for one account holding many tokens. */
  private void fundHolderOnToken(final ConfidentialHolder holder, final ConfidentialIssuance issuance,
    final long amount) throws Exception {
    authorizeHolder(holder.account, issuance.issuanceId);
    payMpt(issuance.issuer, holder.address(), issuance.issuanceId, amount);
    convert(holder, issuance, amount, true);
    mergeInbox(holder, issuance);
  }

  private ConfidentialIssuanceInfo issuanceInfo(final ConfidentialIssuance issuance) {
    return ConfidentialIssuanceInfo.builder()
      .issuerEncryptionKey(issuance.issuerElGamal.publicKey())
      .auditorEncryptionKey(issuance.auditorElGamal.publicKey())
      .outstandingAmount(DECRYPT_BOUND)
      .build();
  }

  private ConfidentialTokenState stateOf(final ConfidentialHolder holder, final ConfidentialIssuance issuance)
    throws Exception {
    final MpTokenObject token = getMpToken(holder.address(), issuance.issuanceId);
    return ConfidentialTokenState.builder()
      .spending(token.confidentialBalanceSpending())
      .inbox(token.confidentialBalanceInbox())
      .issuerEncrypted(token.issuerEncryptedBalance())
      .auditorEncrypted(token.auditorEncryptedBalance())
      .version(token.confidentialBalanceVersion())
      .holderKey(token.holderEncryptionKey())
      .build();
  }

  private HolderOnToken pair(final ConfidentialHolder holder, final ConfidentialIssuance issuance) {
    return new HolderOnToken(holder, issuance);
  }

  private Map<String, ConfidentialTokenState> states(final HolderOnToken... entries) throws Exception {
    final Map<String, ConfidentialTokenState> states = new HashMap<>();
    for (final HolderOnToken entry : entries) {
      states.put(
        ConfidentialBatchRequest.stateKey(entry.holder.address(), entry.issuance.issuanceId),
        stateOf(entry.holder, entry.issuance)
      );
    }
    return states;
  }

  private Map<MpTokenIssuanceId, ConfidentialIssuanceInfo> issuances(final ConfidentialIssuance... issuances) {
    final Map<MpTokenIssuanceId, ConfidentialIssuanceInfo> map = new HashMap<>();
    for (final ConfidentialIssuance issuance : issuances) {
      map.put(issuance.issuanceId, issuanceInfo(issuance));
    }
    return map;
  }

  private Map<Address, UnsignedInteger> sequences(final ConfidentialHolder... holders) {
    final Map<Address, UnsignedInteger> sequences = new HashMap<>();
    for (final ConfidentialHolder holder : holders) {
      sequences.put(holder.address(), currentSequence(holder.address()));
    }
    return sequences;
  }

  private Map<Address, UnsignedInteger> sequences(final KeyPair outer, final ConfidentialHolder... holders) {
    final Map<Address, UnsignedInteger> sequences = new HashMap<>();
    sequences.put(outer.publicKey().deriveAddress(), currentSequence(outer.publicKey().deriveAddress()));
    for (final ConfidentialHolder holder : holders) {
      sequences.put(holder.address(), currentSequence(holder.address()));
    }
    return sequences;
  }

  /** Sign the assembled Batch (each inner participant adds a BatchSigner, then the outer account signs) and submit. */
  private void submitBatch(final Batch unsigned, final KeyPair outer, final ConfidentialHolder... innerSigners)
    throws Exception {
    Batch toSign = unsigned;
    if (innerSigners.length > 0) {
      final List<BatchSignerWrapper> signers = new ArrayList<>();
      for (final ConfidentialHolder signer : innerSigners) {
        final Signature signature = signatureService.signInner(signer.account.privateKey(), unsigned, signer.address());
        signers.add(BatchSignerWrapper.of(BatchSigner.builder()
          .account(signer.address())
          .signingPublicKey(signer.account.publicKey())
          .transactionSignature(signature)
          .build()));
      }
      toSign = Batch.builder().from(unsigned).batchSigners(signers).build();
    }
    final SingleSignedTransaction<Batch> signed = signatureService.sign(outer.privateKey(), toSign);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo("tesSUCCESS");
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), Batch.class));
  }

  /**
   * Give {@code account} a 2-of-2 signer list (its master key stays enabled).
   */
  private void enableMultiSig(final KeyPair account, final KeyPair signer1, final KeyPair signer2) throws Exception {
    final SignerListSet signerListSet = SignerListSet.builder()
      .account(account.publicKey().deriveAddress())
      .fee(networkFee())
      .sequence(currentSequence(account.publicKey().deriveAddress()))
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(SignerEntry.builder()
          .account(signer1.publicKey().deriveAddress()).signerWeight(UnsignedInteger.ONE).build()),
        SignerEntryWrapper.of(SignerEntry.builder()
          .account(signer2.publicKey().deriveAddress()).signerWeight(UnsignedInteger.ONE).build()))
      .signingPublicKey(account.publicKey())
      .build();
    submitAndWait(signatureService.sign(account.privateKey(), signerListSet), SignerListSet.class);
  }

  private UnsignedLong xrpBalance(final KeyPair account) {
    return this.scanForResult(() -> this.getValidatedAccountInfo(account.publicKey().deriveAddress()))
      .accountData().balance().value();
  }

  /** A (holder, issuance) pairing for building the assembler's per-(account, token) state map. */
  private static final class HolderOnToken {
    private final ConfidentialHolder holder;
    private final ConfidentialIssuance issuance;

    private HolderOnToken(final ConfidentialHolder holder, final ConfidentialIssuance issuance) {
      this.holder = holder;
      this.issuance = issuance;
    }
  }
}
