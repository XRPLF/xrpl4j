package com.ripple.xrplj4.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.keypairs.DefaultKeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.AccountDelete;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CheckCancel;
import com.ripple.xrpl4j.model.transactions.CheckCash;
import com.ripple.xrpl4j.model.transactions.CheckCreate;
import com.ripple.xrpl4j.model.transactions.DepositPreAuth;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.Transaction;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrplj4.client.model.accounts.AccountInfoRequestParams;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.accounts.AccountObjectsRequestParams;
import com.ripple.xrplj4.client.model.accounts.AccountObjectsResult;
import com.ripple.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrplj4.client.model.transactions.SubmissionRequestParams;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.rippled.JsonRpcClient;
import com.ripple.xrplj4.client.rippled.JsonRpcRequest;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import com.ripple.xrplj4.client.rippled.XrplMethods;
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
   * @param wallet The {@link Wallet} of the XRPL account submitting {@code unsignedTransaction}.
   * @param unsignedTransaction An unsigned {@link Transaction} to submit. {@link Transaction#transactionSignature()}
   *                            must not be provided, and {@link Transaction#signingPublicKey()} must be provided.
   * @param transactionType The {@link Class} of the specific {@link Transaction} that is being submitted.
   * @param <TxnType> The type of {@link Transaction} that is being submitted.
   * @return The {@link SubmissionResult} resulting from the submission request.
   * @throws RippledClientErrorException If {@code rippledClient} throws an error.
   */
  public <TxnType extends Transaction<? extends Flags.TransactionFlags>> SubmissionResult<TxnType> submit(
    Wallet wallet,
    TxnType unsignedTransaction,
    Class<TxnType> transactionType
  ) throws RippledClientErrorException {
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
      JavaType resultType = objectMapper.getTypeFactory().constructParametricType(SubmissionResult.class, transactionType);
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
   * @throws RippledClientErrorException If {@code rippledClient} throws an error.
   */
  public FeeResult fee() throws RippledClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.FEE)
      .build();

    return jsonRpcClient.send(request, FeeResult.class);
  }

  /**
   * Retrieve information about an XRPL account, its activity, and its XRP balance. All
   * information retrieved is relative to a particular version of the ledger.
   *
   * @see "https://xrpl.org/account_info.html"
   * @param classicAddress The XRPL {@link Address} in classic form of the account to retrieve info for.
   * @return An {@link AccountInfoResult} containing the account information.
   * @throws RippledClientErrorException If {@code rippledClient} throws an error.
   */
  public AccountInfoResult accountInfo(Address classicAddress) throws RippledClientErrorException {
    return accountInfo(AccountInfoRequestParams.of(classicAddress));
  }

  /**
   * Retrieve information about an XRPL account, its activity, and its XRP balance. All
   * information retrieved is relative to a particular version of the ledger, which can be specified by
   * {@code ledgerIndex}.
   *
   * @see "https://xrpl.org/account_info.html"
   * @param classicAddress The XRPL {@link Address} in classic form of the account to retrieve info for.
   * @param ledgerIndex The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   * @return An {@link AccountInfoResult} containing the account information.
   * @throws RippledClientErrorException If {@code rippledClient} throws an error.
   */
  public AccountInfoResult accountInfo(Address classicAddress, String ledgerIndex) throws RippledClientErrorException {
    return accountInfo(AccountInfoRequestParams.builder()
      .account(classicAddress)
      .ledgerIndex(ledgerIndex)
      .build()
    );
  }

  private AccountInfoResult accountInfo(AccountInfoRequestParams params) throws RippledClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_INFO)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, AccountInfoResult.class);
  }

  public AccountObjectsResult accountObjects(AccountObjectsRequestParams params) throws RippledClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_OBJECTS)
      .addParams(params)
      .build();
    return jsonRpcClient.send(request, AccountObjectsResult.class);
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
    }

    throw new IllegalArgumentException("Signing fields could not be added to the unsignedTransaction."); // Never happens

  }

}
