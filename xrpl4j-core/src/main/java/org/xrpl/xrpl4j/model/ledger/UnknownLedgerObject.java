package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an XRPL ledger object whose type is unknown to xrpl4j. This can occur when
 * using a version of xrpl4j that does not have support for new ledger objects on the XRPL. This class
 * simply holds a {@link Map} of field names to field values.
 *
 * <p>Unfortunately, Jackson's polymorphic deserializer consumes the type ID field found in JSON, therefore
 * the {@code "LedgerEntryType"} field will not be present in the {@code properties} map.</p>
 */
public class UnknownLedgerObject implements LedgerObject {
  private final Map<String, Object> properties = new HashMap<>();

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    properties.put(name, value);
  }

  public Map<String, Object> properties() {
    return properties;
  }

  @Override
  public String toString() {
    return "UnknownLedgerObject{" +
        "additionalProperties=" + properties +
        '}';
  }
}
