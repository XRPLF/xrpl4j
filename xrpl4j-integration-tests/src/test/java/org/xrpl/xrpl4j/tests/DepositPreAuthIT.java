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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.DepositPreAuthCredential;
import org.xrpl.xrpl4j.model.client.ledger.DepositPreAuthLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Credential;
import org.xrpl.xrpl4j.model.transactions.CredentialAccept;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An Integration Test to validate submission of DepositPreAuth transactions.
 */
public class DepositPreAuthIT extends AbstractIT {

  private static final CredentialType[] GOOD_CREDENTIALS_TYPES =
    {CredentialType.ofPlainText("driver licence"), CredentialType.ofPlainText("voting card")};

  @Test
  public void preauthorizeAccountAndReceivePayment() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random sender/receiver accounts
    KeyPair receiverKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();

    /////////////////////////
    // Enable Deposit Authorization on the receiver account
    AccountInfoResult receiverAccountInfo = enableDepositAuthorization(receiverKeyPair);

    /////////////////////////
    // Give Preauthorization for the sender to send a funds to the receiver
    FeeResult feeResult = xrplClient.fee();
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .authorize(senderKeyPair.publicKey().deriveAddress())
      .build();

    SingleSignedTransaction<DepositPreAuth> singedDepositPreAuth = this.signatureService.sign(
      receiverKeyPair.privateKey(), depositPreAuth
    );

    SubmitResult<DepositPreAuth> result = xrplClient.submit(singedDepositPreAuth);

    assertThat(result.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "DepositPreauth transaction successful. https://testnet.xrpl.org/transactions/{}",
      result.transactionResult().hash()
    );

    /////////////////////////
    // Validate that the DepositPreAuthObject was added to the receiver's account objects
    DepositPreAuthObject preAuthObject = (DepositPreAuthObject) this.scanForResult(
        () -> this.getValidatedAccountObjects(receiverKeyPair.publicKey().deriveAddress()),
        accountObjects ->
          accountObjects.accountObjects().stream().anyMatch(object ->
            DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
              ((DepositPreAuthObject) object).authorize().get().equals(senderKeyPair.publicKey().deriveAddress())
          )
      ).accountObjects().stream()
      .filter(object -> DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
        ((DepositPreAuthObject) object).authorize().get().equals(senderKeyPair.publicKey().deriveAddress()))
      .findFirst()
      .get();

    assertEntryEqualsObjectFromAccountObjects(depositPreAuth, preAuthObject);

