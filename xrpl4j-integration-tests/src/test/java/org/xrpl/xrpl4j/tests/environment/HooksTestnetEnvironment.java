package org.xrpl.xrpl4j.tests.environment;

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.hooks.HooksFaucetClient;
import org.xrpl.xrpl4j.model.transactions.Address;

public class HooksTestnetEnvironment implements XrplEnvironment {

  private final XrplClient xrplClient = new XrplClient(HttpUrl.get("http://localhost:5005"));
  private final HooksFaucetClient hooksFaucetClient = HooksFaucetClient
    .construct(HttpUrl.get("https://hooks-testnet.xrpl-labs.com"));

  @Override
  public XrplClient getXrplClient() {
    return xrplClient;
  }

  @Override
  public void fundAccount(Address classicAddress) {
    hooksFaucetClient.fundAccount(classicAddress);
  }

}
