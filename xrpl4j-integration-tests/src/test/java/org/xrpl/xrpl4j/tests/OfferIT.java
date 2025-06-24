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
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.OfferLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.path.BookOffersRequestParams;
import org.xrpl.xrpl4j.model.client.path.BookOffersResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Integration tests to validate submission of Offer-related transactions.
 */
public class OfferIT extends AbstractIT {

  public static final String CURRENCY = "USD";

  private static KeyPair issuerKeyPair;

  private static boolean usdIssued = false;

  /**
   * Sets up an issued currency (USD) that can be used to test Offers against this currency.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  @BeforeEach
  public void ensureUsdIssued() throws JsonRpcClientErrorException, JsonProcessingException {
    // this only needs to run once before all tests but can't be a BeforeAll static method due to dependencies on
    // instance methods in AbstractIT
    if (usdIssued) {
      return;
    }
    issuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult =
      this.scanForResult(() -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress()));

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    XrpCurrencyAmount takerPays = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(200.0));
    IssuedCurrencyAmount takerGets = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .value("100")
      .build();
    OfferCreate offerCreate = OfferCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sequence)
      .signingPublicKey(issuerKeyPair.publicKey())
      .takerGets(takerGets)
      .takerPays(takerPays)
      .flags(OfferCreateFlags.builder()
        .tfSell(true)
        .build())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      issuerKeyPair.privateKey(), offerCreate
    );
    SubmitResult<OfferCreate> response = xrplClient.submit(signedOfferCreate);
    assertThat(response.transactionResult().transaction().flags().tfFullyCanonicalSig()).isTrue();
    assertThat(response.transactionResult().transaction().flags().tfSell()).isTrue();

    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    BookOffersResult result = xrplClient.bookOffers(
      BookOffersRequestParams.builder()
        .taker(offerCreate.account())
        .takerGets(
          Issue.builder()
            .currency("USD")
            .issuer(offerCreate.account())
            .build()
        )
        .takerPays(Issue.XRP)
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(result.offers()).asList().hasSize(1);
    assertThat(result.offers().get(0).quality())
      .isEqualTo(BigDecimal.valueOf(takerPays.value().longValue()).divide(new BigDecimal(takerGets.value())));
    assertThat(result.offers().get(0).account()).isEqualTo(offerCreate.account());
    assertThat(result.offers().get(0).flags().lsfSell()).isTrue();
    assertThat(result.offers().get(0).flags().lsfPassive()).isFalse();
    assertThat(result.offers().get(0).sequence()).isEqualTo(offerCreate.sequence());
    assertThat(result.offers().get(0).takerPays()).isEqualTo(takerPays);
    assertThat(result.offers().get(0).takerGets()).isEqualTo(takerGets);
    assertThat(result.offers().get(0).ownerFunds()).isNotEmpty().get().isEqualTo(new BigDecimal(takerGets.value()));
    usdIssued = true;
  }

  @Test
  public void createOpenOfferAndCancel() throws JsonRpcClientErrorException, JsonProcessingException {
    // GIVEN a buy offer that has a poor exchange rate
    // THEN the OfferCreate should generate an open offer on the order book

    //////////////////////
    // Generate and fund purchaser's account
    KeyPair purchaser = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaser.publicKey().deriveAddress())
    );

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    OfferCreate offerCreate = OfferCreate.builder()
      .account(purchaser.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sequence)
      .signingPublicKey(purchaser.publicKey())
      .takerPays(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build()
      )
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .flags(OfferCreateFlags.builder()
        .tfSell(true)
        .build())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      purchaser.privateKey(), offerCreate
    );
    SubmitResult<OfferCreate> response = xrplClient.submit(signedOfferCreate);
    assertThat(response.transactionResult().transaction().flags().tfFullyCanonicalSig()).isTrue();
    assertThat(response.transactionResult().transaction().flags().tfSell()).isTrue();

    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source purchaser's offers, and validate the expected offer exists
    OfferObject offerObject = scanForOffer(purchaser, sequence);
    assertThat(offerObject.takerGets()).isEqualTo(offerCreate.takerGets());
    assertThat(offerObject.takerPays()).isEqualTo(offerCreate.takerPays());

    assertThatEntryEqualsObjectFromAccountObjects(offerObject);

    cancelOffer(purchaser, offerObject.sequence(), "tesSUCCESS");
  }

  @Test
  public void createUnmatchedKillOrFill() throws JsonRpcClientErrorException, JsonProcessingException {
    // GIVEN a buy offer that has a poor exchange rate
    // THEN the OfferCreate should not match any offers and immediately be killed

    //////////////////////
    // Generate and fund purchaser's account
    KeyPair purchaser = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaser.publicKey().deriveAddress())
    );

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    OfferCreate offerCreate = OfferCreate.builder()
      .account(purchaser.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sequence)
      .signingPublicKey(purchaser.publicKey())
      .takerPays(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000")
        .build()
      )
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .flags(OfferCreateFlags.builder()
        .tfImmediateOrCancel(true)
        .build())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      purchaser.privateKey(), offerCreate
    );
    SubmitResult<OfferCreate> response = xrplClient.submit(signedOfferCreate);
    assertThat(response.engineResult()).isEqualTo("tecKILLED");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source purchaser's offers, and validate no offers or balances (ripple states) exist
    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.publicKey().deriveAddress(), OfferObject.class));
    assertEmptyResults(
      () -> this.getValidatedAccountObjects(purchaser.publicKey().deriveAddress(), RippleStateObject.class));
  }

  @Test
  public void createFullyMatchedOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    // GIVEN a buy offer that has a really great exchange rate
    // THEN the OfferCreate should fully match an open offer and generate a balance

    //////////////////////
    // Generate and fund purchaser's account
    KeyPair purchaser = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaser.publicKey().deriveAddress())
    );

    //////////////////////
    // Create an Offer
    UnsignedInteger sequence = accountInfoResult.accountData().sequence();
    IssuedCurrencyAmount requestCurrencyAmount = IssuedCurrencyAmount.builder()
      .currency(CURRENCY)
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .value("0.01")
      .build();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(purchaser.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sequence)
      .signingPublicKey(purchaser.publicKey())
      .takerPays(requestCurrencyAmount)
      .takerGets(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10.0)))
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      purchaser.privateKey(), offerCreate
    );
    SubmitResult<OfferCreate> response = xrplClient.submit(signedOfferCreate);
    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    logger.info(
      "OfferCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source purchaser's balances, and validate the expected currency balance exists
    RippleStateObject issuedCurrency = scanForIssuedCurrency(purchaser, CURRENCY,
      issuerKeyPair.publicKey().deriveAddress());
    // The "issuer" for the balance in a trust line depends on whether the balance is positive or negative.
    // If a RippleState object shows a positive balance, the high account is the issuer.
    // If the balance is negative, the low account is the issuer.
    // Often, the issuer has its limit set to 0 and the other account has a positive limit, but this is not reliable
    // because limits can change without affecting an existing balance.
    if (issuedCurrency.lowLimit().issuer().equals(issuerKeyPair.publicKey().deriveAddress())) {
      assertThat(issuedCurrency.balance().value()).isEqualTo("-" + requestCurrencyAmount.value());
    } else {
      assertThat(issuedCurrency.balance().value()).isEqualTo(requestCurrencyAmount.value());
    }
  }

  @Test
  public void cancelNonExistentOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair purchaser = createRandomAccountEd25519();
    UnsignedInteger nonExistentOfferSequence = UnsignedInteger.valueOf(111111111);
    // cancel offer does the assertions
    cancelOffer(purchaser, nonExistentOfferSequence, "temBAD_SEQUENCE");
  }

  @Test
  public void createPermissionedSellAndBuyOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    // GIVEN a permissioned buy offer that has a really great exchange rate
    // THEN the OfferCreate should fully match an open permissioned sell offer and generate a balance

    KeyPair credentialIssuerKeyPair = createRandomAccountEd25519();
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair sellerKeyPair = createRandomAccountEd25519();
    KeyPair purchaserKeyPair = createRandomAccountEd25519();

    // Create and accept credentials.
    CredentialType[] credentialTypes = {CredentialType.ofPlainText("graduate certificate")};
    createAndAcceptCredentials(credentialIssuerKeyPair, sellerKeyPair, credentialTypes);
    createAndAcceptCredentials(credentialIssuerKeyPair, purchaserKeyPair, credentialTypes);

    // Create a permissioned domain.
    createPermissionedDomain(domainOwnerKeyPair, credentialIssuerKeyPair, credentialTypes);
    PermissionedDomainObject permissionedDomainObject = getPermissionedDomainObject(
      domainOwnerKeyPair.publicKey().deriveAddress());

    // Create a sell offer with a valid DomainID.
    IssuedCurrencyAmount sellerOfferTakerGets = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("INR")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount sellerOfferTakerPays = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2));

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sellerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sellerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger sellerOfferCreateSequence = sellerAccountInfo.accountData().sequence();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(sellerKeyPair.publicKey().deriveAddress())
      .takerGets(sellerOfferTakerGets)
      .takerPays(sellerOfferTakerPays)
      .signingPublicKey(sellerKeyPair.publicKey())
      .sequence(sellerOfferCreateSequence)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .flags(OfferCreateFlags.builder().tfSell(true).build())
      .domainId(permissionedDomainObject.index())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      sellerKeyPair.privateKey(), offerCreate
    );

    SubmitResult<OfferCreate> createTxIntermediateResult = xrplClient.submit(signedOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    OfferObject offerObject = scanForOffer(sellerKeyPair, sellerOfferCreateSequence);
    assertThatEntryEqualsObjectFromAccountObjects(offerObject);

    // Validate `book_offers` RPC with the domain filter, returns the correct Offer object.
    BookOffersResult bookOffersResult = xrplClient.bookOffers(
      BookOffersRequestParams.builder()
        .taker(offerCreate.account())
        .takerGets(
          Issue.builder()
            .currency("INR")
            .issuer(offerCreate.account())
            .build()
        )
        .takerPays(Issue.XRP)
        .domain(permissionedDomainObject.index())
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(bookOffersResult.offers()).asList().hasSize(1);
    assertThat(bookOffersResult.offers().get(0).quality())
      .isEqualTo(
        BigDecimal.valueOf(sellerOfferTakerPays.value().longValue())
          .divide(new BigDecimal(sellerOfferTakerGets.value())));
    assertThat(bookOffersResult.offers().get(0).account()).isEqualTo(offerCreate.account());
    assertThat(bookOffersResult.offers().get(0).flags().lsfSell()).isTrue();
    assertThat(bookOffersResult.offers().get(0).flags().lsfPassive()).isFalse();
    assertThat(bookOffersResult.offers().get(0).sequence()).isEqualTo(offerCreate.sequence());
    assertThat(bookOffersResult.offers().get(0).takerPays()).isEqualTo(sellerOfferTakerPays);
    assertThat(bookOffersResult.offers().get(0).takerGets()).isEqualTo(sellerOfferTakerGets);
    assertThat(bookOffersResult.offers().get(0).ownerFunds()).isNotEmpty().get()
      .isEqualTo(new BigDecimal(sellerOfferTakerGets.value()));
    assertThat(bookOffersResult.offers().get(0).domainId()).isPresent().get()
      .isEqualTo(permissionedDomainObject.index());
    assertThat(bookOffersResult.offers().get(0).additionalBooks()).isEmpty();

    // Create a permissioned buy offer that crosses and fills the above created permissioned sell offer.
    IssuedCurrencyAmount purchaseOfferTakerPays = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("INR")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount purchaseOfferTakerGets = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2));

    AccountInfoResult purchaserAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaserKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger purchaserOfferCreateSequence = purchaserAccountInfo.accountData().sequence();

    OfferCreate purchaserOfferCreate = OfferCreate.builder()
      .account(purchaserKeyPair.publicKey().deriveAddress())
      .takerGets(purchaseOfferTakerGets)
      .takerPays(purchaseOfferTakerPays)
      .signingPublicKey(purchaserKeyPair.publicKey())
      .sequence(purchaserOfferCreateSequence)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .domainId(permissionedDomainObject.index())
      .build();

    SingleSignedTransaction<OfferCreate> signedPurchaserOfferCreate = signatureService.sign(
      purchaserKeyPair.privateKey(), purchaserOfferCreate
    );

    SubmitResult<OfferCreate> intermediateResult = xrplClient.submit(signedPurchaserOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(intermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    // Poll the ledger for the source purchaser's balances, and validate the expected currency balance exists
    RippleStateObject issuedCurrency = scanForIssuedCurrency(purchaserKeyPair, "INR",
      sellerKeyPair.publicKey().deriveAddress());

    if (issuedCurrency.lowLimit().issuer().equals(sellerKeyPair.publicKey().deriveAddress())) {
      assertThat(issuedCurrency.balance().value()).isEqualTo("-" + purchaseOfferTakerPays.value());
    } else {
      assertThat(issuedCurrency.balance().value()).isEqualTo(purchaseOfferTakerPays.value());
    }
  }

  @Test
  public void createPermissionedSellAndOpenBuyOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    // GIVEN an open buy offer that has a really great exchange rate
    // THEN the OfferCreate shouldn't match a permissioned sell offer.

    KeyPair credentialIssuerKeyPair = createRandomAccountEd25519();
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair sellerKeyPair = createRandomAccountEd25519();

    // Create and accept credentials.
    CredentialType[] credentialTypes = {CredentialType.ofPlainText("graduate certificate")};
    createAndAcceptCredentials(credentialIssuerKeyPair, sellerKeyPair, credentialTypes);

    // Create a permissioned domain.
    createPermissionedDomain(domainOwnerKeyPair, credentialIssuerKeyPair, credentialTypes);
    PermissionedDomainObject permissionedDomainObject = getPermissionedDomainObject(
      domainOwnerKeyPair.publicKey().deriveAddress());

    // Create a sell offer with a valid DomainID.
    IssuedCurrencyAmount sellerOfferTakerGets = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("CAD")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount sellerOfferTakerPays = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2));

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sellerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sellerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger sellerOfferCreateSequence = sellerAccountInfo.accountData().sequence();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(sellerKeyPair.publicKey().deriveAddress())
      .takerGets(sellerOfferTakerGets)
      .takerPays(sellerOfferTakerPays)
      .signingPublicKey(sellerKeyPair.publicKey())
      .sequence(sellerOfferCreateSequence)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .flags(OfferCreateFlags.builder().tfSell(true).build())
      .domainId(permissionedDomainObject.index())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      sellerKeyPair.privateKey(), offerCreate
    );

    SubmitResult<OfferCreate> createTxIntermediateResult = xrplClient.submit(signedOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    // Create an open buy offer and assert that it is not crossed and filled by the permissioned sell offer.
    KeyPair purchaserKeyPair = createRandomAccountEd25519();

    IssuedCurrencyAmount purchaseOfferTakerPays = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("CAD")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount purchaseOfferTakerGets = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5));

    AccountInfoResult purchaserAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaserKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger purchaserOfferCreateSequence = purchaserAccountInfo.accountData().sequence();

    OfferCreate purchaserOfferCreate = OfferCreate.builder()
      .account(purchaserKeyPair.publicKey().deriveAddress())
      .takerGets(purchaseOfferTakerGets)
      .takerPays(purchaseOfferTakerPays)
      .signingPublicKey(purchaserKeyPair.publicKey())
      .sequence(purchaserOfferCreateSequence)
      .flags(OfferCreateFlags.builder().tfImmediateOrCancel(true).build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .build();

    SingleSignedTransaction<OfferCreate> signedPurchaserOfferCreate = signatureService.sign(
      purchaserKeyPair.privateKey(), purchaserOfferCreate
    );

    SubmitResult<OfferCreate> intermediateResult = xrplClient.submit(signedPurchaserOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<OfferCreate> offerCreateTransactionResult = this.scanForResult(
      () -> this.getValidatedTransaction(intermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    assertThat(offerCreateTransactionResult.metadata().get().transactionResult())
      .isEqualTo("tecKILLED");
  }

  @Test
  public void createPermissionedBuyAndOpenSellOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    // GIVEN a permissioned buy offer that has a really great exchange rate
    // THEN the OfferCreate shouldn't match an open sell offer.

    // Create an open sell offer.
    KeyPair sellerKeyPair = createRandomAccountEd25519();

    IssuedCurrencyAmount sellerOfferTakerGets = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("MXN")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount sellerOfferTakerPays = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2));

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sellerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sellerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger sellerOfferCreateSequence = sellerAccountInfo.accountData().sequence();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(sellerKeyPair.publicKey().deriveAddress())
      .takerGets(sellerOfferTakerGets)
      .takerPays(sellerOfferTakerPays)
      .signingPublicKey(sellerKeyPair.publicKey())
      .sequence(sellerOfferCreateSequence)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .flags(OfferCreateFlags.builder().tfSell(true).build())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      sellerKeyPair.privateKey(), offerCreate
    );

    SubmitResult<OfferCreate> createTxIntermediateResult = xrplClient.submit(signedOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    // Create a permissioned buy offer and assert that it is not crossed and filled by an open sell offer.

    KeyPair credentialIssuerKeyPair = createRandomAccountEd25519();
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair purchaserKeyPair = createRandomAccountEd25519();

    // Create and accept credentials.
    CredentialType[] credentialTypes = {CredentialType.ofPlainText("graduate certificate")};
    createAndAcceptCredentials(credentialIssuerKeyPair, purchaserKeyPair, credentialTypes);

    // Create a permissioned domain.
    createPermissionedDomain(domainOwnerKeyPair, credentialIssuerKeyPair, credentialTypes);
    PermissionedDomainObject permissionedDomainObject = getPermissionedDomainObject(
      domainOwnerKeyPair.publicKey().deriveAddress());

    IssuedCurrencyAmount purchaseOfferTakerPays = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("MXN")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount purchaseOfferTakerGets = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5));

    AccountInfoResult purchaserAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaserKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger purchaserOfferCreateSequence = purchaserAccountInfo.accountData().sequence();

    OfferCreate purchaserOfferCreate = OfferCreate.builder()
      .account(purchaserKeyPair.publicKey().deriveAddress())
      .takerGets(purchaseOfferTakerGets)
      .takerPays(purchaseOfferTakerPays)
      .signingPublicKey(purchaserKeyPair.publicKey())
      .sequence(purchaserOfferCreateSequence)
      .flags(OfferCreateFlags.builder().tfImmediateOrCancel(true).build())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .domainId(permissionedDomainObject.index())
      .build();

    SingleSignedTransaction<OfferCreate> signedPurchaserOfferCreate = signatureService.sign(
      purchaserKeyPair.privateKey(), purchaserOfferCreate
    );

    SubmitResult<OfferCreate> intermediateResult = xrplClient.submit(signedPurchaserOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<OfferCreate> offerCreateTransactionResult = this.scanForResult(
      () -> this.getValidatedTransaction(intermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    assertThat(offerCreateTransactionResult.metadata().get().transactionResult())
      .isEqualTo("tecKILLED");
  }

  @Test
  public void createHybridSellAndOpenBuyOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    // GIVEN an open buy offer that has a really great exchange rate
    // THEN the OfferCreate should fully match a hybrid sell offer and generate a balance.

    KeyPair credentialIssuerKeyPair = createRandomAccountEd25519();
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair sellerKeyPair = createRandomAccountEd25519();

    // Create and accept credentials.
    CredentialType[] credentialTypes = {CredentialType.ofPlainText("graduate certificate")};
    createAndAcceptCredentials(credentialIssuerKeyPair, sellerKeyPair, credentialTypes);

    // Create a permissioned domain.
    createPermissionedDomain(domainOwnerKeyPair, credentialIssuerKeyPair, credentialTypes);
    PermissionedDomainObject permissionedDomainObject = getPermissionedDomainObject(
      domainOwnerKeyPair.publicKey().deriveAddress());

    // Create a hybrid sell offer with a valid DomainID.
    IssuedCurrencyAmount sellerOfferTakerGets = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("KYD")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount sellerOfferTakerPays = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2));

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult sellerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sellerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger sellerOfferCreateSequence = sellerAccountInfo.accountData().sequence();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(sellerKeyPair.publicKey().deriveAddress())
      .takerGets(sellerOfferTakerGets)
      .takerPays(sellerOfferTakerPays)
      .signingPublicKey(sellerKeyPair.publicKey())
      .sequence(sellerOfferCreateSequence)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .flags(OfferCreateFlags.builder().tfSell(true).tfHybrid(true).build())
      .domainId(permissionedDomainObject.index())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      sellerKeyPair.privateKey(), offerCreate
    );

    SubmitResult<OfferCreate> createTxIntermediateResult = xrplClient.submit(signedOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    OfferObject offerObject = scanForOffer(sellerKeyPair, sellerOfferCreateSequence);
    assertThatEntryEqualsObjectFromAccountObjects(offerObject);

    // Validate `book_offers` RPC with the domain filter, returns the correct Offer object.
    BookOffersResult bookOffersResult = xrplClient.bookOffers(
      BookOffersRequestParams.builder()
        .taker(offerCreate.account())
        .takerGets(
          Issue.builder()
            .currency("KYD")
            .issuer(offerCreate.account())
            .build()
        )
        .takerPays(Issue.XRP)
        .domain(permissionedDomainObject.index())
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(bookOffersResult.offers()).asList().hasSize(1);
    assertThat(bookOffersResult.offers().get(0).quality())
      .isEqualTo(BigDecimal.valueOf(sellerOfferTakerPays.value().longValue())
        .divide(new BigDecimal(sellerOfferTakerGets.value())));
    assertThat(bookOffersResult.offers().get(0).account()).isEqualTo(offerCreate.account());
    assertThat(bookOffersResult.offers().get(0).flags().lsfSell()).isTrue();
    assertThat(bookOffersResult.offers().get(0).flags().lsfPassive()).isFalse();
    assertThat(bookOffersResult.offers().get(0).sequence()).isEqualTo(offerCreate.sequence());
    assertThat(bookOffersResult.offers().get(0).takerPays()).isEqualTo(sellerOfferTakerPays);
    assertThat(bookOffersResult.offers().get(0).takerGets()).isEqualTo(sellerOfferTakerGets);
    assertThat(bookOffersResult.offers().get(0).ownerFunds()).isNotEmpty().get()
      .isEqualTo(new BigDecimal(sellerOfferTakerGets.value()));
    assertThat(bookOffersResult.offers().get(0).domainId()).isPresent().get()
      .isEqualTo(permissionedDomainObject.index());
    assertThat(bookOffersResult.offers().get(0).additionalBooks()).isNotEmpty();

    // Create an open buy offer that crosses and fills the above created hybrid sell offer.
    KeyPair purchaserKeyPair = createRandomAccountEd25519();

    IssuedCurrencyAmount purchaseOfferTakerPays = IssuedCurrencyAmount.builder()
      .value("2")
      .currency("KYD")
      .issuer(sellerKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount purchaseOfferTakerGets = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5));

    AccountInfoResult purchaserAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaserKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger purchaserOfferCreateSequence = purchaserAccountInfo.accountData().sequence();

    OfferCreate purchaserOfferCreate = OfferCreate.builder()
      .account(purchaserKeyPair.publicKey().deriveAddress())
      .takerGets(purchaseOfferTakerGets)
      .takerPays(purchaseOfferTakerPays)
      .signingPublicKey(purchaserKeyPair.publicKey())
      .sequence(purchaserOfferCreateSequence)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .build();

    SingleSignedTransaction<OfferCreate> signedPurchaserOfferCreate = signatureService.sign(
      purchaserKeyPair.privateKey(), purchaserOfferCreate
    );

    SubmitResult<OfferCreate> intermediateResult = xrplClient.submit(signedPurchaserOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(intermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    // Poll the ledger for the source purchaser's balances, and validate the expected currency balance exists
    RippleStateObject issuedCurrency = scanForIssuedCurrency(purchaserKeyPair, "KYD",
      sellerKeyPair.publicKey().deriveAddress());

    if (issuedCurrency.lowLimit().issuer().equals(sellerKeyPair.publicKey().deriveAddress())) {
      assertThat(issuedCurrency.balance().value()).isEqualTo("-" + purchaseOfferTakerPays.value());
    } else {
      assertThat(issuedCurrency.balance().value()).isEqualTo(purchaseOfferTakerPays.value());
    }
  }

  @Test
  public void domainDoesNotExist() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair offerCreaterKeyPair = createRandomAccountEd25519();
    IssuedCurrencyAmount takerGets = IssuedCurrencyAmount.builder()
      .value("10")
      .currency("CNY")
      .issuer(offerCreaterKeyPair.publicKey().deriveAddress())
      .build();
    XrpCurrencyAmount takerPays = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(200.0));

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(offerCreaterKeyPair.publicKey().deriveAddress())
    );

    // Create an offer with DomainID that does not exist.
    OfferCreate offerCreate = OfferCreate.builder()
      .account(offerCreaterKeyPair.publicKey().deriveAddress())
      .takerGets(takerGets)
      .takerPays(takerPays)
      .signingPublicKey(offerCreaterKeyPair.publicKey())
      .sequence(accountInfoResult.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .domainId(Hash256.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      offerCreaterKeyPair.privateKey(), offerCreate
    );
    SubmitResult<OfferCreate> createTxIntermediateResult = xrplClient.submit(signedOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tecNO_PERMISSION");

    // Then wait until the transaction gets committed to a validated ledger
    TransactionResult<OfferCreate> offerCreateTransactionResult = this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    assertThat(offerCreateTransactionResult.metadata().get().transactionResult())
      .isEqualTo("tecNO_PERMISSION");
  }

  private PermissionedDomainObject getPermissionedDomainObject(Address domainOwner) {
    return (PermissionedDomainObject) this.scanForResult(
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
  }

  /**
   * Scan the ledger for a specific Offer.
   *
   * @param purchaser The {@link KeyPair} of the offer owner.
   * @param sequence  The sequence of the offer.
   *
   * @return An {@link OfferObject}.
   */
  public OfferObject scanForOffer(KeyPair purchaser, UnsignedInteger sequence) {
    return this.scanForLedgerObject(
      () -> this.getValidatedAccountObjects(purchaser.publicKey().deriveAddress(), OfferObject.class)
        .stream()
        .filter(object -> object.sequence().equals(sequence) && object.account()
          .equals(purchaser.publicKey().deriveAddress()))
        .findFirst()
        .orElse(null));
  }