    /////////////////////////
    // Validate that the `deposit_authorized` client call is implemented properly by ensuring it aligns with the
    // result found in the account object.
    final boolean depositAuthorized = xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
      .sourceAccount(senderKeyPair.publicKey().deriveAddress())
      .destinationAccount(receiverKeyPair.publicKey().deriveAddress())
      .build()).depositAuthorized();
    assertThat(depositAuthorized).isTrue();

    /////////////////////////
    // Send a Payment from the sender wallet to the receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    Payment payment = Payment.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .build();

    SingleSignedTransaction<Payment> singedPayment = signatureService.sign(
      senderKeyPair.privateKey(), payment
    );

    SubmitResult<Payment> paymentResult = xrplClient.submit(singedPayment);
    assertThat(result.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful. https://testnet.xrpl.org/transactions/{}",
      paymentResult.transactionResult().hash()
    );

    /////////////////////////
    // Validate that the Payment was included in a validated ledger
    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(paymentResult.transactionResult().hash(), Payment.class)
    );

    /////////////////////////
    // And validate that the receiver's balance was updated correctly
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress()),
      info -> {
        XrpCurrencyAmount expectedBalance = receiverAccountInfo.accountData().balance()
          .minus(depositPreAuth.fee())
          .plus(((XrpCurrencyAmount) validatedPayment.transaction().amount()));
        return info.accountData().balance().equals(expectedBalance);
      });
  }

  @Test
  public void preauthorizeCredentialsAndReceivePayment() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random issuer/receiver accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    /////////////////////////
    // Enable Deposit Authorization on the receiver account
    AccountInfoResult receiverAccountInfo = enableDepositAuthorization(receiverKeyPair);

    /////////////////////////
    // Submit a DepositPreAuth transaction with authorizeCredentials set.
    List<CredentialWrapper> credsToAuthorize = Arrays.stream(GOOD_CREDENTIALS_TYPES).map(
      credentialType -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .credentialType(credentialType)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .build()
        ).build()).collect(Collectors.toList());

    FeeResult feeResult = xrplClient.fee();

    DepositPreAuth depositPreAuthTx = DepositPreAuth.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .authorizeCredentials(credsToAuthorize)
      .build();

    SingleSignedTransaction<DepositPreAuth> singedDepositPreAuth = this.signatureService.sign(
      receiverKeyPair.privateKey(), depositPreAuthTx
    );

    SubmitResult<DepositPreAuth> depositPreAuthSubmitResult = xrplClient.submit(singedDepositPreAuth);

    assertThat(depositPreAuthSubmitResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(depositPreAuthSubmitResult.transactionResult().hash(), CredentialAccept.class)
    );

    /////////////////////////
    // Validate that the DepositPreAuthObject was added to the receiver's account objects
    DepositPreAuthObject preAuthObject = (DepositPreAuthObject) this.scanForResult(
        () -> this.getValidatedAccountObjects(receiverKeyPair.publicKey().deriveAddress()),
        accountObjects ->
          accountObjects.accountObjects().stream().anyMatch(object ->
            DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
              ((DepositPreAuthObject) object).authorizeCredentials().equals(credsToAuthorize)
          )
      ).accountObjects().stream()
      .filter(object -> DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
        ((DepositPreAuthObject) object).authorizeCredentials().equals(credsToAuthorize))
      .findFirst()
      .get();

    assertEntryEqualsObjectFromAccountObjects(depositPreAuthTx, preAuthObject);

    /////////////////////////
    // Create credential from issuer to sender account.
    KeyPair senderKeyPair = createRandomAccountEd25519();
    createCredentials(issuerKeyPair, senderKeyPair, GOOD_CREDENTIALS_TYPES);

    List<Hash256> credObjectIds = getCredentialObjectIds(issuerKeyPair, senderKeyPair, GOOD_CREDENTIALS_TYPES);

    /////////////////////////
    // Validate badCredentials (credentials aren't accepted) is thrown since credentials are not yet accepted.
    assertThrows(JsonRpcClientErrorException.class,
      () -> xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
        .sourceAccount(senderKeyPair.publicKey().deriveAddress())
        .destinationAccount(receiverKeyPair.publicKey().deriveAddress())
        .credentials(credObjectIds)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()),
      "badCredentials (credentials aren't accepted)"
    );

    /////////////////////////
    // Accept credential from the issuer.
    acceptCredentials(issuerKeyPair, senderKeyPair, GOOD_CREDENTIALS_TYPES);

    /////////////////////////
    // Validate that the `deposit_authorized` client call is implemented properly by ensuring it aligns with the
    // result found in the account object.
    final boolean depositAuthorizedAfterAccepting =
      xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
        .sourceAccount(senderKeyPair.publicKey().deriveAddress())
        .destinationAccount(receiverKeyPair.publicKey().deriveAddress())
        .credentials(credObjectIds)
        .build()).depositAuthorized();

    assertThat(depositAuthorizedAfterAccepting).isTrue();

    /////////////////////////
    // Send a Payment from the sender wallet to the receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    Payment payment = Payment.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .credentialIds(credObjectIds)
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .build();

    SingleSignedTransaction<Payment> singedPayment = signatureService.sign(
      senderKeyPair.privateKey(), payment
    );

    SubmitResult<Payment> paymentResult = xrplClient.submit(singedPayment);
    assertThat(paymentResult.engineResult()).isEqualTo("tesSUCCESS");

    /////////////////////////
    // Validate that the Payment was included in a validated ledger
    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(paymentResult.transactionResult().hash(), Payment.class)
    );

    /////////////////////////
    // And validate that the receiver's balance was updated correctly
    this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverKeyPair.publicKey().deriveAddress()),
      info -> {
        XrpCurrencyAmount expectedBalance = receiverAccountInfo.accountData().balance()
          .minus(payment.fee())
          .plus(((XrpCurrencyAmount) validatedPayment.transaction().amount()));
        return info.accountData().balance().equals(expectedBalance);
      });
  }

  @Test
  public void unableToReceivePaymentWithWithoutCreds() throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random issuer/receiver accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair receiverKeyPair = createRandomAccountEd25519();

    /////////////////////////
    // Enable Deposit Authorization on the receiver account
    AccountInfoResult receiverAccountInfo = enableDepositAuthorization(receiverKeyPair);

    /////////////////////////
    // Submit a DepositPreAuth transaction with authorizeCredentials set.
    List<CredentialWrapper> credsToAuthorize = Arrays.stream(GOOD_CREDENTIALS_TYPES).map(
      credentialType -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .credentialType(credentialType)
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .build()
        ).build()).collect(Collectors.toList());

    FeeResult feeResult = xrplClient.fee();

    DepositPreAuth depositPreAuthTx = DepositPreAuth.builder()
      .account(receiverKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(receiverAccountInfo.accountData().sequence())
      .signingPublicKey(receiverKeyPair.publicKey())
      .authorizeCredentials(credsToAuthorize)
      .build();

    SingleSignedTransaction<DepositPreAuth> singedDepositPreAuth = this.signatureService.sign(
      receiverKeyPair.privateKey(), depositPreAuthTx
    );

    SubmitResult<DepositPreAuth> depositPreAuthSubmitResult = xrplClient.submit(singedDepositPreAuth);

    assertThat(depositPreAuthSubmitResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(depositPreAuthSubmitResult.transactionResult().hash(), CredentialAccept.class)
    );

    /////////////////////////
    // Validate that the DepositPreAuthObject was added to the receiver's account objects
    DepositPreAuthObject preAuthObject = (DepositPreAuthObject) this.scanForResult(
        () -> this.getValidatedAccountObjects(receiverKeyPair.publicKey().deriveAddress()),
        accountObjects ->
          accountObjects.accountObjects().stream().anyMatch(object ->
            DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
              ((DepositPreAuthObject) object).authorizeCredentials().equals(credsToAuthorize)
          )
      ).accountObjects().stream()
      .filter(object -> DepositPreAuthObject.class.isAssignableFrom(object.getClass()) &&
        ((DepositPreAuthObject) object).authorizeCredentials().equals(credsToAuthorize))
      .findFirst()
      .get();

    assertEntryEqualsObjectFromAccountObjects(depositPreAuthTx, preAuthObject);

    /////////////////////////
    // Create credential from issuer to sender account.
    KeyPair senderKeyPair = createRandomAccountEd25519();
    createCredentials(issuerKeyPair, senderKeyPair, GOOD_CREDENTIALS_TYPES);

    List<Hash256> credObjectIds = getCredentialObjectIds(issuerKeyPair, senderKeyPair, GOOD_CREDENTIALS_TYPES);

    /////////////////////////
    // Validate badCredentials (credentials aren't accepted) is thrown since credentials are not yet accepted.
    assertThrows(JsonRpcClientErrorException.class,
      () -> xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
        .sourceAccount(senderKeyPair.publicKey().deriveAddress())
        .destinationAccount(receiverKeyPair.publicKey().deriveAddress())
        .credentials(credObjectIds)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()),
      "badCredentials (credentials aren't accepted)"
    );

    /////////////////////////
    // Accept credential from the issuer.
    acceptCredentials(issuerKeyPair, senderKeyPair, GOOD_CREDENTIALS_TYPES);

    /////////////////////////
    // Validate that the `deposit_authorized` client call is implemented properly by ensuring it aligns with the
    // result found in the account object.
    final boolean depositAuthorizedAfterAccepting =
      xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
        .sourceAccount(senderKeyPair.publicKey().deriveAddress())
        .destinationAccount(receiverKeyPair.publicKey().deriveAddress())
        .credentials(credObjectIds)
        .build()).depositAuthorized();

    assertThat(depositAuthorizedAfterAccepting).isTrue();

    /////////////////////////
    // Send a Payment from the sender wallet to the receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    Payment payment = Payment.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .build();

    SingleSignedTransaction<Payment> singedPayment = signatureService.sign(
      senderKeyPair.privateKey(), payment
    );

    SubmitResult<Payment> paymentResult = xrplClient.submit(singedPayment);
    assertThat(paymentResult.engineResult()).isEqualTo("tecNO_PERMISSION");
  }

  @Test
  public void accountUnableToReceivePaymentsWithoutPreauthorization()
    throws JsonRpcClientErrorException, JsonProcessingException {
    /////////////////////////
    // Create random sender/receiver accounts
    KeyPair receiverKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();

    /////////////////////////
    // Enable Deposit Preauthorization on the receiver account
    enableDepositAuthorization(receiverKeyPair);

    /////////////////////////
    // Validate that the receiver has not given authorization to anyone to send them Payments
    AccountObjectsResult receiverObjects = this.scanForResult(
      () -> this.getValidatedAccountObjects(receiverKeyPair.publicKey().deriveAddress()));
    assertThat(receiverObjects.accountObjects().stream()
      .anyMatch(ledgerObject ->
        DepositPreAuthObject.class.isAssignableFrom(ledgerObject.getClass())
      )
    ).isFalse();

    /////////////////////////
    // Try to send a Payment from sender wallet to receiver wallet
    AccountInfoResult senderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );
    FeeResult feeResult = xrplClient.fee();
    Payment payment = Payment.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(senderAccountInfo.accountData().sequence())
      .signingPublicKey(senderKeyPair.publicKey())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(receiverKeyPair.publicKey().deriveAddress())
      .build();

    /////////////////////////
    // And validate that the transaction failed with a tecNO_PERMISSION error code
    SingleSignedTransaction<Payment> singedPayment = signatureService.sign(
      senderKeyPair.privateKey(), payment
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(singedPayment);
    assertThat(paymentResult.engineResult()).isEqualTo("tecNO_PERMISSION");
  }

  @Test
  public void updateDepositPreAuthWithLedgerIndex() throws JsonRpcClientErrorException {
    // Create random sender/receiver accounts
    KeyPair receiverKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();

    assertThat(
      xrplClient.depositAuthorized(
        DepositAuthorizedRequestParams.builder()
          .sourceAccount(senderKeyPair.publicKey().deriveAddress())
          .destinationAccount(receiverKeyPair.publicKey().deriveAddress())
          .ledgerSpecifier(LedgerSpecifier.CURRENT)
          .build()
      ).depositAuthorized()
    ).isTrue();
  }

  @Test
  public void updateDepositPreAuthWithLedgerHash() {
    // Create random sender/receiver accounts
    KeyPair receiverKeyPair = createRandomAccountEd25519();
    KeyPair senderKeyPair = createRandomAccountEd25519();

    assertThrows(JsonRpcClientErrorException.class,
      () -> xrplClient.depositAuthorized(DepositAuthorizedRequestParams.builder()
        .sourceAccount(senderKeyPair.publicKey().deriveAddress())
        .destinationAccount(receiverKeyPair.publicKey().deriveAddress())
        .ledgerSpecifier(
          LedgerSpecifier.of(Hash256.of("19DB20F9037D75361582E804233C517532C1DC5F3158845A9332190342009795"))
        )
        .build()).depositAuthorized(),
      "org.xrpl.xrpl4j.client.JsonRpcClientErrorException: ledgerNotFound"
    );
  }

  private void assertEntryEqualsObjectFromAccountObjects(
    DepositPreAuth depositPreAuth,
    DepositPreAuthObject preAuthObject
  ) throws JsonRpcClientErrorException {

    LedgerEntryResult<DepositPreAuthObject> preAuthEntry;

    if (depositPreAuth.authorize().isPresent()) {
      preAuthEntry = xrplClient.ledgerEntry(
        LedgerEntryRequestParams.depositPreAuth(
          DepositPreAuthLedgerEntryParams.builder()
            .owner(depositPreAuth.account())
            .authorized(depositPreAuth.authorize().get())
            .build(),
          LedgerSpecifier.CURRENT
        )
      );
    } else {
      List<DepositPreAuthCredential> authorizedCredentials =
        depositPreAuth.authorizeCredentials().stream()
          .map(cw -> DepositPreAuthCredential.builder()
            .credentialType(cw.credential().credentialType())
            .issuer(cw.credential().issuer())
            .build())
          .collect(Collectors.toList());

      preAuthEntry = xrplClient.ledgerEntry(
        LedgerEntryRequestParams.depositPreAuth(
          DepositPreAuthLedgerEntryParams.builder()
            .owner(depositPreAuth.account())
            .authorizedCredentials(authorizedCredentials)
            .build(),
          LedgerSpecifier.CURRENT
        )
      );
    }

    assertThat(preAuthEntry.node()).isEqualTo(preAuthObject);

    LedgerEntryResult<DepositPreAuthObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(preAuthObject.index(), DepositPreAuthObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(preAuthEntry.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(preAuthObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());
  }
}
