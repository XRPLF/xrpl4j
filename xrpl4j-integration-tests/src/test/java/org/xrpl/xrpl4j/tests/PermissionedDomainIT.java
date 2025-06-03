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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.PermissionedDomainLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Credential;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.PermissionedDomainDelete;
import org.xrpl.xrpl4j.model.transactions.PermissionedDomainSet;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Integration tests to validate create, update, and delete of permissioned domains.
 */
public class PermissionedDomainIT extends AbstractIT {

  private static final CredentialType[] GOOD_CREDENTIALS_TYPES =
    {CredentialType.ofPlainText("driver licence"), CredentialType.ofPlainText("voting card")};

  @Test
  public void testPermissionedDomainCreateUpdateAndDelete() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create Credential issuer, subject and domain owner keys.
    KeyPair issuerKeyPair = createRandomAccountSecp256k1();
    KeyPair subjectKeyPair = createRandomAccountSecp256k1();
    KeyPair ownerKeyPair = createRandomAccountSecp256k1();

    // create credentials from issuer to subject.
    createCredentials(issuerKeyPair, subjectKeyPair, GOOD_CREDENTIALS_TYPES);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult ownerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(ownerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger createSequence = ownerAccountInfo.accountData().sequence();

    // Create a PermissionedDomain object.
    List<CredentialWrapper> credentials = Arrays.stream(GOOD_CREDENTIALS_TYPES)
      .map(credentialType ->
        CredentialWrapper.builder()
          .credential(Credential.builder()
            .issuer(issuerKeyPair.publicKey().deriveAddress())
            .credentialType(credentialType)
            .build())
          .build())
      .collect(Collectors.toList());

    PermissionedDomainSet permissionedDomainSetTx = PermissionedDomainSet.builder()
      .account(ownerKeyPair.publicKey().deriveAddress())
      .sequence(createSequence)
      .fee(feeResult.drops().openLedgerFee())
      .acceptedCredentials(credentials)
      .signingPublicKey(ownerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainSet> signedCreateTx = signatureService.sign(
      ownerKeyPair.privateKey(), permissionedDomainSetTx
    );

    SubmitResult<PermissionedDomainSet> domainSetTxIntermediateResult = xrplClient.submit(signedCreateTx);

    assertThat(domainSetTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        domainSetTxIntermediateResult.transactionResult().hash(), PermissionedDomainSet.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      ownerKeyPair.publicKey().deriveAddress(), createSequence
    );

    // Update PermissionedDomain object.
    ownerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(ownerKeyPair.publicKey().deriveAddress())
    );

    Hash256 permissionedDomainId = getPermissionedDomainId(ownerKeyPair.publicKey().deriveAddress(), createSequence);

    PermissionedDomainSet updateDomainTx = PermissionedDomainSet.builder()
      .account(ownerKeyPair.publicKey().deriveAddress())
      .sequence(ownerAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .acceptedCredentials(credentials.subList(0, 1))
      .domainId(permissionedDomainId)
      .signingPublicKey(ownerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainSet> signedUpdateTx = signatureService.sign(
      ownerKeyPair.privateKey(), updateDomainTx
    );

    SubmitResult<PermissionedDomainSet> updateTxIntermediateResult = xrplClient.submit(signedUpdateTx);

    assertThat(updateTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        updateTxIntermediateResult.transactionResult().hash(), PermissionedDomainSet.class)
    );

    assertEntryEqualsObjectFromAccountObjects(ownerKeyPair.publicKey().deriveAddress(), createSequence);

    // Delete PermissionedDomain object.
    ownerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(ownerKeyPair.publicKey().deriveAddress())
    );

    PermissionedDomainDelete deleteDomainTx = PermissionedDomainDelete.builder()
      .account(ownerKeyPair.publicKey().deriveAddress())
      .sequence(ownerAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .domainId(permissionedDomainId)
      .signingPublicKey(ownerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainDelete> signedDeleteTx = signatureService.sign(
      ownerKeyPair.privateKey(), deleteDomainTx
    );

    SubmitResult<PermissionedDomainDelete> deleteTxIntermediateResult = xrplClient.submit(signedDeleteTx);

    assertThat(deleteTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        deleteTxIntermediateResult.transactionResult().hash(), PermissionedDomainDelete.class)
    );

    assertPermissionedDomainDeleted(ownerKeyPair.publicKey().deriveAddress(), createSequence);
  }

  @Test
  public void testPermissionedDomainUpdateInvalidDomainIdAndOwner()
    throws JsonRpcClientErrorException, JsonProcessingException {

    // Create Credential issuer, subject and domain owner keys.
    KeyPair issuerKeyPair = createRandomAccountSecp256k1();
    KeyPair subjectKeyPair = createRandomAccountSecp256k1();
    KeyPair ownerKeyPair = createRandomAccountSecp256k1();

    // create credentials from issuer to subject.
    createCredentials(issuerKeyPair, subjectKeyPair, GOOD_CREDENTIALS_TYPES);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult ownerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(ownerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger createSequence = ownerAccountInfo.accountData().sequence();

    // Create a PermissionedDomain object.
    List<CredentialWrapper> credentials = Arrays.stream(GOOD_CREDENTIALS_TYPES)
      .map(credentialType ->
        CredentialWrapper.builder()
          .credential(Credential.builder()
            .issuer(issuerKeyPair.publicKey().deriveAddress())
            .credentialType(credentialType)
            .build())
          .build())
      .collect(Collectors.toList());

    PermissionedDomainSet permissionedDomainSetTx = PermissionedDomainSet.builder()
      .account(ownerKeyPair.publicKey().deriveAddress())
      .sequence(createSequence)
      .fee(feeResult.drops().openLedgerFee())
      .acceptedCredentials(credentials)
      .signingPublicKey(ownerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainSet> signedCreateTx = signatureService.sign(
      ownerKeyPair.privateKey(), permissionedDomainSetTx
    );

    SubmitResult<PermissionedDomainSet> domainSetTxIntermediateResult = xrplClient.submit(signedCreateTx);

    assertThat(domainSetTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(
        domainSetTxIntermediateResult.transactionResult().hash(), PermissionedDomainSet.class)
    );

    assertEntryEqualsObjectFromAccountObjects(
      ownerKeyPair.publicKey().deriveAddress(), createSequence
    );

    // Update PermissionedDomain object with incorrect DomainID.
    ownerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(ownerKeyPair.publicKey().deriveAddress())
    );

    PermissionedDomainSet updateDomainTx = PermissionedDomainSet.builder()
      .account(ownerKeyPair.publicKey().deriveAddress())
      .sequence(ownerAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .acceptedCredentials(credentials.subList(0, 1))
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .signingPublicKey(ownerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainSet> signedUpdateTx = signatureService.sign(
      ownerKeyPair.privateKey(), updateDomainTx
    );

    SubmitResult<PermissionedDomainSet> updateTxIntermediateResult = xrplClient.submit(signedUpdateTx);

    assertThat(updateTxIntermediateResult.engineResult()).isEqualTo("tecNO_ENTRY");

    // Update PermissionedDomain object with incorrect domain Owner.
    KeyPair randomKeyPair = createRandomAccountEd25519();
    AccountInfoResult randomAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(randomKeyPair.publicKey().deriveAddress())
    );

    Hash256 permissionedDomainId = getPermissionedDomainId(ownerKeyPair.publicKey().deriveAddress(), createSequence);

    updateDomainTx = PermissionedDomainSet.builder()
      .account(randomKeyPair.publicKey().deriveAddress())
      .sequence(randomAccountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .acceptedCredentials(credentials.subList(0, 1))
      .domainId(permissionedDomainId)
      .signingPublicKey(randomKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainSet> signedUpdateWrongOwnerTx = signatureService.sign(
      randomKeyPair.privateKey(), updateDomainTx
    );

    SubmitResult<PermissionedDomainSet> updateWrongOwnerTxIntermediateResult =
      xrplClient.submit(signedUpdateWrongOwnerTx);

    assertThat(updateWrongOwnerTxIntermediateResult.engineResult()).isEqualTo("tecNO_PERMISSION");
  }

  private void assertEntryEqualsObjectFromAccountObjects(
    Address domainOwner,
    UnsignedInteger createSequence
  ) throws JsonRpcClientErrorException {
    PermissionedDomainObject permissionedDomainObject = (PermissionedDomainObject) this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.PERMISSIONED_DOMAIN)
            .account(domainOwner)
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.size() == 1
    ).get(0);

    LedgerEntryResult<PermissionedDomainObject> permissionedDomainEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.permissionedDomain(
        PermissionedDomainLedgerEntryParams.builder()
          .account(domainOwner)
          .seq(createSequence)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    assertThat(permissionedDomainEntry.node()).isEqualTo(permissionedDomainObject);

    LedgerEntryResult<PermissionedDomainObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams
        .index(permissionedDomainObject.index(), PermissionedDomainObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(permissionedDomainEntry.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(permissionedDomainObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());
  }

  private Hash256 getPermissionedDomainId(Address domainOwner, UnsignedInteger sequence)
    throws JsonRpcClientErrorException {

    return xrplClient.ledgerEntry(
      LedgerEntryRequestParams.permissionedDomain(
        PermissionedDomainLedgerEntryParams.builder()
          .account(domainOwner)
          .seq(sequence)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node().index();
  }

  private void assertPermissionedDomainDeleted(Address domainOwner, UnsignedInteger sequence) {
    assertThatThrownBy(() -> xrplClient.ledgerEntry(
      LedgerEntryRequestParams.permissionedDomain(
        PermissionedDomainLedgerEntryParams.builder()
          .account(domainOwner)
          .seq(sequence)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    )).isInstanceOf(JsonRpcClientErrorException.class)
      .hasMessage("entryNotFound (n/a)");
  }
}
