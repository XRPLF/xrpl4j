package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.dex.DexClient;
import org.xrpl.xrpl4j.client.dex.model.OrderBook;
import org.xrpl.xrpl4j.client.dex.model.Ticker;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.tests.environment.MainnetEnvironment;

public class MainnetDexClientIT {

  private static final Address GATEHUB_ADDRESS = Address.of("rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq");
  private static final String USD = "USD";
  private static final String XRP = "XRP";
  private final XrplClient mainnetClient = new MainnetEnvironment().getXrplClient();
  private final DexClient dexClient = new DexClient(mainnetClient);

  /**
   * Hits the actual mainnet DEX to verify the order book works and the results are mapped sanely.
   */
  @Test
  public void testDex() throws JsonRpcClientErrorException {
    OrderBook orderBook = dexClient.getOrderBook(Ticker.of(XRP, USD), GATEHUB_ADDRESS);
    // first bid is the highest bid, first ask is the lowest ask
    // highest bid should always be less the lowest ask
    assertThat(orderBook.bids().get(0).counterPrice())
      .isLessThan(orderBook.asks().get(0).counterPrice());
  }

}
