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
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutableOfferCreate;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Integration test to validate submission of Payment transactions.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class SubmitPaymentIT extends AbstractIT {

  public static final String SUCCESS_STATUS = "tesSUCCESS";

  private static final String CURRENCY = "USD";

  @Test
  public void sendPayment() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(12345);
    Payment payment = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(amount)
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(sourceKeyPair.privateKey(), payment);
    SubmitResult<Payment> result = xrplClient.submit(signedPayment);
    assertThat(result.engineResult()).isEqualTo(SUCCESS_STATUS);
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class)
    );

    assertThat(validatedPayment.metadata().flatMap(TransactionMetadata::deliveredAmount)).hasValue(amount);
    assertThat(validatedPayment.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    assertPaymentCloseTimeMatchesLedgerCloseTime(validatedPayment);
  }

  @Test
  public void sendPaymentFromSecp256k1KeyPair() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair senderKeyPair = this.createRandomAccountSecp256k1();
    logger.info("Generated source testnet wallet with address " + senderKeyPair.publicKey().deriveAddress());

    KeyPair destinationKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderKeyPair.publicKey().deriveAddress())
    );

    Payment payment = Payment.builder()
      .account(senderKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(senderKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(senderKeyPair.privateKey(), payment);
    SubmitResult<Payment> result = xrplClient.submit(signedPayment);
    assertThat(result.engineResult()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  public void testPermissionedPaymentConsumesPermissionedOffer() throws JsonRpcClientErrorException,
    JsonProcessingException {
    // Create all necessary accounts involved in the transaction flow.
    KeyPair tokenIssuerKeyPair = createRandomAccountEd25519();
    KeyPair domainOfferCreatorKeyPair = createRandomAccountEd25519();
    KeyPair openOfferCreatorKeyPair = createRandomAccountEd25519();
    KeyPair credentialIssuerKeyPair = createRandomAccountEd25519();
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair sourceKeyPair = createRandomAccountEd25519();
    KeyPair destinationKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // Create issued currency and trust lines between token issuer, destination and offer creators.
    IssuedCurrencyAmount issuedCurrencyAmount = IssuedCurrencyAmount.builder()
      .issuer(tokenIssuerKeyPair.publicKey().deriveAddress())
      .currency(CURRENCY)
      .value("1000")
      .build();

    createTrustLine(
      domainOfferCreatorKeyPair, issuedCurrencyAmount,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee(), TrustSetFlags.empty()
    );

    createTrustLine(
      openOfferCreatorKeyPair, issuedCurrencyAmount,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee(), TrustSetFlags.empty()
    );

    createTrustLine(
      destinationKeyPair, issuedCurrencyAmount,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee(), TrustSetFlags.empty()
    );

    // Send some issued currency to the domain offer creator account.
    sendIssuedCurrency(
      tokenIssuerKeyPair, domainOfferCreatorKeyPair, issuedCurrencyAmount,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Send some issued currency to the open offer creator account.
    sendIssuedCurrency(
      tokenIssuerKeyPair, openOfferCreatorKeyPair, issuedCurrencyAmount,
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Credential issuer creates the credentials. Domain offer creator, payment sender and payment receiver accepts
    // them. Note - Open offer creator does not have these credentials. So it won't be a member of permissioned domain.
    CredentialType[] credentialTypes = {CredentialType.ofPlainText("graduate certificate")};
    createAndAcceptCredentials(credentialIssuerKeyPair, domainOfferCreatorKeyPair, credentialTypes);
    createAndAcceptCredentials(credentialIssuerKeyPair, sourceKeyPair, credentialTypes);
    createAndAcceptCredentials(credentialIssuerKeyPair, destinationKeyPair, credentialTypes);

    // Create a permissioned domain.
    createPermissionedDomain(domainOwnerKeyPair, credentialIssuerKeyPair, credentialTypes);
    PermissionedDomainObject permissionedDomainObject = getPermissionedDomainObject(
      domainOwnerKeyPair.publicKey().deriveAddress());

    // Create permissioned and open offer.
    IssuedCurrencyAmount takerPays = IssuedCurrencyAmount.builder()
      .issuer(tokenIssuerKeyPair.publicKey().deriveAddress())
      .currency(CURRENCY)
      .value("2")
      .build();

    createOffer(
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2)), takerPays, domainOfferCreatorKeyPair,
      Optional.ofNullable(permissionedDomainObject.index())
    );

    createOffer(
      XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2)), takerPays, openOfferCreatorKeyPair, Optional.empty()
    );

    // Submit a permissioned payment transaction.

    AccountInfoResult sourceAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceKeyPair.publicKey().deriveAddress())
    );

    Payment payment = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sourceAccountInfo.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(takerPays)
      .sendMax(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5)))
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(sourceKeyPair.privateKey(), payment);
    SubmitResult<Payment> result = xrplClient.submit(signedPayment);
    assertThat(result.engineResult()).isEqualTo("tesSUCCESS");

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  private void createOffer(
    XrpCurrencyAmount takerPays,
    IssuedCurrencyAmount takerGets,
    KeyPair offerCreatorKeyPair,
    Optional<Hash256> domain
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult offerCreatorInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(offerCreatorKeyPair.publicKey().deriveAddress())
    );
    UnsignedInteger offerCreateSequence = offerCreatorInfo.accountData().sequence();

    ImmutableOfferCreate.Builder builder = OfferCreate.builder()
      .account(offerCreatorKeyPair.publicKey().deriveAddress())
      .takerGets(takerGets)
      .takerPays(takerPays)
      .signingPublicKey(offerCreatorKeyPair.publicKey())
      .sequence(offerCreateSequence)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee());
    //.flags(OfferCreateFlags.builder().tfSell(true).build());

    domain.ifPresent(builder::domainId);

    OfferCreate offerCreate = builder.build();

    SingleSignedTransaction<OfferCreate> signedOfferCreate = signatureService.sign(
      offerCreatorKeyPair.privateKey(), offerCreate
    );

    SubmitResult<OfferCreate> createTxIntermediateResult = xrplClient.submit(signedOfferCreate);
    assertThat(createTxIntermediateResult.engineResult()).isEqualTo("tesSUCCESS");

    // Then wait until the transaction gets committed to a validated ledger
    this.scanForResult(
      () -> this.getValidatedTransaction(createTxIntermediateResult.transactionResult().hash(), OfferCreate.class)
    );
  }

  private void assertPaymentCloseTimeMatchesLedgerCloseTime(TransactionResult<Payment> validatedPayment)
    throws JsonRpcClientErrorException {

    LedgerResult ledger = this.scanForResult(
      () -> {
        try {
          return xrplClient.ledger(
            LedgerRequestParams.builder()
              .ledgerSpecifier(LedgerSpecifier.of(validatedPayment.ledgerIndex().get()))
              .build()
          );
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      }
    );

    assertThat(validatedPayment.closeDateHuman()).isNotEmpty();
    assertThat(ledger.ledger().closeTimeHuman()).isNotEmpty();
    assertThat(validatedPayment.closeDateHuman()).isEqualTo(ledger.ledger().closeTimeHuman());
  }

}