package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link LedgerIndex}s.
 *
 * FIXME: Remove this class
 */
public class OldLedgerIndexDeserializer extends StdDeserializer<LedgerIndex> {

  protected OldLedgerIndexDeserializer() {
    super(LedgerIndex.class);
  }

  @Override
  public LedgerIndex deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    return LedgerIndex.of(jsonParser.getValueAsString());
  }
}
