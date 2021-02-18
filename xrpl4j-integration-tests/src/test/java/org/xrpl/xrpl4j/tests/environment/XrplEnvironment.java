package org.xrpl.xrpl4j.tests.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Abstraction for the XRP Ledger environment that the integration tests talk to. Provides access to resources
 * need to interact to the with the ledger.
 */
public interface XrplEnvironment {

  Logger logger = LoggerFactory.getLogger(XrplEnvironment.class);

  /**
   * Gets the XRPL environment to use (based on existence of -DuseTestnet property).
   *
   * @return
   */
  static XrplEnvironment getConfiguredEnvironment() {
    // Use the local rippled environment by default because it's faster and more predictable for testing.
    // TestnetEnvironment can make it easier to debug transactions using in the testnet explorer website.
    boolean isTestnetEnabled = System.getProperty("useTestnet") != null;
    logger.info("useTestnet set to {}, using {} for testing.",
      isTestnetEnabled, isTestnetEnabled ? "testnet" : "local rippled"
    );
    return isTestnetEnabled ? new TestnetEnvironment() : new LocalRippledEnvironment();
  }

  XrplClient getXrplClient();

  void fundAccount(Address classicAddress);

}
