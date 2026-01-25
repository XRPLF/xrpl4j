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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.BatchFlags;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSigner;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration tests for {@link Batch} transactions (XLS-0056).
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "BatchTransactionIT only runs on local rippled node.")
public class BatchTransactionIT extends AbstractIT {

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  // //////////////////////
  // 1. This section tests a Batch transaction with two inner payments where all transactions are signed
  // the same single-sign account. Each Batch flag is tested over the same test.
  // //////////////////////

  /**
   * Test a single-account batch transaction containing two payments.
   */
  @Test
  void batchWithInnerSingleSigOuterSingleSigSame() throws JsonRpcClientErrorException, JsonProcessingException {
    final KeyPair sourceKeyPair = createRandomAccountEd25519();
    final KeyPair destination1KeyPair = createRandomAccountEd25519();
    final KeyPair destination2KeyPair = createRandomAccountEd25519();

    batchWithInnerSingleSigOuterSingleSigSameHelper(sourceKeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofAllOrNothing());
    batchWithInnerSingleSigOuterSingleSigSameHelper(sourceKeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofOnlyOne());
    batchWithInnerSingleSigOuterSingleSigSameHelper(sourceKeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofUntilFailure());
    batchWithInnerSingleSigOuterSingleSigSameHelper(sourceKeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofIndependent());
  }

  /**
   * Helper method to Test a single-account batch transaction containing two payments with the specified Batch flags.
   */
  private void batchWithInnerSingleSigOuterSingleSigSameHelper(
    final KeyPair sourceKeyPair,
    final KeyPair destination1KeyPair,
    final KeyPair destination2KeyPair,
    final BatchFlags batchFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(sourceKeyPair);
    Objects.requireNonNull(destination1KeyPair);
    Objects.requireNonNull(destination2KeyPair);
    Objects.requireNonNull(batchFlags);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    // Create inner payment1
    Payment innerPayment1 = createInnerPayment(
      sourceKeyPair.publicKey().deriveAddress(),
      sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE),
      destination1KeyPair.publicKey().deriveAddress(),
      10000
    );

