package org.xrpl.xrpl4j.client;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.MultiSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountCurrenciesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountNftsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountNftsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountOffersRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountOffersResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsResult;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesResult;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoRequestParams;
import org.xrpl.xrpl4j.model.client.amm.AmmInfoResult;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyRequestParams;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.nft.NftBuyOffersRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftBuyOffersResult;
import org.xrpl.xrpl4j.model.client.nft.NftInfoRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftInfoResult;
import org.xrpl.xrpl4j.model.client.nft.NftSellOffersRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftSellOffersResult;
import org.xrpl.xrpl4j.model.client.oracle.GetAggregatePriceRequestParams;
import org.xrpl.xrpl4j.model.client.oracle.GetAggregatePriceResult;
import org.xrpl.xrpl4j.model.client.path.BookOffersRequestParams;
import org.xrpl.xrpl4j.model.client.path.BookOffersResult;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedResult;
import org.xrpl.xrpl4j.model.client.path.ImmutableBookOffersRequestParams;
import org.xrpl.xrpl4j.model.client.path.PathCurrency;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.serverinfo.LedgerRangeUtils;
import org.xrpl.xrpl4j.model.client.serverinfo.RippledServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.LastClose;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.ValidatedLedger;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.AccountRootFlags;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test for {@link XrplClient}.
 */
public class XrplClientTest {

  private final KeyPair keyPair = Seed.ed25519Seed().deriveKeyPair();

  @Mock
  private JsonRpcClient jsonRpcClientMock;
  private XrplClient xrplClient;

  @BeforeEach
  void setUp() {
    openMocks(this);
    xrplClient = new XrplClient(jsonRpcClientMock);
  }

