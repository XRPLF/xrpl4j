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
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class IsFinalIT extends AbstractIT {

  KeyPair keyPair = createRandomAccountEd25519();
  Address sourceAddress = keyPair.publicKey().deriveAddress();

  ImmutablePayment.Builder payment;
  UnsignedInteger lastLedgerSequence;
  AccountInfoResult accountInfo;

  @BeforeEach
  void setup() throws JsonRpcClientErrorException {
    ///////////////////////
    // Get validated account info and validate account state
    accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceAddress));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    FeeResult feeResult = xrplClient.fee();

    LedgerIndex validatedLedger = xrplClient.ledger(
        LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .build()
      )
      .ledgerIndexSafe();


    lastLedgerSequence = validatedLedger.plus(UnsignedInteger.ONE).unsignedIntegerValue();
    KeyPair destinationKeyPair = createRandomAccountEd25519();
    payment = Payment.builder()
      .account(sourceAddress)
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationKeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(10))
      .signingPublicKey(keyPair.publicKey());
  }

  @Test
  public void simpleIsFinalTest() throws JsonRpcClientErrorException, JsonProcessingException {
    Payment builtPayment = payment.build();
    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(keyPair.privateKey(), builtPayment);
    SubmitResult<Payment> response = xrplClient.submit(signedPayment);
    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    Hash256 txHash = response.transactionResult().hash();

    assertThat(
      xrplClient.isFinal(
        txHash,
        response.validatedLedgerIndex(),
        lastLedgerSequence,
        accountInfo.accountData().sequence(),
        sourceAddress
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.NOT_FINAL);

    this.scanForResult(
      () -> getValidatedTransaction(txHash, Payment.class)
    );

    assertThat(
      xrplClient.isFinal(
        txHash,
        response.validatedLedgerIndex(),
        lastLedgerSequence,
        accountInfo.accountData().sequence(),
        sourceAddress
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);
  }

  @Test
  public void isFinalExpiredTxTest() throws JsonRpcClientErrorException, JsonProcessingException {

    Payment builtPayment = payment
      .sequence(accountInfo.accountData().sequence().minus(UnsignedInteger.ONE))
      .build();
    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(keyPair.privateKey(), builtPayment);
    SubmitResult<Payment> response = xrplClient.submit(signedPayment);
    Hash256 txHash = response.transactionResult().hash();

    assertThat(
      xrplClient.isFinal(
        txHash,
        response.validatedLedgerIndex(),
        lastLedgerSequence.minus(UnsignedInteger.ONE),
        accountInfo.accountData().sequence(),
        sourceAddress
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.NOT_FINAL);

    this.scanForResult(
      () -> xrplClient.isFinal(
        response.transactionResult().hash(),
        response.validatedLedgerIndex(),
        lastLedgerSequence.minus(UnsignedInteger.ONE),
        accountInfo.accountData().sequence(),
        sourceAddress
      ).finalityStatus(),
      finalityStatus -> finalityStatus.equals(FinalityStatus.EXPIRED)
    );
  }

  @Test
  public void isFinalNoTrustlineIouPayment_ValidatedFailureResponse()
    throws JsonRpcClientErrorException, JsonProcessingException {

    Payment builtPayment = payment
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(
        sourceAddress).value("500").build()
      ).build();
    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(keyPair.privateKey(), builtPayment);
    SubmitResult<Payment> response = xrplClient.submit(signedPayment);
    Hash256 txHash = response.transactionResult().hash();

    assertThat(
      xrplClient.isFinal(
        txHash,
        response.validatedLedgerIndex(),
        lastLedgerSequence,
        accountInfo.accountData().sequence(),
        sourceAddress
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.NOT_FINAL);

    this.scanForResult(
      () -> xrplClient.isFinal(
        response.transactionResult().hash(),
        response.validatedLedgerIndex(),
        lastLedgerSequence.minus(UnsignedInteger.ONE),
        accountInfo.accountData().sequence(),
        sourceAddress
      ).finalityStatus(),
      finalityStatus -> finalityStatus.equals(FinalityStatus.VALIDATED_FAILURE)
    );
  }
}
