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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.ledger.DelegateObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.AccountPermission;
import org.xrpl.xrpl4j.model.transactions.AccountPermissionWrapper;
import org.xrpl.xrpl4j.model.transactions.DelegateSet;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Integration tests for XLS-75 Permission Delegation.
 *
 * <p>This test class covers:</p>
 * <ul>
 *   <li>DelegateSet transaction functionality (creating, updating, removing delegations)</li>
 *   <li>Using the Delegate field on transactions (Payment, TrustSet)</li>
 *   <li>Granular permissions</li>
 *   <li>Verification of DelegateObject ledger entries</li>
 * </ul>
 */
public class DelegateIT extends AbstractIT {

  public static final String SUCCESS_STATUS = "tesSUCCESS";

  private NetworkId networkId;

  @BeforeEach
  public void setUp() throws JsonRpcClientErrorException {
    networkId = xrplClient.serverInformation().info().networkId().orElse(null);
    logger.info("Using NetworkID: {}", networkId);
  }

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
        AccountPermissionWrapper.builder().permission(AccountPermission.of(TransactionType.PAYMENT)).build(),
        AccountPermissionWrapper.builder().permission(AccountPermission.of(TransactionType.TRUST_SET)).build()
      )
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .build();

    SingleSignedTransaction<DelegateSet> signedDelegateSet = signatureService.sign(
      delegatingAccountKeyPair.privateKey(), delegateSet
    );
    SubmitResult<DelegateSet> result = xrplClient.submit(signedDelegateSet);
    assertThat(result.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("DelegateSet successful: https://testnet.xrpl.org/transactions/{}",
      result.transactionResult().hash());

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
      .addPermissions(AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.PAYMENT))
        .build())
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

    // Get updated account info
    AccountInfoResult updatedDelegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    // Get delegate account info to check balance before payment
    final AccountInfoResult delegateAccountInfoBefore = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegateKeyPair.publicKey().deriveAddress())
    );

    // Now send a Payment transaction with the Delegate field
    // The delegate signs the transaction, but the Account field is the delegating account
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(12345);
    Payment payment = Payment.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())  // Delegating account
      .delegate(delegateKeyPair.publicKey().deriveAddress())          // Delegate
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(updatedDelegatingAccountInfo.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(amount)
      .signingPublicKey(delegateKeyPair.publicKey())  // Delegate's public key
      .build();

    // Sign with delegate's private key
    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(delegateKeyPair.privateKey(), payment);
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedPayment);
    assertThat(paymentResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("Payment with Delegate successful: https://testnet.xrpl.org/transactions/{}",
      paymentResult.transactionResult().hash());

    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(paymentResult.transactionResult().hash(), Payment.class)
    );

    assertThat(validatedPayment.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    // Verify that the delegate paid the fees (delegate's balance should decrease)
    AccountInfoResult delegateAccountInfoAfter = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegateKeyPair.publicKey().deriveAddress())
    );

    XrpCurrencyAmount delegateBalanceBefore = delegateAccountInfoBefore.accountData().balance();
    XrpCurrencyAmount delegateBalanceAfter = delegateAccountInfoAfter.accountData().balance();
    XrpCurrencyAmount fee = payment.fee();

    // Delegate's balance should decrease by the fee amount
    assertThat(delegateBalanceAfter.plus(fee)).isEqualTo(delegateBalanceBefore);

    // Verify that only the delegating account's sequence number was incremented
    AccountInfoResult finalDelegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    assertThat(finalDelegatingAccountInfo.accountData().sequence())
      .isEqualTo(updatedDelegatingAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE));
  }

  @Test
  public void testTrustSetWithDelegate() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create three accounts: delegating account, delegate, and issuer
    KeyPair delegatingAccountKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();
    KeyPair issuerKeyPair = createRandomAccountEd25519();

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
      .addPermissions(AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.TRUST_SET))
        .build())
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

    // Get updated account info
    AccountInfoResult updatedDelegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    // Now send a TrustSet transaction with the Delegate field
    IssuedCurrencyAmount limitAmount = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .value("1000")
      .build();

    TrustSet trustSet = TrustSet.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())  // Delegating account
      .delegate(delegateKeyPair.publicKey().deriveAddress())          // Delegate
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(updatedDelegatingAccountInfo.accountData().sequence())
      .limitAmount(limitAmount)
      .flags(TrustSetFlags.empty())
      .signingPublicKey(delegateKeyPair.publicKey())  // Delegate's public key
      .build();

    // Sign with delegate's private key
    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(delegateKeyPair.privateKey(), trustSet);
    SubmitResult<TrustSet> trustSetResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("TrustSet with Delegate successful: https://testnet.xrpl.org/transactions/{}",
      trustSetResult.transactionResult().hash());

    TransactionResult<TrustSet> validatedTrustSet = this.scanForResult(
      () -> this.getValidatedTransaction(trustSetResult.transactionResult().hash(), TrustSet.class)
    );

    assertThat(validatedTrustSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

  @Test
  public void testDelegateSetWithGranularPermissions() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create two accounts: delegating account and delegate
    KeyPair delegatingAccountKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult delegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    // Create a DelegateSet transaction with granular permissions
    DelegateSet delegateSet = DelegateSet.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatingAccountInfo.accountData().sequence())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .addPermissions(
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(GranularPermission.TRUSTLINE_AUTHORIZE))
          .build(),
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(GranularPermission.TRUSTLINE_FREEZE))
          .build(),
        AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(GranularPermission.PAYMENT_MINT))
          .build()
      )
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .build();

    SingleSignedTransaction<DelegateSet> signedDelegateSet = signatureService.sign(
      delegatingAccountKeyPair.privateKey(), delegateSet
    );
    SubmitResult<DelegateSet> result = xrplClient.submit(signedDelegateSet);
    assertThat(result.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("DelegateSet with granular permissions successful: https://testnet.xrpl.org/transactions/{}",
      result.transactionResult().hash());

    TransactionResult<DelegateSet> validatedDelegateSet = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), DelegateSet.class)
    );

    assertThat(validatedDelegateSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

  @Test
  public void createDelegationWithPermissions() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random delegator and delegate accounts
    KeyPair delegatorKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();

    AccountInfoResult delegatorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatorKeyPair.publicKey().deriveAddress())
    );

    /////////////////////////
    // Create a DelegateSet transaction with permissions
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("TrustSet").build())
        .build()
    );

    FeeResult feeResult = xrplClient.fee();
    DelegateSet delegateSet = DelegateSet.builder()
      .account(delegatorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatorAccountInfo.accountData().sequence())
      .signingPublicKey(delegatorKeyPair.publicKey())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .permissions(permissions)
      .build();

    SingleSignedTransaction<DelegateSet> signedDelegateSet = this.signatureService.sign(
      delegatorKeyPair.privateKey(), delegateSet
    );

    SubmitResult<DelegateSet> result = xrplClient.submit(signedDelegateSet);

    assertThat(result.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "DelegateSet transaction successful. Hash: {}",
      result.transactionResult().hash()
    );

    /////////////////////////
    // Wait for the transaction to be validated
    TransactionResult<DelegateSet> validatedTransaction = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), DelegateSet.class)
    );

    // Verify key fields of the validated transaction match the original
    assertThat(validatedTransaction.transaction().account()).isEqualTo(delegateSet.account());
    assertThat(validatedTransaction.transaction().authorize()).isEqualTo(delegateSet.authorize());
    assertThat(validatedTransaction.transaction().permissions()).isEqualTo(delegateSet.permissions());

    /////////////////////////
    // Verify the Delegate object was created
    AccountObjectsResult accountObjects = this.scanForResult(
      () -> getValidatedAccountObjects(delegatorKeyPair.publicKey().deriveAddress())
    );

    Optional<DelegateObject> delegateObject = accountObjects.accountObjects().stream()
      .filter(obj -> DelegateObject.class.isAssignableFrom(obj.getClass()))
      .map(obj -> (DelegateObject) obj)
      .filter(obj -> obj.authorize().equals(delegateKeyPair.publicKey().deriveAddress()))
      .findFirst();

    assertThat(delegateObject).isPresent();
    assertThat(delegateObject.get().account()).isEqualTo(delegatorKeyPair.publicKey().deriveAddress());
    assertThat(delegateObject.get().authorize()).isEqualTo(delegateKeyPair.publicKey().deriveAddress());
    assertThat(delegateObject.get().permissions()).hasSize(2);
    assertThat(delegateObject.get().permissions().get(0).permission().permissionValue()).isEqualTo("Payment");
    assertThat(delegateObject.get().permissions().get(1).permission().permissionValue()).isEqualTo("TrustSet");

    logger.info("Delegate object verified successfully");
  }

  @Test
  public void removeDelegation() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random delegator and delegate accounts
    KeyPair delegatorKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();

    AccountInfoResult delegatorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatorKeyPair.publicKey().deriveAddress())
    );

    /////////////////////////
    // First, create a delegation
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
        .build()
    );

    FeeResult feeResult = xrplClient.fee();
    DelegateSet createDelegateSet = DelegateSet.builder()
      .account(delegatorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatorAccountInfo.accountData().sequence())
      .signingPublicKey(delegatorKeyPair.publicKey())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .permissions(permissions)
      .build();

    SingleSignedTransaction<DelegateSet> signedCreateDelegateSet = this.signatureService.sign(
      delegatorKeyPair.privateKey(), createDelegateSet
    );

    SubmitResult<DelegateSet> createResult = xrplClient.submit(signedCreateDelegateSet);
    assertThat(createResult.engineResult()).isEqualTo("tesSUCCESS");

    /////////////////////////
    // Wait for the create transaction to be validated
    TransactionResult<DelegateSet> validatedCreateTransaction = this.scanForResult(
      () -> this.getValidatedTransaction(createResult.transactionResult().hash(), DelegateSet.class)
    );

    /////////////////////////
    // Verify the Delegate object was created
    AccountObjectsResult accountObjectsAfterCreate = this.scanForResult(
      () -> getValidatedAccountObjects(delegatorKeyPair.publicKey().deriveAddress())
    );

    Optional<DelegateObject> delegateObjectAfterCreate = accountObjectsAfterCreate.accountObjects().stream()
      .filter(obj -> DelegateObject.class.isAssignableFrom(obj.getClass()))
      .map(obj -> (DelegateObject) obj)
      .filter(obj -> obj.authorize().equals(delegateKeyPair.publicKey().deriveAddress()))
      .findFirst();

    assertThat(delegateObjectAfterCreate).isPresent();
    logger.info("Delegate object created successfully");

    /////////////////////////
    // Get updated account info for next sequence number
    AccountInfoResult updatedDelegatorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatorKeyPair.publicKey().deriveAddress())
    );

    /////////////////////////
    // Now remove the delegation by sending DelegateSet with empty permissions
    DelegateSet removeDelegateSet = DelegateSet.builder()
      .account(delegatorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(updatedDelegatorAccountInfo.accountData().sequence())
      .signingPublicKey(delegatorKeyPair.publicKey())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .permissions(Collections.emptyList())
      .build();

    SingleSignedTransaction<DelegateSet> signedRemoveDelegateSet = this.signatureService.sign(
      delegatorKeyPair.privateKey(), removeDelegateSet
    );

    SubmitResult<DelegateSet> removeResult = xrplClient.submit(signedRemoveDelegateSet);
    assertThat(removeResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "DelegateSet removal transaction successful. Hash: {}",
      removeResult.transactionResult().hash()
    );

    /////////////////////////
    // Wait for the removal transaction to be validated
    TransactionResult<DelegateSet> validatedRemoveTransaction = this.scanForResult(
      () -> this.getValidatedTransaction(removeResult.transactionResult().hash(), DelegateSet.class)
    );

    /////////////////////////
    // Verify the Delegate object was removed
    AccountObjectsResult accountObjectsAfterRemove = this.scanForResult(
      () -> getValidatedAccountObjects(delegatorKeyPair.publicKey().deriveAddress())
    );

    Optional<DelegateObject> delegateObjectAfterRemove = accountObjectsAfterRemove.accountObjects().stream()
      .filter(obj -> DelegateObject.class.isAssignableFrom(obj.getClass()))
      .map(obj -> (DelegateObject) obj)
      .filter(obj -> obj.authorize().equals(delegateKeyPair.publicKey().deriveAddress()))
      .findFirst();

    assertThat(delegateObjectAfterRemove).isEmpty();
    logger.info("Delegate object removed successfully");
  }

  @Test
  public void updateDelegationPermissions() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random delegator and delegate accounts
    KeyPair delegatorKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();

    AccountInfoResult delegatorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatorKeyPair.publicKey().deriveAddress())
    );

    /////////////////////////
    // Create initial delegation with Payment permission
    List<AccountPermissionWrapper> initialPermissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
        .build()
    );

    FeeResult feeResult = xrplClient.fee();
    DelegateSet initialDelegateSet = DelegateSet.builder()
      .account(delegatorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatorAccountInfo.accountData().sequence())
      .signingPublicKey(delegatorKeyPair.publicKey())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .permissions(initialPermissions)
      .build();

    SingleSignedTransaction<DelegateSet> signedInitialDelegateSet = this.signatureService.sign(
      delegatorKeyPair.privateKey(), initialDelegateSet
    );

    SubmitResult<DelegateSet> initialResult = xrplClient.submit(signedInitialDelegateSet);
    assertThat(initialResult.engineResult()).isEqualTo("tesSUCCESS");

    /////////////////////////
    // Wait for initial transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(initialResult.transactionResult().hash(), DelegateSet.class)
    );

    /////////////////////////
    // Get updated account info
    AccountInfoResult updatedDelegatorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatorKeyPair.publicKey().deriveAddress())
    );

    /////////////////////////
    // Update delegation with different permissions
    List<AccountPermissionWrapper> updatedPermissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("TrustSet").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("OfferCreate").build())
        .build()
    );

    DelegateSet updateDelegateSet = DelegateSet.builder()
      .account(delegatorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(updatedDelegatorAccountInfo.accountData().sequence())
      .signingPublicKey(delegatorKeyPair.publicKey())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .permissions(updatedPermissions)
      .build();

    SingleSignedTransaction<DelegateSet> signedUpdateDelegateSet = this.signatureService.sign(
      delegatorKeyPair.privateKey(), updateDelegateSet
    );

    SubmitResult<DelegateSet> updateResult = xrplClient.submit(signedUpdateDelegateSet);
    assertThat(updateResult.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "DelegateSet update transaction successful. Hash: {}",
      updateResult.transactionResult().hash()
    );

    /////////////////////////
    // Wait for update transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(updateResult.transactionResult().hash(), DelegateSet.class)
    );

    /////////////////////////
    // Verify the Delegate object has updated permissions
    AccountObjectsResult accountObjectsAfterUpdate = this.scanForResult(
      () -> getValidatedAccountObjects(delegatorKeyPair.publicKey().deriveAddress())
    );

    Optional<DelegateObject> delegateObjectAfterUpdate = accountObjectsAfterUpdate.accountObjects().stream()
      .filter(obj -> DelegateObject.class.isAssignableFrom(obj.getClass()))
      .map(obj -> (DelegateObject) obj)
      .filter(obj -> obj.authorize().equals(delegateKeyPair.publicKey().deriveAddress()))
      .findFirst();

    assertThat(delegateObjectAfterUpdate).isPresent();
    assertThat(delegateObjectAfterUpdate.get().permissions()).hasSize(2);
    assertThat(delegateObjectAfterUpdate.get().permissions().get(0).permission().permissionValue())
      .isEqualTo("TrustSet");
    assertThat(delegateObjectAfterUpdate.get().permissions().get(1).permission().permissionValue())
      .isEqualTo("OfferCreate");

    logger.info("Delegate object permissions updated successfully");
  }

  @Test
  public void testLedgerEntryDelegate() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random delegator and delegate accounts
    KeyPair delegatorKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();

    AccountInfoResult delegatorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatorKeyPair.publicKey().deriveAddress())
    );

    /////////////////////////
    // Create a delegation
    List<AccountPermissionWrapper> permissions = Arrays.asList(
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("Payment").build())
        .build(),
      AccountPermissionWrapper.builder()
        .permission(AccountPermission.builder().permissionValue("TrustSet").build())
        .build()
    );

    FeeResult feeResult = xrplClient.fee();
    DelegateSet delegateSet = DelegateSet.builder()
      .account(delegatorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatorAccountInfo.accountData().sequence())
      .signingPublicKey(delegatorKeyPair.publicKey())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .permissions(permissions)
      .build();

    SingleSignedTransaction<DelegateSet> signedDelegateSet = this.signatureService.sign(
      delegatorKeyPair.privateKey(), delegateSet
    );

    SubmitResult<DelegateSet> result = xrplClient.submit(signedDelegateSet);
    assertThat(result.engineResult()).isEqualTo("tesSUCCESS");

    /////////////////////////
    // Wait for the transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), DelegateSet.class)
    );

    /////////////////////////
    // Get the Delegate object via account_objects to verify it exists
    AccountObjectsResult accountObjects = this.scanForResult(
      () -> getValidatedAccountObjects(delegatorKeyPair.publicKey().deriveAddress())
    );

    Optional<DelegateObject> delegateObjectFromAccountObjects = accountObjects.accountObjects().stream()
      .filter(obj -> DelegateObject.class.isAssignableFrom(obj.getClass()))
      .map(obj -> (DelegateObject) obj)
      .filter(obj -> obj.authorize().equals(delegateKeyPair.publicKey().deriveAddress()))
      .findFirst();

    assertThat(delegateObjectFromAccountObjects).isPresent();
    DelegateObject expectedDelegateObject = delegateObjectFromAccountObjects.get();

    /////////////////////////
    // Query the Delegate object via ledger_entry using DelegateLedgerEntryParams
    org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult<DelegateObject> delegateEntry = xrplClient.ledgerEntry(
      org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams.delegate(
        org.xrpl.xrpl4j.model.client.ledger.DelegateLedgerEntryParams.builder()
          .account(delegatorKeyPair.publicKey().deriveAddress())
          .authorize(delegateKeyPair.publicKey().deriveAddress())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(delegateEntry.node()).isEqualTo(expectedDelegateObject);
    assertThat(delegateEntry.node().account()).isEqualTo(delegatorKeyPair.publicKey().deriveAddress());
    assertThat(delegateEntry.node().authorize()).isEqualTo(delegateKeyPair.publicKey().deriveAddress());
    assertThat(delegateEntry.node().permissions()).hasSize(2);

    /////////////////////////
    // Also query by index to verify both methods return the same object
    org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult<DelegateObject> entryByIndex = xrplClient.ledgerEntry(
      org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams.index(
        expectedDelegateObject.index(),
        DelegateObject.class,
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(entryByIndex.node()).isEqualTo(delegateEntry.node());

    /////////////////////////
    // Query by index without type parameter
    org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams.index(
        expectedDelegateObject.index(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());

    logger.info("Delegate object successfully queried via ledger_entry");
  }

  /**
   * Negative test: Verify that a delegate cannot execute a transaction without proper permission.
   * This test creates a delegation with Payment permission, then tries to execute a TrustSet transaction
   * which should fail with terNO_DELEGATE_PERMISSION.
   */
  @Test
  public void testDelegateWithoutPermission() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create three accounts: delegating account, delegate, and issuer
    KeyPair delegatingAccountKeyPair = createRandomAccountEd25519();
    KeyPair delegateKeyPair = createRandomAccountEd25519();
    KeyPair issuerKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // First, authorize the delegate with ONLY Payment permission
    AccountInfoResult delegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatingAccountInfo.accountData().sequence())
      .authorize(delegateKeyPair.publicKey().deriveAddress())
      .addPermissions(AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.PAYMENT))
        .build())
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

    // Get updated account info
    AccountInfoResult updatedDelegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    // Now try to send a TrustSet transaction (which the delegate does NOT have permission for)
    IssuedCurrencyAmount limitAmount = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .value("1000")
      .build();

    TrustSet trustSet = TrustSet.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())  // Delegating account
      .delegate(delegateKeyPair.publicKey().deriveAddress())          // Delegate
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(updatedDelegatingAccountInfo.accountData().sequence())
      .limitAmount(limitAmount)
      .flags(TrustSetFlags.empty())
      .signingPublicKey(delegateKeyPair.publicKey())  // Delegate's public key
      .build();

    // Sign with delegate's private key
    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(delegateKeyPair.privateKey(), trustSet);
    SubmitResult<TrustSet> trustSetResult = xrplClient.submit(signedTrustSet);

    // Should fail with terNO_DELEGATE_PERMISSION
    assertThat(trustSetResult.engineResult()).isEqualTo("terNO_DELEGATE_PERMISSION");
    logger.info("TrustSet without permission correctly failed with: {}", trustSetResult.engineResult());
  }

  /**
   * Negative test: Verify that the wrong delegate cannot use a delegation.
   * This test creates a delegation for delegate1, then tries to execute a transaction
   * using delegate2's signature, which should fail.
   */
  @Test
  public void testWrongDelegateCannotUsePermission() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create four accounts: delegating account, authorized delegate, unauthorized delegate, and destination
    KeyPair delegatingAccountKeyPair = createRandomAccountEd25519();
    KeyPair authorizedDelegateKeyPair = createRandomAccountEd25519();
    KeyPair unauthorizedDelegateKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // Authorize only the first delegate
    AccountInfoResult delegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    DelegateSet delegateSet = DelegateSet.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(delegatingAccountInfo.accountData().sequence())
      .authorize(authorizedDelegateKeyPair.publicKey().deriveAddress())  // Only authorize this delegate
      .addPermissions(AccountPermissionWrapper.builder()
        .permission(AccountPermission.of(TransactionType.PAYMENT))
        .build())
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

    // Get updated account info
    AccountInfoResult updatedDelegatingAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(delegatingAccountKeyPair.publicKey().deriveAddress())
    );

    // Now try to send a Payment with the WRONG delegate (unauthorizedDelegate)
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(12345);
    Payment payment = Payment.builder()
      .account(delegatingAccountKeyPair.publicKey().deriveAddress())      // Delegating account
      .delegate(unauthorizedDelegateKeyPair.publicKey().deriveAddress())  // WRONG delegate
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(updatedDelegatingAccountInfo.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(amount)
      .signingPublicKey(unauthorizedDelegateKeyPair.publicKey())  // Wrong delegate's public key
      .build();

    // Sign with wrong delegate's private key
    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(
      unauthorizedDelegateKeyPair.privateKey(), payment
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedPayment);

    // Should fail with terNO_DELEGATE_PERMISSION (no delegation exists for this delegate)
    assertThat(paymentResult.engineResult()).isEqualTo("terNO_DELEGATE_PERMISSION");
    logger.info("Payment with wrong delegate correctly failed with: {}", paymentResult.engineResult());
  }

  /**
   * Negative test: Verify that an account cannot delegate to itself.
   * This test tries to create a DelegateSet where Account and Authorize are the same,
   * which should fail with temBAD_SIGNER.
   */
  @Test
  public void testCannotDelegateToSelf() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create one account that will try to delegate to itself
    KeyPair accountKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(accountKeyPair.publicKey().deriveAddress())
    );

    // Try to create a DelegateSet where Account == Authorize (self-delegation)
    // This should throw IllegalArgumentException during build() due to client-side validation
    try {
      DelegateSet delegateSet = DelegateSet.builder()
        .account(accountKeyPair.publicKey().deriveAddress())
        .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
        .sequence(accountInfo.accountData().sequence())
        .authorize(accountKeyPair.publicKey().deriveAddress())  // Same as Account - self delegation
        .addPermissions(AccountPermissionWrapper.builder()
          .permission(AccountPermission.of(TransactionType.PAYMENT))
          .build())
        .signingPublicKey(accountKeyPair.publicKey())
        .build();

      // If we get here, the validation didn't work
      throw new AssertionError("Expected IllegalArgumentException for self-delegation");
    } catch (IllegalArgumentException e) {
      // Expected - client-side validation caught the self-delegation attempt
      assertThat(e.getMessage()).contains("Authorize and Account must be different");
      logger.info("Self-delegation correctly prevented by client-side validation: {}", e.getMessage());
    }
  }
}
