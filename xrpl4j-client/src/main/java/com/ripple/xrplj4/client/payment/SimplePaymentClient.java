package com.ripple.xrplj4.client.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.keypairs.Ed25519KeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.Payment;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrplj4.client.model.fees.FeeInfoResponse;
import com.ripple.xrplj4.client.model.transactions.SubmitTransactionResponse;
import com.ripple.xrplj4.client.rippled.ImmutableJsonRpcRequest;
import com.ripple.xrplj4.client.rippled.JsonRpcRequest;
import com.ripple.xrplj4.client.rippled.RippledClient;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import com.ripple.xrplj4.client.rippled.TransactionBlobWrapper;
import com.ripple.xrplj4.client.rippled.XrplMethods;
import okhttp3.HttpUrl;

public interface SimplePaymentClient {

  SimplePaymentResponse submit(SimplePaymentRequest request);


  class Impl implements SimplePaymentClient {

    private ObjectMapper objectMapper = ObjectMapperFactory.create();

    private XrplBinaryCodec binaryCodec = new XrplBinaryCodec();

    private RippledClient rippledClient =
        RippledClient.construct(HttpUrl.parse("https://s.altnet.rippletest.net:51234"));

    private KeyPairService keyPairService = Ed25519KeyPairService.getInstance();

    @Override
    public SimplePaymentResponse submit(SimplePaymentRequest request) {
      try {
        String trx = paymentRequest(request.wallet(),
            request.destinationAddress(),
            request.amount());

        JsonRpcRequest submitRequest = JsonRpcRequest.builder()
            .method(XrplMethods.SUBMIT)
            .addParams(TransactionBlobWrapper.of(trx))
            .build();

        SubmitTransactionResponse response = rippledClient.sendRequest(submitRequest, SubmitTransactionResponse.class);
        if (response.accepted()) {
          return SimplePaymentResponse.builder()
              .engineResult(response.engineResult())
              .transactionHash(response.txJson().hash().get())
              .build();
        }
        return SimplePaymentResponse.builder().engineResult(response.engineResult()).build();
      } catch (JsonProcessingException e) {
        throw new IllegalStateException("Houston, we have a bug", e);
      } catch (RippledClientErrorException e) {
        return SimplePaymentResponse.builder().error(e.getMessage()).build();
      }
    }

    private FeeInfoResponse getFeeInfo() throws RippledClientErrorException {
      ImmutableJsonRpcRequest request = JsonRpcRequest.builder()
          .method(XrplMethods.FEE)
          .build();
      return rippledClient.sendRequest(request, FeeInfoResponse.class);
    }


    private String paymentRequest(Wallet wallet, Address destination, XrpCurrencyAmount amount)
        throws JsonProcessingException, RippledClientErrorException {
      FeeInfoResponse feeInfo = getFeeInfo();

      Payment unsignedPayment = Payment.builder()
          .tfFullyCanonicalSig(true)
          .account(Address.of(wallet.classicAddress()))
          .sequence(feeInfo.currentLedgerIndex())
          .destination(destination)
          .amount(amount)
          .fee(feeInfo.drops().minimumFee())
          .signingPublicKey(wallet.publicKey())
          .build();

      String unsignedPaymentJson = objectMapper.writeValueAsString(unsignedPayment);

      String unsignedBinaryHex = binaryCodec.encodeForSigning(unsignedPaymentJson);

      String signature = keyPairService.sign(unsignedBinaryHex, wallet.privateKey().get());

      Payment signed = Payment.builder().from(unsignedPayment)
          .transactionSignature(signature)
          .build();

      String signedPaymentJson = objectMapper.writeValueAsString(signed);

      return binaryCodec.encode(signedPaymentJson);
    }


  }


}
