package org.xrpl.xrpl4j.tests.environment;

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * XRPL mainnet environment.
 */
public class MainnetEnvironment implements XrplEnvironment {

  private final XrplClient xrplClient = new XrplClient(HttpUrl.parse("https://s1.ripple.com:51234"));

  @Override
  public XrplClient getXrplClient() {
    return xrplClient;
  }

  @Override
  public void fundAccount(Address classicAddress) {
    throw new UnsupportedOperationException("funding not supported on mainnet");
  }

}
