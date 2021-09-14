package org.xrpl.xrpl4j.tests;

import org.xrpl.xrpl4j.tests.environment.LocalRippledHooksEnvironment;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

public abstract class AbstractHookIT extends AbstractIT {

  protected static XrplEnvironment xrplEnvironment = new LocalRippledHooksEnvironment();

  @Override
  protected XrplEnvironment getXrplEnvironment() {
    if (xrplEnvironment == null) {
      xrplEnvironment = XrplEnvironment.getConfiguredEnvironment();
    }
    return xrplEnvironment;
  }

}
