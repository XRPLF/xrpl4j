package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link LedgerIndex}s.
 */
public class LedgerIndexSerializer extends StdSerializer<LedgerIndex> {

  public LedgerIndexSerializer() {
    super(LedgerIndex.class, false);
  }

  @Override
  public void serialize(
    LedgerIndex ledgerIndex,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  ) throws IOException {
    jsonGenerator.writeNumber(ledgerIndex.value().longValue());
  }
}
