package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountSponsoringRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountSponsoringResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.SponsorshipObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Integration test for the {@code account_sponsoring} RPC method.
 *
 * <p>This test is disabled by default because the {@code account_sponsoring} RPC method
 * requires the featureSponsorship amendment to be enabled, which may not be available
 * on all test networks.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Disabled("account_sponsoring requires featureSponsorship amendment which may not be enabled on test networks")
public class AccountSponsoringIT extends AbstractIT {

  @Test
  public void testAccountSponsoringWithNewAccount() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create a random account
    KeyPair keyPair = createRandomAccountEd25519();
    Address address = keyPair.publicKey().deriveAddress();

    // Call account_sponsoring for the newly created account
    // A new account should not be sponsoring any objects
    AccountSponsoringResult result = xrplClient.accountSponsoring(
      AccountSponsoringRequestParams.builder()
        .account(address)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    );

    // Validate the result
    assertThat(result.account()).isEqualTo(address);
    assertThat(result.sponsoredObjects()).isEmpty();
    assertThat(result.validated()).isTrue();
    assertThat(result.ledgerHash()).isPresent();
    assertThat(result.ledgerIndex()).isPresent();
  }

  @Test
  public void testAccountSponsoringWithFactoryMethod() throws JsonRpcClientErrorException {
    // Create a random account
    KeyPair keyPair = createRandomAccountEd25519();
    Address address = keyPair.publicKey().deriveAddress();

    // Use the factory method to create request params
    AccountSponsoringRequestParams params = AccountSponsoringRequestParams.of(address);

    // Call account_sponsoring
    AccountSponsoringResult result = xrplClient.accountSponsoring(params);

    // Validate the result
    assertThat(result.account()).isEqualTo(address);
    assertThat(result.sponsoredObjects()).isEmpty();
  }

  @Test
  public void testAccountSponsoringSafeHelpers() throws JsonRpcClientErrorException {
    // Create a random account
    KeyPair keyPair = createRandomAccountEd25519();
    Address address = keyPair.publicKey().deriveAddress();

    // Call account_sponsoring with VALIDATED ledger specifier
    AccountSponsoringResult result = xrplClient.accountSponsoring(
      AccountSponsoringRequestParams.builder()
        .account(address)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    );

    // Test the safe helper methods
    assertThat(result.ledgerHashSafe()).isNotNull();
    assertThat(result.ledgerIndexSafe()).isNotNull();
  }

  @Test
  public void testAccountSponsoringWithCurrentLedger() throws JsonRpcClientErrorException {
    // Create a random account
    KeyPair keyPair = createRandomAccountEd25519();
    Address address = keyPair.publicKey().deriveAddress();

    // Call account_sponsoring with CURRENT ledger specifier
    AccountSponsoringResult result = xrplClient.accountSponsoring(
      AccountSponsoringRequestParams.builder()
        .account(address)
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    // Validate the result
    assertThat(result.account()).isEqualTo(address);
    assertThat(result.sponsoredObjects()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isPresent();
    assertThat(result.ledgerCurrentIndexSafe()).isNotNull();
  }

  @Test
  public void testSponsorshipSetWithFeeAmount() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create sponsor and sponsee accounts
    KeyPair sponsorKeyPair = createRandomAccountEd25519();
    KeyPair sponseeKeyPair = createRandomAccountEd25519();
    Address sponsorAddress = sponsorKeyPair.publicKey().deriveAddress();
    Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();

    // Get current fee and account info
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sponsorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponsorAddress)
    );

    // Create a SponsorshipSet transaction with FeeAmount
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(sponsorAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sponsorAccountInfo.accountData().sequence())
      .sponsee(sponseeAddress)
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000)) // 1 XRP for fees
      .signingPublicKey(sponsorKeyPair.publicKey())
      .build();

    // Sign and submit the transaction
    SingleSignedTransaction<SponsorshipSet> signedTransaction = signatureService.sign(
      sponsorKeyPair.privateKey(),
      sponsorshipSet
    );
    SubmitResult<SponsorshipSet> submitResult = xrplClient.submit(signedTransaction);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    logInfo(sponsorshipSet.transactionType(), submitResult.transactionResult().hash());

    // Wait for the transaction to be validated
    TransactionResult<SponsorshipSet> validatedTransaction = this.scanForResult(
      () -> this.getValidatedTransaction(submitResult.transactionResult().hash(), SponsorshipSet.class)
    );

    assertThat(validatedTransaction.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    // Verify the Sponsorship ledger object was created
    AccountObjectsResult accountObjects = xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .account(sponsorAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .type(AccountObjectType.SPONSORSHIP)
        .build()
    );

    List<SponsorshipObject> sponsorships = accountObjects.accountObjects().stream()
      .filter(obj -> obj instanceof SponsorshipObject)
      .map(obj -> (SponsorshipObject) obj)
      .collect(Collectors.toList());

    assertThat(sponsorships).isNotEmpty();
    Optional<SponsorshipObject> createdSponsorship = sponsorships.stream()
      .filter(s -> s.sponsee().equals(sponseeAddress))
      .findFirst();

    assertThat(createdSponsorship).isPresent();
    assertThat(createdSponsorship.get().owner()).isEqualTo(sponsorAddress);
    assertThat(createdSponsorship.get().feeAmount()).hasValue(XrpCurrencyAmount.ofDrops(1000000));
  }

  @Test
  public void testSponsorshipSetWithReserveCount() throws JsonRpcClientErrorException, JsonProcessingException {
    // Create sponsor and sponsee accounts
    KeyPair sponsorKeyPair = createRandomAccountEd25519();
    KeyPair sponseeKeyPair = createRandomAccountEd25519();
    Address sponsorAddress = sponsorKeyPair.publicKey().deriveAddress();
    Address sponseeAddress = sponseeKeyPair.publicKey().deriveAddress();

    // Get current fee and account info
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sponsorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sponsorAddress)
    );

    // Create a SponsorshipSet transaction with ReserveCount
    SponsorshipSet sponsorshipSet = SponsorshipSet.builder()
      .account(sponsorAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sponsorAccountInfo.accountData().sequence())
      .sponsee(sponseeAddress)
      .reserveCount(UnsignedInteger.valueOf(5)) // Sponsor 5 reserve units
      .signingPublicKey(sponsorKeyPair.publicKey())
      .build();

    // Sign and submit the transaction
    SingleSignedTransaction<SponsorshipSet> signedTransaction = signatureService.sign(
      sponsorKeyPair.privateKey(),
      sponsorshipSet
    );
    SubmitResult<SponsorshipSet> submitResult = xrplClient.submit(signedTransaction);
    assertThat(submitResult.engineResult()).isEqualTo(SUCCESS_STATUS);
    logInfo(sponsorshipSet.transactionType(), submitResult.transactionResult().hash());

    // Wait for the transaction to be validated
    TransactionResult<SponsorshipSet> validatedTransaction = this.scanForResult(
      () -> this.getValidatedTransaction(submitResult.transactionResult().hash(), SponsorshipSet.class)
    );

    assertThat(validatedTransaction.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    // Verify the Sponsorship ledger object was created
    AccountObjectsResult accountObjects = xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .account(sponsorAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .type(AccountObjectType.SPONSORSHIP)
        .build()
    );

    List<SponsorshipObject> sponsorships = accountObjects.accountObjects().stream()
      .filter(obj -> obj instanceof SponsorshipObject)
      .map(obj -> (SponsorshipObject) obj)
      .collect(Collectors.toList());

    assertThat(sponsorships).isNotEmpty();
    Optional<SponsorshipObject> createdSponsorship = sponsorships.stream()
      .filter(s -> s.sponsee().equals(sponseeAddress))
      .findFirst();

    assertThat(createdSponsorship).isPresent();
    assertThat(createdSponsorship.get().owner()).isEqualTo(sponsorAddress);
    assertThat(createdSponsorship.get().reserveCount()).isEqualTo(UnsignedInteger.valueOf(5));
  }
}

