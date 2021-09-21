package org.xrpl.xrpl4j.tests;

import org.xrpl.xrpl4j.tests.environment.HooksTestnetEnvironment;
import org.xrpl.xrpl4j.tests.environment.LocalRippledHooksEnvironment;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

public abstract class AbstractHookIT extends AbstractIT {

  protected static XrplEnvironment xrplEnvironment = (System.getProperty("useHooksTestnet") != null) ?
    new HooksTestnetEnvironment() :
    new LocalRippledHooksEnvironment();

  @Override
  protected XrplEnvironment getXrplEnvironment() {
    if (xrplEnvironment == null) {
      xrplEnvironment = XrplEnvironment.getConfiguredEnvironment();
    }
    return xrplEnvironment;
  }

}
