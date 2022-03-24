package org.xrpl.xrpl4j.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;

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

  }
}
