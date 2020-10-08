package com.ripple.xrplj4.client.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.Payment;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;
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

    @Override
    public SimplePaymentResponse submit(SimplePaymentRequest request) {
      try {
        String trx = paymentRequest(request.sourceAddress(), request.destinationAddress(), request.amount());
        JsonRpcRequest submitRequest = JsonRpcRequest.builder()
            .method(XrplMethods.SUBMIT)
            .addParams(TransactionBlobWrapper.of(trx))
            .build();

        SubmitTransactionResponse response = rippledClient.sendRequest(submitRequest, SubmitTransactionResponse.class);
        if (response.accepted()) {
          return SimplePaymentResponse.builder()
              .engineResult(response.engineResult())
              .transactionHash(response.txJson().hash())
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


    private String paymentRequest(Address source, Address destination, XrpCurrencyAmount amount)
        throws JsonProcessingException, RippledClientErrorException {
      FeeInfoResponse feeInfo = getFeeInfo();

      // FIXME
      String signingPublicKey = "03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB";
      String transactionSignature = "3045022100A7CCD11455E47547FF617D5BFC15D120D9053DFD0536B044F10CA3631CD609E502203B61DEE4AC027C5743A1B56AF568D1E2B8E79BB9E9E14744AC87F38375C3C2F1";
      Payment payment = Payment.builder()
          .tfFullyCanonicalSig(true)
          .account(source)
          .sequence(feeInfo.currentLedgerIndex())
          .destination(destination)
          .amount(amount)
          .fee(feeInfo.drops().minimumFee())
          .signingPublicKey(signingPublicKey)
          .transactionSignature(transactionSignature)
          .build();

      String paymentJson = objectMapper.writeValueAsString(payment);

      return binaryCodec.encode(paymentJson);
    }


  }


}
