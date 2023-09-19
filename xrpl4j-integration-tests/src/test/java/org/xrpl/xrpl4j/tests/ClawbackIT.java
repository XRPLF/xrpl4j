package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Clawback;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class ClawbackIT extends AbstractIT {

  @Test
  void issueBalanceAndClawback() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair holderKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccount = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    XrpCurrencyAmount fee = FeeUtils.computeNetworkFees(feeResult).recommendedFee();
    setAllowClawback(issuerKeyPair, issuerAccount, fee);

    createTrustLine(
      "USD",
      "10000",
      issuerKeyPair,
      holderKeyPair,
      fee
    );

    sendIssuedCurrency(
      "USD",
      "100",
      issuerKeyPair,
      holderKeyPair,
      fee
    );

    issuerAccount = this.getValidatedAccountInfo(issuerAccount.accountData().account());
    clawback(
      "USD",
      "10",
      holderKeyPair.publicKey().deriveAddress(),
      issuerKeyPair,
      issuerAccount,
      fee
    );

    TrustLine trustline = this.getValidatedAccountLines(
        issuerAccount.accountData().account(),
        holderKeyPair.publicKey().deriveAddress()
      ).lines().stream()
      .filter(line -> line.currency().equals("USD"))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("No trustline found."));

    assertThat(trustline.balance()).isEqualTo("-90");

    issuerAccount = this.getValidatedAccountInfo(issuerAccount.accountData().account());
    clawback(
      "USD",
      "90",
      holderKeyPair.publicKey().deriveAddress(),
      issuerKeyPair,
      issuerAccount,
      fee
    );

    trustline = this.getValidatedAccountLines(
        issuerAccount.accountData().account(),
        holderKeyPair.publicKey().deriveAddress()
      ).lines().stream()
      .filter(line -> line.currency().equals("USD"))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("No trustline found."));
    assertThat(trustline.balance()).isEqualTo("0");
  }

  private void clawback(
    String currencyCode,
    String amount,
    Address holderAddress,
    KeyPair issuerKeyPair,
    AccountInfoResult issuerAccountInfo,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Clawback clawback = Clawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .amount(
        IssuedCurrencyAmount.builder()
          .currency(currencyCode)
          .value(amount)
          .issuer(holderAddress)
          .build()
      )
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .build();

    SingleSignedTransaction<Clawback> signedClawback = signatureService.sign(issuerKeyPair.privateKey(), clawback);
    SubmitResult<Clawback> submitResult = xrplClient.submit(signedClawback);
    assertThat(submitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedClawback.hash(),
      issuerAccountInfo.ledgerIndexSafe(),
      clawback.lastLedgerSequence().get(),
      clawback.sequence(),
      clawback.account()
    );
  }

  private void setAllowClawback(
    KeyPair issuerKeyPair,
    AccountInfoResult issuerAccount,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    AccountSet accountSet = AccountSet.builder()
      .account(issuerAccount.accountData().account())
      .fee(fee)
      .sequence(issuerAccount.accountData().sequence())
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(issuerAccount.ledgerIndexSafe().unsignedIntegerValue().plus(UnsignedInteger.valueOf(4)))
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK)
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(issuerKeyPair.privateKey(), accountSet);
    SubmitResult<AccountSet> submitResult = xrplClient.submit(signedAccountSet);
    assertThat(submitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    scanForFinality(
      signedAccountSet.hash(),
      issuerAccount.ledgerIndexSafe(),
      accountSet.lastLedgerSequence().get(),
      accountSet.sequence(),
      accountSet.account()
    );

    AccountInfoResult accountInfoAfterSet = xrplClient.accountInfo(
      AccountInfoRequestParams.of(issuerAccount.accountData().account())
    );

    assertThat(accountInfoAfterSet.accountData().flags().lsfAllowTrustLineClawback()).isTrue();
  }
}