    // Create inner payment2
    Payment innerPayment2 = createInnerPayment(
      sourceKeyPair.publicKey().deriveAddress(),
      sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2L)),
      destination2KeyPair.publicKey().deriveAddress(),
      20000
    );

    // Build the Batch transaction
    Batch batch = createBatchTransaction(
      sourceKeyPair, feeResult, sourceAccountInfo.accountData().sequence(),
      innerPayment1, innerPayment2, batchFlags
    );

    // ///////////////
    // Outer Sign
    // ///////////////

    // Only outer needs to be signed because the same accounts authorize inner and outer transactions.
    SingleSignedTransaction<Batch> signedBatch = signatureService.signOuter(sourceKeyPair.privateKey(), batch);
    SubmitResult<Batch> submitResult = xrplClient.submit(signedBatch);
    assertTesSuccess(submitResult);

    TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(submitResult.transactionResult().hash(), Batch.class)
    );
    assertTesSuccess(validatedBatch);
    logger.info("Batch transaction (flags: {}) successful: {}", batchFlags, submitResult.transactionResult().hash());

    // Verify metadata
    verifyBatchMetadata(validatedBatch);

    // Verify destination accounts received the payments
    AccountInfoResult dest1Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(destination1KeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult dest2Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(destination2KeyPair.publicKey().deriveAddress())
    );

    // Accounts should exist and have received funds (minus reserve)
    assertThat(dest1Info.accountData().balance()).isNotNull();
    assertThat(dest2Info.accountData().balance()).isNotNull();
  }

  // //////////////////////
  // 2. This section tests a Batch transaction with two inner payments where each inner payment transaction is
  // authorized by a different single-sign account. Each Batch flag is tested over the same test.
  // //////////////////////

  /**
   * Test a multi-account batch transaction where multiple accounts contribute inner transactions.
   */
  @Test
  void batchWithInnerSingleSigOuterSingleSigDifferent() throws JsonRpcClientErrorException, JsonProcessingException {
    final KeyPair account1KeyPair = createRandomAccountEd25519();
    final KeyPair account2KeyPair = createRandomAccountEd25519();
    final KeyPair destinationKeyPair = createRandomAccountEd25519();

    batchWithInnerSingleSigOuterSingleSigDifferentHelper(account1KeyPair, account2KeyPair, destinationKeyPair,
      BatchFlags.ofAllOrNothing());
    batchWithInnerSingleSigOuterSingleSigDifferentHelper(account1KeyPair, account2KeyPair, destinationKeyPair,
      BatchFlags.ofOnlyOne());
    batchWithInnerSingleSigOuterSingleSigDifferentHelper(account1KeyPair, account2KeyPair, destinationKeyPair,
      BatchFlags.ofUntilFailure());
    batchWithInnerSingleSigOuterSingleSigDifferentHelper(account1KeyPair, account2KeyPair, destinationKeyPair,
      BatchFlags.ofIndependent());
  }

  /**
   * Helper to test a multi-account batch transaction where multiple different accounts contribute inner transactions
   * with the specified flags.
   */
  private void batchWithInnerSingleSigOuterSingleSigDifferentHelper(
    final KeyPair account1KeyPair,
    final KeyPair account2KeyPair,
    final KeyPair destinationKeyPair,
    final BatchFlags batchFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(account1KeyPair);
    Objects.requireNonNull(account2KeyPair);
    Objects.requireNonNull(destinationKeyPair);
    Objects.requireNonNull(batchFlags);

    final FeeResult feeResult = xrplClient.fee();
    final AccountInfoResult account1Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(account1KeyPair.publicKey().deriveAddress())
    );
    final AccountInfoResult account2Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(account2KeyPair.publicKey().deriveAddress())
    );

    // Create inner payment from account1
    final Payment innerPayment1 = createInnerPayment(
      account1KeyPair.publicKey().deriveAddress(),
      account1Info.accountData().sequence().plus(UnsignedInteger.ONE),
      destinationKeyPair.publicKey().deriveAddress(),
      10000
    );

    // Create inner payment from account2
    final Payment innerPayment2 = createInnerPayment(
      account2KeyPair.publicKey().deriveAddress(),
      account2Info.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      20000
    );

    // Build the Batch transaction - account1 is the batch submitter
    final Batch unsignedBatch = Batch.builder()
      .account(account1KeyPair.publicKey().deriveAddress())
      .signingPublicKey(account1KeyPair.publicKey())
      .fee(FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(2L)))
      .sequence(account1Info.accountData().sequence())
      .flags(batchFlags)
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // ///////////////
    // Inner Sign (account2 is the inner signer)
    // ///////////////
    final List<BatchSignerWrapper> signerWrappers = Lists.newArrayList(
      BatchSignerWrapper.of(BatchSigner.builder()
        .account(account2KeyPair.publicKey().deriveAddress())
        .signingPublicKey(account2KeyPair.publicKey())
        .transactionSignature(
          signatureService.signInner(account2KeyPair.privateKey(), unsignedBatch) // <-- `signInner` is crucial here
        )
        .build()
      )
    );

    // ///////////////
    // Outer Sign (account1 is the batch submitter)
    // ///////////////
    final SingleSignedTransaction<Batch> signedBatch = signatureService.signOuter(
      account1KeyPair.privateKey(),
      Batch.builder().from(unsignedBatch)
        .batchSigners(signerWrappers)
        .build()
    );

    // Submit and wait for validation
    final SubmitResult<Batch> result = xrplClient.submit(signedBatch);
    assertTesSuccess(result);
    final TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Batch.class)
    );
    assertTesSuccess(validatedBatch);

    // Verify metadata
    verifyBatchMetadata(validatedBatch);

    // Verify the destination account received both payments
    final AccountInfoResult destInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    assertThat(destInfo.accountData().balance()).isNotNull();
  }

  // //////////////////////
  // 3. This section tests a Batch transaction with two inner payments where each inner payment transaction is
  // authorized by the same multi-sig account. Each Batch flag is tested over the same test.
  // //////////////////////

  /**
   * Test a batch transaction where both inner transactions are from the same multi-sig account.
   */
  @Test
  void batchWithInnerMultiSigOuterMultiSigSame() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create accounts: one multi-sig account owner, two signers (signer1 and signer2), and two destinations
    final KeyPair account1KeyPair = createRandomAccountEd25519();
    final KeyPair account1Signer1KeyPair = createRandomAccountEd25519();
    final KeyPair account2Signer1KeyPair = createRandomAccountEd25519();
    final KeyPair destination1KeyPair = createRandomAccountEd25519();
    final KeyPair destination2KeyPair = createRandomAccountEd25519();

    batchWithInnerMultiSigOuterMultiSigSameHelper(
      account1KeyPair, account1Signer1KeyPair, account2Signer1KeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofAllOrNothing()
    );
    batchWithInnerMultiSigOuterMultiSigSameHelper(
      account1KeyPair, account1Signer1KeyPair, account2Signer1KeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofOnlyOne()
    );
    batchWithInnerMultiSigOuterMultiSigSameHelper(
      account1KeyPair, account1Signer1KeyPair, account2Signer1KeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofIndependent()
    );
    batchWithInnerMultiSigOuterMultiSigSameHelper(
      account1KeyPair, account1Signer1KeyPair, account2Signer1KeyPair, destination1KeyPair, destination2KeyPair,
      BatchFlags.ofUntilFailure()
    );
  }

  /**
   * Helper to a batch transaction where both inner transactions are from the same multi-sig account with the specified
   * flags.
   */
  private void batchWithInnerMultiSigOuterMultiSigSameHelper(
    final KeyPair account1KeyPair,
    final KeyPair account1Signer1KeyPair,
    final KeyPair account1Signer2KeyPair,
    final KeyPair destination1KeyPair,
    final KeyPair destination2KeyPair,
    final BatchFlags batchFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(account1KeyPair);
    Objects.requireNonNull(account1Signer1KeyPair);
    Objects.requireNonNull(account1Signer2KeyPair);
    Objects.requireNonNull(destination1KeyPair);
    Objects.requireNonNull(destination2KeyPair);
    Objects.requireNonNull(batchFlags);

    FeeResult feeResult = xrplClient.fee();

    final AccountInfoResult account1Result = this.setupMultiSigAccount(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPair
    );

    final Payment innerPayment1 = createInnerPayment(
      account1KeyPair.publicKey().deriveAddress(),
      account1Result.accountData().sequence().plus(UnsignedInteger.ONE),
      destination1KeyPair.publicKey().deriveAddress(),
      10000
    );
    // Create inner payment from account2
    final Payment innerPayment2 = createInnerPayment(
      account1KeyPair.publicKey().deriveAddress(),
      account1Result.accountData().sequence().plus(UnsignedInteger.valueOf(2)),
      destination2KeyPair.publicKey().deriveAddress(),
      20000
    );

    // Build the Batch transaction with Independent mode
    Batch unsignedBatch = Batch.builder()
      .account(account1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(2L)))
      .sequence(account1Result.accountData().sequence())
      .flags(batchFlags)
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // ///////////////
    // Outer MultiSign (account1Signer1 and account1Signer2 are the outer multi-signers)
    // ///////////////

    Set<Signer> signers = Lists.newArrayList(account1Signer1KeyPair, account1Signer2KeyPair).stream()
      .map(keyPair -> {
        Signature signature = signatureService.multiSignOuter(keyPair.privateKey(), unsignedBatch);
        return Signer.builder()
          .signingPublicKey(keyPair.publicKey())
          .transactionSignature(signature)
          .build();
      })
      .collect(Collectors.toSet());

    MultiSignedTransaction<Batch> multiSignedBatch = MultiSignedTransaction.<Batch>builder()
      .unsignedTransaction(unsignedBatch)
      .signerSet(signers)
      .build();

    // Submit and wait for validation
    SubmitMultiSignedResult<Batch> result = xrplClient.submitMultisigned(multiSignedBatch);
    assertTesSuccess(result);
    TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transaction().hash(), Batch.class)
    );
    assertTesSuccess(validatedBatch);

    // Verify metadata
    verifyBatchMetadata(validatedBatch);

    // Verify destination accounts received the payments
    AccountInfoResult dest1Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(destination1KeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult dest2Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(destination2KeyPair.publicKey().deriveAddress())
    );

    assertThat(dest1Info.accountData().balance()).isNotNull();
    assertThat(dest2Info.accountData().balance()).isNotNull();
  }

  // //////////////////////
  // 4. This section tests a Batch transaction with two inner payments where each inner payment transaction is
  // authorized by a different multi-sig account. Each Batch flag is tested over the same test.
  // //////////////////////

  /**
   * Test a batch transaction where inner transactions are from different multi-sig accounts.
   */
  @Test
  void batchWithInnerMultiSigOuterMultiSigDifferent() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create two multi-sig accounts, each with their own signers
    final KeyPair account1KeyPair = createRandomAccountEd25519();
    final KeyPair account1Signer1KeyPair = createRandomAccountEd25519();
    final KeyPair account1Signer2KeyPaid = createRandomAccountEd25519();

    final KeyPair account2KeyPair = createRandomAccountEd25519();
    final KeyPair account2Signer1KeyPair = createRandomAccountEd25519();
    final KeyPair account2Signer2KeyPair = createRandomAccountEd25519();

    final KeyPair destinationKeyPair = createRandomAccountEd25519();

    batchWithInnerMultiSigOuterMultiSigDifferentHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofAllOrNothing()
    );
    batchWithInnerMultiSigOuterMultiSigDifferentHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofOnlyOne()
    );
    batchWithInnerMultiSigOuterMultiSigDifferentHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofUntilFailure()
    );
    batchWithInnerMultiSigOuterMultiSigDifferentHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofIndependent()
    );
  }

  /**
   * Test a batch transaction where inner transactions are from different multi-sig accounts with the specified Batch
   * flag.
   */
  private void batchWithInnerMultiSigOuterMultiSigDifferentHelper(
    final KeyPair account1KeyPair,
    final KeyPair account1Signer1KeyPair,
    final KeyPair account1Signer2KeyPair,
    final KeyPair account2KeyPair,
    final KeyPair account2Signer1KeyPair,
    final KeyPair account2Signer2KeyPair,
    final KeyPair destinationKeyPair,
    final BatchFlags batchFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(account1KeyPair);
    Objects.requireNonNull(account1Signer1KeyPair);
    Objects.requireNonNull(account1Signer2KeyPair);
    Objects.requireNonNull(account2KeyPair);
    Objects.requireNonNull(account2Signer1KeyPair);
    Objects.requireNonNull(account2Signer2KeyPair);
    Objects.requireNonNull(destinationKeyPair);
    Objects.requireNonNull(batchFlags);

    final FeeResult feeResult = xrplClient.fee();

    final AccountInfoResult account1Result = setupMultiSigAccount(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPair
    );

    final AccountInfoResult account2Result = setupMultiSigAccount(
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair
    );

    // Create inner payment from account1
    final Payment innerPayment1 = createInnerPayment(
      account1KeyPair.publicKey().deriveAddress(),
      account1Result.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      10000
    );
    // Create inner payment from account2
    final Payment innerPayment2 = createInnerPayment(
      account2KeyPair.publicKey().deriveAddress(),
      account2Result.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      20000
    );

    // Build the Batch transaction - account1 is the batch submitter
    Batch unsignedBatch = Batch.builder()
      .account(account1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(2L)))
      .sequence(account1Result.accountData().sequence())
      .flags(batchFlags)
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // ///////////////
    // Inner Multisign (account2Signer1 and account2Signer2 are the inner multi-signers)
    // ///////////////

    final List<SignerWrapper> innerSignerWrappers = Lists.newArrayList(account2Signer1KeyPair, account2Signer2KeyPair)
      .stream()
      .map(keyPair -> {
        final PublicKey signingPublicKey = signatureService.derivePublicKey(keyPair.privateKey());
        final Signer signer = Signer.builder()
          .signingPublicKey(signingPublicKey)
          .transactionSignature(signatureService.multiSignInner(keyPair.privateKey(), unsignedBatch))
          .build();
        return SignerWrapper.of(signer);
      })
      .collect(Collectors.toList());

    // ///////////////
    // Outer Multisign (account1Signer1 and account1Signer2 are the outer multi-signers)
    // ///////////////

    final Batch unsignedBatchWithInnerBatchSigner = Batch.builder()
      .from(unsignedBatch)
      .batchSigners(
        Lists.newArrayList(
          BatchSignerWrapper.of(BatchSigner.builder()
            .account(account2KeyPair.publicKey().deriveAddress())
            .signers(innerSignerWrappers)
            .build()
          )))
      .build();

    // Sign the outer batch transaction (account1 is the batch submitter)
    final Signature signedOuterBatchBySigner1 = signatureService.multiSignOuter(
      account1Signer1KeyPair.privateKey(),
      unsignedBatchWithInnerBatchSigner
    );

    final Signature signedOuterBatchBySigner21 = signatureService.multiSignOuter(
      account1Signer2KeyPair.privateKey(),
      unsignedBatchWithInnerBatchSigner
    );

    // Sort outer signers by account address (required by XRPL)
    final List<Signer> outerMultiSigners = Lists.newArrayList(
      Signer.builder()
        // account is derived from the signing public key
        .signingPublicKey(account1Signer1KeyPair.publicKey())
        .transactionSignature(signedOuterBatchBySigner1)
        .build(),
      Signer.builder()
        // account is derived from the signing public key
        .signingPublicKey(account1Signer2KeyPair.publicKey())
        .transactionSignature(signedOuterBatchBySigner21)
        .build()
    );

    final MultiSignedTransaction<Batch> multiSignedBatch = MultiSignedTransaction.<Batch>builder()
      .unsignedTransaction(unsignedBatchWithInnerBatchSigner)
      .signerSet(outerMultiSigners)
      .build();

    // Submit and wait for validation
    final SubmitMultiSignedResult<Batch> result = xrplClient.submitMultisigned(multiSignedBatch);
    assertTesSuccess(result);
    final TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transaction().hash(), Batch.class)
    );
    assertTesSuccess(validatedBatch);

    // Verify metadata
    verifyBatchMetadata(validatedBatch);

    // Verify the destination account received both payments
    final AccountInfoResult destInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    assertThat(destInfo.accountData().balance()).isNotNull();
  }

  // //////////////////////
  // 5. This section tests a Batch transaction with two inner payments where each inner payment transaction is
  // authorized by the same multi-sig account, and the outer transaction is from a different single-sig account. Each
  // Batch flag is tested over the same test.
  // //////////////////////

  /**
   * Test a batch transaction where inner transactions are from the same single-sig account, and the outer transaction
   * is from a multi-sig account.
   */
  @Test
  void batchWithInnerMultiSigOuterSingleSig() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create two multi-sig accounts, each with their own signers
    final KeyPair account1KeyPair = createRandomAccountEd25519();
    final KeyPair account1Signer1KeyPair = createRandomAccountEd25519();
    final KeyPair account1Signer2KeyPaid = createRandomAccountEd25519();

    final KeyPair account2KeyPair = createRandomAccountEd25519();

    final KeyPair destinationKeyPair = createRandomAccountEd25519();

    batchWithInnerMultiSigOuterSingleSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair,
      destinationKeyPair,
      BatchFlags.ofAllOrNothing()
    );
    batchWithInnerMultiSigOuterSingleSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair,
      destinationKeyPair,
      BatchFlags.ofOnlyOne()
    );
    batchWithInnerMultiSigOuterSingleSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair,
      destinationKeyPair,
      BatchFlags.ofUntilFailure()
    );
    batchWithInnerMultiSigOuterSingleSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair,
      destinationKeyPair,
      BatchFlags.ofIndependent()
    );
  }

  /**
   * Test a batch transaction where the inner transactions are from the same single-sig account with the specified Batch
   * flag.
   */
  private void batchWithInnerMultiSigOuterSingleSigHelper(
    final KeyPair account1KeyPair,
    final KeyPair account1Signer1KeyPair,
    final KeyPair account1Signer2KeyPair,
    final KeyPair account2KeyPair,
    final KeyPair destinationKeyPair,
    final BatchFlags batchFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(account1KeyPair);
    Objects.requireNonNull(account1Signer1KeyPair);
    Objects.requireNonNull(account1Signer2KeyPair);
    Objects.requireNonNull(account2KeyPair);
    Objects.requireNonNull(destinationKeyPair);
    Objects.requireNonNull(batchFlags);

    final FeeResult feeResult = xrplClient.fee();

    final AccountInfoResult account1Result = setupMultiSigAccount(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPair
    );

    final AccountInfoResult account2Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(account2KeyPair.publicKey().deriveAddress())
    );

    // Create inner payment from account1
    final Payment innerPayment1 = createInnerPayment(
      account1KeyPair.publicKey().deriveAddress(),
      account1Result.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      10000
    );
    // Create inner payment from account2
    final Payment innerPayment2 = createInnerPayment(
      account2KeyPair.publicKey().deriveAddress(),
      account2Info.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      20000
    );

    // Build the Batch transaction - account1 is the batch submitter
    Batch unsignedBatch = Batch.builder()
      .account(account1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(2L)))
      .sequence(account1Result.accountData().sequence())
      .flags(batchFlags)
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // ///////////////
    // Inner Sign (account2 is the inner single-signer)
    // ///////////////

    final List<BatchSignerWrapper> signerWrappers = Lists.newArrayList(
      BatchSignerWrapper.of(BatchSigner.builder()
        .account(account2KeyPair.publicKey().deriveAddress())
        .signingPublicKey(account2KeyPair.publicKey())
        .transactionSignature(
          signatureService.signInner(account2KeyPair.privateKey(), unsignedBatch) // <-- `signInner` is crucial here
        )
        .build()
      )
    );

    // ///////////////
    // Outer Multisign (account1Signer1 and account1Signer2 are the outer multi-signers)
    // ///////////////

    final Batch unsignedBatchWithInnerBatchSigner = Batch.builder()
      .from(unsignedBatch)
      .batchSigners(signerWrappers)
      .build();

    // Sign the outer batch transaction (account1 is the batch submitter)
    final Signature signedOuterBatchBySigner1 = signatureService.multiSignOuter(
      account1Signer1KeyPair.privateKey(),
      unsignedBatchWithInnerBatchSigner
    );

    final Signature signedOuterBatchBySigner21 = signatureService.multiSignOuter(
      account1Signer2KeyPair.privateKey(),
      unsignedBatchWithInnerBatchSigner
    );

    // Sort outer signers by account address (required by XRPL)
    final List<Signer> outerMultiSigners = Lists.newArrayList(
      Signer.builder()
        // account is derived from the signing public key
        .signingPublicKey(account1Signer1KeyPair.publicKey())
        .transactionSignature(signedOuterBatchBySigner1)
        .build(),
      Signer.builder()
        // account is derived from the signing public key
        .signingPublicKey(account1Signer2KeyPair.publicKey())
        .transactionSignature(signedOuterBatchBySigner21)
        .build()
    );

    final MultiSignedTransaction<Batch> multiSignedBatch = MultiSignedTransaction.<Batch>builder()
      .unsignedTransaction(unsignedBatchWithInnerBatchSigner)
      .signerSet(outerMultiSigners)
      .build();

    // Submit and wait for validation
    final SubmitMultiSignedResult<Batch> result = xrplClient.submitMultisigned(multiSignedBatch);
    assertTesSuccess(result);
    final TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transaction().hash(), Batch.class)
    );
    assertTesSuccess(validatedBatch);

    // Verify metadata
    verifyBatchMetadata(validatedBatch);

    // Verify the destination account received both payments
    final AccountInfoResult destInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    assertThat(destInfo.accountData().balance()).isNotNull();
  }

  // //////////////////////
  // 6. This section tests a Batch transaction with two inner payments where each inner payment transaction is
  // authorized by the same single-sig account, and the outer transaction is authorized by a different multi-sig
  // account. Each Batch flag is tested over the same test.
  // //////////////////////

  /**
   * Test a batch transaction where inner transactions are from different multi-sig accounts.
   */
  @Test
  void batchWithInnerSingleSigOuterMultiSig() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create two multi-sig accounts, each with their own signers
    final KeyPair account1KeyPair = createRandomAccountEd25519();
    final KeyPair account1Signer1KeyPair = createRandomAccountEd25519();
    final KeyPair account1Signer2KeyPaid = createRandomAccountEd25519();

    final KeyPair account2KeyPair = createRandomAccountEd25519();
    final KeyPair account2Signer1KeyPair = createRandomAccountEd25519();
    final KeyPair account2Signer2KeyPair = createRandomAccountEd25519();

    final KeyPair destinationKeyPair = createRandomAccountEd25519();

    batchWithInnerSingleSigOuterMultiSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofAllOrNothing()
    );
    batchWithInnerSingleSigOuterMultiSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofOnlyOne()
    );
    batchWithInnerSingleSigOuterMultiSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofUntilFailure()
    );
    batchWithInnerSingleSigOuterMultiSigHelper(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPaid,
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair,
      destinationKeyPair,
      BatchFlags.ofIndependent()
    );
  }

  /**
   * Test a batch transaction where inner transactions are from different multi-sig accounts with the specified Batch
   * flag.
   */
  private void batchWithInnerSingleSigOuterMultiSigHelper(
    final KeyPair account1KeyPair,
    final KeyPair account1Signer1KeyPair,
    final KeyPair account1Signer2KeyPair,
    final KeyPair account2KeyPair,
    final KeyPair account2Signer1KeyPair,
    final KeyPair account2Signer2KeyPair,
    final KeyPair destinationKeyPair,
    final BatchFlags batchFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(account1KeyPair);
    Objects.requireNonNull(account1Signer1KeyPair);
    Objects.requireNonNull(account1Signer2KeyPair);
    Objects.requireNonNull(account2KeyPair);
    Objects.requireNonNull(account2Signer1KeyPair);
    Objects.requireNonNull(account2Signer2KeyPair);
    Objects.requireNonNull(destinationKeyPair);
    Objects.requireNonNull(batchFlags);

    final FeeResult feeResult = xrplClient.fee();

    final AccountInfoResult account1Result = setupMultiSigAccount(
      account1KeyPair, account1Signer1KeyPair, account1Signer2KeyPair
    );

    final AccountInfoResult account2Result = setupMultiSigAccount(
      account2KeyPair, account2Signer1KeyPair, account2Signer2KeyPair
    );

    // Create inner payment from account1
    final Payment innerPayment1 = createInnerPayment(
      account1KeyPair.publicKey().deriveAddress(),
      account1Result.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      10000
    );
    // Create inner payment from account2
    final Payment innerPayment2 = createInnerPayment(
      account2KeyPair.publicKey().deriveAddress(),
      account2Result.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      20000
    );

    // Build the Batch transaction - account1 is the batch submitter
    Batch unsignedBatch = Batch.builder()
      .account(account1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(2L)))
      .sequence(account1Result.accountData().sequence())
      .flags(batchFlags)
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // ///////////////
    // Inner Multisign (account2Signer1 and account2Signer2 are the inner multi-signers)
    // ///////////////

    final List<BatchSignerWrapper> innerSignerWrappers = Lists.newArrayList(
      BatchSignerWrapper.of(BatchSigner.builder()
        .account(account2KeyPair.publicKey().deriveAddress())
        .signingPublicKey(account2KeyPair.publicKey())
        .transactionSignature(
          signatureService.signInner(account2KeyPair.privateKey(), unsignedBatch) // <-- `signInner` is crucial here
        )
        .build()
      )
    );

    // ///////////////
    // Outer Multisign (account1Signer1 and account1Signer2 are the outer multi-signers)
    // ///////////////

    final Batch unsignedBatchWithInnerBatchSigner = Batch.builder()
      .from(unsignedBatch)
      .batchSigners(innerSignerWrappers)
      .build();

    // Sign the outer batch transaction (account1 is the batch submitter)
    final Signature signedOuterBatchBySigner1 = signatureService.multiSignOuter(
      account1Signer1KeyPair.privateKey(),
      unsignedBatchWithInnerBatchSigner
    );

    final Signature signedOuterBatchBySigner21 = signatureService.multiSignOuter(
      account1Signer2KeyPair.privateKey(),
      unsignedBatchWithInnerBatchSigner
    );

    // Sort outer signers by account address (required by XRPL)
    final List<Signer> outerMultiSigners = Lists.newArrayList(
      Signer.builder()
        // account is derived from the signing public key
        .signingPublicKey(account1Signer1KeyPair.publicKey())
        .transactionSignature(signedOuterBatchBySigner1)
        .build(),
      Signer.builder()
        // account is derived from the signing public key
        .signingPublicKey(account1Signer2KeyPair.publicKey())
        .transactionSignature(signedOuterBatchBySigner21)
        .build()
    );

    final MultiSignedTransaction<Batch> multiSignedBatch = MultiSignedTransaction.<Batch>builder()
      .unsignedTransaction(unsignedBatchWithInnerBatchSigner)
      .signerSet(outerMultiSigners)
      .build();

    // Submit and wait for validation
    final SubmitMultiSignedResult<Batch> result = xrplClient.submitMultisigned(multiSignedBatch);
    assertTesSuccess(result);
    final TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transaction().hash(), Batch.class)
    );
    assertTesSuccess(validatedBatch);

    // Verify metadata
    verifyBatchMetadata(validatedBatch);

    // Verify the destination account received both payments
    final AccountInfoResult destInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    assertThat(destInfo.accountData().balance()).isNotNull();
  }

  // //////////////////////
  // 7. This section tests a Batch transaction with two inner payments where each inner payment transaction is
  // authorized by a different single-sig account, and the outer signer is a distinct third single-sig account. Each
  // Batch flag is tested over the same test.
  // //////////////////////

  /**
   * Test a batch transaction where two different single-sig accounts contribute inner transactions and a third account
   * signs the outer batch transaction.
   */
  @Test
  void batchWithTwoDifferentInnerSingleSigsPlusThirdOuterSingleSig()
    throws JsonRpcClientErrorException, JsonProcessingException {
    final KeyPair account1KeyPair = createRandomAccountEd25519();
    final KeyPair account2KeyPair = createRandomAccountEd25519();
    final KeyPair outerSignerKeyPair = createRandomAccountEd25519(); // Third account that signs the outer batch
    final KeyPair destinationKeyPair = createRandomAccountEd25519();

    this.batchWithTwoDifferentInnerSingleSigsPlusThirdOuterSingleSigHelper(
      account1KeyPair, account2KeyPair, outerSignerKeyPair, destinationKeyPair, BatchFlags.ofAllOrNothing()
    );
    this.batchWithTwoDifferentInnerSingleSigsPlusThirdOuterSingleSigHelper(
      account1KeyPair, account2KeyPair, outerSignerKeyPair, destinationKeyPair, BatchFlags.ofOnlyOne()
    );
    this.batchWithTwoDifferentInnerSingleSigsPlusThirdOuterSingleSigHelper(
      account1KeyPair, account2KeyPair, outerSignerKeyPair, destinationKeyPair, BatchFlags.ofUntilFailure()
    );
    this.batchWithTwoDifferentInnerSingleSigsPlusThirdOuterSingleSigHelper(
      account1KeyPair, account2KeyPair, outerSignerKeyPair, destinationKeyPair, BatchFlags.ofIndependent()
    );
  }

  /**
   * Test a batch transaction with Independent mode where two different single-sig accounts contribute inner
   * transactions and a third account signs the outer batch transaction.
   */
  private void batchWithTwoDifferentInnerSingleSigsPlusThirdOuterSingleSigHelper(
    final KeyPair innerSigner1KeyPair,
    final KeyPair innerSigner2KeyPair,
    final KeyPair outerSignerKeyPair,
    final KeyPair destinationKeyPair,
    final BatchFlags batchFlags
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(innerSigner1KeyPair);
    Objects.requireNonNull(innerSigner2KeyPair);
    Objects.requireNonNull(outerSignerKeyPair);
    Objects.requireNonNull(destinationKeyPair);
    Objects.requireNonNull(batchFlags);

    final FeeResult feeResult = xrplClient.fee();
    final AccountInfoResult account1InfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(innerSigner1KeyPair.publicKey().deriveAddress())
    );
    final AccountInfoResult account2InfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(innerSigner2KeyPair.publicKey().deriveAddress())
    );
    final AccountInfoResult outerSignerInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(outerSignerKeyPair.publicKey().deriveAddress())
    );

    // Create inner payment from account1
    final Payment innerPayment1 = createInnerPayment(
      innerSigner1KeyPair.publicKey().deriveAddress(),
      account1InfoResult.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      10000
    );
    // Create inner payment from account2
    final Payment innerPayment2 = createInnerPayment(
      innerSigner2KeyPair.publicKey().deriveAddress(),
      account2InfoResult.accountData().sequence(),
      destinationKeyPair.publicKey().deriveAddress(),
      20000
    );

    // Build the Batch transaction with Independent mode
    final Batch unsignedBatch = Batch.builder()
      .account(outerSignerKeyPair.publicKey().deriveAddress())
      .signingPublicKey(outerSignerKeyPair.publicKey())
      .fee(FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(2L)))
      .sequence(outerSignerInfo.accountData().sequence())
      .flags(batchFlags) // <-- One crux of the test
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // ///////////////
    // Inner Sign (innerSigner1 and innerSigner2 are the inner signers)
    // ///////////////

    final List<BatchSignerWrapper> signerWrappers = Lists.newArrayList(
      // innerSigner1
      BatchSignerWrapper.of(BatchSigner.builder()
        .account(innerSigner1KeyPair.publicKey().deriveAddress())
        .signingPublicKey(innerSigner1KeyPair.publicKey())
        .transactionSignature(
          signatureService.signInner(innerSigner1KeyPair.privateKey(), unsignedBatch) // <-- `signInner` is crucial here
        )
        .build()
      ),
      // innerSigner1
      BatchSignerWrapper.of(BatchSigner.builder()
        .account(innerSigner2KeyPair.publicKey().deriveAddress())
        .signingPublicKey(innerSigner2KeyPair.publicKey())
        .transactionSignature(
          signatureService.signInner(innerSigner2KeyPair.privateKey(), unsignedBatch) // <-- `signInner` is crucial here
        )
        .build()
      )
    );

    // ///////////////
    // Inner Sign (outerSigner is the outer signer)
    // ///////////////

    // Sign the outer batch transaction with the third account
    final SingleSignedTransaction<Batch> signedBatch = signatureService.signOuter(
      outerSignerKeyPair.privateKey(),
      Batch.builder()
        .from(unsignedBatch)
        .batchSigners(signerWrappers)
        .build()
    );

    // Submit and wait for validation
    final SubmitResult<Batch> result = xrplClient.submit(signedBatch);
    assertTesSuccess(result);
    final TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Batch.class)
    );
    assertTesSuccess(validatedBatch);

    // Verify metadata
    verifyBatchMetadata(validatedBatch);

    // Verify the destination account received both payments
    final AccountInfoResult destInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    assertThat(destInfo.accountData().balance()).isNotNull();
  }

  // //////////////////////
  // Helper methods to reduce code duplication
  // //////////////////////

  /**
   * Helper method to set up a multi-sig account with two signers.
   *
   * @param accountKeyPair The account to convert to multi-sig
   * @param signer1KeyPair First signer
   * @param signer2KeyPair Second signer
   *
   * @return AccountInfoResult after the signer list is set
   */
  private AccountInfoResult setupMultiSigAccount(
    KeyPair accountKeyPair,
    KeyPair signer1KeyPair,
    KeyPair signer2KeyPair
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(accountKeyPair.publicKey().deriveAddress())
    );

    SignerListSet signerListSet = SignerListSet.builder()
      .account(accountKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(signer1KeyPair.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(signer2KeyPair.publicKey().deriveAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .signingPublicKey(accountKeyPair.publicKey())
      .build();

    SingleSignedTransaction<SignerListSet> signedSignerListSet = signatureService.sign(
      accountKeyPair.privateKey(), signerListSet
    );
    SubmitResult<SignerListSet> signerListSetResult = xrplClient.submit(signedSignerListSet);
    assertTesSuccess(signerListSetResult);

    // Wait for the signer list to be set
    return scanForResult(
      () -> this.getValidatedAccountInfo(accountKeyPair.publicKey().deriveAddress()),
      infoResult -> infoResult.accountData().signerLists().size() == 1
    );
  }

  /**
   * Helper method to create an inner payment for a batch transaction.
   *
   * @param sourceAddress      The source account address
   * @param sequence           The sequence number for the payment
   * @param destinationAddress The destination address
   * @param amount             The amount in drops
   *
   * @return The Payment transaction
   */
  private Payment createInnerPayment(
    Address sourceAddress,
    UnsignedInteger sequence,
    Address destinationAddress,
    long amount
  ) {
    return Payment.builder()
      .account(sourceAddress)
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sequence)
      .destination(destinationAddress)
      .amount(XrpCurrencyAmount.ofDrops(amount))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();
  }

  /**
   * Asserts that the specified batch transaction result indicates a successful TES_SUCCESS outcome.
   *
   * @param validatedBatch The result of a batch transaction, represented as a {@link TransactionResult}. This object
   *                       must contain metadata with a transaction result code. Throws an exception if the metadata is
   *                       missing or if the transaction result does not match TES_SUCCESS.
   */
  private void assertTesSuccess(TransactionResult<Batch> validatedBatch) {
    assertThat(validatedBatch.metadata()
      .orElseThrow(() -> new RuntimeException("Metadata is missing."))
      .transactionResult()
    ).isEqualTo(TransactionResultCodes.TES_SUCCESS);
  }

  /**
   * Asserts that the specified batch transaction result indicates a successful TES_SUCCESS outcome.
   *
   * @param validatedBatch The result of a batch transaction, represented as a {@link SubmitResult} of type
   *                       {@link Batch}. This object must contain metadata with a transaction result code. Throws an
   *                       exception if the metadata is missing or if the transaction result does not match
   *                       TES_SUCCESS.
   */
  private void assertTesSuccess(SubmitResult<?> validatedBatch) {
    assertThat(validatedBatch.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
  }

  /**
   * Asserts that the given multi-signed batch transaction result indicates a successful TES_SUCCESS outcome.
   *
   * @param validatedBatch The result of a multi-signed batch transaction, represented as a
   *                       {@link SubmitMultiSignedResult} of type {@link Batch}. This object must contain metadata with
   *                       a transaction result code. An exception will be thrown if the metadata is missing or if the
   *                       transaction result code does not match TES_SUCCESS.
   */
  private void assertTesSuccess(SubmitMultiSignedResult<?> validatedBatch) {
    assertThat(validatedBatch.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
  }

  /**
   * Creates a batch transaction composed of two inner payment transactions.
   *
   * @param sourceKeyPair   The {@link KeyPair} of the source account initiating the batch transaction.
   * @param feeResult       The {@link FeeResult} used to determine the appropriate transaction fee for the batch.
   * @param accountSequence The sequence number of the source account used for the batch transaction.
   * @param innerPayment1   The first {@link Payment} transaction to be included in the batch.
   * @param innerPayment2   The second {@link Payment} transaction to be included in the batch.
   * @param batchFlags      The {@link BatchFlags} specifying additional settings or behaviors for the batch
   *                        transaction.
   *
   * @return A {@link Batch} object representing the created batch transaction.
   */
  private Batch createBatchTransaction(
    final KeyPair sourceKeyPair, final FeeResult feeResult, final UnsignedInteger accountSequence,
    final Payment innerPayment1, final Payment innerPayment2, final BatchFlags batchFlags
  ) {
    return Batch.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeBatchFee(feeResult, UnsignedInteger.valueOf(2L)))
      .sequence(accountSequence)
      .signingPublicKey(sourceKeyPair.publicKey())
      .flags(batchFlags)
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();
  }

  /**
   * Verifies that the batch transaction metadata is properly populated and contains expected data.
   *
   * @param validatedBatch The validated batch transaction result
   */
  private void verifyBatchMetadata(TransactionResult<Batch> validatedBatch) {
    // Verify metadata is present
    assertThat(validatedBatch.metadata()).isPresent();

    TransactionMetadata metadata = validatedBatch.metadata()
      .orElseThrow(() -> new RuntimeException("Metadata is missing."));

    // Verify the transaction result is success
    assertThat(metadata.transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // Verify the transaction index is set (should be >= 0)
    assertThat(metadata.transactionIndex()).isNotNull();

    // Verify affected nodes are present
    // A batch transaction should affect multiple nodes:
    // - The outer account's AccountRoot (for fee and sequence)
    // - Each inner transaction's source AccountRoot (for sequence)
    // - Each destination AccountRoot (for balance changes)
    assertThat(metadata.affectedNodes()).isNotEmpty();

    // Log metadata details for debugging
    logger.info("Batch transaction metadata - TransactionIndex: {}, AffectedNodes count: {}, TransactionResult: {}",
      metadata.transactionIndex(),
      metadata.affectedNodes().size(),
      metadata.transactionResult()
    );
  }
}
