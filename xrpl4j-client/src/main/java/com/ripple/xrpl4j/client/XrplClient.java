package com.ripple.xrpl4j.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.ripple.xrpl4j.client.model.accounts.AccountInfoRequestParams;
import com.ripple.xrpl4j.client.model.accounts.AccountInfoResult;
import com.ripple.xrpl4j.client.model.accounts.AccountLinesRequestParams;
import com.ripple.xrpl4j.client.model.accounts.AccountLinesResult;
import com.ripple.xrpl4j.client.model.accounts.AccountObjectsRequestParams;
import com.ripple.xrpl4j.client.model.accounts.AccountObjectsResult;
import com.ripple.xrpl4j.client.model.fees.FeeResult;
import com.ripple.xrpl4j.client.model.ledger.LedgerRequestParams;
import com.ripple.xrpl4j.client.model.ledger.LedgerResult;
import com.ripple.xrpl4j.client.model.path.RipplePathFindRequestParams;
import com.ripple.xrpl4j.client.model.path.RipplePathFindResult;
import com.ripple.xrpl4j.client.model.transactions.SubmissionRequestParams;
import com.ripple.xrpl4j.client.model.transactions.SubmissionResult;
import com.ripple.xrpl4j.client.model.transactions.TransactionRequestParams;
import com.ripple.xrpl4j.client.model.transactions.TransactionResult;
import com.ripple.xrpl4j.client.rippled.JsonRpcClient;
import com.ripple.xrpl4j.client.rippled.JsonRpcClientErrorException;
import com.ripple.xrpl4j.client.rippled.JsonRpcRequest;
import com.ripple.xrpl4j.client.rippled.XrplMethods;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.keypairs.DefaultKeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.AccountDelete;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.CheckCancel;
import com.ripple.xrpl4j.model.transactions.CheckCash;
import com.ripple.xrpl4j.model.transactions.CheckCreate;
import com.ripple.xrpl4j.model.transactions.DepositPreAuth;
import com.ripple.xrpl4j.model.transactions.EscrowCancel;
import com.ripple.xrpl4j.model.transactions.EscrowCreate;
import com.ripple.xrpl4j.model.transactions.EscrowFinish;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.Transaction;
import com.ripple.xrpl4j.model.transactions.TrustSet;
import com.ripple.xrpl4j.wallet.Wallet;
import okhttp3.HttpUrl;
import org.immutables.value.Value;

/**
 * A client which wraps a rippled network client and is responsible for higher order functionality such as signing
 * and serializing transactions, as well as hiding certain implementation details from the public API such as JSON
 * RPC request object creation.
 */
