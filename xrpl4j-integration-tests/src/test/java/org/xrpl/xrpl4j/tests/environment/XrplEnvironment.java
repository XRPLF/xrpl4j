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
   * @return {@link XrplEnvironment}
   */
  static XrplEnvironment getConfiguredEnvironment() {
    // Use the local rippled environment by default because it's faster and more predictable for testing.
    // TestnetEnvironment can make it easier to debug transactions using in the testnet explorer website.
    boolean isTestnetEnabled = System.getProperty("useTestnet") != null;
    if (isTestnetEnabled) {
      logger.info("System property 'useTestnet' detected; Using Testnet for integration testing.");
      return new TestnetEnvironment();
    } else {
      logger.info("'useTestNet' system property not detected." +
        " Using local rippled for integration testing.");
      return new LocalRippledEnvironment();
    }
  }

  XrplClient getXrplClient();

  void fundAccount(Address classicAddress);

}
