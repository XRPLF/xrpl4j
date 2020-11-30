package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.client.model.accounts.AccountInfoResult;
import com.ripple.xrpl4j.client.model.fees.FeeResult;
import com.ripple.xrpl4j.client.model.transactions.SubmissionResult;
import com.ripple.xrpl4j.client.rippled.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.SetRegularKey;
import com.ripple.xrpl4j.wallet.Wallet;
import org.junit.jupiter.api.Test;

public class SetRegularKeyIT extends AbstractIT {

  @Test
  void setRegularKeyOnAccount() throws JsonRpcClientErrorException {
    //////////////////////////
    // Create a random account
    Wallet wallet = createRandomAccount();

    //////////////////////////
    // Wait for the account to show up on ledger
    AccountInfoResult accountInfo = scanForResult(() -> getValidatedAccountInfo(wallet.classicAddress()));

    //////////////////////////
    // Generate a new wallet locally
    Wallet newWallet = walletFactory.randomWallet(true).wallet();

    //////////////////////////
    // Submit a SetRegularKey transaction with the new wallet's address so that we
    // can sign future transactions with the new wallet's keypair
    FeeResult feeResult = xrplClient.fee();
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .regularKey(newWallet.classicAddress())
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmissionResult<SetRegularKey> setResult = xrplClient.submit(wallet, setRegularKey);
    assertThat(setResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("SetRegularKey transaction successful. https://testnet.xrpl.org/transactions/{}",
      setResult.transaction().hash().orElse("n/a")
    );

    //////////////////////////
    // Verify that the SetRegularKey transaction worked by submitting empty
    // AccountSet transactions, signed with the new wallet key pair, until
    // we get a successful response.
    scanForResult(
      () -> {
        AccountSet accountSet = AccountSet.builder()
          .account(wallet.classicAddress())
          .fee(feeResult.drops().openLedgerFee())
          .sequence(accountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
          .signingPublicKey(newWallet.publicKey())
          .build();

        try {
          return xrplClient.submit(newWallet, accountSet);
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    );

  }

  @Test
  void removeRegularKeyFromAccount() throws JsonRpcClientErrorException {
    //////////////////////////
    // Create a random account
    Wallet wallet = createRandomAccount();

    //////////////////////////
    // Wait for the account to show up on ledger
    AccountInfoResult accountInfo = scanForResult(() -> getValidatedAccountInfo(wallet.classicAddress()));

    //////////////////////////
    // Generate a new wallet locally
    Wallet newWallet = walletFactory.randomWallet(true).wallet();

    //////////////////////////
    // Submit a SetRegularKey transaction with the new wallet's address so that we
    // can sign future transactions with the new wallet's keypair
    FeeResult feeResult = xrplClient.fee();
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .regularKey(newWallet.classicAddress())
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmissionResult<SetRegularKey> setResult = xrplClient.submit(wallet, setRegularKey);
    assertThat(setResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("SetRegularKey transaction successful. https://testnet.xrpl.org/transactions/{}",
      setResult.transaction().hash().orElse("n/a")
    );

    //////////////////////////
    // Verify that the SetRegularKey transaction worked by submitting empty
    // AccountSet transactions, signed with the new wallet key pair, until
    // we get a successful response.
    scanForResult(
      () -> {
        AccountSet accountSet = AccountSet.builder()
          .account(wallet.classicAddress())
          .fee(feeResult.drops().openLedgerFee())
          .sequence(accountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
          .signingPublicKey(newWallet.publicKey())
          .build();

        try {
          return xrplClient.submit(newWallet, accountSet);
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    );


    SetRegularKey removeRegularKey = SetRegularKey.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmissionResult<SetRegularKey> removeResult = xrplClient.submit(wallet, removeRegularKey);
    assertThat(removeResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("SetRegularKey transaction successful. https://testnet.xrpl.org/transactions/{}",
      removeResult.transaction().hash().orElse("n/a")
    );

    scanForResult(
      () -> getValidatedAccountInfo(wallet.classicAddress()),
      infoResult -> !infoResult.accountData().regularKey().isPresent()
    );
  }
}
