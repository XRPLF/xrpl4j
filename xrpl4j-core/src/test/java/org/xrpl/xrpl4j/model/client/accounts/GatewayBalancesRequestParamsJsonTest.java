package org.xrpl.xrpl4j.model.client.accounts;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
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

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class GatewayBalancesRequestParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testWithLedgerHash() throws Exception {
    String json = "{\n" +
      "  \"account\": \"rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q\",\n" +
      "  \"hotwallet\": [\n" +
      "    \"rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ\",\n" +
      "    \"ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt\"\n" +
      "  ],\n" +
      "  \"ledger_hash\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
      "  \"strict\": true\n" +
      "}";

    GatewayBalancesRequestParams params = GatewayBalancesRequestParams
      .builder()
      .account(Address.of("rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q"))
      .ledgerSpecifier(
        LedgerSpecifier.of(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
      )
      .hotWallets(
        Lists.newArrayList(
          Address.of("rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ"),
          Address.of("ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt"))
      )
      .build();

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndex() throws Exception {
    String json = "{\n" +
      "  \"account\": \"rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q\",\n" +
      "  \"hotwallet\": [\n" +
      "    \"rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ\",\n" +
      "    \"ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt\"\n" +
      "  ],\n" +
      "  \"ledger_index\": \"validated\"," +
      "  \"strict\": true\n" +
      "}";

    GatewayBalancesRequestParams params = GatewayBalancesRequestParams
      .builder()
      .account(Address.of("rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .hotWallets(
        Lists.newArrayList(
          Address.of("rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ"),
          Address.of("ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt"))
      )
      .build();

    assertCanSerializeAndDeserialize(params, json);
  }
}
