package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;
import com.ripple.xrplj4.client.faucet.FaucetAccountResponse;
import com.ripple.xrplj4.client.faucet.FaucetClient;
import com.ripple.xrplj4.client.payment.SimplePaymentClient;
import com.ripple.xrplj4.client.payment.SimplePaymentRequest;
import com.ripple.xrplj4.client.payment.SimplePaymentResponse;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

public class SubmitPaymentIT {

  public static final FaucetClient faucetClient =
      FaucetClient.construct(HttpUrl.parse("https://faucet.altnet.rippletest.net"));

  public static final SimplePaymentClient paymentClient = new SimplePaymentClient.Impl();

  @Test
  public void sendPayment() {
    FaucetAccountResponse senderAccount = faucetClient.generateFaucetAccount();
    FaucetAccountResponse receiverAccount = faucetClient.generateFaucetAccount();

    SimplePaymentRequest request = SimplePaymentRequest.builder()
        .amount(XrpCurrencyAmount.of("12345"))
        .sourceAddress(Address.of(senderAccount.account().classicAddress()))
        .destinationAddress(Address.of(receiverAccount.account().classicAddress()))
        .build();

    SimplePaymentResponse response = paymentClient.submit(request);
    System.out.println(response);
    assertThat(response.engineResult()).isNotEmpty();
  }

}
