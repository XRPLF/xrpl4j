package com.ripple.xrplj4.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.keypairs.DefaultKeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.Transaction;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrplj4.client.model.accounts.AccountInfoRequestParams;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrplj4.client.model.transactions.SubmissionRequestParams;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.rippled.JsonRpcRequest;
import com.ripple.xrplj4.client.rippled.RippledClient;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import com.ripple.xrplj4.client.rippled.XrplMethods;
import okhttp3.HttpUrl;

public class XrplClient {

  private final ObjectMapper objectMapper;
  private final XrplBinaryCodec binaryCodec;
  private final RippledClient rippledClient;
  private final KeyPairService keyPairService;

  public XrplClient() {
    this.objectMapper = ObjectMapperFactory.create();
    this.binaryCodec = new XrplBinaryCodec();
    this.rippledClient = RippledClient.construct(HttpUrl.parse("https://s.altnet.rippletest.net:51234"));
    this.keyPairService = DefaultKeyPairService.getInstance();
  }

  public <TxnType extends Transaction<? extends Flags.TransactionFlags>> SubmissionResult<TxnType> submit(
    Wallet wallet,
    TxnType unsignedTransaction,
    Class<TxnType> transactionType
  ) throws JsonProcessingException, RippledClientErrorException {
    String signedTransaction = serializeAndSignTransaction(wallet, unsignedTransaction);
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.SUBMIT)
      .addParams(SubmissionRequestParams.of(signedTransaction))
      .build();
    JavaType resultType = objectMapper.getTypeFactory().constructParametricType(SubmissionResult.class, transactionType);
    return rippledClient.send(request, resultType);
  }

  public FeeResult fee() throws RippledClientErrorException, JsonProcessingException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.FEE)
      .build();

    return rippledClient.send(request, FeeResult.class);
  }

  public AccountInfoResult accountInfo(Address classicAddress, boolean strict, boolean queue, boolean signerLists) throws RippledClientErrorException, JsonProcessingException {
    return accountInfo(classicAddress, null, strict, queue, signerLists);
  }

  public AccountInfoResult accountInfo(Address classicAddress, String ledgerHash, boolean strict, boolean queue, boolean signerLists) throws RippledClientErrorException, JsonProcessingException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_INFO)
      .addParams(AccountInfoRequestParams.builder()
        .account(classicAddress)
        .ledgerHash(ledgerHash)
        .strict(strict)
        .queue(queue)
        .signerLists(signerLists)
        .build()
      )
      .build();

    return rippledClient.send(request, AccountInfoResult.class);
  }

  private String serializeAndSignTransaction(
    Wallet wallet,
    Transaction<? extends Flags.TransactionFlags> unsignedTransaction
  ) throws JsonProcessingException {
    String unsignedJson = objectMapper.writeValueAsString(unsignedTransaction);
    String unsignedBinaryHex = binaryCodec.encodeForSigning(unsignedJson);
    String signature = keyPairService.sign(unsignedBinaryHex, wallet.privateKey()
      .orElseThrow(() -> new RuntimeException("Wallet must provide a private key to sign the transaction.")));

    Transaction signedTransaction = addSigningFields(unsignedTransaction, signature, wallet.publicKey());

    String signedJson = objectMapper.writeValueAsString(signedTransaction);
    return binaryCodec.encode(signedJson);
  }

  private Transaction addSigningFields(Transaction transaction, String signature, String publicKey) {
    if (Payment.class.isAssignableFrom(transaction.getClass())) {
      return Payment.builder().from((Payment) transaction)
        .transactionSignature(signature)
        .signingPublicKey(publicKey)
        .build();
    }

    throw new IllegalArgumentException("Signing fields could not be added to the transaction."); // Never happens

  }

}
