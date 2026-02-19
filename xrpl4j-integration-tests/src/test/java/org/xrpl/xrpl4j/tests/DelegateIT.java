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
import org.xrpl.xrpl4j.model.transactions.DelegateSet;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Permission;
import org.xrpl.xrpl4j.model.transactions.PermissionWrapper;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Integration tests for the Delegate field on transactions (XLS-75 Permission Delegation).
 *
 * <p><b>IMPORTANT:</b> These tests are currently disabled because the PermissionDelegation amendment
 * is not yet implemented in any released version of rippled. The amendment was disabled in rippled 2.6.1
 * due to a bug and has not been re-enabled. These tests will fail with "temDISABLED" until XLS-75 is
 * implemented and enabled on a rippled node.</p>
 *
 * <p>To run these tests (they will fail): mvn test -Dtest=DelegateIT -DenableXLS75Tests=true</p>
 */
@DisabledIf(value = "shouldNotRun", disabledReason = "XLS-75 PermissionDelegation is not yet implemented in rippled")
public class DelegateIT extends AbstractIT {

  static boolean shouldNotRun() {
    // XLS-75 (PermissionDelegation) is not yet implemented in any released version of rippled.
    // The amendment was disabled in rippled 2.6.1 due to a bug and has not been re-enabled.
    // These tests will always be skipped until XLS-75 is available in rippled.
    //
    // To force run these tests (they will fail with temDISABLED):
    //   mvn test -Dtest=DelegateIT -DenableXLS75Tests=true
    return System.getProperty("enableXLS75Tests") == null;
  }

  public static final String SUCCESS_STATUS = "tesSUCCESS";

  private NetworkId networkId;

  @BeforeEach
  public void setUp() throws JsonRpcClientErrorException {
    // Query the network ID from the server
    networkId = xrplClient.serverInformation().info().networkId()
      .orElse(NetworkId.of(UnsignedInteger.ZERO));
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
        PermissionWrapper.builder().permission(Permission.of(TransactionType.PAYMENT)).build(),
        PermissionWrapper.builder().permission(Permission.of(TransactionType.TRUST_SET)).build()
      )
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .networkId(networkId)
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
      .addPermissions(PermissionWrapper.builder().permission(Permission.of(TransactionType.PAYMENT)).build())
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .networkId(networkId)
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
      .networkId(networkId)
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
      .addPermissions(PermissionWrapper.builder().permission(Permission.of(TransactionType.TRUST_SET)).build())
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .networkId(networkId)
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
      .networkId(networkId)
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
        PermissionWrapper.builder().permission(Permission.of(GranularPermission.TRUSTLINE_AUTHORIZE)).build(),
        PermissionWrapper.builder().permission(Permission.of(GranularPermission.TRUSTLINE_FREEZE)).build(),
        PermissionWrapper.builder().permission(Permission.of(GranularPermission.PAYMENT_MINT)).build()
      )
      .signingPublicKey(delegatingAccountKeyPair.publicKey())
      .networkId(networkId)
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
}
