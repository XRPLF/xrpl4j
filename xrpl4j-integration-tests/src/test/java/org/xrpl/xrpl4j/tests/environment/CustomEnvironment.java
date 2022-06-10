package org.xrpl.xrpl4j.tests.environment;

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Create a custom environment to talk to custom rippled by providing the serverUrl.
 * Also, has faucet support to fund accounts.
 */
public class CustomEnvironment implements XrplEnvironment {

  private final XrplClient xrplClient;
  private FaucetClient faucetClient;

  private HttpUrl faucetUrl;

  /** To use custom environment, following must be passed in as system properties:
   * -DuseCustomEnv=true
   * -DserverUrl="http://example.serverurl.com:6789"
   * -DfaucetUrl="http://faucet.example.com:6789"
   *
   * @param serverUrl to connect to a running XRPL server.
   * @param faucetUrl to fund the accounts on the server.
   */
  public CustomEnvironment(HttpUrl serverUrl, HttpUrl faucetUrl) {
    this.xrplClient = new XrplClient(serverUrl);
    this.faucetUrl = faucetUrl;
  }

  @Override
  public XrplClient getXrplClient() {
    return xrplClient;
  }

  @Override
  public void fundAccount(Address classicAddress) {
    if (faucetUrl.url().equals(HttpUrl.parse(""))) {
      throw new UnsupportedOperationException("funding not supported on this custom env");
    } else {
      faucetClient = FaucetClient.construct(faucetUrl);
      faucetClient.fundAccount(FundAccountRequest.of(classicAddress));
    }
  }

}