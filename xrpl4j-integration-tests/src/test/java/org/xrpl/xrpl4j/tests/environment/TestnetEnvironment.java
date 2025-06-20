package org.xrpl.xrpl4j.tests.environment;

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

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * XRPL testnet environment.
 */
public abstract class TestnetEnvironment extends AbstractXrplEnvironment implements XrplEnvironment {

  private final FaucetClient faucetClient =
    FaucetClient.construct(HttpUrl.parse("https://apex-faucet.altnet.rippletest.net"));

  @Override
  public abstract XrplClient getXrplClient();

  @Override
  public void fundAccount(Address classicAddress) {
    faucetClient.fundAccount(FundAccountRequest.of(classicAddress));
  }

}
