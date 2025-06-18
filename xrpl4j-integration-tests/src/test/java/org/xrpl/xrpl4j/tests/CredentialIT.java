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
import com.google.common.primitives.UnsignedLong;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.CredentialLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.CredentialObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialAccept;
import org.xrpl.xrpl4j.model.transactions.CredentialCreate;
import org.xrpl.xrpl4j.model.transactions.CredentialDelete;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialUri;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * Integration test to validate creation, acceptance, and deletion of credentials using `CredentialCreate`,
 * `CredentialAccept` and `CredentialDelete` transactions respectively.
 */
public class CredentialIT extends AbstractIT {

  private static final CredentialType DRIVER_LICENCE = CredentialType.ofPlainText("driver licence");

  private static final CredentialUri DRIVER_LICENCE_URI = CredentialUri.ofPlainText("https://link-to-vc-document.pdf");

  @Test
  public void testCreateAcceptDeleteCredential() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountSecp256k1();
    KeyPair subjectKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Create a Credential representing driver license.
    CredentialCreate credCreateTx = CredentialCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .subject(subjectKeyPair.publicKey().deriveAddress())
      .credentialType(DRIVER_LICENCE)
      .uri(DRIVER_LICENCE_URI)
      .expiration(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofDays(365))))
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CredentialCreate> signedCreateTx = signatureService.sign(
      issuerKeyPair.privateKey(), credCreateTx
    );

    SubmitResult<CredentialCreate> createTxIntermediateResult = xrplClient.submit(signedCreateTx);

    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), CredentialCreate.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      issuerKeyPair.publicKey().deriveAddress(),
      subjectKeyPair.publicKey().deriveAddress(),
      DRIVER_LICENCE
    );

    // Check Credential object is not yet accepted.
    assertCredentialObjectAcceptedStatus(
      issuerKeyPair.publicKey().deriveAddress(),
      subjectKeyPair.publicKey().deriveAddress(),
      DRIVER_LICENCE,
      Boolean.FALSE
    );

    AccountInfoResult subjectAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(subjectKeyPair.publicKey().deriveAddress())
    );

    // Accept Credential
    CredentialAccept credAcceptTx = CredentialAccept.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .account(subjectKeyPair.publicKey().deriveAddress())
      .credentialType(DRIVER_LICENCE)
      .sequence(subjectAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .signingPublicKey(subjectKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CredentialAccept> signedAcceptTx = signatureService.sign(
      subjectKeyPair.privateKey(), credAcceptTx
    );

    SubmitResult<CredentialAccept> acceptTxIntermediateResult = xrplClient.submit(signedAcceptTx);

    assertThat(acceptTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(acceptTxIntermediateResult.transactionResult().hash(), CredentialAccept.class)
    );

    assertCredentialObjectAcceptedStatus(
      issuerKeyPair.publicKey().deriveAddress(),
      subjectKeyPair.publicKey().deriveAddress(),
      DRIVER_LICENCE,
      Boolean.TRUE
    );

    subjectAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(subjectKeyPair.publicKey().deriveAddress())
    );

    // Delete Credential
    CredentialDelete credDeleteTx = CredentialDelete.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .account(subjectKeyPair.publicKey().deriveAddress())
      .credentialType(DRIVER_LICENCE)
      .sequence(subjectAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .signingPublicKey(subjectKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CredentialDelete> signedDeleteTx = signatureService.sign(
      subjectKeyPair.privateKey(), credDeleteTx
    );

    SubmitResult<CredentialDelete> deleteTxIntermediateResult = xrplClient.submit(signedDeleteTx);

    assertThat(deleteTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(deleteTxIntermediateResult.transactionResult().hash(), CredentialDelete.class)
    );

    assertCredentialDeleted(
      issuerKeyPair.publicKey().deriveAddress(),
      subjectKeyPair.publicKey().deriveAddress(),
      DRIVER_LICENCE
    );
  }

  @Test
  public void testAcceptCredentialAfterExpiration()
    throws JsonRpcClientErrorException, JsonProcessingException, InterruptedException {

    KeyPair issuerKeyPair = createRandomAccountSecp256k1();
    KeyPair subjectKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    UnsignedLong expirationTime = instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(3)));

    CredentialCreate credCreateTx = CredentialCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .subject(subjectKeyPair.publicKey().deriveAddress())
      .credentialType(DRIVER_LICENCE)
      .uri(DRIVER_LICENCE_URI)
      .expiration(expirationTime)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CredentialCreate> signedCreateTx = signatureService.sign(
      issuerKeyPair.privateKey(), credCreateTx
    );

    SubmitResult<CredentialCreate> createTxIntermediateResult = xrplClient.submit(signedCreateTx);

    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), CredentialCreate.class)
    );

    waitForCredentialToExpire(expirationTime);

    AccountInfoResult subjectAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(subjectKeyPair.publicKey().deriveAddress())
    );

    CredentialAccept credAcceptTx = CredentialAccept.builder()
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .account(subjectKeyPair.publicKey().deriveAddress())
      .credentialType(DRIVER_LICENCE)
      .sequence(subjectAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .signingPublicKey(subjectKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CredentialAccept> signedAcceptTx = signatureService.sign(
      subjectKeyPair.privateKey(), credAcceptTx
    );

    SubmitResult<CredentialAccept> acceptTxIntermediateResult = xrplClient.submit(signedAcceptTx);

    assertThat(acceptTxIntermediateResult.engineResult()).isEqualTo("tecEXPIRED");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(acceptTxIntermediateResult.transactionResult().hash(), CredentialAccept.class)
    );

    // Accepting Credential after expiry automatically deletes the object.
    assertCredentialDeleted(
      issuerKeyPair.publicKey().deriveAddress(),
      subjectKeyPair.publicKey().deriveAddress(),
      DRIVER_LICENCE
    );
  }

  private void assertEntryEqualsObjectFromAccountObjects(
    Address issuer,
    Address subject,
    CredentialType credentialType
  ) throws JsonRpcClientErrorException {
    CredentialObject credentialObject = (CredentialObject) this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.CREDENTIAL)
            .account(issuer)
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.size() == 1
    ).get(0);

    LedgerEntryResult<CredentialObject> credentialEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.credential(
        CredentialLedgerEntryParams.builder()
          .issuer(issuer)
          .subject(subject)
          .credentialType(credentialType)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(credentialEntry.node()).isEqualTo(credentialObject);

    LedgerEntryResult<CredentialObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(credentialObject.index(), CredentialObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(credentialEntry.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(credentialObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());
  }

  private void assertCredentialObjectAcceptedStatus(
    Address issuer,
    Address subject,
    CredentialType credentialType,
    boolean isAccepted
  ) throws JsonRpcClientErrorException {

    LedgerEntryResult<CredentialObject> credentialEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.credential(
        CredentialLedgerEntryParams.builder()
          .issuer(issuer)
          .subject(subject)
          .credentialType(credentialType)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(credentialEntry.node().flags().lsfAccepted()).isEqualTo(isAccepted);
  }

  private void assertCredentialDeleted(Address issuer, Address subject, CredentialType credentialType) {
    try {
      this.scanForResult(
        () -> {
          try {
            return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
              .type(AccountObjectType.CREDENTIAL)
              .account(issuer)
              .ledgerSpecifier(LedgerSpecifier.VALIDATED)
              .build()).accountObjects();
          } catch (JsonRpcClientErrorException e) {
            throw new RuntimeException(e);
          }
        },
        ledgerObjects -> ledgerObjects.stream().noneMatch(findMatch(issuer, subject, credentialType))
      );
    } catch (ConditionTimeoutException e) {
      throw new IllegalStateException("CredentialObject still exists after expected deletion.", e);
    }
  }

  private Predicate<LedgerObject> findMatch(Address issuer, Address subject, CredentialType credentialType) {
    return ledgerObject -> ((CredentialObject) ledgerObject).issuer().equals(issuer) &&
      ((CredentialObject) ledgerObject).credentialType().equals(credentialType) &&
      ((CredentialObject) ledgerObject).subject().equals(subject);
  }

  private void waitForCredentialToExpire(UnsignedLong expirationTimeRippleEpoch) throws JsonRpcClientErrorException {
    LedgerResult ledgerResult;
    do {
      ledgerResult = xrplClient.ledger(
        LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED).build());
    } while (expirationTimeRippleEpoch.compareTo(ledgerResult.ledger().closeTime().get()) >= 0);
  }
}
