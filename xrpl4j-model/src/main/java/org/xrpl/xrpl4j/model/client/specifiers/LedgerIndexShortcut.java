package org.xrpl.xrpl4j.model.client.specifiers;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * TODO: Javadoc
 */
public class LedgerIndexShortcut {

  public static final LedgerIndexShortcut CURRENT = new LedgerIndexShortcut("current");
  public static final LedgerIndexShortcut VALIDATED = new LedgerIndexShortcut("validated");
  public static final LedgerIndexShortcut CLOSED = new LedgerIndexShortcut("closed");

  @JsonValue
  String value;

  public LedgerIndexShortcut(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LedgerIndexShortcut)) {
      return false;
    }

    LedgerIndexShortcut that = (LedgerIndexShortcut) o;

    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}