  @Test
  public void depositAuthorized() throws JsonRpcClientErrorException {
    DepositAuthorizedRequestParams depositAuthorized = DepositAuthorizedRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .sourceAccount(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .destinationAccount(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .build();
    xrplClient.depositAuthorized(depositAuthorized);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(DepositAuthorizedResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.DEPOSIT_AUTHORIZED);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().size()).isEqualTo(1);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(depositAuthorized);
  }

  @Test
  void isFinalThrowsForNullParams() {
    assertThrows(
      RuntimeException.class,
      () -> xrplClient.isFinal(
        null,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      )
    );
    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThrows(
      RuntimeException.class,
      () -> xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        null,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      )
    );
    assertThrows(
      RuntimeException.class,
      () -> xrplClient.isFinal(
        transactionHash,
        null,
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      )
    );
    assertThrows(
      RuntimeException.class,
      () -> xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        null,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      )
    );
    assertThrows(
      RuntimeException.class,
      () -> xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        null
      )
    );
  }

  @Test
  void isFinalForValidatedTransactionWithoutMetadata_ReturnsValidatedUnknown() {
    TransactionResult<? extends TransactionResult<? extends Transaction>> mockTransactionResult = mock(
      TransactionResult.class
    );
    when(mockTransactionResult.metadata()).thenReturn(Optional.empty());

    AtomicReference<Hash256> calledWithHash = new AtomicReference<>();
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
        Hash256 transactionHash
      ) {
        calledWithHash.set(transactionHash);
        return Optional.of(mockTransactionResult);
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.VALIDATED_UNKNOWN);

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }

  @Test
  void isFinalForValidatedSuccessTransaction_ReturnsValidatedSuccess() {
    TransactionResult<? extends TransactionResult<? extends Transaction>> mockTransactionResult = mock(
      TransactionResult.class
    );
    TransactionMetadata metadata = TransactionMetadata.builder()
      .transactionResult("tesSUCCESS")
      .transactionIndex(UnsignedInteger.MAX_VALUE)
      .deliveredAmount(XrpCurrencyAmount.ofDrops(10))
      .build();
    when(mockTransactionResult.metadata()).thenReturn(Optional.of(metadata));

    AtomicReference<Hash256> calledWithHash = new AtomicReference<>();
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
        Hash256 transactionHash
      ) {
        calledWithHash.set(transactionHash);
        return Optional.of(mockTransactionResult);
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }

  @Test
  void isFinalForValidatedSuccessTransaction_ReturnsValidatedFailure() {
    TransactionResult<? extends TransactionResult<? extends Transaction>> mockTransactionResult = mock(
      TransactionResult.class
    );
    TransactionMetadata metadata = TransactionMetadata.builder()
      .transactionResult("tefPAST_SEQ")
      .transactionIndex(UnsignedInteger.MAX_VALUE)
      .deliveredAmount(XrpCurrencyAmount.ofDrops(10))
      .build();
    when(mockTransactionResult.metadata()).thenReturn(Optional.of(metadata));

    AtomicReference<Hash256> calledWithHash = new AtomicReference<>();
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
        Hash256 transactionHash
      ) {
        calledWithHash.set(transactionHash);
        return Optional.of(mockTransactionResult);
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.VALIDATED_FAILURE);

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }

  @Test
  void isFinalForWaitingTransaction_ReturnsNotFinal() {
    // lastLedgerSeq has not passed in this case, but the transaction is yet to be validated
    AtomicReference<Hash256> calledWithHash = new AtomicReference<>();
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
        Hash256 transactionHash
      ) {
        calledWithHash.set(transactionHash);
        return Optional.empty();
      }

      @Override
      protected UnsignedInteger getMostRecentlyValidatedLedgerIndex() {
        return UnsignedInteger.ONE;
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.valueOf(2),
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.NOT_FINAL);

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }

  @Test
  void isFinalForWaitingTransactionAndLedgerMissing_ReturnsNotFinal() {
    AtomicReference<Hash256> calledWithHash = new AtomicReference<>();
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
        Hash256 transactionHash
      ) {
        calledWithHash.set(transactionHash);
        return Optional.empty();
      }

      @Override
      protected UnsignedInteger getMostRecentlyValidatedLedgerIndex() {
        return UnsignedInteger.valueOf(2);
      }

      @Override
      public ServerInfoResult serverInformation() {
        ServerInfoResult mockServerInfoResult = mock(ServerInfoResult.class);
        ServerInfo mockServerInfo = mock(ServerInfo.class);
        when(mockServerInfo.completeLedgers())
          .thenReturn(Lists.newArrayList(Range.closed(UnsignedLong.ZERO, UnsignedLong.ZERO)));
        when(mockServerInfoResult.info()).thenReturn(mockServerInfo);
        return mockServerInfoResult;
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.NOT_FINAL);

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }

  @Test
  void isFinalForExpiredTransaction_ReturnsExpired() {
    AtomicReference<Hash256> calledWithHash = new AtomicReference<>();
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
        Hash256 transactionHash
      ) {
        calledWithHash.set(transactionHash);
        return Optional.empty();
      }

      @Override
      protected UnsignedInteger getMostRecentlyValidatedLedgerIndex() {
        return UnsignedInteger.valueOf(2);
      }

      @Override
      public ServerInfoResult serverInformation() {
        ServerInfoResult mockServerInfoResult = mock(ServerInfoResult.class);
        ServerInfo mockServerInfo = mock(ServerInfo.class);
        when(mockServerInfo.completeLedgers())
          .thenReturn(Lists.newArrayList(Range.closed(UnsignedLong.ONE, UnsignedLong.ONE)));
        when(mockServerInfoResult.info()).thenReturn(mockServerInfo);
        return mockServerInfoResult;
      }

      @Override
      public AccountInfoResult accountInfo(AccountInfoRequestParams params) {
        return AccountInfoResult.builder()
          .accountData(AccountRootObject.builder()
            .accountTransactionId(Hash256.of(Strings.repeat("0", 64)))
            .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
            .balance(XrpCurrencyAmount.ofDrops(1000))
            .ownerCount(UnsignedInteger.ONE)
            .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
            .previousTransactionLedgerSequence(UnsignedInteger.ONE)
            .sequence(UnsignedInteger.ONE)
            .flags(AccountRootFlags.DISABLE_MASTER)
            .index(Hash256.of(Strings.repeat("0", 64)))
            .build())
          .build();
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.EXPIRED);

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }

  @Test
  void isFinalForNoLedgerGapAndLastLedgerSequencePassedTransaction_ReturnsExpired() {
    AtomicReference<Hash256> calledWithHash = new AtomicReference<>();
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected Optional<? extends TransactionResult<? extends Transaction>> getValidatedTransaction(
        Hash256 transactionHash
      ) {
        calledWithHash.set(transactionHash);
        return Optional.empty();
      }

      @Override
      protected UnsignedInteger getMostRecentlyValidatedLedgerIndex() {
        return UnsignedInteger.valueOf(2);
      }

      @Override
      public ServerInfoResult serverInformation() {
        ServerInfoResult mockServerInfoResult = mock(ServerInfoResult.class);
        ServerInfo mockServerInfo = mock(ServerInfo.class);
        when(mockServerInfo.completeLedgers())
          .thenReturn(Lists.newArrayList(Range.closed(UnsignedLong.ONE, UnsignedLong.ONE)));
        when(mockServerInfoResult.info()).thenReturn(mockServerInfo);
        return mockServerInfoResult;
      }

      @Override
      public AccountInfoResult accountInfo(AccountInfoRequestParams params) {
        return AccountInfoResult.builder()
          .accountData(AccountRootObject.builder()
            .accountTransactionId(Hash256.of(Strings.repeat("0", 64)))
            .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
            .balance(XrpCurrencyAmount.ofDrops(1000))
            .ownerCount(UnsignedInteger.ONE)
            .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
            .previousTransactionLedgerSequence(UnsignedInteger.ONE)
            .sequence(UnsignedInteger.ONE)
            .flags(AccountRootFlags.DISABLE_MASTER)
            .index(Hash256.of(Strings.repeat("0", 64)))
            .build())
          .build();
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));

    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ZERO,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.EXPIRED_WITH_SPENT_ACCOUNT_SEQUENCE);

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }

  @Test
  void ledgerGapsExistBetweenTest() {
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      public ServerInfoResult serverInformation() {
        ServerInfoResult mockServerInfoResult = mock(ServerInfoResult.class);
        ServerInfo mockServerInfo = mock(ServerInfo.class);
        when(mockServerInfo.completeLedgers())
          .thenReturn(Lists.newArrayList(Range.closed(UnsignedLong.valueOf(2), UnsignedLong.valueOf(4))));
        when(mockServerInfoResult.info()).thenReturn(mockServerInfo);
        return mockServerInfoResult;
      }
    };
    assertThat(xrplClient.ledgerGapsExistBetween(UnsignedLong.valueOf(2), UnsignedLong.valueOf(4))).isFalse();
    assertThat(xrplClient.ledgerGapsExistBetween(UnsignedLong.valueOf(2), UnsignedLong.valueOf(3))).isFalse();
    assertThat(xrplClient.ledgerGapsExistBetween(UnsignedLong.valueOf(1), UnsignedLong.valueOf(2))).isTrue();
    assertThat(xrplClient.ledgerGapsExistBetween(UnsignedLong.valueOf(1), UnsignedLong.valueOf(4))).isTrue();
    assertThrows(NullPointerException.class, () -> xrplClient.ledgerGapsExistBetween(null, UnsignedLong.ONE));
    assertThrows(NullPointerException.class, () -> xrplClient.ledgerGapsExistBetween(UnsignedLong.ONE, null));

  }

  @Test
  void getValidatedTransactionThrows_ReturnsEmpty() {
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      public <T extends Transaction> TransactionResult<T> transaction(
        TransactionRequestParams params,
        Class<T> transactionType
      ) throws JsonRpcClientErrorException {
        throw new JsonRpcClientErrorException("Could not connect to client.");
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(xrplClient.getValidatedTransaction(transactionHash)).isEqualTo(Optional.empty());
  }

  @Test
  void ledgerGapExistsBetweenThrows_ReturnsTrue() {
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      public ServerInfoResult serverInformation() throws JsonRpcClientErrorException {
        throw new JsonRpcClientErrorException("Could not connect to client.");
      }
    };

    assertThat(xrplClient.ledgerGapsExistBetween(UnsignedLong.ONE, UnsignedLong.ONE)).isTrue();
  }

  @Test
  void isFinalForGetMostRecentlyValidatedTxThrows_ReturnsNotFinal() {
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      protected UnsignedInteger getMostRecentlyValidatedLedgerIndex() throws JsonRpcClientErrorException {
        throw new JsonRpcClientErrorException("Could not connect to client.");
      }
    };

    Hash256 transactionHash = Hash256.of(Strings.repeat("0", 64));
    assertThat(
      xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ZERO,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      ).finalityStatus()
    ).isEqualTo(FinalityStatus.NOT_FINAL);
  }

  @Test
  public void getJsonRpcClientTest() {
    assertThat(xrplClient.getJsonRpcClient() instanceof JsonRpcClient).isTrue();
    assertThat(xrplClient.getJsonRpcClient() instanceof XrplClient).isFalse();
  }

  @Test
  public void getXrplClientTest() {
    HttpUrl rippledUrl = HttpUrl.parse("https://s.altnet.rippletest.net:51234");
    assertThat(new XrplClient(rippledUrl) instanceof XrplClient).isTrue();
    assertThat(new XrplClient(rippledUrl) instanceof JsonRpcClient).isFalse();
  }

  @Test
  public void submitSingleSignedTransaction() {
    BcSignatureService bcSignatureService = new BcSignatureService();
    jsonRpcClientMock = new JsonRpcClient() {

      @Override
      public JsonNode postRpcRequest(JsonRpcRequest rpcRequest) {
        return mock(JsonNode.class);
      }

      @Override
      public <T extends XrplResult> T send(
        JsonRpcRequest request,
        JavaType resultType
      ) {
        return (T) mock(SubmitResult.class);
      }
    };

    Payment payment = Payment.builder()
      .ticketSequence(UnsignedInteger.ONE)
      .account(keyPair.publicKey().deriveAddress())
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey(keyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> paymentSignedTransaction = bcSignatureService.sign(keyPair.privateKey(), payment);
    xrplClient = new XrplClient(jsonRpcClientMock);
    assertDoesNotThrow(() -> xrplClient.submit(paymentSignedTransaction));
  }

  @Test
  public void submitMultiSignedTest() throws JsonRpcClientErrorException {
    jsonRpcClientMock = new JsonRpcClient() {

      @Override
      public JsonNode postRpcRequest(JsonRpcRequest rpcRequest) {
        return mock(JsonNode.class);
      }

      @Override
      public <T extends XrplResult> T send(
        JsonRpcRequest request,
        JavaType resultType
      ) {
        SubmitMultiSignedResult submitMultiSignedResult = SubmitMultiSignedResult.builder()
          .engineResult("tesSUCCESS")
          .engineResultCode(200)
          .engineResultMessage("Submitted")
          .transactionBlob("blob")
          .transaction(mock(TransactionResult.class))
          .build();
        return (T) submitMultiSignedResult;
      }
    };
    KeyPair keyPair = Seed.ed25519Seed().deriveKeyPair();

    Payment unsignedPayment = Payment.builder()
      .account(this.keyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(keyPair.publicKey().deriveAddress())
      .build();

    Signer signatureWithPublicKey = Signer.builder()
      .signingPublicKey(Seed.ed25519Seed().deriveKeyPair().publicKey())
      .transactionSignature(Signature.fromBase16("ABCD"))
      .build();
    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    Set<Signer> signers = Sets.newHashSet(
      signatureWithPublicKey
    );
    MultiSignedTransaction<Payment> multiSignedPayment = MultiSignedTransaction.<Payment>builder()
      .unsignedTransaction(unsignedPayment)
      .signerSet(signers)
      .build();

    xrplClient = new XrplClient(jsonRpcClientMock);
    SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSignedPayment);
    assertThat(paymentResult.engineResult()).isEqualTo("tesSUCCESS");
    assertThat(paymentResult.engineResultCode()).isEqualTo(200);
    assertThat(paymentResult.transactionBlob()).isEqualTo("blob");
  }

  @Test
  public void fee() throws JsonRpcClientErrorException {
    xrplClient.fee();

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(FeeResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.FEE);
  }

  @Test
  public void serverInformation() {
    ValidatedLedger serverInfoLedger = ValidatedLedger.builder()
      .hash(Hash256.of(Strings.repeat("0", 64)))
      .age(UnsignedInteger.ONE)
      .reserveBaseXrp(XrpCurrencyAmount.ofXrp(BigDecimal.ONE))
      .reserveIncXrp(XrpCurrencyAmount.ofXrp(BigDecimal.ONE))
      .sequence(LedgerIndex.of(UnsignedInteger.ONE))
      .baseFeeXrp(BigDecimal.ONE)
      .build();
    ServerInfo serverInfo = RippledServerInfo.builder()
      .completeLedgers(LedgerRangeUtils.completeLedgersToListOfRange("1-2"))
      .amendmentBlocked(true)
      .buildVersion("1")
      .closedLedger(serverInfoLedger)
      .hostId("id")
      .ioLatencyMs(UnsignedLong.valueOf(2))
      .jqTransOverflow("flow")
      .lastClose(LastClose.builder()
        .convergeTimeSeconds(BigDecimal.valueOf(1.11))
        .proposers(UnsignedInteger.ONE)
        .build())
      .publicKeyNode("node")
      .serverState("full")
      .serverStateDurationUs("10")
      .time(ZonedDateTime.now())
      .upTime(UnsignedLong.ONE)
      .validationQuorum(UnsignedInteger.ONE)
      .build();

    ServerInfoResult serverInfoResult = ServerInfoResult.builder()
      .info(serverInfo)
      .build();

    jsonRpcClientMock = new JsonRpcClient() {
      @Override
      public JsonNode postRpcRequest(JsonRpcRequest rpcRequest) {
        return mock(JsonNode.class);
      }

      @Override
      public <T extends XrplResult> T send(
        JsonRpcRequest request,
        JavaType resultType
      ) {
        return (T) serverInfoResult;
      }
    };
    xrplClient = new XrplClient(jsonRpcClientMock);
    ServerInfoResult serverInfoResponse = assertDoesNotThrow(() -> xrplClient.serverInformation());
    assertThat(serverInfoResponse.info()).isEqualTo(serverInfo);
  }

  @Test
  public void accountChannels() throws JsonRpcClientErrorException {
    AccountChannelsRequestParams accountChannelsRequestParams = AccountChannelsRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    xrplClient.accountChannels(accountChannelsRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountChannelsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_CHANNELS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountChannelsRequestParams);
  }

  @Test
  public void accountCurrencies() throws JsonRpcClientErrorException {
    AccountCurrenciesRequestParams accountCurrenciesRequestParams = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    xrplClient.accountCurrencies(accountCurrenciesRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountCurrenciesResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_CURRENCIES);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountCurrenciesRequestParams);
  }

  @Test
  public void accountInfo() throws JsonRpcClientErrorException {
    AccountInfoRequestParams accountInfoRequestParams = AccountInfoRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    xrplClient.accountInfo(accountInfoRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountInfoResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_INFO);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountInfoRequestParams);
  }

  @Test
  public void accountObjects() throws JsonRpcClientErrorException {
    AccountObjectsRequestParams accountObjectsRequestParams = AccountObjectsRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    xrplClient.accountObjects(accountObjectsRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountObjectsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_OBJECTS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountObjectsRequestParams);
  }

  @Test
  public void accountOffers() throws JsonRpcClientErrorException {
    AccountOffersRequestParams accountOffersRequestParams = AccountOffersRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    xrplClient.accountOffers(accountOffersRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountOffersResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_OFFERS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountOffersRequestParams);
  }

  @Test
  public void accountTransactionsUsingBuilder() throws JsonRpcClientErrorException {
    AccountTransactionsRequestParams accountTransactionsRequestParams = AccountTransactionsRequestParams
      .unboundedBuilder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountTransactions(accountTransactionsRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountTransactionsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_TX);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountTransactionsRequestParams);
  }

  @Test
  public void accountTransactionsUsingAddress() throws JsonRpcClientErrorException {
    Address account = Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw");
    xrplClient.accountTransactions(account);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountTransactionsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_TX);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0))
      .isEqualTo(AccountTransactionsRequestParams.unboundedBuilder()
        .account(account)
        .build());
  }

  @Test
  public void accountNftsUsingBuilder() throws JsonRpcClientErrorException {
    AccountNftsRequestParams accountNftsRequestParams = AccountNftsRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountNfts(accountNftsRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountNftsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_NFTS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountNftsRequestParams);
  }

  @Test
  public void accountNftsUsingAddress() throws JsonRpcClientErrorException {
    Address account = Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw");
    xrplClient.accountNfts(account);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountNftsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_NFTS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0))
      .isEqualTo(AccountNftsRequestParams.builder()
        .account(account)
        .build());
  }

  @Test
  public void transaction() throws JsonRpcClientErrorException {

    jsonRpcClientMock = new JsonRpcClient() {

      @Override
      public JsonNode postRpcRequest(JsonRpcRequest rpcRequest) {
        return mock(JsonNode.class);
      }

      @Override
      public <T extends XrplResult> T send(
        JsonRpcRequest request,
        JavaType resultType
      ) {
        TransactionResult submitTransactionResult = TransactionResult.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .transaction(mock(Transaction.class))
          .build();
        return (T) submitTransactionResult;
      }
    };
    xrplClient = new XrplClient(jsonRpcClientMock);

    TransactionRequestParams transactionRequestParams = TransactionRequestParams.builder()
      .transaction(Hash256.of(Strings.repeat("0", 64)))
      .build();
    TransactionResult transactionResult = assertDoesNotThrow(() ->
      xrplClient.transaction(transactionRequestParams, Transaction.class));
    assertThat(transactionResult.hash().value()).isEqualTo(Strings.repeat("0", 64));
  }

  @Test
  public void ledger() throws JsonRpcClientErrorException {
    LedgerRequestParams ledgerRequestParams = LedgerRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();
    xrplClient.ledger(ledgerRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(LedgerResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.LEDGER);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(ledgerRequestParams);
  }

  @Test
  public void ripplePathFind() throws JsonRpcClientErrorException {
    RipplePathFindRequestParams ripplePathFindRequestParams = RipplePathFindRequestParams.builder()
      .sourceAccount(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .destinationAccount(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .destinationAmount(XrpCurrencyAmount.ofDrops(1))
      .addSourceCurrencies(PathCurrency.of("XRP"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();
    xrplClient.ripplePathFind(ripplePathFindRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(RipplePathFindResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.RIPPLE_PATH_FIND);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(ripplePathFindRequestParams);
  }

  @Test
  void bookOffers() throws JsonRpcClientErrorException {
    BookOffersRequestParams params = BookOffersRequestParams.builder()
      .taker(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .takerGets(Issue.XRP)
      .takerPays(
        Issue.builder()
          .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
          .currency("USD")
          .build()
      )
      .limit(UnsignedInteger.valueOf(10))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();

    BookOffersResult resultMock = mock(BookOffersResult.class);
    when(jsonRpcClientMock.send(
      JsonRpcRequest.builder()
        .method(XrplMethods.BOOK_OFFERS)
        .addParams(params)
        .build(),
      BookOffersResult.class
    )).thenReturn(resultMock);
    BookOffersResult result = xrplClient.bookOffers(params);
    assertThat(result).isEqualTo(resultMock);
  }

  @Test
  public void accountLines() throws JsonRpcClientErrorException {
    AccountLinesRequestParams accountLinesRequestParams = AccountLinesRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    xrplClient.accountLines(accountLinesRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountLinesResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_LINES);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountLinesRequestParams);
  }

  @Test
  public void channelVerify() throws JsonRpcClientErrorException {
    ChannelVerifyRequestParams channelVerifyRequestParams = ChannelVerifyRequestParams.builder()
      .amount(XrpCurrencyAmount.ofDrops(1))
      .channelId(Hash256.of(Strings.repeat("0", 64)))
      .publicKey("publicKey")
      .signature("signature")
      .build();
    xrplClient.channelVerify(channelVerifyRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(ChannelVerifyResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.CHANNEL_VERIFY);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(channelVerifyRequestParams);
  }

  @Test
  public void gatewayBalances() throws JsonRpcClientErrorException {
    GatewayBalancesRequestParams gatewayBalancesRequestParams = GatewayBalancesRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .ledgerSpecifier(LedgerSpecifier.CLOSED)
      .build();
    xrplClient.gatewayBalances(gatewayBalancesRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(GatewayBalancesResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.GATEWAY_BALANCES);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(gatewayBalancesRequestParams);
  }

  @Test
  public void nftBuyOffers() throws JsonRpcClientErrorException {
    NftBuyOffersRequestParams nftBuyOffersRequestParams = NftBuyOffersRequestParams.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .build();
    xrplClient.nftBuyOffers(nftBuyOffersRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(NftBuyOffersResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.NFT_BUY_OFFERS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(nftBuyOffersRequestParams);
  }

  @Test
  public void nftSellOffers() throws JsonRpcClientErrorException {
    NftSellOffersRequestParams nftSellOffersRequestParams = NftSellOffersRequestParams.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .build();
    xrplClient.nftSellOffers(nftSellOffersRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(NftSellOffersResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.NFT_SELL_OFFERS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(nftSellOffersRequestParams);
  }

  @Test
  void ammInfo() throws JsonRpcClientErrorException {
    AmmInfoRequestParams params = mock(AmmInfoRequestParams.class);
    JsonRpcRequest expectedRequest = JsonRpcRequest.builder()
      .method(XrplMethods.AMM_INFO)
      .addParams(params)
      .build();
    AmmInfoResult mockResult = mock(AmmInfoResult.class);
    when(jsonRpcClientMock.send(expectedRequest, AmmInfoResult.class)).thenReturn(mockResult);
    AmmInfoResult result = xrplClient.ammInfo(params);

    assertThat(result).isEqualTo(mockResult);
  }

  @Test
  void ledgerEntry() throws JsonRpcClientErrorException {
    LedgerEntryRequestParams<LedgerObject> params = LedgerEntryRequestParams.index(
      Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E"),
      LedgerSpecifier.VALIDATED
    );

    LedgerEntryResult<?> mockResult = mock(LedgerEntryResult.class);
    when(jsonRpcClientMock.send(
      JsonRpcRequest.builder()
        .method(XrplMethods.LEDGER_ENTRY)
        .addParams(params)
        .build(),
        ObjectMapperFactory.create().getTypeFactory().constructParametricType(
          LedgerEntryResult.class, LedgerObject.class
        )
    )).thenReturn(mockResult);

    LedgerEntryResult<LedgerObject> result = xrplClient.ledgerEntry(params);
    assertThat(result).isEqualTo(mockResult);
  }

  @Test
  void nftInfo() throws JsonRpcClientErrorException {
    NftInfoRequestParams params = NftInfoRequestParams.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();

    NftInfoResult mockResult = mock(NftInfoResult.class);
    when(jsonRpcClientMock.send(
      JsonRpcRequest.builder()
        .method(XrplMethods.NFT_INFO)
        .addParams(params)
        .build(),
      NftInfoResult.class
    )).thenReturn(mockResult);

    NftInfoResult result = xrplClient.nftInfo(params);

    assertThat(result).isEqualTo(mockResult);
  }

  @Test
  void getAggregatePrice() throws JsonRpcClientErrorException {
    GetAggregatePriceRequestParams params = mock(GetAggregatePriceRequestParams.class);
    GetAggregatePriceResult expectedResult = mock(GetAggregatePriceResult.class);

    when(jsonRpcClientMock.send(
      JsonRpcRequest.builder()
        .method(XrplMethods.GET_AGGREGATE_PRICE)
        .addParams(params)
        .build(),
      GetAggregatePriceResult.class
    )).thenReturn(expectedResult);

    GetAggregatePriceResult result = xrplClient.getAggregatePrice(params);

    assertThat(result).isEqualTo(expectedResult);
  }
}
