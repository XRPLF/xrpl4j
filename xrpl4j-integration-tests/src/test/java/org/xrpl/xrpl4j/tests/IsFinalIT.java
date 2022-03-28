package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.JsonRpcRequest;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XAddress;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

public class IsFinalIT extends AbstractIT {

  Wallet wallet = createRandomAccount();

  @Test
  public void simpleIsFinalTest() throws JsonRpcClientErrorException, InterruptedException {

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    FeeResult feeResult = xrplClient.fee();

    LedgerIndex validatedLedger = xrplClient.ledger(
        LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .build()
      )
      .ledgerIndexSafe();


    UnsignedInteger lastLedgerSequence = UnsignedInteger.valueOf(
      validatedLedger.plus(UnsignedLong.valueOf(4)).unsignedLongValue().intValue()
    );

    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .setFlag(AccountSet.AccountSetFlag.ACCOUNT_TXN_ID)
      .lastLedgerSequence(lastLedgerSequence)
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult<AccountSet> response = xrplClient.submit(wallet, accountSet);
    assertThat(response.result()).isEqualTo("tesSUCCESS");

    assertThat(xrplClient.isFinal(
      response.transactionResult().hash(),
      response.validatedLedgerIndex(),
      lastLedgerSequence,
      accountInfo.accountData().sequence(),
      wallet.classicAddress()
    )).isEqualTo(XrplClient.FinalityStatus.NOT_FINAL);
    Thread.sleep(4000);
    assertThat(xrplClient.isFinal(
      response.transactionResult().hash(),
      response.validatedLedgerIndex(),
      lastLedgerSequence,
      accountInfo.accountData().sequence(),
      wallet.classicAddress()
    )).isEqualTo(XrplClient.FinalityStatus.VALIDATED_SUCCESS);
  }

  @Test
  public void isFinalExpiredTxTest() throws JsonRpcClientErrorException, InterruptedException {

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    FeeResult feeResult = xrplClient.fee();

    LedgerIndex validatedLedger = xrplClient.ledger(
        LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .build()
      )
      .ledgerIndexSafe();


    UnsignedInteger lastLedgerSequence = UnsignedInteger.valueOf(
      validatedLedger.plus(UnsignedLong.valueOf(1)).unsignedLongValue().intValue()
    );

    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence().minus(UnsignedInteger.ONE))
      .setFlag(AccountSet.AccountSetFlag.ACCOUNT_TXN_ID)
      .lastLedgerSequence(lastLedgerSequence)
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult<AccountSet> response = xrplClient.submit(wallet, accountSet);

    assertThat(xrplClient.isFinal(
      response.transactionResult().hash(),
      response.validatedLedgerIndex(),
      lastLedgerSequence,
      accountInfo.accountData().sequence(),
      wallet.classicAddress()
    )).isEqualTo(XrplClient.FinalityStatus.NOT_FINAL);
    Thread.sleep(1000);

    assertThat(xrplClient.isFinal(
      response.transactionResult().hash(),
      response.validatedLedgerIndex(),
      lastLedgerSequence,
      accountInfo.accountData().sequence(),
      wallet.classicAddress()
    )).isEqualTo(XrplClient.FinalityStatus.EXPIRED);
  }

  @Test
  public void isFinalValidatedFailureResponseTest() throws JsonRpcClientErrorException, InterruptedException {

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    FeeResult feeResult = xrplClient.fee();

    LedgerIndex validatedLedger = xrplClient.ledger(
        LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED)
          .build()
      )
      .ledgerIndexSafe();


    UnsignedInteger lastLedgerSequence = UnsignedInteger.valueOf(
      validatedLedger.plus(UnsignedLong.valueOf(1)).unsignedLongValue().intValue()
    );

    Wallet destinationWallet = createRandomAccount();
    Payment payment = Payment.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(
      wallet.classicAddress()).value("500").build()
      ).signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult<Payment> response = xrplClient.submit(wallet, payment);

    assertThat(xrplClient.isFinal(
      response.transactionResult().hash(),
      response.validatedLedgerIndex(),
      lastLedgerSequence,
      accountInfo.accountData().sequence(),
      wallet.classicAddress()
    )).isEqualTo(XrplClient.FinalityStatus.NOT_FINAL);
    Thread.sleep(1000);

    assertThat(xrplClient.isFinal(
      response.transactionResult().hash(),
      response.validatedLedgerIndex(),
      lastLedgerSequence,
      accountInfo.accountData().sequence(),
      wallet.classicAddress()
    )).isEqualTo(XrplClient.FinalityStatus.VALIDATED_FAILURE);
  }
}
