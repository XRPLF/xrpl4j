package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.jackson.modules.UnknownLedgerObjectDeserializer;

/**
 * Represents an XRPL ledger object whose type is unknown to xrpl4j. This can occur when
 * using a version of xrpl4j that does not have support for new ledger objects on the XRPL.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableUnknownLedgerObject.class)
@JsonDeserialize(as = ImmutableUnknownLedgerObject.class, using = UnknownLedgerObjectDeserializer.class)
public interface UnknownLedgerObject extends LedgerObject {

  static ImmutableUnknownLedgerObject.Builder builder() {
    return ImmutableUnknownLedgerObject.builder();
  }

  @Value.Derived
  default String ledgerEntryType() {
    return properties().get("LedgerEntryType").asText();
  }

  JsonNode properties();

}
