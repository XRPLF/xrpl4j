package org.xrpl.xrpl4j.tests.environment;

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Objects;

/**
 * Create a custom environment to talk to custom rippled by providing the serverUrl.
 * Also, has faucet support to fund accounts.
 */
public class CustomEnvironment implements XrplEnvironment {

  private final XrplClient xrplClient;
  private FaucetClient faucetClient;

  private HttpUrl faucetUrl;

  /** Constructor to create custom environment.
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