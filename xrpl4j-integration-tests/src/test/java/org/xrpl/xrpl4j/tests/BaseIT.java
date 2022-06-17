package org.xrpl.xrpl4j.tests;

import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

public class BaseIT extends AbstractIT {

  private static XrplEnvironment xrplEnvironment = XrplEnvironment.getConfiguredEnvironment();

  protected static XrplClient xrplClient = xrplEnvironment.getXrplClient();

  @Override
  protected XrplEnvironment xrplEnvironment() {
    return xrplEnvironment;
  }
}
