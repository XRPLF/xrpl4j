package org.xrpl.xrpl4j.model.client.accounts;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class GatewayBalancesRequestParamsJsonTest extends AbstractJsonTest {

  /*
   * {
   *             "account": "rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q",
   *             "hotwallet": [
   *                 "rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ",
   *                 "ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt"
   *             ],
   *             "ledger_index": "validated",
   *             "strict": true
   *         }
   */

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
      .hotwallet(
        Lists.newArrayList(
          Address.of("rKm4uWpg9tfwbVSeATv4KxDe6mpE9yPkgJ"),
          Address.of("ra7JkEzrgeKHdzKgo4EUUVBnxggY4z37kt"))
      )
      .strict(true)
      .build();

    assertCanSerializeAndDeserialize(params, json);
  }
}
