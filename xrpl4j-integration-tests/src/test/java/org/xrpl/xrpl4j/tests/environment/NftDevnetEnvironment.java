package org.xrpl.xrpl4j.tests.environment;

import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * XRPL NFT Devnet environment.
 */
public class NftDevnetEnvironment extends CustomEnvironment implements XrplEnvironment {

  public NftDevnetEnvironment() {
    super(
      HttpUrl.parse("http://xls20-sandbox.rippletest.net:51234"),
      HttpUrl.parse("https://faucet-nft.ripple.com")
    );
  }

  @Override
  public XrplClient getXrplClient() {
    return super.getXrplClient();
  }

  @Override
  public void fundAccount(Address classicAddress) {
    super.fundAccount(classicAddress);
  }
}
