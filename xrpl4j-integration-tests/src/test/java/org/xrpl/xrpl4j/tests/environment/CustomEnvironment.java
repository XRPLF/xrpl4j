package org.xrpl.xrpl4j.tests.environment;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.jsonrpc.FaucetClient;
import org.xrpl.xrpl4j.client.jsonrpc.XrplClient;
import org.xrpl.xrpl4j.client.jsonrpc.model.FundAccountRequest;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Objects;

/**
 * Create a custom environment to talk to custom rippled by providing the serverUrl. Also, has faucet support to fund
 * accounts.
 */
public class CustomEnvironment implements XrplEnvironment {
  
  private final XrplClient xrplClient;
  private FaucetClient faucetClient;
  
  private HttpUrl faucetUrl;
  
  /**
   * Constructor to create custom environment.
   *
   * @param serverUrl to connect to a running XRPL server.
   * @param faucetUrl to fund the accounts on the server.
   */
  public CustomEnvironment(HttpUrl serverUrl, HttpUrl faucetUrl) {
    Objects.requireNonNull(serverUrl);
    this.xrplClient = new XrplClient(serverUrl);
    this.faucetUrl = faucetUrl;
  }
  
  @Override
  public XrplClient getXrplClient() {
    return xrplClient;
  }
  
  @Override
  public void fundAccount(Address classicAddress) {
    if (faucetUrl == null) {
      throw new UnsupportedOperationException("funding not supported on this custom env");
    } else {
      faucetClient = FaucetClient.construct(faucetUrl);
      faucetClient.fundAccount(FundAccountRequest.of(classicAddress));
    }
  }
  
}
