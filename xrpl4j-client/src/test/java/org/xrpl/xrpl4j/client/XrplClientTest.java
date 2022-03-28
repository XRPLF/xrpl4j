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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedResult;
import org.xrpl.xrpl4j.model.client.server.ServerInfo;
import org.xrpl.xrpl4j.model.client.server.ServerInfoLastClose;
import org.xrpl.xrpl4j.model.client.server.ServerInfoLedger;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test for {@link XrplClient}.
 */
public class XrplClientTest {

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
  void isFinalForValidatedTransactionWithoutMetadata_ThrowsException() {
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
    assertThatThrownBy(
      () -> xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ONE,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      )
    ).isInstanceOf(RuntimeException.class)
      .hasMessage("Metadata not found in the validated transaction.");

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
      )
    ).isEqualTo(XrplClient.FinalityStatus.VALIDATED_SUCCESS);

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
      )
    ).isEqualTo(XrplClient.FinalityStatus.VALIDATED_FAILURE);

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
      )
    ).isEqualTo(XrplClient.FinalityStatus.NOT_FINAL);

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
        return UnsignedInteger.ONE;
      }

      @Override
      public ServerInfo serverInfo() {
        ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .age(UnsignedInteger.ONE)
          .reserveBaseXrp(UnsignedInteger.ONE)
          .reserveIncXrp(UnsignedInteger.ONE)
          .sequence(LedgerIndex.of(UnsignedInteger.ONE))
          .baseFeeXrp(BigDecimal.ONE)
          .build();
        ServerInfo serverInfo = ServerInfo.builder()
          .completeLedgers("0,0")
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
      )
    ).isEqualTo(XrplClient.FinalityStatus.NOT_FINAL);

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
        return UnsignedInteger.ONE;
      }

      @Override
      public ServerInfo serverInfo() {
        ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .age(UnsignedInteger.ONE)
          .reserveBaseXrp(UnsignedInteger.ONE)
          .reserveIncXrp(UnsignedInteger.ONE)
          .sequence(LedgerIndex.of(UnsignedInteger.ONE))
          .baseFeeXrp(BigDecimal.ONE)
          .build();
        ServerInfo serverInfo = ServerInfo.builder()
          .completeLedgers("1,1")
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
      )
    ).isEqualTo(XrplClient.FinalityStatus.EXPIRED);

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
        return UnsignedInteger.ONE;
      }

      @Override
      public ServerInfo serverInfo() {
        ServerInfoLedger serverInfoLedger = ServerInfoLedger.builder()
          .hash(Hash256.of(Strings.repeat("0", 64)))
          .age(UnsignedInteger.ONE)
          .reserveBaseXrp(UnsignedInteger.ONE)
          .reserveIncXrp(UnsignedInteger.ONE)
          .sequence(LedgerIndex.of(UnsignedInteger.ONE))
          .baseFeeXrp(BigDecimal.ONE)
          .build();
        ServerInfo serverInfo = ServerInfo.builder()
          .completeLedgers("1,1")
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

    assertThatThrownBy(
      () -> xrplClient.isFinal(
        transactionHash,
        LedgerIndex.of(UnsignedInteger.ONE),
        UnsignedInteger.ONE,
        UnsignedInteger.ZERO,
        Address.of("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      )
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("Something unexpected happened. Tx not final.");

    assertThat(calledWithHash.get()).isEqualTo(transactionHash);
  }
}
