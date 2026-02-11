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
import org.xrpl.xrpl4j.model.ledger.DelegateObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.DelegateSet;
import org.xrpl.xrpl4j.model.transactions.Permission;
import org.xrpl.xrpl4j.model.transactions.PermissionWrapper;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An Integration Test to validate submission of DelegateSet transactions.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class DelegateSetIT extends AbstractIT {

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
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Payment").build())
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("TrustSet").build())
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
    List<PermissionWrapper> permissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Payment").build())
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
    List<PermissionWrapper> initialPermissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("Payment").build())
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
    List<PermissionWrapper> updatedPermissions = Arrays.asList(
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("TrustSet").build())
        .build(),
      PermissionWrapper.builder()
        .permission(Permission.builder().permissionValue("OfferCreate").build())
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
}

