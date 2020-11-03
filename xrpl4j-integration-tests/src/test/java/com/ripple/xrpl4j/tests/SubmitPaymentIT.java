package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.SeedWalletGenerationResult;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrplj4.client.faucet.FaucetAccountResponse;
import com.ripple.xrplj4.client.faucet.FundAccountRequest;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import org.junit.jupiter.api.Test;

public class SubmitPaymentIT extends AbstractIT {

  @Test
  public void sendPayment() throws RippledClientErrorException {
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    logger.info("Generated source testnet wallet with address " + seedResult.wallet().xAddress());

    SeedWalletGenerationResult destinationResult = walletFactory.randomWallet(true);
    logger.info("Generated destination testnet wallet with address " + seedResult.wallet().xAddress());

    FaucetAccountResponse fundResponse =
        faucetClient.fundAccount(FundAccountRequest.of(seedResult.wallet().classicAddress().value()));

    logger.info("Source account has been funded");

    assertThat(fundResponse.amount()).isGreaterThan(0);

    logger.info("Destination account has been funded");

    FaucetAccountResponse fundDestinationResponse =
        faucetClient.fundAccount(FundAccountRequest.of(destinationResult.wallet().classicAddress().value()));

    assertThat(fundDestinationResponse.amount()).isGreaterThan(0);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = xrplClient.accountInfo(seedResult.wallet().classicAddress());
    Payment payment = Payment.builder()
      .account(seedResult.wallet().classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationResult.wallet().classicAddress())
      .amount(XrpCurrencyAmount.of("12345"))
      .signingPublicKey(seedResult.wallet().publicKey())
      .build();

    SubmissionResult<Payment> result = xrplClient.submit(seedResult.wallet(), payment, Payment.class);
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" + result.transaction().hash().orElse("n/a"));
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() throws RippledClientErrorException {
    Wallet senderWallet = walletFactory.fromSeed("sp5fghtJtpUorTwvof1NpDXAzNwf5", true);
    logger.info("Generated source testnet wallet with address " + senderWallet.xAddress());

    SeedWalletGenerationResult destinationResult = walletFactory.randomWallet(true);
    logger.info("Generated destination testnet wallet with address " + destinationResult.wallet().xAddress());

    FaucetAccountResponse fundResponse =
      faucetClient.fundAccount(FundAccountRequest.of(senderWallet.classicAddress().value()));

    logger.info("Source account has been funded");

    assertThat(fundResponse.amount()).isGreaterThan(0);

    logger.info("Destination account has been funded");

    FaucetAccountResponse fundDestinationResponse =
      faucetClient.fundAccount(FundAccountRequest.of(destinationResult.wallet().classicAddress().value()));

    assertThat(fundDestinationResponse.amount()).isGreaterThan(0);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = xrplClient.accountInfo(senderWallet.classicAddress());
    Payment payment = Payment.builder()
      .account(senderWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationResult.wallet().classicAddress())
      .amount(XrpCurrencyAmount.of("12345"))
      .signingPublicKey(senderWallet.publicKey())
      .build();

    SubmissionResult<Payment> result = xrplClient.submit(senderWallet, payment, Payment.class);
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" + result.transaction().hash().orElse("n/a"));
  }

}
