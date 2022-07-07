package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;

/**
 * Integration Tests to validate submission of SetRegularKey transactions.
 */
public class SetRegularKeyIT extends AbstractIT {

  @Test
  void setRegularKeyOnAccount() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////////
    // Create a random account
    Wallet wallet = createRandomAccountEd25519();

    //////////////////////////
    // Wait for the account to show up on ledger
    AccountInfoResult accountInfo = scanForResult(() -> getValidatedAccountInfo(wallet.address()));

    //////////////////////////
    // Generate a new wallet locally
    Wallet newWallet = walletFactory.randomWallet().wallet();

    //////////////////////////
    // Submit a SetRegularKey transaction with the new wallet's address so that we
    // can sign future transactions with the new wallet's keypair
    FeeResult feeResult = xrplClient.fee();
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(wallet.address())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .regularKey(newWallet.address())
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<SetRegularKey> signedSetRegularKey = signatureService.sign(
      wallet.privateKey(), setRegularKey
    );
    SubmitResult<SetRegularKey> setResult = xrplClient.submit(signedSetRegularKey);
    assertThat(setResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SetRegularKey transaction successful. https://testnet.xrpl.org/transactions/{}",
      setResult.transactionResult().hash()
    );

    //////////////////////////
    // Verify that the SetRegularKey transaction worked by submitting empty
    // AccountSet transactions, signed with the new wallet key pair, until
    // we get a successful response.
    scanForResult(
      () -> {
        AccountSet accountSet = AccountSet.builder()
          .account(wallet.address())
          .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
          .sequence(accountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
          .signingPublicKey(newWallet.publicKey().base16Value())
          .build();
        SingleSingedTransaction<AccountSet> signedAccountSet = signatureService.sign(
          newWallet.privateKey(), accountSet
        );
        try {
          return xrplClient.submit(signedAccountSet);
        } catch (JsonRpcClientErrorException | JsonProcessingException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    );

  }

  @Test
  void removeRegularKeyFromAccount() throws JsonRpcClientErrorException, JsonProcessingException {
    //////////////////////////
    // Create a random account
    Wallet wallet = createRandomAccountEd25519();

    //////////////////////////
    // Wait for the account to show up on ledger
    AccountInfoResult accountInfo = scanForResult(() -> getValidatedAccountInfo(wallet.address()));

    //////////////////////////
    // Generate a new wallet locally
    Wallet newWallet = walletFactory.randomWallet().wallet();

    //////////////////////////
    // Submit a SetRegularKey transaction with the new wallet's address so that we
    // can sign future transactions with the new wallet's keypair
    FeeResult feeResult = xrplClient.fee();
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(wallet.address())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .regularKey(newWallet.address())
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<SetRegularKey> signedSetRegularKey = signatureService.sign(
      wallet.privateKey(), setRegularKey
    );
    SubmitResult<SetRegularKey> setResult = xrplClient.submit(signedSetRegularKey);
    assertThat(setResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SetRegularKey transaction successful. https://testnet.xrpl.org/transactions/{}",
      setResult.transactionResult().hash()
    );

    //////////////////////////
    // Verify that the SetRegularKey transaction worked by submitting empty
    // AccountSet transactions, signed with the new wallet key pair, until
    // we get a successful response.
    scanForResult(
      () -> {
        AccountSet accountSet = AccountSet.builder()
          .account(wallet.address())
          .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
          .sequence(accountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
          .signingPublicKey(newWallet.publicKey().base16Value())
          .build();

        SingleSingedTransaction<AccountSet> signedAccountSet = signatureService.sign(
          newWallet.privateKey(), accountSet
        );
        try {
          return xrplClient.submit(signedAccountSet);
        } catch (JsonRpcClientErrorException | JsonProcessingException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    );

    SetRegularKey removeRegularKey = SetRegularKey.builder()
      .account(wallet.address())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();
    SingleSingedTransaction<SetRegularKey> signedRemoveRegularKey = signatureService.sign(
      wallet.privateKey(), removeRegularKey
    );
    SubmitResult<SetRegularKey> removeResult = xrplClient.submit(signedRemoveRegularKey);
    assertThat(removeResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "SetRegularKey transaction successful. https://testnet.xrpl.org/transactions/{}",
      removeResult.transactionResult().hash()
    );

    scanForResult(
      () -> getValidatedAccountInfo(wallet.address()),
      infoResult -> !infoResult.accountData().regularKey().isPresent()
    );
  }
}
