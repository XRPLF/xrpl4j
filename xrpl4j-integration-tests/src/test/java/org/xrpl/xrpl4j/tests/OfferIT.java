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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.OfferLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.path.BookOffersRequestParams;
import org.xrpl.xrpl4j.model.client.path.BookOffersResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Integration tests to validate submission of Offer-related transactions.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
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
          IouIssue.builder()
            .currency("USD")
            .issuer(offerCreate.account())
            .build()
        )
        .takerPays(XrpIssue.XRP)
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
      .currency(CURRENCY)
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
          IouIssue.builder()
            .currency(CURRENCY)
            .issuer(offerCreate.account())
            .build()
        )
        .takerPays(XrpIssue.XRP)
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
    // Only Offers with tfHybrid flag set will have non-empty additionalBooks.
    assertThat(bookOffersResult.offers().get(0).additionalBooks()).isEmpty();

    // Create a permissioned buy offer that crosses and fills the above created permissioned sell offer.
    IssuedCurrencyAmount purchaseOfferTakerPays = IssuedCurrencyAmount.builder()
      .value("2")
      .currency(CURRENCY)
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
    TransactionResult<OfferCreate> offerCreateTransactionResult = this.scanForResult(
      () -> this.getValidatedTransaction(intermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    assertThat(offerCreateTransactionResult.metadata().get().transactionResult())
      .isEqualTo("tesSUCCESS");

    // Poll the ledger for the source purchaser's balances, and validate the expected currency balance exists
    RippleStateObject issuedCurrency = scanForIssuedCurrency(purchaserKeyPair, CURRENCY,
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
      .currency(CURRENCY)
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
      .currency(CURRENCY)
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
      .currency(CURRENCY)
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
      .currency(CURRENCY)
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
      .currency(CURRENCY)
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
          IouIssue.builder()
            .currency(CURRENCY)
            .issuer(offerCreate.account())
            .build()
        )
        .takerPays(XrpIssue.XRP)
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
      .currency(CURRENCY)
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
    TransactionResult<OfferCreate> offerCreateTransactionResult = this.scanForResult(
      () -> this.getValidatedTransaction(intermediateResult.transactionResult().hash(), OfferCreate.class)
    );

    assertThat(offerCreateTransactionResult.metadata().get().transactionResult())
      .isEqualTo("tesSUCCESS");

    // Poll the ledger for the source purchaser's balances, and validate the expected currency balance exists
    RippleStateObject issuedCurrency = scanForIssuedCurrency(purchaserKeyPair, CURRENCY,
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
      .currency(CURRENCY)
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

  // =============================================
  // MPT Offer Integration Tests
  // =============================================

  /**
   * Creates an MPT issuance, authorizes a holder, mints tokens, then creates an {@link OfferCreate} with
   * MPT as {@code TakerGets} and XRP as {@code TakerPays}. Verifies the offer is on ledger via
   * {@link OfferLedgerEntryParams} and verifiable via the {@code book_offers} RPC using {@link MptIssue}.
   *
   * <p><strong>Note:</strong> This test requires rippled built from the MPT DEX feature branch with XLS-82d support.
   * MPT support in the DEX is available in the feature branch and will be merged to develop soon.</p>
   *
   * @see <a href="https://github.com/XRPLF/XRPL-Standards/discussions/177">XLS-82d: MPT DEX Integration</a>
   */
  @Test
  public void mptOfferCreateAndVerifyWithBookOffers() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance with tfMptCanTrade to allow DEX usage
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Extract MPT issuance ID from transaction metadata
    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Create an OfferCreate where the issuer sells MPT (TakerGets) for XRP (TakerPays)
    // The issuer can use MPT directly from their issuance as TakerGets
    MptCurrencyAmount takerGetsMpt = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("500")
      .build();
    XrpCurrencyAmount takerPaysXrp = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10));

    AccountInfoResult issuerInfoForOffer = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger offerSequence = issuerInfoForOffer.accountData().sequence();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(offerSequence)
      .signingPublicKey(issuerKeyPair.publicKey())
      .takerGets(takerGetsMpt)
      .takerPays(takerPaysXrp)
      .lastLedgerSequence(
        issuerInfoForOffer.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      issuerKeyPair.privateKey(), offerCreate
    );
    SubmitResult<OfferCreate> offerCreateResult = xrplClient.submit(signedOfferCreate);
    assertThat(offerCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for the offer to appear on ledger
    TransactionResult<OfferCreate> offerCreateTxResult = this.scanForResult(
      () -> this.getValidatedTransaction(offerCreateResult.transactionResult().hash(), OfferCreate.class)
    );
    assertThat(offerCreateTxResult.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    // Scan for the offer object in account objects
    OfferObject offerObject = scanForOffer(issuerKeyPair, offerSequence);
    assertThat(offerObject.takerGets()).isInstanceOf(MptCurrencyAmount.class);
    assertThat(((MptCurrencyAmount) offerObject.takerGets()).mptIssuanceId()).isEqualTo(mptIssuanceId);
    assertThat(offerObject.takerPays()).isEqualTo(takerPaysXrp);

    // Verify ledger entry via OfferLedgerEntryParams
    assertThatEntryEqualsObjectFromAccountObjects(offerObject);

    // Verify book_offers with MptIssue as takerGets
    BookOffersResult bookOffersResult = xrplClient.bookOffers(
      BookOffersRequestParams.builder()
        .taker(issuerKeyPair.publicKey().deriveAddress())
        .takerGets(MptIssue.of(mptIssuanceId))
        .takerPays(XrpIssue.XRP)
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(bookOffersResult.offers()).asList().isNotEmpty();
    assertThat(bookOffersResult.offers().get(0).account()).isEqualTo(issuerKeyPair.publicKey().deriveAddress());
    assertThat(bookOffersResult.offers().get(0).sequence()).isEqualTo(offerSequence);
    assertThat(bookOffersResult.offers().get(0).takerGets()).isInstanceOf(MptCurrencyAmount.class);
    assertThat(((MptCurrencyAmount) bookOffersResult.offers().get(0).takerGets()).mptIssuanceId())
      .isEqualTo(mptIssuanceId);
    assertThat(bookOffersResult.offers().get(0).takerPays()).isEqualTo(takerPaysXrp);
  }

  /**
   * Creates an MPT issuance, authorizes a buyer, mints MPT to a seller, then creates two crossing
   * offers: a sell offer (MPT for XRP) and a buy offer (XRP for MPT). Verifies the offers cross and
   * the buyer receives MPT tokens.
   *
   * <p><strong>Note:</strong> This test requires rippled built from the MPT DEX feature branch with XLS-82d support.
   * MPT support in the DEX is available in the feature branch and will be merged to develop soon.</p>
   *
   * @see <a href="https://github.com/XRPLF/XRPL-Standards/discussions/177">XLS-82d: MPT DEX Integration</a>
   */
  @Test
  public void mptOfferCrossing() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair buyerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult issuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    AccountInfoResult buyerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(buyerKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Authorize buyer to hold MPT
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(buyerKeyPair.publicKey().deriveAddress())
      .sequence(buyerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(buyerKeyPair.publicKey())
      .lastLedgerSequence(buyerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mptIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(
      buyerKeyPair.privateKey(), authorize
    );
    SubmitResult<MpTokenAuthorize> authorizeResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedAuthorize.hash(),
      authorizeResult.validatedLedgerIndex(),
      authorize.lastLedgerSequence().get(),
      authorize.sequence(),
      buyerKeyPair.publicKey().deriveAddress()
    );

    // Issuer creates a sell offer: give 100 MPT tokens, want 5 XRP
    AccountInfoResult issuerInfoForOffer = scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger sellOfferSequence = issuerInfoForOffer.accountData().sequence();

    OfferCreate sellOffer = OfferCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sellOfferSequence)
      .signingPublicKey(issuerKeyPair.publicKey())
      .takerGets(MptCurrencyAmount.builder().mptIssuanceId(mptIssuanceId).value("100").build())
      .takerPays(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5)))
      .lastLedgerSequence(
        issuerInfoForOffer.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<OfferCreate> signedSellOffer = signatureService.sign(
      issuerKeyPair.privateKey(), sellOffer
    );
    SubmitResult<OfferCreate> sellOfferResult = xrplClient.submit(signedSellOffer);
    assertThat(sellOfferResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedSellOffer.hash(),
      sellOfferResult.validatedLedgerIndex(),
      sellOffer.lastLedgerSequence().get(),
      sellOffer.sequence(),
      issuerKeyPair.publicKey().deriveAddress()
    );

    // Verify sell offer is visible via book_offers with MptIssue
    BookOffersResult bookOffersBeforeCross = xrplClient.bookOffers(
      BookOffersRequestParams.builder()
        .takerGets(MptIssue.of(mptIssuanceId))
        .takerPays(XrpIssue.XRP)
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );
    assertThat(bookOffersBeforeCross.offers()).asList().isNotEmpty();
    assertThat(bookOffersBeforeCross.offers().get(0).account())
      .isEqualTo(issuerKeyPair.publicKey().deriveAddress());

    // Buyer creates a crossing buy offer: want 100 MPT tokens, give 5 XRP
    AccountInfoResult buyerInfoForOffer = scanForResult(
      () -> this.getValidatedAccountInfo(buyerKeyPair.publicKey().deriveAddress())
    );

    OfferCreate buyOffer = OfferCreate.builder()
      .account(buyerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(buyerInfoForOffer.accountData().sequence())
      .signingPublicKey(buyerKeyPair.publicKey())
      .takerPays(MptCurrencyAmount.builder().mptIssuanceId(mptIssuanceId).value("100").build())
      .takerGets(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5)))
      .lastLedgerSequence(
        buyerInfoForOffer.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<OfferCreate> signedBuyOffer = signatureService.sign(
      buyerKeyPair.privateKey(), buyOffer
    );
    SubmitResult<OfferCreate> buyOfferResult = xrplClient.submit(signedBuyOffer);
    assertThat(buyOfferResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<OfferCreate> buyOfferTxResult = this.scanForResult(
      () -> this.getValidatedTransaction(buyOfferResult.transactionResult().hash(), OfferCreate.class)
    );
    assertThat(buyOfferTxResult.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
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

  /**
   * Creates an MPT issuance and IOU trust line, then creates an {@link OfferCreate} with
   * MPT as {@code TakerGets} and IOU as {@code TakerPays}. Verifies the offer via {@code book_offers} RPC
   * using {@link MptIssue} and {@link CurrencyIssue}.
   *
   * <p><strong>Note:</strong> This test requires rippled built from the MPT DEX feature branch with XLS-82d support.
   * MPT support in the DEX is available in the feature branch and will be merged to develop soon.</p>
   *
   * @see <a href="https://github.com/XRPLF/XRPL-Standards/discussions/177">XLS-82d: MPT DEX Integration</a>
   */
  @Test
  public void mptOfferCreateWithIouAndVerifyWithBookOffers()
    throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair mptIssuerKeyPair = createRandomAccountEd25519();
    KeyPair iouIssuerKeyPair = createRandomAccountEd25519();
    FeeResult feeResult = xrplClient.fee();

    AccountInfoResult mptIssuerAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );
    scanForResult(
      () -> this.getValidatedAccountInfo(iouIssuerKeyPair.publicKey().deriveAddress())
    );

    // Create MPT issuance with tfMptCanTrade to allow DEX usage
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .sequence(mptIssuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(
        mptIssuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .build())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      mptIssuerKeyPair.privateKey(), issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIssuanceCreate.hash(),
      issuanceCreateResult.validatedLedgerIndex(),
      issuanceCreate.lastLedgerSequence().get(),
      issuanceCreate.sequence(),
      mptIssuerKeyPair.publicKey().deriveAddress()
    );

    final MpTokenIssuanceId mptIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Create IOU trust line from MPT issuer to IOU issuer
    AccountInfoResult mptIssuerInfoBeforeTrust = scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    String iouCurrency = "USD";
    TrustSet trustSet = TrustSet.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerInfoBeforeTrust.accountData().sequence())
      .lastLedgerSequence(
        mptIssuerInfoBeforeTrust.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .limitAmount(IssuedCurrencyAmount.builder()
        .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
        .currency(iouCurrency)
        .value("100000")
        .build())
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(
      mptIssuerKeyPair.privateKey(), trustSet
    );
    SubmitResult<TrustSet> trustSetResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedTrustSet.hash(),
      trustSetResult.validatedLedgerIndex(),
      trustSet.lastLedgerSequence().get(),
      trustSet.sequence(),
      mptIssuerKeyPair.publicKey().deriveAddress()
    );

    // IOU issuer sends IOU to MPT issuer
    AccountInfoResult iouIssuerInfoBeforePayment = scanForResult(
      () -> this.getValidatedAccountInfo(iouIssuerKeyPair.publicKey().deriveAddress())
    );

    Payment iouPayment = Payment.builder()
      .account(iouIssuerKeyPair.publicKey().deriveAddress())
      .destination(mptIssuerKeyPair.publicKey().deriveAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
        .currency(iouCurrency)
        .value("10000")
        .build())
      .sequence(iouIssuerInfoBeforePayment.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(
        iouIssuerInfoBeforePayment.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .signingPublicKey(iouIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedIouPayment = signatureService.sign(
      iouIssuerKeyPair.privateKey(), iouPayment
    );
    SubmitResult<Payment> iouPaymentResult = xrplClient.submit(signedIouPayment);
    assertThat(iouPaymentResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    scanForFinality(
      signedIouPayment.hash(),
      iouPaymentResult.validatedLedgerIndex(),
      iouPayment.lastLedgerSequence().get(),
      iouPayment.sequence(),
      iouIssuerKeyPair.publicKey().deriveAddress()
    );

    // Create offer: MPT issuer offers MPT (TakerGets) for IOU (TakerPays)
    AccountInfoResult mptIssuerInfoBeforeOffer = scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    MptCurrencyAmount takerGetsMpt = MptCurrencyAmount.builder()
      .mptIssuanceId(mptIssuanceId)
      .value("500")
      .build();

    IssuedCurrencyAmount takerPaysIou = IssuedCurrencyAmount.builder()
      .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
      .currency(iouCurrency)
      .value("100")
      .build();

    OfferCreate offerCreate = OfferCreate.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerInfoBeforeOffer.accountData().sequence())
      .lastLedgerSequence(
        mptIssuerInfoBeforeOffer.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .takerGets(takerGetsMpt)
      .takerPays(takerPaysIou)
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      mptIssuerKeyPair.privateKey(), offerCreate
    );
    SubmitResult<OfferCreate> offerCreateResult = xrplClient.submit(signedOfferCreate);
    assertThat(offerCreateResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    UnsignedInteger offerSequence = offerCreate.sequence();

    scanForFinality(
      signedOfferCreate.hash(),
      offerCreateResult.validatedLedgerIndex(),
      offerCreate.lastLedgerSequence().get(),
      offerSequence,
      mptIssuerKeyPair.publicKey().deriveAddress()
    );

    // Verify book_offers with MptIssue as takerGets and CurrencyIssue as takerPays
    BookOffersResult bookOffersResult = xrplClient.bookOffers(
      BookOffersRequestParams.builder()
        .taker(mptIssuerKeyPair.publicKey().deriveAddress())
        .takerGets(MptIssue.of(mptIssuanceId))
        .takerPays(IouIssue.builder()
          .issuer(iouIssuerKeyPair.publicKey().deriveAddress())
          .currency(iouCurrency)
          .build())
        .ledgerSpecifier(LedgerSpecifier.CURRENT)
        .build()
    );

    assertThat(bookOffersResult.offers()).asList().isNotEmpty();
    assertThat(bookOffersResult.offers().get(0).account()).isEqualTo(mptIssuerKeyPair.publicKey().deriveAddress());
    assertThat(bookOffersResult.offers().get(0).sequence()).isEqualTo(offerSequence);
    assertThat(bookOffersResult.offers().get(0).takerGets()).isInstanceOf(MptCurrencyAmount.class);
    assertThat(((MptCurrencyAmount) bookOffersResult.offers().get(0).takerGets()).mptIssuanceId())
      .isEqualTo(mptIssuanceId);
    assertThat(bookOffersResult.offers().get(0).takerPays()).isEqualTo(takerPaysIou);

    logger.info("Successfully verified book_offers with MPT/IOU pair");
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