public class XrplClient {

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
   * @see "https://xrpl.org/submit.html"
   * @param <TxnType> The type of {@link Transaction} that is being submitted.
   * @param wallet The {@link Wallet} of the XRPL account submitting {@code unsignedTransaction}.
   * @param unsignedTransaction An unsigned {@link Transaction} to submit. {@link Transaction#transactionSignature()}
   *                            must not be provided, and {@link Transaction#signingPublicKey()} must be provided.
   * @return The {@link SubmissionResult} resulting from the submission request.
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public <TxnType extends Transaction<? extends Flags.TransactionFlags>> SubmissionResult<TxnType> submit(
    Wallet wallet,
    TxnType unsignedTransaction
  ) throws JsonRpcClientErrorException {
    try {
      Preconditions.checkArgument(
        unsignedTransaction.signingPublicKey().isPresent(),
        "Transaction.signingPublicKey() must be set."
      );

      String signedTransaction = serializeAndSignTransaction(wallet, unsignedTransaction);
      JsonRpcRequest request = JsonRpcRequest.builder()
        .method(XrplMethods.SUBMIT)
        .addParams(SubmissionRequestParams.of(signedTransaction))
        .build();
      JavaType resultType = objectMapper.getTypeFactory().constructParametricType(SubmissionResult.class, unsignedTransaction.getClass());
      return jsonRpcClient.send(request, resultType);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Get the current state of the open-ledger requirements for transaction costs.
   *
   * @see "https://xrpl.org/fee.html"
   * @return A {@link FeeResult} containing information about current transaction costs.
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public FeeResult fee() throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.FEE)
      .build();

    return jsonRpcClient.send(request, FeeResult.class);
  }

  /**
   * Get the {@link AccountInfoResult} for the account specified in {@code params} by making an account_info method
   * call.
   *
   * @param params The {@link AccountInfoRequestParams} to send in the request.
   * @return The {@link AccountInfoResult} returned by the account_info method call.
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
   * @return The {@link AccountObjectsResult} returned by the account_objects method call.
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
   * Get a transaction from the ledger by sending a tx method request.
   *
   * @param params The {@link TransactionRequestParams} to send in the request.
   * @param transactionType The {@link Transaction} type of the transaction with the hash {@code params.transaction()}.
   * @param <TxnType> Type parameter for the type of {@link Transaction} that the {@link TransactionResult} will
   *                contain.
   * @return A {@link TransactionResult} containing the requested transaction and other metadata.
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public <TxnType extends Transaction<? extends Flags>> TransactionResult<TxnType> transaction(
    TransactionRequestParams params,
    Class<TxnType> transactionType
  ) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.TX)
      .addParams(params)
      .build();

    JavaType resultType = objectMapper.getTypeFactory().constructParametricType(TransactionResult.class, transactionType);
    return jsonRpcClient.send(request, resultType);
  }

  /**
   * Get the contents of a ledger by sending a ledger method request.
   *
   * @param params The {@link LedgerRequestParams} to send in the request.
   * @return A {@link LedgerResult} containing the ledger details.
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
   * @return A {@link RipplePathFindResult} containing possible paths.
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
   * @return The {@link AccountLinesResult} containing the requested trust lines.
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
   * Serialize a {@link Transaction} to binary and sign it using {@code wallet.privateKey()}.
   *
   * @param wallet The {@link Wallet} of the XRPL account submitting {@code unsignedTransaction}.
   * @param unsignedTransaction An unsigned {@link Transaction} to submit. {@link Transaction#transactionSignature()}
   *                            must not be provided, and {@link Transaction#signingPublicKey()} must be provided.
   * @return The signed transaction as hex encoded {@link String}.
   * @throws JsonProcessingException If the transaction cannot be serialized.
   */
  private String serializeAndSignTransaction(
    Wallet wallet,
    Transaction<? extends Flags.TransactionFlags> unsignedTransaction
  ) throws JsonProcessingException {
    String unsignedJson = objectMapper.writeValueAsString(unsignedTransaction);
    String unsignedBinaryHex = binaryCodec.encodeForSigning(unsignedJson);
    String signature = keyPairService.sign(unsignedBinaryHex, wallet.privateKey()
      .orElseThrow(() -> new RuntimeException("Wallet must provide a private key to sign the transaction.")));

    Transaction signedTransaction = addSignature(unsignedTransaction, signature);

    String signedJson = objectMapper.writeValueAsString(signedTransaction);
    return binaryCodec.encode(signedJson);
  }

  /**
   * Add {@link Transaction#transactionSignature()} to the given unsignedTransaction. Because {@link Transaction} is not
   * a {@link Value.Immutable}, it does not have a generated builder like the subclasses.  Thus, this method
   * needs to rebuild transactions based on their runtime type.
   *
   * @param unsignedTransaction An unsigned {@link Transaction} to add a signature to.
   *                            {@link Transaction#transactionSignature()} must not be provided,
   *                            and {@link Transaction#signingPublicKey()} must be provided.
   * @param signature The hex encoded {@link String} containing the transaction signature.
   * @return A copy of {@code unsignedTransaction} with the {@link Transaction#transactionSignature()} field added.
   */
  private Transaction<? extends Flags.TransactionFlags> addSignature(
    Transaction<? extends Flags.TransactionFlags> unsignedTransaction,
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
    }  else if (CheckCancel.class.isAssignableFrom(unsignedTransaction.getClass())) {
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
    }

    // TODO: Add other transactions

    throw new IllegalArgumentException("Signing fields could not be added to the unsignedTransaction."); // Never happens

  }

}
