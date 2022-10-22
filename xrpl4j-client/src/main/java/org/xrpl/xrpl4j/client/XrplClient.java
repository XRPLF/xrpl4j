package org.xrpl.xrpl4j.client;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: client
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import okhttp3.HttpUrl;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.client.Finality;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountOffersRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountOffersResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsResult;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesResult;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyRequestParams;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedResult;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.server.ServerInfo;
import org.xrpl.xrpl4j.model.client.server.ServerInfoResult;
import org.xrpl.xrpl4j.model.client.serverinfo.ClioServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.ReportingModeServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.RippledServerInfo;
import org.xrpl.xrpl4j.model.client.transactions.SignedTransaction;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
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
import org.xrpl.xrpl4j.model.transactions.TicketCreate;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>A client which wraps a rippled network client and is responsible for higher order functionality such as signing
 * and serializing transactions, as well as hiding certain implementation details from the public API such as JSON RPC
 * request object creation.</p>
 *
 * <p>Note: This client is currently marked as {@link Beta}, and should be used as a reference implementation ONLY.</p>
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
  public XrplClient(final HttpUrl rippledUrl) {
    this(JsonRpcClient.construct(rippledUrl));
  }

  /**
   * Required-args constructor (exists for testing purposes only).
   *
   * @param jsonRpcClient A {@link JsonRpcClient}.
   */
  @VisibleForTesting
  XrplClient(final JsonRpcClient jsonRpcClient) {
    this.jsonRpcClient = Objects.requireNonNull(jsonRpcClient);
    this.objectMapper = ObjectMapperFactory.create();
    this.binaryCodec = new XrplBinaryCodec();
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
   * @deprecated Prefer submitting a signed transaction without a wallet.
   */
  @Deprecated
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
   * @deprecated Prefer the submit method with the variant from the new crypto package.
   */
  @Deprecated
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
   * @deprecated Prefer the submit method with the variant from the new crypto package.
   */
  @Deprecated
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
   * Submit a {@link SingleSignedTransaction} to the XRP Ledger.
   *
   * @param <T>               The type of signed {@link Transaction} that is being submitted.
   * @param signedTransaction A {@link SingleSignedTransaction} to submit.
   *
   * @return The {@link SubmitResult} resulting from the submission request.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @throws JsonProcessingException     if any JSON is invalid.
   * @see "https://xrpl.org/submit.html"
   */
  public <T extends Transaction> SubmitResult<T> submit(final SingleSignedTransaction signedTransaction)
    throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(signedTransaction);

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
  public <T extends Transaction> SubmitMultiSignedResult<T> submitMultisigned(T transaction)
    throws JsonRpcClientErrorException {
    Objects.requireNonNull(transaction);

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
   * Get the ledger index of a tx result response. If not present, throw an exception.
   *
   * @return A string containing value of last validated ledger index.
   *
   * @throws JsonRpcClientErrorException when client encounters errors related to calling rippled JSON RPC API..
   */
  protected UnsignedInteger getMostRecentlyValidatedLedgerIndex() throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.LEDGER)
      .addParams(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.VALIDATED).build())
      .build();
    return jsonRpcClient.send(request, LedgerResult.class).ledgerIndexSafe().unsignedIntegerValue();
  }

  /**
   * Get the {@link TransactionResult} for the transaction with the hash transactionHash.
   *
   * @param transactionHash {@link Hash256} of the transaction to get the TransactionResult for.
   *
   * @return the {@link TransactionResult} for a validated transaction and empty response for a {@link Transaction} that
   *   is expired or not found.
   */
  protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
    final Hash256 transactionHash
  ) {
    Objects.requireNonNull(transactionHash);

    try {
      final TransactionResult<? extends Transaction> transactionResult = this.transaction(
        TransactionRequestParams.of(transactionHash), Transaction.class
      );
      return Optional.ofNullable(transactionResult).filter(TransactionResult::validated);
    } catch (JsonRpcClientErrorException e) {
      // The transaction was not found on ledger, warn on this, but otherwise return.
      LOGGER.warn(e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * Check if there missing ledgers in rippled in the given range.
   *
   * @param submittedLedgerSequence {@link LedgerIndex} at which the {@link Transaction} was submitted on.
   * @param lastLedgerSequence      he ledger index/sequence of type {@link UnsignedInteger} after which the transaction
   *                                will expire and won't be applied to the ledger.
   *
   * @return {@link Boolean} to indicate if there are gaps in the ledger range.
   */
  protected boolean ledgerGapsExistBetween(
    final UnsignedLong submittedLedgerSequence,
    final UnsignedLong lastLedgerSequence
  ) {
    final ServerInfo serverInfo;
    try {
      serverInfo = this.serverInfo();
    } catch (JsonRpcClientErrorException e) {
      LOGGER.error(e.getMessage(), e);
      return true; // Assume ledger gaps exist so this can be retried.
    }

    Range<UnsignedLong> submittedToLast = Range.closed(submittedLedgerSequence, lastLedgerSequence);
    return serverInfo.completeLedgerRanges().stream()
      .noneMatch(range -> range.encloses(submittedToLast));
  }

  /**
   * Check if the transaction is final on the ledger or not.
   *
   * @param transactionHash            {@link Hash256} of the submitted transaction to check the status for.
   * @param submittedOnLedgerIndex     {@link LedgerIndex} on which the transaction with hash transactionHash was
   *                                   submitted. This can be obtained from submit() response of the tx as
   *                                   validatedLedgerIndex.
   * @param lastLedgerSequence         The ledger index/sequence of type {@link UnsignedInteger} after which the
   *                                   transaction will expire and won't be applied to the ledger.
   * @param transactionAccountSequence The sequence number of the account submitting the {@link Transaction}. A
   *                                   {@link Transaction} is only valid if the Sequence number is exactly 1 greater
   *                                   than the previous transaction from the same account.
   * @param account                    The unique {@link Address} of the account that initiated this transaction.
   *
   * @return {@code true} if the {@link Transaction} is final/validated else {@code false}.
   */
  public Finality isFinal(
    Hash256 transactionHash,
    LedgerIndex submittedOnLedgerIndex,
    UnsignedInteger lastLedgerSequence,
    UnsignedInteger transactionAccountSequence,
    Address account
  ) {
    return getValidatedTransaction(transactionHash)
      .map(transactionResult -> {
        // Note from https://xrpl.org/transaction-metadata.html#transaction-metadata:
        // "Any transaction that gets included in a ledger has metadata, regardless of whether it is
        // successful." However, we handle missing metadata as a failure, just in case rippled doesn't perfectly
        // conform
        final boolean isTesSuccess = transactionResult.metadata()
          .map(TransactionMetadata::transactionResult)
          .filter("tesSUCCESS"::equals)
          .map($ -> true)
          .isPresent();

        final boolean metadataExists = transactionResult.metadata().isPresent();

        if (isTesSuccess) {
          LOGGER.debug("Transaction with hash: {} was validated with success", transactionHash);
          return Finality.builder()
            .finalityStatus(FinalityStatus.VALIDATED_SUCCESS)
            .resultCode(transactionResult.metadata().get().transactionResult())
            .build();
        } else if (!metadataExists) {
          return Finality.builder().finalityStatus(FinalityStatus.VALIDATED_UNKNOWN).build();
        } else {
          LOGGER.debug("Transaction with hash: {} was validated with failure", transactionHash);
          return Finality.builder()
            .finalityStatus(FinalityStatus.VALIDATED_FAILURE)
            .resultCode(transactionResult.metadata().get().transactionResult())
            .build();
        }
      }).orElseGet(() -> {
        try {
          final boolean isTransactionExpired = FluentCompareTo.is(getMostRecentlyValidatedLedgerIndex())
            .greaterThan(lastLedgerSequence);
          if (!isTransactionExpired) {
            LOGGER.debug("Transaction with hash: {} has not expired yet, check again", transactionHash);
            return Finality.builder().finalityStatus(FinalityStatus.NOT_FINAL).build();
          } else {
            boolean isMissingLedgers = ledgerGapsExistBetween(UnsignedLong.valueOf(submittedOnLedgerIndex.toString()),
              UnsignedLong.valueOf(lastLedgerSequence.toString()));
            if (isMissingLedgers) {
              LOGGER.debug("Transaction with hash: {} has expired and rippled is missing some to confirm if it" +
                " was validated", transactionHash);
              return Finality.builder().finalityStatus(FinalityStatus.NOT_FINAL).build();
            } else {
              AccountInfoResult accountInfoResult = this.accountInfo(
                AccountInfoRequestParams.of(account)
              );
              UnsignedInteger accountSequence = accountInfoResult.accountData().sequence();
              if (FluentCompareTo.is(transactionAccountSequence).lessThan(accountSequence)) {
                // a different transaction with this sequence has a final outcome.
                // this represents an unexpected case
                return Finality.builder().finalityStatus(FinalityStatus.EXPIRED_WITH_SPENT_ACCOUNT_SEQUENCE).build();
              } else {
                LOGGER.debug("Transaction with hash: {} has expired, consider resubmitting with updated" +
                  " lastledgersequence and fee", transactionHash);
                return Finality.builder().finalityStatus(FinalityStatus.EXPIRED).build();
              }
            }
          }
        } catch (JsonRpcClientErrorException e) {
          LOGGER.warn(e.getMessage(), e);
          return Finality.builder().finalityStatus(FinalityStatus.NOT_FINAL).build();
        }
      });
  }

  /**
   * Get the "server_info" for the rippled node.
   *
   * @return A {@link ServerInfo} containing information about the server.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @see "https://xrpl.org/server_info.html"
   * @deprecated This method is deprecated to accommodate 3 different types of server_info responses from the
   *   specialised Clio {@link ClioServerInfo} and Reporting Mode {@link ReportingModeServerInfo} servers and the
   *   generic rippled server {@link RippledServerInfo}. Use {@link XrplClient#serverInformation()}.
   */
  @Deprecated
  public ServerInfo serverInfo() throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.SERVER_INFO)
      .build();

    return jsonRpcClient.send(request, ServerInfoResult.class).info();
  }

  /**
   * Get the "server_info" for the rippled node {@link RippledServerInfo}, clio server {@link ClioServerInfo} and
   * reporting mode server {@link ReportingModeServerInfo}. You should be able to handle all these response types as
   * {@link org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo}.
   *
   * @return A {@link org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult} containing information about the server.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @see "https://xrpl.org/server_info.html"
   */
  public org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult serverInformation()
    throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.SERVER_INFO)
      .build();

    return jsonRpcClient.send(request, org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult.class);
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
   * Get the {@link AccountCurrenciesResult} for the account specified in {@code params} by making an account_currencies
   * method call.
   *
   * @param params The {@link AccountCurrenciesRequestParams} to send in the request.
   *
   * @return The {@link AccountCurrenciesResult} returned by the account_currencies method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public AccountCurrenciesResult accountCurrencies(
    AccountCurrenciesRequestParams params
  ) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_CURRENCIES)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, AccountCurrenciesResult.class);
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
   * Get the {@link AccountOffersResult} for the account specified in {@code params} by making an account_offers method
   * call.
   *
   * @param params The {@link AccountOffersRequestParams} to send in the request.
   *
   * @return The {@link AccountOffersResult} returned by the account_offers method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public AccountOffersResult accountOffers(AccountOffersRequestParams params) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.ACCOUNT_OFFERS)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, AccountOffersResult.class);
  }

  /**
   * Indicates whether one account is authorized to send payments directly to another.
   *
   * @param params A {@link DepositAuthorizedRequestParams} to send in the request.
   *
   * @return The {@link DepositAuthorizedResult} returned by the deposit_authorized method call.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   */
  public DepositAuthorizedResult depositAuthorized(DepositAuthorizedRequestParams params)
    throws JsonRpcClientErrorException {
    Objects.requireNonNull(params);
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.DEPOSIT_AUTHORIZED)
      .addParams(params)
      .build();
    return jsonRpcClient.send(request, DepositAuthorizedResult.class);
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
    final TransactionRequestParams params,
    final Class<T> transactionType
  ) throws JsonRpcClientErrorException {
    Objects.requireNonNull(params);
    Objects.requireNonNull(transactionType);

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
   * @param params The {@link ChannelVerifyRequestParams} to send in the request.
   *
   * @return The result of the request, as a {@link ChannelVerifyResult}.
   *
   * @throws JsonRpcClientErrorException if {@code jsonRpcClient} throws an error.
   */
  public ChannelVerifyResult channelVerify(ChannelVerifyRequestParams params) throws JsonRpcClientErrorException {

    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.CHANNEL_VERIFY)
      .addParams(params)
      .build();

    return jsonRpcClient.send(request, ChannelVerifyResult.class);
  }

  /**
   * Get the issued currency balances of an issuing account by making a "gateway_balances" rippled API method call.
   *
   * @param params The {@link GatewayBalancesRequestParams} to send in the request.
   *
   * @return The result of the request, as a {@link GatewayBalancesResult}.
   *
   * @throws JsonRpcClientErrorException if {@code jsonRpcClient} throws an error.
   */
  public GatewayBalancesResult gatewayBalances(
    GatewayBalancesRequestParams params
  ) throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method(XrplMethods.GATEWAY_BALANCES)
      .addParams(params)
      .build();
    return jsonRpcClient.send(request, GatewayBalancesResult.class);
  }

  /**
   * Sign a {@link Transaction} with the private key from a {@link Wallet}.
   *
   * @param wallet              A {@link Wallet} with a private key to sign the transaction with.
   * @param unsignedTransaction A {@link Transaction} without a signature.
   * @param <T>                 The actual polymorphic type of {@link Transaction} to sign.
   *
   * @return A {@link SignedTransaction}.
   */
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
   * @param unsignedTransaction An unsigned {@link Transaction} to add a signature to.
   *                            {@link Transaction#transactionSignature()} must not be provided, and
   *                            {@link Transaction#signingPublicKey()} must be provided. a {@link Value.Immutable}, it
   *                            does not have a generated builder like the subclasses.  Thus, this method needs to
   *                            rebuild transactions based on their runtime type.
   * @param signature           The hex encoded {@link String} containing the transaction signature.
   *
   * @return A copy of {@code unsignedTransaction} with the {@link Transaction#transactionSignature()} field added.
   *
   * @deprecated This method will go away in a future version and be replaced with the implementation in SignatureUtils
   *   in the xrpl4j-crypto module.
   */
  @Deprecated
  protected Transaction addSignature(
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
    } else if (TicketCreate.class.isAssignableFrom(unsignedTransaction.getClass())) {
      return TicketCreate.builder().from((TicketCreate) unsignedTransaction)
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
