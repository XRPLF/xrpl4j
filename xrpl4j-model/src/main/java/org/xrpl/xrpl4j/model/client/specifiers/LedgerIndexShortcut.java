package org.xrpl.xrpl4j.model.client.specifiers;

/**
 * TODO: Javadoc
 */
public class LedgerIndexShortcut {

  public static final LedgerIndexShortcut CURRENT = new LedgerIndexShortcut("current");
  public static final LedgerIndexShortcut VALIDATED = new LedgerIndexShortcut("validated");
  public static final LedgerIndexShortcut CLOSED = new LedgerIndexShortcut("closed");

  String value;

  LedgerIndexShortcut(String value) {
    this.value = value;
  }
}
