package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link LedgerSpecifier}s.
 */
public class LedgerSpecifierSerializer extends StdSerializer<LedgerSpecifier> {

  /**
   * No-args constructor.
   */
  public LedgerSpecifierSerializer() {
    super(LedgerSpecifier.class, false);
  }

  @Override
  public boolean isUnwrappingSerializer() {
    return true;
  }

  @Override
  public void serialize(
    LedgerSpecifier ledgerSpecifier,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  ) throws IOException {
    if (ledgerSpecifier.ledgerHash().isPresent()) {
      jsonGenerator.writeStringField("ledger_hash", ledgerSpecifier.ledgerHash().get().value());
    } else if (ledgerSpecifier.ledgerIndex().isPresent()) {
      jsonGenerator.writeNumberField(
        "ledger_index",
        ledgerSpecifier.ledgerIndex().get().unsignedIntegerValue().intValue()
      );
    } else if (ledgerSpecifier.ledgerIndexShortcut().isPresent()) {
      jsonGenerator.writeStringField("ledger_index", ledgerSpecifier.ledgerIndexShortcut().get().toString());
    }
  }
}
