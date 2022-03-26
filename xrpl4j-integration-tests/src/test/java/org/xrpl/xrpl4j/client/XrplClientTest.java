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
}
