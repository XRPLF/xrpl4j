package org.xrpl.xrpl4j.tests.environment;

import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Abstraction for the XRP Ledger environment that the integration tests talk to. Provides access to resources
 * need to interact to the with the ledger.
 */
public interface XrplEnvironment {

  XrplClient getXrplClient();

  void fundAccount(Address classicAddress);

}
