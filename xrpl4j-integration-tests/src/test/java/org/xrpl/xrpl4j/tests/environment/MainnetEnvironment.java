package org.xrpl.xrpl4j.tests.environment;

import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.transactions.Address;

public abstract class MainnetEnvironment implements XrplEnvironment {

  @Override
  public abstract XrplClient getXrplClient();

  @Override
  public void fundAccount(Address classicAddress) {
    throw new UnsupportedOperationException("funding not supported on mainnet");
  }

}
