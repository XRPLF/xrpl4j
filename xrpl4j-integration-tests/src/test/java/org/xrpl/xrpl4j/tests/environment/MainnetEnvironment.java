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
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * XRPL mainnet environment.
 */
public class MainnetEnvironment implements XrplEnvironment {

  // FIXME: At the time of writing this, Clio is powering some traffic to s1 and s2. Clio behaves differently
  //  than reporting mode servers/rippled p2p servers in responses to account_tx RPC calls with
  //  "ledger_index" = "validated", which breaks AccountTransactionsIT. xrplcluster.com is not powered by Clio, so
  //  this client is pointed at xrplcluster.com. However, once this bug in Clio is resolved, we should revert back to
  //  using s1/s2 here.
  private final XrplClient xrplClient = new XrplClient(HttpUrl.parse("https://xrplcluster.com"));

  @Override
  public XrplClient getXrplClient() {
    return xrplClient;
  }

  @Override
  public void fundAccount(Address classicAddress) {
    throw new UnsupportedOperationException("funding not supported on mainnet");
  }

}
