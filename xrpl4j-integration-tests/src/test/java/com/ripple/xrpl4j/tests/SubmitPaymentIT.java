package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.DefaultWalletFactory;
import com.ripple.xrpl4j.wallet.SeedWalletGenerationResult;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrpl4j.wallet.WalletFactory;
import com.ripple.xrplj4.client.faucet.FaucetAccountResponse;
import com.ripple.xrplj4.client.faucet.FaucetClient;
import com.ripple.xrplj4.client.faucet.FundAccountRequest;
import com.ripple.xrplj4.client.payment.SimplePaymentClient;
import com.ripple.xrplj4.client.payment.SimplePaymentRequest;
import com.ripple.xrplj4.client.payment.SimplePaymentResponse;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

public class SubmitPaymentIT {

  public static final FaucetClient faucetClient =
      FaucetClient.construct(HttpUrl.parse("https://faucet.altnet.rippletest.net"));

  public static final SimplePaymentClient paymentClient = new SimplePaymentClient.Impl();

  public static final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

  @Test
  public void sendPayment() {
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    System.out.println("Generated source testnet wallet with address " + seedResult.wallet().xAddress());

    SeedWalletGenerationResult destinationResult = walletFactory.randomWallet(true);
    System.out.println("Generated destination testnet wallet with address " + seedResult.wallet().xAddress());

    FaucetAccountResponse fundResponse =
        faucetClient.fundAccount(FundAccountRequest.of(seedResult.wallet().classicAddress()));

    System.out.println("Source account has been funded");

    assertThat(fundResponse.amount()).isGreaterThan(0);

    System.out.println("Destination account has been funded");

    FaucetAccountResponse fundDestinationResponse =
        faucetClient.fundAccount(FundAccountRequest.of(destinationResult.wallet().classicAddress()));

    assertThat(fundDestinationResponse.amount()).isGreaterThan(0);

    SimplePaymentRequest request = SimplePaymentRequest.builder()
        .amount(XrpCurrencyAmount.of("12345"))
        .wallet(seedResult.wallet())
        .destinationAddress(Address.of(destinationResult.wallet().classicAddress()))
        .build();

    SimplePaymentResponse response = paymentClient.submit(request);
    System.out.println("Payment successful: https://testnet.xrpl.org/transactions/" + response.transactionHash().get());

    assertThat(response.engineResult()).isNotEmpty();
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() {
    Wallet senderWallet = walletFactory.fromSeed("sp5fghtJtpUorTwvof1NpDXAzNwf5", true);
    System.out.println("Generated source testnet wallet with address " + senderWallet.xAddress());

    SeedWalletGenerationResult destinationResult = walletFactory.randomWallet(true);
    System.out.println("Generated destination testnet wallet with address " + destinationResult.wallet().xAddress());

    FaucetAccountResponse fundResponse =
      faucetClient.fundAccount(FundAccountRequest.of(senderWallet.classicAddress()));

    System.out.println("Source account has been funded");

    assertThat(fundResponse.amount()).isGreaterThan(0);

    System.out.println("Destination account has been funded");

    FaucetAccountResponse fundDestinationResponse =
      faucetClient.fundAccount(FundAccountRequest.of(destinationResult.wallet().classicAddress()));

    assertThat(fundDestinationResponse.amount()).isGreaterThan(0);

    SimplePaymentRequest request = SimplePaymentRequest.builder()
      .amount(XrpCurrencyAmount.of("12345"))
      .wallet(senderWallet)
      .destinationAddress(Address.of(destinationResult.wallet().classicAddress()))
      .build();

    SimplePaymentResponse response = paymentClient.submit(request);
    System.out.println("Payment successful: https://testnet.xrpl.org/transactions/" + response.transactionHash().get());

    assertThat(response.engineResult()).isNotEmpty();
  }

}
