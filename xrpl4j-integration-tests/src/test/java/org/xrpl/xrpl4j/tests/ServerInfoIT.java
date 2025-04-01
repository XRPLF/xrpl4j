package org.xrpl.xrpl4j.tests;

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

import static org.assertj.core.api.Fail.fail;

import com.google.common.primitives.UnsignedInteger;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo;
import org.xrpl.xrpl4j.model.transactions.NetworkId;

/**
 * Integration test for different types of ServerInfo values.
 */
public class ServerInfoIT {

  /**
   * This test validates actual Server Info models. {@link ServerInfo} is an implementation detail and different servers
   * could respond differently, so this test essentially verifies the correctness of these models.
   *
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @throws InterruptedException        If {@link Thread} is interrupted.
   * @see "https://xrpl.org/docs/tutorials/public-servers/#mainnet"
   */
  @Test
  public void testServerInfoAcrossAllTypes() throws JsonRpcClientErrorException, InterruptedException {

    // XRPL Cluster
    getXrplClient(HttpUrl.parse("https://xrplcluster.com/")).serverInformation().info().handle(
      this::assertValidNetworkId,
      clioServerInfo -> fail("Shouldn't be a Clio server"),
      reportingModeServerInfo -> fail("Shouldn't be a Reporting server")
    );

    // XRPL Cluster (testnet)
    // Not executed due to rate limiting.

    // Ripple Mainnet (s1)
    getXrplClient(HttpUrl.parse("https://s1.ripple.com:51234")).serverInformation().info().handle(
      rippledServerInfo -> fail("Shouldn't be a rippled server"),
      this::assertValidNetworkId,
      reportingModeServerInfo -> fail("Shouldn't be a Reporting server")
    );

    // Ripple Mainnet (s2)
    getXrplClient(HttpUrl.parse("https://s2.ripple.com:51234")).serverInformation().info().handle(
      rippledServerInfo -> fail("Shouldn't be a rippled server"),
      this::assertValidNetworkId,
      reportingModeServerInfo -> fail("Shouldn't be a Reporting server")
    );

    // Ripple Testnet
    getXrplClient(HttpUrl.parse("https://s.altnet.rippletest.net:51234/")).serverInformation().info().handle(
      this::assertValidNetworkId,
      clioServerInfo -> fail("Shouldn't be a Clio server"),
      reportingModeServerInfo -> fail("Shouldn't be a Reporting server")
    );

    // Ripple Testnet (Clio)
    getXrplClient(HttpUrl.parse("https://clio.altnet.rippletest.net:51234/")).serverInformation().info().handle(
      rippledServerInfo -> fail("Shouldn't be a rippled server"),
      this::assertValidNetworkId,
      reportingModeServerInfo -> fail("Shouldn't be a Reporting server")
    );

    // Ripple Devnet
    getXrplClient(HttpUrl.parse("https://s.devnet.rippletest.net:51234/")).serverInformation().info().handle(
      this::assertValidNetworkId,
      clioServerInfo -> fail("Shouldn't be a Clio server"),
      reportingModeServerInfo -> fail("Shouldn't be a Reporting server")
    );

  }

  private String getType(ServerInfo info) {
    return info.map(
      rippled -> "rippled",
      clio -> "clio",
      reporting -> "reporting"
    );
  }

  private XrplClient getXrplClient(HttpUrl serverUrl) {
    return new XrplClient(serverUrl);
  }

  private void assertValidNetworkId(ServerInfo serverInfo) {
    serverInfo.networkId()
      .map(NetworkId::value)
      .map($ -> $.equals(UnsignedInteger.ZERO))
      .orElseThrow(() -> new RuntimeException("networkId should have existed"));
  }
}
