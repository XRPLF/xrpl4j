package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.model.transactions.AccountDelete;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrplj4.client.faucet.FaucetAccountResponse;
import com.ripple.xrplj4.client.faucet.FundAccountRequest;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class AccountDeleteIT extends AbstractIT {



  @Test
  public void deleteAccount() throws RippledClientErrorException {
    ///////////////////////
    // Create the account
    final Wallet senderWallet = walletFactory.randomWallet(true).wallet();
    logger.info("Generated source testnet wallet with address {}", senderWallet.xAddress());

    ///////////////////////
    // Fund the account
    FaucetAccountResponse senderFundResponse = faucetClient.fundAccount(FundAccountRequest.of(senderWallet.classicAddress().value()));
    logger.info("Source account has been funded: {}", senderFundResponse);
    assertThat(senderFundResponse.amount()).isGreaterThan(0);

    ///////////////////////
    // Create the destination account
    final Wallet destinationWallet = walletFactory.randomWallet(true).wallet();
    logger.info("Generated destination testnet wallet with address {}", destinationWallet.xAddress());

    ///////////////////////
    // Fund the destination account
    FaucetAccountResponse destinationFundResponse = faucetClient.fundAccount(FundAccountRequest.of(destinationWallet.classicAddress().value()));
    logger.info("Destination account has been funded: {}", destinationFundResponse);
    assertThat(destinationFundResponse.amount()).isGreaterThan(0);



  }

  private Optional<Boolean> validateAccountDeleted(Wallet senderWallet, Address destination) throws RippledClientErrorException {
    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = null;
    assertThat(accountInfo.status()).isEqualTo("success");

    AccountDelete accountDelete = AccountDelete.builder()
      .account(senderWallet.classicAddress())
      .fee(XrpCurrencyAmount.of("5")) // Special high fee for AccountDelete transactions
      .sequence(accountInfo.accountData().sequence())
      .destination(destination)
      .build();

    SubmissionResult<AccountDelete> result = xrplClient.submit(senderWallet, accountDelete, AccountDelete.class);

    return null;
  }
}
