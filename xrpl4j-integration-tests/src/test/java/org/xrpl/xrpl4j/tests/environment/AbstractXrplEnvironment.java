package org.xrpl.xrpl4j.tests.environment;

import java.time.Duration;

/**
 * An abstract implementation of {@link XrplEnvironment}. Note that this abstract implementation disallows manually
 * accepting a ledger using the `ledger_accept` method, so subclasses that wish to enable this functionality must
 * override the default behavior defined in this class.
 */
public abstract class AbstractXrplEnvironment implements XrplEnvironment {

  @Override
  public void acceptLedger() {
    throw new UnsupportedOperationException("Manual `ledger_accept` calls are not allowed for this Environment.");
  }

  @Override
  public void startLedgerAcceptor(Duration acceptIntervalMillis) {
    throw new UnsupportedOperationException("Manual `ledger_accept` calls are not allowed for this Environment.");
  }

  @Override
  public void stopLedgerAcceptor() {
    throw new UnsupportedOperationException("Manual `ledger_accept` calls are not allowed for this Environment.");
  }
}
