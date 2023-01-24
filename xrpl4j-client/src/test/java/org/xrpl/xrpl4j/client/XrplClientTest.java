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
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import okhttp3.HttpUrl;
import org.assertj.core.util.Lists;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.crypto.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.XrplMethods;
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
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyRequestParams;
import org.xrpl.xrpl4j.model.client.channels.ChannelVerifyResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.nft.NftBuyOffersRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftBuyOffersResult;
import org.xrpl.xrpl4j.model.client.nft.NftSellOffersRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftSellOffersResult;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedResult;
import org.xrpl.xrpl4j.model.client.path.PathCurrency;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.server.ServerInfo;
import org.xrpl.xrpl4j.model.client.server.ServerInfoLastClose;
import org.xrpl.xrpl4j.model.client.server.ServerInfoLedger;
import org.xrpl.xrpl4j.model.client.server.ServerInfoResult;
import org.xrpl.xrpl4j.model.client.serverinfo.LedgerRangeUtils;
import org.xrpl.xrpl4j.model.client.serverinfo.RippledServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.LastClose;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.ValidatedLedger;
import org.xrpl.xrpl4j.model.client.transactions.SignedTransaction;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
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
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NfTokenAcceptOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenBurn;
import org.xrpl.xrpl4j.model.transactions.NfTokenCancelOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenCreateOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.PathStep;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.SeedWalletGenerationResult;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test for {@link XrplClient}.
 */
public class XrplClientTest {

