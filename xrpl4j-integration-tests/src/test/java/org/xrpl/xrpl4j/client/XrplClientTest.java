package org.xrpl.xrpl4j.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedRequestParams;
import org.xrpl.xrpl4j.model.client.path.DepositAuthorizedResult;
import org.xrpl.xrpl4j.model.transactions.Address;

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
      .sourceAccount(Address.of("123"))
      .destinationAccount(Address.of("abc"))
      .build();
    xrplClient.depositAuthorized(depositAuthorized);

    ArgumentCaptor<JsonRpcRequest> jsonRpcRequestArgumentCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);
    Mockito.verify(jsonRpcClientMock).send(jsonRpcRequestArgumentCaptor.capture(), eq(DepositAuthorizedResult.class));
    assertThat(jsonRpcRequestArgumentCaptor.getValue().method()).isEqualTo(XrplMethods.DEPOSIT_AUTHORIZED);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().size()).isEqualTo(1);
    assertThat(jsonRpcRequestArgumentCaptor.getValue().params().get(0)).isEqualTo(depositAuthorized);
  }
}