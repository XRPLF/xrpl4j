package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Ed25519WalletFactory;
import com.ripple.xrpl4j.wallet.SeedWalletGenerationResult;
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

  public static final WalletFactory walletFactory = Ed25519WalletFactory.getInstance();

  @Test
  public void sendPayment() {

    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);

    SeedWalletGenerationResult destinationResult = walletFactory.randomWallet(true);

    FaucetAccountResponse fundResponse =
        faucetClient.fundAccount(FundAccountRequest.of(seedResult.wallet().classicAddress()));

    assertThat(fundResponse.amount()).isGreaterThan(0);

    FaucetAccountResponse fundDestinationResponse =
        faucetClient.fundAccount(FundAccountRequest.of(destinationResult.wallet().classicAddress()));

    assertThat(fundDestinationResponse.amount()).isGreaterThan(0);

    SimplePaymentRequest request = SimplePaymentRequest.builder()
        .amount(XrpCurrencyAmount.of("12345"))
        .wallet(seedResult.wallet())
        .destinationAddress(Address.of(destinationResult.wallet().classicAddress()))
        .build();

    SimplePaymentResponse response = paymentClient.submit(request);
    System.out.println(response);
    assertThat(response.engineResult()).isNotEmpty();
  }

}