  protected final WalletFactory walletFactory = DefaultWalletFactory.getInstance();
  SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
  final Wallet wallet = seedResult.wallet();
  @Mock
  private JsonRpcClient jsonRpcClientMock;
  private XrplClient xrplClient;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(DepositAuthorizedResult.class));
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
      public ServerInfo serverInfo() {
        ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .age(UnsignedInteger.ONE)
          .reserveBaseXrp(UnsignedInteger.ONE)
          .reserveBaseAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .reserveIncXrp(UnsignedInteger.ONE)
          .reserveIncAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .sequence(LedgerIndex.of(UnsignedInteger.ONE))
          .baseFeeXrp(BigDecimal.ONE)
          .build();
        ServerInfo serverInfo = ServerInfo.builder()
          .completeLedgers("0-0")
          .amendmentBlocked(true)
          .buildVersion("1")
          .closedLedger(serverInfoLedger)
          .hostId("id")
          .ioLatencyMs(UnsignedLong.valueOf(2))
          .jqTransOverflow("flow")
          .lastClose(ServerInfoLastClose.builder()
            .convergeTimeSeconds(1.11)
            .proposers(UnsignedInteger.ONE)
            .build())
          .publicKeyNode("node")
          .serverState("full")
          .serverStateDurationUs("10")
          .time(ZonedDateTime.now())
          .upTime(UnsignedLong.ONE)
          .validationQuorum(UnsignedInteger.ONE)
          .build();
        return serverInfo;
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
      public ServerInfo serverInfo() {
        ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .age(UnsignedInteger.ONE)
          .reserveBaseXrp(UnsignedInteger.ONE)
          .reserveBaseAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .reserveIncXrp(UnsignedInteger.ONE)
          .reserveIncAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .sequence(LedgerIndex.of(UnsignedInteger.ONE))
          .baseFeeXrp(BigDecimal.ONE)
          .build();
        ServerInfo serverInfo = ServerInfo.builder()
          .completeLedgers("1-1")
          .amendmentBlocked(true)
          .buildVersion("1")
          .closedLedger(serverInfoLedger)
          .hostId("id")
          .ioLatencyMs(UnsignedLong.valueOf(2))
          .jqTransOverflow("flow")
          .lastClose(ServerInfoLastClose.builder()
            .convergeTimeSeconds(1.11)
            .proposers(UnsignedInteger.ONE)
            .build())
          .publicKeyNode("node")
          .serverState("full")
          .serverStateDurationUs("10")
          .time(ZonedDateTime.now())
          .upTime(UnsignedLong.ONE)
          .validationQuorum(UnsignedInteger.ONE)
          .build();
        return serverInfo;
      }

      @Override
      public AccountInfoResult accountInfo(AccountInfoRequestParams params) {
        AccountRootObject root = AccountRootObject.builder()
          .accountTransactionId(Hash256.of(Strings.repeat("0", 64)))
          .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
          .balance(XrpCurrencyAmount.ofDrops(1000))
          .ownerCount(UnsignedInteger.ONE)
          .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
          .previousTransactionLedgerSequence(UnsignedInteger.ONE)
          .sequence(UnsignedInteger.ONE)
          .flags(Flags.AccountRootFlags.DISABLE_MASTER)
          .index(Hash256.of(Strings.repeat("0", 64)))
          .build();
        AccountInfoResult accountInfoResult = AccountInfoResult.builder()
          .accountData(root)
          .build();

        return accountInfoResult;
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
      public ServerInfo serverInfo() {
        ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .age(UnsignedInteger.ONE)
          .reserveBaseXrp(UnsignedInteger.ONE)
          .reserveBaseAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .reserveIncXrp(UnsignedInteger.ONE)
          .reserveIncAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .sequence(LedgerIndex.of(UnsignedInteger.ONE))
          .baseFeeXrp(BigDecimal.ONE)
          .build();
        ServerInfo serverInfo = ServerInfo.builder()
          .completeLedgers("1-1")
          .amendmentBlocked(true)
          .buildVersion("1")
          .closedLedger(serverInfoLedger)
          .hostId("id")
          .ioLatencyMs(UnsignedLong.valueOf(2))
          .jqTransOverflow("flow")
          .lastClose(ServerInfoLastClose.builder()
            .convergeTimeSeconds(1.11)
            .proposers(UnsignedInteger.ONE)
            .build())
          .publicKeyNode("node")
          .serverState("full")
          .serverStateDurationUs("10")
          .time(ZonedDateTime.now())
          .upTime(UnsignedLong.ONE)
          .validationQuorum(UnsignedInteger.ONE)
          .build();
        return serverInfo;
      }

      @Override
      public AccountInfoResult accountInfo(AccountInfoRequestParams params) {
        AccountRootObject root = AccountRootObject.builder()
          .accountTransactionId(Hash256.of(Strings.repeat("0", 64)))
          .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
          .balance(XrpCurrencyAmount.ofDrops(1000))
          .ownerCount(UnsignedInteger.ONE)
          .previousTransactionId(Hash256.of(Strings.repeat("0", 64)))
          .previousTransactionLedgerSequence(UnsignedInteger.ONE)
          .sequence(UnsignedInteger.ONE)
          .flags(Flags.AccountRootFlags.DISABLE_MASTER)
          .index(Hash256.of(Strings.repeat("0", 64)))
          .build();
        AccountInfoResult accountInfoResult = AccountInfoResult.builder()
          .accountData(root)
          .build();

        return accountInfoResult;
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
      public ServerInfo serverInfo() {
        ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .age(UnsignedInteger.ONE)
          .reserveBaseXrp(UnsignedInteger.ONE)
          .reserveBaseAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .reserveIncXrp(UnsignedInteger.ONE)
          .reserveIncAsXrp(XrpCurrencyAmount.ofDrops(1000000))
          .sequence(LedgerIndex.of(UnsignedInteger.ONE))
          .baseFeeXrp(BigDecimal.ONE)
          .build();
        ServerInfo serverInfo = ServerInfo.builder()
          .completeLedgers("2-4")
          .amendmentBlocked(true)
          .buildVersion("1")
          .closedLedger(serverInfoLedger)
          .hostId("id")
          .ioLatencyMs(UnsignedLong.valueOf(2))
          .jqTransOverflow("flow")
          .lastClose(ServerInfoLastClose.builder()
            .convergeTimeSeconds(1.11)
            .proposers(UnsignedInteger.ONE)
            .build())
          .publicKeyNode("node")
          .serverState("full")
          .serverStateDurationUs("10")
          .time(ZonedDateTime.now())
          .upTime(UnsignedLong.ONE)
          .validationQuorum(UnsignedInteger.ONE)
          .build();
        return serverInfo;
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
      public ServerInfo serverInfo() throws JsonRpcClientErrorException {
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
  public void addSignatureTx() {
    Payment payment = Payment.builder()
      .ticketSequence(UnsignedInteger.ONE)
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .build();
    assertThat(xrplClient.addSignature(payment, "sign").transactionSignature().get()).isEqualTo("sign");
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .build();
    assertThat(xrplClient.addSignature(accountSet, "sign").transactionSignature().get()).isEqualTo("sign");
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .build();
    assertThat(xrplClient.addSignature(accountDelete, "sign").transactionSignature().get()).isEqualTo("sign");
    CheckCancel checkCancel = CheckCancel.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .checkId(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .sequence(UnsignedInteger.valueOf(12))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .build();
    assertThat(xrplClient.addSignature(checkCancel, "sign").transactionSignature().get()).isEqualTo("sign");
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .deliverMin(XrpCurrencyAmount.ofDrops(100))
      .build();
    assertThat(xrplClient.addSignature(checkCash, "sign").transactionSignature().get()).isEqualTo("sign");
    CheckCreate checkCreate = CheckCreate.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destinationTag(UnsignedInteger.ONE)
      .sendMax(XrpCurrencyAmount.ofDrops(100000000))
      .expiration(UnsignedInteger.valueOf(570113521))
      .invoiceId(Hash256.of("6F1DFD1D0FE8A32E40E1F2C05CF1C15545BAB56B617F9C6C2D63A6B704BEF59B"))
      .build();
    assertThat(xrplClient.addSignature(checkCreate, "sign").transactionSignature().get()).isEqualTo("sign");
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .authorize(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .build();
    assertThat(xrplClient.addSignature(depositPreAuth, "sign").transactionSignature().get()).isEqualTo("sign");
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .build();
    assertThat(xrplClient.addSignature(escrowCreate, "sign").transactionSignature().get()).isEqualTo("sign");
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .build();
    assertThat(xrplClient.addSignature(escrowCancel, "sign").transactionSignature().get()).isEqualTo("sign");
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .build();
    assertThat(xrplClient.addSignature(escrowFinish, "sign").transactionSignature().get()).isEqualTo("sign");
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(wallet.classicAddress())
      .buyOffer(Hash256.of(Strings.repeat("0", 64)))
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(wallet.publicKey())
      .build();
    assertThat(xrplClient.addSignature(nfTokenAcceptOffer, "sign").transactionSignature().get()).isEqualTo("sign");
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(UnsignedInteger.ONE)
      .build();
    assertThat(xrplClient.addSignature(nfTokenBurn, "sign").transactionSignature().get()).isEqualTo("sign");
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .addTokenOffers(Hash256.of(Strings.repeat("0", 64)))
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(UnsignedInteger.valueOf(2))
      .signingPublicKey(wallet.publicKey())
      .build();
    assertThat(xrplClient.addSignature(nfTokenCancelOffer, "sign").transactionSignature().get()).isEqualTo("sign");
    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet.classicAddress())
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.NfTokenCreateOfferFlags.builder()
        .tfSellToken(true)
        .build())
      .signingPublicKey(wallet.publicKey())
      .build();
    assertThat(xrplClient.addSignature(nfTokenCreateOffer, "sign").transactionSignature().get()).isEqualTo("sign");
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(UnsignedInteger.ONE)
      .build();
    assertThat(xrplClient.addSignature(nfTokenMint, "sign").transactionSignature().get()).isEqualTo("sign");
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .sequence(UnsignedInteger.ONE)
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("currency")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .value("1000")
        .build())
      .build();
    assertThat(xrplClient.addSignature(trustSet, "sign").transactionSignature().get()).isEqualTo("sign");
    OfferCreate offerCreate = OfferCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .takerPays(XrpCurrencyAmount.ofDrops(100))
      .takerGets(XrpCurrencyAmount.ofDrops(100))
      .build();
    assertThat(xrplClient.addSignature(offerCreate, "sign").transactionSignature().get()).isEqualTo("sign");
    OfferCancel offerCancel = OfferCancel.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .build();
    assertThat(xrplClient.addSignature(offerCancel, "sign").transactionSignature().get()).isEqualTo("sign");
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("123")
      .build();
    assertThat(xrplClient.addSignature(paymentChannelCreate, "sign").transactionSignature().get()).isEqualTo("sign");
    PaymentChannelClaim paymentChannelClaim = PaymentChannelClaim.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .build();
    assertThat(xrplClient.addSignature(paymentChannelClaim, "sign").transactionSignature().get()).isEqualTo("sign");
    PaymentChannelFund paymentChannelFund = PaymentChannelFund.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .amount(XrpCurrencyAmount.ofDrops(100L))
      .build();
    assertThat(xrplClient.addSignature(paymentChannelFund, "sign").transactionSignature().get()).isEqualTo("sign");
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .regularKey(Address.of("rAR8rR8sUkBoCZFawhkWzY4Y5YoyuznwD"))
      .build();
    assertThat(xrplClient.addSignature(setRegularKey, "sign").transactionSignature().get()).isEqualTo("sign");
    SignerListSet signerListSet = SignerListSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .signerQuorum(UnsignedInteger.valueOf(3))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
            .signerWeight(UnsignedInteger.valueOf(2))
            .build()
        )
      )
      .build();
    assertThat(xrplClient.addSignature(signerListSet, "sign").transactionSignature().get()).isEqualTo("sign");
    TicketCreate ticketCreate = TicketCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .ticketCount(UnsignedInteger.ONE)
      .build();
    assertThat(xrplClient.addSignature(ticketCreate, "sign").transactionSignature().get()).isEqualTo("sign");
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
  public void addSignatureToIncorrectTxType_ThrowsCannotAddSign() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> xrplClient.addSignature(mock(Transaction.class), "")
    );
    assertThat(exception.getMessage()).isEqualTo("Signing fields could not be added to the unsignedTransaction.");
  }

  @Test
  public void submitWithCryptoSigningSignedTx() {
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

    String edPrivateKeyHex = "60F72F359647AD376D2CB783340CD843BD57CCD46093AA16B0C4D3A5143BADC5";
    Ed25519PrivateKeyParameters knownEd25519PrivateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(edPrivateKeyHex), 0
    );
    SingleKeySignatureService ecSignatureService = new SingleKeySignatureService(
      BcKeyUtils.toPrivateKey(knownEd25519PrivateKeyParameters)
    );
    Payment payment = Payment.builder()
      .ticketSequence(UnsignedInteger.ONE)
      .account(wallet.classicAddress())
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey(wallet.publicKey())
      .build();

    org.xrpl.xrpl4j.crypto.signing.SignedTransaction<Payment> paymentSignedTransaction = ecSignatureService.sign(
      mock(KeyMetadata.class), payment
    );
    xrplClient = new XrplClient(jsonRpcClientMock);
    assertDoesNotThrow(() -> xrplClient.submit(paymentSignedTransaction));
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

    KeyPair keypair = Seed.ed25519Seed().deriveKeyPair();
    Payment payment = Payment.builder()
      .ticketSequence(UnsignedInteger.ONE)
      .account(wallet.classicAddress())
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey(wallet.publicKey())
      .build();

    SingleSignedTransaction<Payment> paymentSignedTransaction = bcSignatureService.sign(keypair.privateKey(), payment);
    xrplClient = new XrplClient(jsonRpcClientMock);
    assertDoesNotThrow(() -> xrplClient.submit(paymentSignedTransaction));
  }

  @Test
  public void submitWithSignedTx() {

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
      .account(wallet.classicAddress())
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey(wallet.publicKey())
      .build();
    xrplClient = new XrplClient(jsonRpcClientMock);
    SignedTransaction<Payment> paymentSignedTransaction = xrplClient.signTransaction(wallet, payment);
    assertDoesNotThrow(() -> xrplClient.submit(paymentSignedTransaction));

  }

  @Test
  public void signTxCatchesJsonProcessingException_ThrowsException() {
    Payment mockPayment = mock(Payment.class);
    assertThrows(RuntimeException.class, () -> xrplClient.signTransaction(wallet, mockPayment));
  }

  @Test
  public void submitUnsignedTxWithoutSigningPubKey_Throws() {
    xrplClient = new XrplClient(jsonRpcClientMock);
    Payment mockPayment = mock(Payment.class);

    IllegalArgumentException thrownException = Assertions.assertThrows(
      IllegalArgumentException.class, () -> xrplClient.submit(wallet, mockPayment));
    assertThat(thrownException.getMessage())
      .isEqualTo("Transaction.signingPublicKey() must be set.");
  }

  @Test
  public void submitUnsignedTxWithSigningPubKey() {
    SubmitResult mockSubmitResult = mock(SubmitResult.class);
    SignedTransaction mockSignedTransaction = mock(SignedTransaction.class);
    xrplClient = new XrplClient(jsonRpcClientMock) {
      @Override
      public <T extends Transaction> SignedTransaction<T> signTransaction(
        Wallet wallet, T unsignedTransaction
      ) {
        return mockSignedTransaction;
      }

      @Override
      public <T extends Transaction> SubmitResult<T> submit(
        SignedTransaction<T> signedTransaction
      ) {
        return mockSubmitResult;
      }
    };
    Payment payment = Payment.builder()
      .ticketSequence(UnsignedInteger.ONE)
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .build();

    assertDoesNotThrow(() -> xrplClient.submit(wallet, payment));
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
          .result("tesSUCCESS")
          .resultCode(200)
          .resultMessage("Submitted")
          .transactionBlob("blob")
          .transaction(mock(TransactionResult.class))
          .build();
        return (T) submitMultiSignedResult;
      }
    };
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    final Wallet wallet2 = seedResult.wallet();

    Payment unsignedPayment = Payment.builder()
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(wallet2.classicAddress())
      .signingPublicKey("")
      .build();

    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    List<SignerWrapper> signers = new ArrayList<>();
    Payment multiSigPayment = Payment.builder()
      .from(unsignedPayment)
      .signers(signers)
      .build();

    xrplClient = new XrplClient(jsonRpcClientMock);
    SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSigPayment);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    assertThat(paymentResult.resultCode()).isEqualTo(200);
    assertThat(paymentResult.transactionBlob()).isEqualTo("blob");
  }

  @Test
  public void fee() throws JsonRpcClientErrorException {
    xrplClient.fee();

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(FeeResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.FEE);
  }

  @Test
  public void serverInfo() {
    ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
      .hash(Hash256.of(Strings.repeat("0", 64)))
      .age(UnsignedInteger.ONE)
      .reserveBaseXrp(UnsignedInteger.ONE)
      .reserveBaseAsXrp(XrpCurrencyAmount.ofDrops(1000000))
      .reserveIncXrp(UnsignedInteger.ONE)
      .reserveIncAsXrp(XrpCurrencyAmount.ofDrops(1000000))
      .sequence(LedgerIndex.of(UnsignedInteger.ONE))
      .baseFeeXrp(BigDecimal.ONE)
      .build();
    ServerInfo serverInfo = ServerInfo.builder()
      .completeLedgers("1-2")
      .amendmentBlocked(true)
      .buildVersion("1")
      .closedLedger(serverInfoLedger)
      .hostId("id")
      .ioLatencyMs(UnsignedLong.valueOf(2))
      .jqTransOverflow("flow")
      .lastClose(ServerInfoLastClose.builder()
        .convergeTimeSeconds(1.11)
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
    ServerInfo serverInfoResponse = assertDoesNotThrow(() -> xrplClient.serverInfo());
    assertThat(serverInfoResponse).isEqualTo(serverInfo);
  }

  @Test
  public void serverInformation() {
    ValidatedLedger serverInfoLedger =
      ValidatedLedger.builder()
      .hash(Hash256.of(Strings.repeat("0", 64)))
      .age(UnsignedInteger.ONE)
      .reserveBaseXrp(XrpCurrencyAmount.ofXrp(BigDecimal.ONE))
      .reserveIncXrp(XrpCurrencyAmount.ofXrp(BigDecimal.ONE))
      .sequence(LedgerIndex.of(UnsignedInteger.ONE))
      .baseFeeXrp(BigDecimal.ONE)
      .build();
    org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo serverInfo = RippledServerInfo.builder()
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

    org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult serverInfoResult =
      org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult.builder()
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
    org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult serverInfoResponse =
      assertDoesNotThrow(() -> xrplClient.serverInformation());
    assertThat(serverInfoResponse.info()).isEqualTo(serverInfo);
  }

  @Test
  public void accountChannels() throws JsonRpcClientErrorException {
    AccountChannelsRequestParams accountChannelsRequestParams = AccountChannelsRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountChannels(accountChannelsRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountChannelsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_CHANNELS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountChannelsRequestParams);
  }

  @Test
  public void accountCurrencies() throws JsonRpcClientErrorException {
    AccountCurrenciesRequestParams accountCurrenciesRequestParams = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountCurrencies(accountCurrenciesRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountCurrenciesResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_CURRENCIES);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountCurrenciesRequestParams);
  }

  @Test
  public void accountInfo() throws JsonRpcClientErrorException {
    AccountInfoRequestParams accountInfoRequestParams = AccountInfoRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountInfo(accountInfoRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountInfoResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_INFO);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountInfoRequestParams);
  }

  @Test
  public void accountObjects() throws JsonRpcClientErrorException {
    AccountObjectsRequestParams accountObjectsRequestParams = AccountObjectsRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountObjects(accountObjectsRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountObjectsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_OBJECTS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountObjectsRequestParams);
  }

  @Test
  public void accountOffers() throws JsonRpcClientErrorException {
    AccountOffersRequestParams accountOffersRequestParams = AccountOffersRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountOffers(accountOffersRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountOffersResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_OFFERS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountOffersRequestParams);
  }

  @Test
  public void accountTransactionsUsingBuilder() throws JsonRpcClientErrorException {
    AccountTransactionsRequestParams accountTransactionsRequestParams = AccountTransactionsRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountTransactions(accountTransactionsRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountTransactionsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_TX);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountTransactionsRequestParams);
  }

  @Test
  public void accountTransactionsUsingAddress() throws JsonRpcClientErrorException {
    Address account = Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw");
    xrplClient.accountTransactions(account);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountTransactionsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_TX);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0))
      .isEqualTo(AccountTransactionsRequestParams.builder()
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountNftsResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.ACCOUNT_NFTS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(accountNftsRequestParams);
  }

  @Test
  public void accountNftsUsingAddress() throws JsonRpcClientErrorException {
    Address account = Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw");
    xrplClient.accountNfts(account);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountNftsResult.class));
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(LedgerResult.class));
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(RipplePathFindResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.RIPPLE_PATH_FIND);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(ripplePathFindRequestParams);
  }

  @Test
  public void accountLines() throws JsonRpcClientErrorException {
    AccountLinesRequestParams accountLinesRequestParams = AccountLinesRequestParams.builder()
      .account(Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw"))
      .build();
    xrplClient.accountLines(accountLinesRequestParams);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(AccountLinesResult.class));
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(ChannelVerifyResult.class));
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(GatewayBalancesResult.class));
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(NftBuyOffersResult.class));
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
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(NftSellOffersResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.NFT_SELL_OFFERS);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(nftSellOffersRequestParams);
  }

  @Test
  public void signTransaction() {
    Payment payment = Payment.builder()
      .account(wallet.classicAddress())
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .build();
    assertDoesNotThrow(() -> xrplClient.signTransaction(wallet, payment));
  }

  @Test
  public void signPaymentWithPaths_DoesNotThrow() {
    List<PathStep> paths = Lists.newArrayList(
      PathStep.builder().account(Address.of("rUpy3eEg8rqjqfUoLeBnZkscbKbFsKXC3v")).currency("ABC").build(),
      PathStep.builder().account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW")).currency("XYZ").build()
    );
    Payment payment = Payment.builder()
      .account(wallet.classicAddress())
      .amount(XrpCurrencyAmount.ofDrops(10))
      .destination(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .signingPublicKey(wallet.publicKey())
      .fee(XrpCurrencyAmount.ofDrops(12))
      .addPaths(paths)
      .build();

    assertDoesNotThrow(() -> xrplClient.signTransaction(wallet, payment));
  }

  @Test
  public void signTransactionWithWalletMissingPrivateKey() {
    Wallet wallet2 = Wallet.builder()
      .publicKey(wallet.publicKey())
      .classicAddress(wallet.classicAddress())
      .xAddress(wallet.xAddress())
      .isTest(true)
      .build();
    Payment payment = Payment.builder()
      .account(wallet2.classicAddress())
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .build();
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      xrplClient.signTransaction(wallet2, payment));
    assertThat(exception.getMessage()).isEqualTo("Wallet must provide a private key to sign the transaction.");
  }
}
