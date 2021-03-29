package org.xrpl.xrpl4j.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import okhttp3.HttpUrl;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsResult;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyRequestParams;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.server.ServerInfo;
import org.xrpl.xrpl4j.model.client.server.ServerInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SignedTransaction;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

/**
 * A client which wraps a rippled network client and is responsible for higher order functionality such as signing and
 * serializing transactions, as well as hiding certain implementation details from the public API such as JSON RPC
 * request object creation.
 * <p>
 * Note: This client is currently marked as {@link Beta}, and should be used as a reference implementation ONLY.
 */
@Beta
public class XrplClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(XrplClient.class);

  private final ObjectMapper objectMapper;
  private final XrplBinaryCodec binaryCodec;
  private final JsonRpcClient jsonRpcClient;
  private final KeyPairService keyPairService;

  /**
   * Public constructor.
   *
   * @param rippledUrl The {@link HttpUrl} of the rippled node to connect to.
   */
  public XrplClient(HttpUrl rippledUrl) {
    this.objectMapper = ObjectMapperFactory.create();
    this.binaryCodec = new XrplBinaryCodec();
    this.jsonRpcClient = JsonRpcClient.construct(rippledUrl);
    this.keyPairService = DefaultKeyPairService.getInstance();
  }

  /**
   * Submit a {@link Transaction} to the XRP Ledger.
   *
   * @param <T>                 The type of {@link Transaction} that is being submitted.
   * @param wallet              The {@link Wallet} of the XRPL account submitting {@code unsignedTransaction}.
   * @param unsignedTransaction An unsigned {@link Transaction} to submit. {@link Transaction#transactionSignature()}
   *                            must not be provided, and {@link Transaction#signingPublicKey()} must be provided.
   *
   * @return The {@link SubmitResult} resulting from the submission request.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @see "https://xrpl.org/submit.html"
   */
  public <T extends Transaction> SubmitResult<T> submit(
    Wallet wallet,
    T unsignedTransaction
  ) throws JsonRpcClientErrorException {
    Preconditions.checkArgument(
      unsignedTransaction.signingPublicKey().isPresent(),
      "Transaction.signingPublicKey() must be set."
    );

    SignedTransaction<T> signedTransaction = signTransaction(wallet, unsignedTransaction);
    return submit(signedTransaction);
  }

  /**
   * Submit a {@link SignedTransaction} to the ledger.
   *
   * @param signedTransaction A {@link SignedTransaction} to submit.
   * @param <T>               The type of {@link Transaction} contained in the {@link SignedTransaction} object.
   *
   * @return The {@link SubmitResult} resulting from the submission request.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public <T extends Transaction> SubmitResult<T> submit(
    SignedTransaction<T> signedTransaction
  ) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.SUBMIT)
      .addParams(SubmitRequestParams.of(signedTransaction.signedTransactionBlob()))
      .build();
    JavaType resultType = objectMapper.getTypeFactory()
      .constructParametricType(SubmitResult.class, signedTransaction.signedTransaction().getClass());
    return jsonRpcClient.send(request, resultType);
  }

  /**
   * Submit a {@link SignedTransaction} to the XRP Ledger.
   *
   * @param <T>               The type of signed {@link Transaction} that is being submitted.
   * @param signedTransaction A {@link org.xrpl.xrpl4j.crypto.signing.SignedTransaction} to submit.
   *
   * @return The {@link SubmitResult} resulting from the submission request.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @throws JsonProcessingException     if any JSON is invalid.
   * @see "https://xrpl.org/submit.html"
   */
  public <T extends Transaction> SubmitResult<T> submit(
    final org.xrpl.xrpl4j.crypto.signing.SignedTransaction signedTransaction
  ) throws JsonRpcClientErrorException, JsonProcessingException {

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("About to submit signedTransaction: {}", signedTransaction);
    }

    String signedJson = objectMapper.writeValueAsString(signedTransaction.signedTransaction());
    String signedBlob = binaryCodec.encode(signedJson); // <-- txBlob must be binary-encoded.
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.SUBMIT)
      .addParams(SubmitRequestParams.of(signedBlob))
      .build();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("About to submit JsonRpcRequest: {}", request);
    }

    JavaType resultType = objectMapper.getTypeFactory()
      .constructParametricType(SubmitResult.class, signedTransaction.unsignedTransaction().getClass());
    return jsonRpcClient.send(request, resultType);
  }

  /**
   * Submit a multisigned {@link Transaction} to the ledger.
   *
   * @param transaction A multisigned {@link Transaction}.
   * @param <T>         A type parameter for the type of {@link Transaction} being submitted.
   *
   * @return A {@link SubmitMultiSignedResult} of type {@link T}.
   *
   * @throws JsonRpcClientErrorException if {@code jsonRpcClient} throws an error.
   */
  public <T extends Transaction> SubmitMultiSignedResult<T> submitMultisigned(
    T transaction
  ) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.SUBMIT_MULTISIGNED)
      .addParams(SubmitMultiSignedRequestParams.of(transaction))
      .build();
    JavaType resultType = objectMapper.getTypeFactory().constructParametricType(
      SubmitMultiSignedResult.class, transaction.getClass()
    );
    return jsonRpcClient.send(request, resultType);
  }

  /**
   * Get the current state of the open-ledger requirements for transaction costs.
   *
   * @return A {@link FeeResult} containing information about current transaction costs.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @see "https://xrpl.org/fee.html"
   */
  public FeeResult fee() throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.FEE)
      .build();

    return jsonRpcClient.send(request, FeeResult.class);
  }

  /**
   * Get the "server_info" for the rippled node.
   *
   * @return A {@link ServerInfo} containing information about the server.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @see "https://xrpl.org/server_info.html"
   */
  public ServerInfo serverInfo() throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.SERVER_INFO)
      .build();

    return jsonRpcClient.send(request, ServerInfoResult.class).info();
  }

  /**
   * Get the {@link AccountChannelsResult} for the account specified in {@code params} by making an account_channels
   * method call.
   *
   * @param params The {@link AccountChannelsRequestParams} to send in the request.
   *
   * @return The {@link AccountChannelsResult} returned by the account_channels method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public AccountChannelsResult accountChannels(AccountChannelsRequestParams params) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_CHANNELS)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, AccountChannelsResult.class);
  }

  /**
   * Get the {@link AccountInfoResult} for the account specified in {@code params} by making an account_info method
   * call.
   *
   * @param params The {@link AccountInfoRequestParams} to send in the request.
   *
   * @return The {@link AccountInfoResult} returned by the account_info method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public AccountInfoResult accountInfo(AccountInfoRequestParams params) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_INFO)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, AccountInfoResult.class);
  }

  /**
   * Get the {@link AccountObjectsResult} for the account specified in {@code params} by making an account_objects
   * method call.
   *
   * @param params The {@link AccountObjectsRequestParams} to send in the request.
   *
   * @return The {@link AccountObjectsResult} returned by the account_objects method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public AccountObjectsResult accountObjects(AccountObjectsRequestParams params) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_OBJECTS)
      .addParams(params)
      .build();
    return jsonRpcClient.send(request, AccountObjectsResult.class);
  }

  /**
   * Get the {@link AccountTransactionsResult} for the specified {@code address} by making an account_tx method call.
   *
   * @param address The {@link Address} of the account to request.
   *
   * @return The {@link AccountTransactionsResult} returned by the account_tx method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public AccountTransactionsResult accountTransactions(Address address) throws JsonRpcClientErrorException {
    return accountTransactions(AccountTransactionsRequestParams.builder()
      .account(address)
      .build());
  }

  /**
   * Get the {@link AccountTransactionsResult} for the account specified in {@code params} by making an account_tx
   * method call.
   *
   * @param params The {@link AccountTransactionsRequestParams} to send in the request.
   *
   * @return The {@link AccountTransactionsResult} returned by the account_tx method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public AccountTransactionsResult accountTransactions(AccountTransactionsRequestParams params)
    throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_TX)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, AccountTransactionsResult.class);
  }

  /**
   * Get a transaction from the ledger by sending a tx method request.
   *
   * @param params          The {@link TransactionRequestParams} to send in the request.
   * @param transactionType The {@link Transaction} type of the transaction with the hash {@code params.transaction()}.
   * @param <T>             Type parameter for the type of {@link Transaction} that the {@link TransactionResult} will
   *
   * @return A {@link TransactionResult} containing the requested transaction and other metadata.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public <T extends Transaction> TransactionResult<T> transaction(
    TransactionRequestParams params,
    Class<T> transactionType
  ) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.TX)
      .addParams(params)
      .build();

    JavaType resultType = objectMapper.getTypeFactory()
      .constructParametricType(TransactionResult.class, transactionType);
    return jsonRpcClient.send(request, resultType);
  }

  /**
   * Get the contents of a ledger by sending a ledger method request.
   *
   * @param params The {@link LedgerRequestParams} to send in the request.
   *
   * @return A {@link LedgerResult} containing the ledger details.
   *
   * @throws JsonRpcClientErrorException if {@code jsonRpcClient} throws an error.
   */
  public LedgerResult ledger(LedgerRequestParams params) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.LEDGER)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, LedgerResult.class);
  }

  /**
   * Try to find a payment path for a rippling payment by sending a ripple_path_find method request.
   *
   * @param params The {@link RipplePathFindRequestParams} to send in the request.
   *
   * @return A {@link RipplePathFindResult} containing possible paths.
   *
   * @throws JsonRpcClientErrorException if {@code jsonRpcClient} throws an error.
   */
  public RipplePathFindResult ripplePathFind(RipplePathFindRequestParams params) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.RIPPLE_PATH_FIND)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, RipplePathFindResult.class);
  }

  /**
   * Get the trust lines for a given account by sending an account_lines method request.
   *
   * @param params The {@link AccountLinesRequestParams} to send in the request.
   *
   * @return The {@link AccountLinesResult} containing the requested trust lines.
   *
   * @throws JsonRpcClientErrorException if {@code jsonRpcClient} throws an error.
   */
  public AccountLinesResult accountLines(AccountLinesRequestParams params) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_LINES)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, AccountLinesResult.class);
  }

  /**
   * Verify a payment channel claim signature by making a "channel_verify" rippled API method call.
   *
   * @param channelId A {@link Hash256} containing the Channel ID.
   * @param amount    An {@link XrpCurrencyAmount} representing the amount of the claim.
   * @param signature The signature of the {@link PaymentChannelClaim} transaction.
   * @param publicKey A {@link String} containing the public key associated with the key used to generate the
   *                  signature.
   *
   * @return The result of the request, as a {@link ChannelVerifyResult}.
   *
   * @throws JsonRpcClientErrorException if {@code jsonRpcClient} throws an error.
   */
  public ChannelVerifyResult channelVerify(
    Hash256 channelId,
    XrpCurrencyAmount amount,
    String signature,
    String publicKey
  ) throws JsonRpcClientErrorException {
    ChannelVerifyRequestParams params = ChannelVerifyRequestParams.builder()
      .channelId(channelId)
      .amount(amount)
      .signature(signature)
      .publicKey(publicKey)
      .build();

    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.CHANNEL_VERIFY)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, ChannelVerifyResult.class);
  }

  public <T extends Transaction> SignedTransaction<T> signTransaction(
    Wallet wallet, T unsignedTransaction
  ) {
    try {
      String unsignedJson = objectMapper.writeValueAsString(unsignedTransaction);

      String unsignedBinaryHex = binaryCodec.encodeForSigning(unsignedJson);
      String signature = keyPairService.sign(unsignedBinaryHex, wallet.privateKey()
        .orElseThrow(() -> new RuntimeException("Wallet must provide a private key to sign the transaction.")));

      T signedTransaction = (T) addSignature(unsignedTransaction, signature);

      String signedJson = objectMapper.writeValueAsString(signedTransaction);
      String signedBinary = binaryCodec.encode(signedJson);
      return SignedTransaction.<T>builder()
        .signedTransaction(signedTransaction)
        .signedTransactionBlob(signedBinary)
        .build();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Add {@link Transaction#transactionSignature()} to the given unsignedTransaction. Because {@link Transaction} is not
   * a {@link Value.Immutable}, it does not have a generated builder like the subclasses.  Thus, this method needs to
   * rebuild transactions based on their runtime type.
   *
   * @param unsignedTransaction An unsigned {@link Transaction} to add a signature to. {@link
   *                            Transaction#transactionSignature()} must not be provided, and {@link
   *                            Transaction#signingPublicKey()} must be provided. a {@link Value.Immutable}, it does not
   *                            have a generated builder like the subclasses.  Thus, this method needs to rebuild
   *                            transactions based on their runtime type.
   * @param signature           The hex encoded {@link String} containing the transaction signature.
   *
   * @return A copy of {@code unsignedTransaction} with the {@link Transaction#transactionSignature()} field added.
   *
   * @deprecated This method will go away in a future version and be replaced with the implementation in SignatureUtils
   *   in the xrpl4j-crypto module.
   */
  @Deprecated
  private Transaction addSignature(
    Transaction unsignedTransaction,
    String signature
  ) {
    if (Payment.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return Payment.builder().from((Payment) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (AccountSet.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return AccountSet.builder().from((AccountSet) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (AccountDelete.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return AccountDelete.builder().from((AccountDelete) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (CheckCancel.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return CheckCancel.builder().from((CheckCancel) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (CheckCash.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return CheckCash.builder().from((CheckCash) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (CheckCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return CheckCreate.builder().from((CheckCreate) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (DepositPreAuth.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return DepositPreAuth.builder().from((DepositPreAuth) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (EscrowCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return EscrowCreate.builder().from((EscrowCreate) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (EscrowCancel.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return EscrowCancel.builder().from((EscrowCancel) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (EscrowFinish.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return EscrowFinish.builder().from((EscrowFinish) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (TrustSet.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return TrustSet.builder().from((TrustSet) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (OfferCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return OfferCreate.builder().from((OfferCreate) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (OfferCancel.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return OfferCancel.builder().from((OfferCancel) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (PaymentChannelCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return PaymentChannelCreate.builder().from((PaymentChannelCreate) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (PaymentChannelClaim.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return PaymentChannelClaim.builder().from((PaymentChannelClaim) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (PaymentChannelFund.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return PaymentChannelFund.builder().from((PaymentChannelFund) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (SetRegularKey.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return SetRegularKey.builder().from((SetRegularKey) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    } else if (SignerListSet.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return SignerListSet.builder().from((SignerListSet) unsignedTransaction)
        .transactionSignature(signature)
        .build();
    }

    // Never happens
    throw new IllegalArgumentException("Signing fields could not be added to the unsignedTransaction.");

  }

  public JsonRpcClient getJsonRpcClient() {
    return jsonRpcClient;
  }
}
