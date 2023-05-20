package org.xrpl.xrpl4j.tests.environment;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.jsonrpc.XrplClient;
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
   * @return XRPL environment of the correct type.
   */
  static XrplEnvironment getConfiguredEnvironment() {
    // Use the testnet and devnet environment by default to run integration testing.
    // Use -DuseLocal test to run integration tests on local rippled.
    boolean isTestnetEnabled = System.getProperty("useTestnet") != null;
    boolean isDevnetEnabled = System.getProperty("useDevnet") != null;
    if (isTestnetEnabled) {
      logger.info("System property 'useTestnet' detected; Using Testnet for integration testing.");
      return new TestnetEnvironment();
    } else if (isDevnetEnabled) {
      logger.info("System property 'useDevnet' detected; Using Devnet for integration testing.");
      return new DevnetEnvironment();
    } else {
      logger.info("Neither 'useTestNet' nor 'useDevnet' System properties detected." +
        " Using local rippled for integration testing.");
      return new LocalRippledEnvironment();
    }
  }

  XrplClient getXrplClient();

  void fundAccount(Address classicAddress);

}