  /**
   * Scan the ledger until the purchaser account has a trustline with the issuer account for a given currency.
   *
   * @param purchaser The {@link KeyPair} of the source account.
   * @param currency  A {@link String} denoting the currency code.
   * @param issuer    The {@link Address} of the issuer account.
   *
   * @return The {@link RippleStateObject} between the purchaser and issuer, if found.
   */
  public RippleStateObject scanForIssuedCurrency(KeyPair purchaser, String currency, Address issuer) {
    return this.scanForLedgerObject(
      () -> this.getValidatedAccountObjects(purchaser.publicKey().deriveAddress(), RippleStateObject.class)
        .stream()
        .filter(state ->
          state.balance().currency().equals(currency) &&
            (state.lowLimit().issuer().equals(issuer)) || state.highLimit().issuer().equals(issuer))
        .findFirst()
        .orElse(null));
  }


  private void assertThatEntryEqualsObjectFromAccountObjects(OfferObject offerObject)
    throws JsonRpcClientErrorException {
    LedgerEntryResult<OfferObject> entry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.offer(
        OfferLedgerEntryParams.builder()
          .account(offerObject.account())
          .seq(offerObject.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    LedgerEntryResult<OfferObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(offerObject.index(), OfferObject.class, LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entry.node());

    LedgerEntryResult<LedgerObject> entryByIndexUnTyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(offerObject.index(), LedgerSpecifier.VALIDATED)
    );

    assertThat(entryByIndex.node()).isEqualTo(entryByIndexUnTyped.node());
  }

  /**
   * Asserts the supplier returns empty results, waiting up to 10 seconds for that condition to be true.
   *
   * @param supplier results supplier.
   */
  private void assertEmptyResults(Supplier<Collection<?>> supplier) {
    Awaitility.await()
      .atMost(AT_MOST_INTERVAL)
      .until(supplier::get, Matchers.empty());
  }

  /**
   * Cancels an offer and verifies the offer no longer exists on ledger for the account.
   *
   * @param purchaser     The {@link KeyPair} of the buyer.
   * @param offerSequence The sequence of the offer.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  private void cancelOffer(
    KeyPair purchaser,
    UnsignedInteger offerSequence,
    String expectedResult
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountInfoResult infoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(purchaser.publicKey().deriveAddress()));
    UnsignedInteger nextSequence = infoResult.accountData().sequence();

    FeeResult feeResult = xrplClient.fee();

    OfferCancel offerCancel = OfferCancel.builder()
      .account(purchaser.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(nextSequence)
      .offerSequence(offerSequence)
      .signingPublicKey(purchaser.publicKey())
      .build();

    SingleSignedTransaction<OfferCancel> signedOfferCancel = signatureService.sign(
      purchaser.privateKey(), offerCancel
    );
    SubmitResult<OfferCancel> cancelResponse = xrplClient.submit(signedOfferCancel);
    assertThat(cancelResponse.engineResult()).isEqualTo(expectedResult);

    assertEmptyResults(() -> this.getValidatedAccountObjects(purchaser.publicKey().deriveAddress(), OfferObject.class));
    assertEmptyResults(
      () -> this.getValidatedAccountObjects(purchaser.publicKey().deriveAddress(), RippleStateObject.class));
  }
}
