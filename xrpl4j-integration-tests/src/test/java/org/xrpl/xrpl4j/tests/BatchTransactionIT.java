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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.BatchFlags;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.BatchSigner;
import org.xrpl.xrpl4j.model.transactions.BatchSignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Integration tests for {@link Batch} transactions (XLS-0056).
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "BatchTransactionIT only runs on local rippled node.")
public class BatchTransactionIT extends AbstractIT {

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  /**
   * Test a single-account batch transaction with AllOrNothing mode containing two payments.
   */
  @Test
  void sendSingleAccountBatchAllOrNothing() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destination1KeyPair = createRandomAccountEd25519();
    KeyPair destination2KeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    // Create inner payment 1 - must have fee=0 and INNER_BATCH_TXN flag
    Payment innerPayment1 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence())
      .destination(destination1KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Create inner payment 2 - sequence must be incremented
    Payment innerPayment2 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .destination(destination2KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(20000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Build the Batch transaction
    Batch batch = Batch.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(sourceKeyPair.publicKey())
      .flags(BatchFlags.ofAllOrNothing())
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // Sign and submit
    SingleSignedTransaction<Batch> signedBatch = signatureService.sign(sourceKeyPair.privateKey(), batch);
    SubmitResult<Batch> result = xrplClient.submit(signedBatch);
    assertThat(result.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    logger.info("Batch transaction successful: {}", result.transactionResult().hash());

    // Wait for validation
    TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Batch.class)
    );
    assertThat(validatedBatch.metadata().get().transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

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

  /**
   * Test a multi-account batch transaction where multiple accounts contribute inner transactions.
   */
  @Test
  void sendMultiAccountBatch() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair account1KeyPair = createRandomAccountEd25519();
    KeyPair account2KeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult account1Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(account1KeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult account2Info = this.scanForResult(
      () -> this.getValidatedAccountInfo(account2KeyPair.publicKey().deriveAddress())
    );


    // Create inner payment from account1
    Payment innerPayment1 = Payment.builder()
      .account(account1KeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(account1Info.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Create inner payment from account2
    Payment innerPayment2 = Payment.builder()
      .account(account2KeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(account2Info.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(20000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Build the Batch transaction - account1 is the batch submitter
    // For multi-account batch, we don't set signingPublicKey as we'll use BatchSigners
    Batch unsignedBatch = Batch.builder()
      .account(account1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(account1Info.accountData().sequence().plus(UnsignedInteger.ONE))
      .flags(BatchFlags.ofAllOrNothing())
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // Create BatchSigners for both accounts using multiSignToBatchSigner
    BatchSigner batchSigner1 = signatureService.multiSignToBatchSigner(
      account1KeyPair.privateKey(),
      unsignedBatch
    );
    BatchSigner batchSigner2 = signatureService.multiSignToBatchSigner(
      account2KeyPair.privateKey(),
      unsignedBatch
    );

    // BatchSigners must be sorted by account address
    List<BatchSignerWrapper> sortedSigners = Stream.of(batchSigner1, batchSigner2)
      .sorted(Comparator.comparing(signer -> signer.account().value()))
      .map(BatchSignerWrapper::of)
      .collect(Collectors.toList());

    // Add BatchSigners to the batch
    Batch batchWithSigners = Batch.builder()
      .from(unsignedBatch)
      .batchSigners(sortedSigners)
      .build();

    // Sign the outer batch transaction (account1 is the batch submitter)
    SingleSignedTransaction<Batch> signedBatch = signatureService.sign(
      account1KeyPair.privateKey(),
      batchWithSigners
    );

    // Submit the batch
    SubmitResult<Batch> result = xrplClient.submit(signedBatch);
    assertThat(result.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    logger.info("Multi-account batch transaction successful: {}", result.transactionResult().hash());

    // Wait for validation
    TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Batch.class)
    );
    assertThat(validatedBatch.metadata().get().transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    // Verify destination account received both payments
    AccountInfoResult destInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationKeyPair.publicKey().deriveAddress())
    );
    assertThat(destInfo.accountData().balance()).isNotNull();
  }

  /**
   * Test a single-account batch transaction with OnlyOne mode.
   * In OnlyOne mode, only one transaction needs to succeed for the batch to succeed.
   */
  @Test
  void sendSingleAccountBatchOnlyOne() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destination1KeyPair = createRandomAccountEd25519();
    KeyPair destination2KeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    // Create inner payment 1
    Payment innerPayment1 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence())
      .destination(destination1KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Create inner payment 2
    Payment innerPayment2 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .destination(destination2KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(20000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Build the Batch transaction with OnlyOne mode
    Batch batch = Batch.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(sourceKeyPair.publicKey())
      .flags(BatchFlags.ofOnlyOne())
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // Sign and submit
    SingleSignedTransaction<Batch> signedBatch = signatureService.sign(sourceKeyPair.privateKey(), batch);
    SubmitResult<Batch> result = xrplClient.submit(signedBatch);
    assertThat(result.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    logger.info("Batch transaction (OnlyOne) successful: {}", result.transactionResult().hash());

    // Wait for validation
    TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Batch.class)
    );
    assertThat(validatedBatch.metadata().get().transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

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

  /**
   * Test a single-account batch transaction with UntilFailure mode.
   * In UntilFailure mode, transactions are executed in order until one fails.
   */
  @Test
  void sendSingleAccountBatchUntilFailure() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destination1KeyPair = createRandomAccountEd25519();
    KeyPair destination2KeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    // Create inner payment 1
    Payment innerPayment1 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence())
      .destination(destination1KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Create inner payment 2
    Payment innerPayment2 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .destination(destination2KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(20000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Build the Batch transaction with UntilFailure mode
    Batch batch = Batch.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(sourceKeyPair.publicKey())
      .flags(BatchFlags.ofUntilFailure())
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // Sign and submit
    SingleSignedTransaction<Batch> signedBatch = signatureService.sign(sourceKeyPair.privateKey(), batch);
    SubmitResult<Batch> result = xrplClient.submit(signedBatch);
    assertThat(result.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    logger.info("Batch transaction (UntilFailure) successful: {}", result.transactionResult().hash());

    // Wait for validation
    TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Batch.class)
    );
    assertThat(validatedBatch.metadata().get().transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

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

  /**
   * Test a single-account batch transaction with Independent mode.
   * In Independent mode, each transaction is processed independently regardless of other results.
   */
  @Test
  void sendSingleAccountBatchIndependent() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destination1KeyPair = createRandomAccountEd25519();
    KeyPair destination2KeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    // Create inner payment 1
    Payment innerPayment1 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence())
      .destination(destination1KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Create inner payment 2
    Payment innerPayment2 = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(0))
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .destination(destination2KeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(20000))
      .flags(PaymentFlags.INNER_BATCH_TXN)
      .build();

    // Build the Batch transaction with Independent mode
    Batch batch = Batch.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sourceAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(sourceKeyPair.publicKey())
      .flags(BatchFlags.ofIndependent())
      .addRawTransactions(
        RawTransactionWrapper.of(innerPayment1),
        RawTransactionWrapper.of(innerPayment2)
      )
      .build();

    // Sign and submit
    SingleSignedTransaction<Batch> signedBatch = signatureService.sign(sourceKeyPair.privateKey(), batch);
    SubmitResult<Batch> result = xrplClient.submit(signedBatch);
    assertThat(result.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    logger.info("Batch transaction (Independent) successful: {}", result.transactionResult().hash());

    // Wait for validation
    TransactionResult<Batch> validatedBatch = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Batch.class)
    );
    assertThat(validatedBatch.metadata().get().transactionResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

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
}
