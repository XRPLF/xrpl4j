package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.client.model.accounts.AccountInfoResult;
import com.ripple.xrpl4j.client.model.fees.FeeResult;
import com.ripple.xrpl4j.client.model.ledger.objects.RippleStateObject;
import com.ripple.xrpl4j.client.model.transactions.SubmissionResult;
import com.ripple.xrpl4j.client.rippled.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.TrustSet;
import com.ripple.xrpl4j.wallet.Wallet;
import org.junit.jupiter.api.Test;

public class IssuedCurrencyIT extends AbstractIT {

  @Test
  public void issueIssuedCurrencyBalance() throws JsonRpcClientErrorException {
    ///////////////////////////
    // Create random accounts for the issuer and the counterparty
    Wallet issuerWallet = createRandomAccount();
    Wallet counterpartyWallet = createRandomAccount();

    ///////////////////////////
    // Create a Trust Line between issuer and counterparty by submitting a TrustSet transaction
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult counterpartyAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartyWallet.classicAddress())
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartyWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(counterpartyAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerWallet.classicAddress())
        .value("100")
        .build())
      .signingPublicKey(counterpartyWallet.publicKey())
      .build();

    SubmissionResult<TrustSet> trustSetSubmitResult = xrplClient.submit(counterpartyWallet, trustSet);
    assertThat(trustSetSubmitResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/" + trustSetSubmitResult.transaction().hash()
        .orElse("n/a")
    );

    ///////////////////////////
    // Wait for the TrustSet transaction to be included in a validated ledger
    this.scanForResult(
      () -> getValidatedTransaction(
        trustSetSubmitResult.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Cannot look up transaction because no hash was returned.")),
        TrustSet.class
      ));

    ///////////////////////////
    // Issuer sends a payment with the issued currency to the counterparty
    AccountInfoResult issuerAccountInfo = this.scanForResult(() -> getValidatedAccountInfo(issuerWallet.classicAddress()));

    Payment fundCounterparty = Payment.builder()
      .account(issuerWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(counterpartyWallet.classicAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerWallet.classicAddress())
        .currency("USD")
        .value("50")
        .build())
      .signingPublicKey(issuerWallet.publicKey())
      .build();

    SubmissionResult<Payment> paymentResult = xrplClient.submit(issuerWallet, fundCounterparty);
    assertThat(paymentResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" + paymentResult.transaction().hash()
        .orElse("n/a")
    );

    ///////////////////////////
    // Validate that the payment was included in a validated ledger and that the
    // RippleState object, which tracks the Trust Line, gets updated correctly
    this.scanForResult(
      () -> this.getValidatedAccountObjects(counterpartyWallet.classicAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          RippleStateObject.class.isAssignableFrom(object.getClass()) &&
            ((RippleStateObject) object).balance().value().equals(
              ((RippleStateObject) object).lowLimit().issuer().equals(counterpartyWallet.classicAddress()) ? "50" : "-50"
            )
        ))
      .accountObjects().stream()
      .filter(object -> RippleStateObject.class.isAssignableFrom(object.getClass()))
      .findFirst().get();
  }

}
