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
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.DelegateSet;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Permission;
import org.xrpl.xrpl4j.model.transactions.PermissionWrapper;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;

/**
 * Integration tests for the Delegate field on transactions (XLS-75 Permission Delegation).
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "DelegateIT only runs on local rippled node with XLS-75 enabled.")
public class DelegateIT extends AbstractIT {

  static boolean shouldNotRun() {
    // XLS-75 is not yet enabled on testnet, so these tests only run on local rippled
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null;
  }

  public static final String SUCCESS_STATUS = "tesSUCCESS";

  @Test
  public void testDelegateSetTransaction() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create two accounts: delegating account and delegate
    KeyPair delegatingAccountKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult delegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    // Create a DelegateSet transaction to authorize the delegate
    DelegateSet delegateSet = DelegateSet.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatingAccountInfo.accountData().sequence())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .addPermissions(
        PermissionWrapper.of(Permission.of(TransactionType.PAYMENT)),
        PermissionWrapper.of(Permission.of(TransactionType.TRUST_SET))
      )
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .build();

    SingleSignedTransaction<DelegateSet> signedDelegateSet = signatureService.sign(
      delegatingAccountKeyPair.privateKey(), delegateSet
    );
    SubmitResult<DelegateSet> result = xrplClient.submit(signedDelegateSet);
    assertThat(result.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("DelegateSet successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    TransactionResult<DelegateSet> validatedDelegateSet = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), DelegateSet.class)
    );

    assertThat(validatedDelegateSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

  @Test
  public void testPaymentWithDelegate() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create three accounts: delegating account, delegate, and destination
    KeyPair delegatingAccountKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // First, authorize the delegate using DelegateSet
    AccountInfoResult delegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatingAccountInfo.accountData().sequence())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .addPermissions(PermissionWrapper.of(Permission.of(TransactionType.PAYMENT)))
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .build();

    SingleSignedTransaction<DelegateSet> signedDelegateSet = signatureService.sign(
      delegatingAccountKeyPair.privateKey(), delegateSet
    );
    SubmitResult<DelegateSet> delegateSetResult = xrplClient.submit(signedDelegateSet);
    assertThat(delegateSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(delegateSetResult.transactionResult().hash(), DelegateSet.class)
    );

    // Get updated account info for the delegating account
    AccountInfoResult updatedDelegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    // Get delegate account info to check balance before payment
    AccountInfoResult delegateAccountInfoBefore = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegateKeyPair.publicKey().deriveAddress())
    );

